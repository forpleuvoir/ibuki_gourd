package moe.forpleuvoir.ibukigourd.mod.waht.ecs

import kotlin.reflect.KClass

/**
 * ECS 世界:实体、组件、系统、事件的容器与调度核心。
 *
 * 内部维护 archetype 注册表(按组件类型集合分组),实体数据以列存储方式分散在 archetype 中。
 * 由外部驱动 [update],内部完成:固定步长分发 → 帧更新 → 死亡实体清理。
 *
 * 典型用法:
 * ```kotlin
 * val world = world {
 *     system { onFixedUpdate { w, dt -> w.forEach<Pos, Vel> { _, p, v -> p.x += v.x * dt } } }
 *     entity { +Pos(0f, 0f); +Vel(1f, 0f); tag("player") }
 * }
 * // 外部每帧调用
 * fun frame(dtSeconds: Float) = world.update(dtSeconds)
 * ```
 */
class World {

    /**
     * 类型签名 → archetype。直接以 [Set] 作键,依赖其内容相等性
     * (Kotlin 的 `Set` 已正确定义 `equals`/`hashCode`)。
     */
    private val archetypes = LinkedHashMap<Set<KClass<out Component>>, Archetype>()

    private val entities = HashMap<EntityId, Entity>()

    private val systems = mutableListOf<System>()

    private var nextId: Long = 0L

    /** 事件总线。 */
    val events: EventBus = EventBus()

    /** 固定步长(秒),默认 60Hz。 */
    var fixedStep: Float = 1f / 60f

    /** 单次 [update] 内最多执行的 fixedUpdate 次数,防止"死亡螺旋"(累积步数爆炸)。 */
    var maxFixedStepsPerUpdate: Int = 5

    private var accumulator: Float = 0f

    // ============ 构建入口 ============

    /**
     * 创建一个实体并应用配置。
     *
     * @param parent 父实体(null 表示根)
     * @param block [EntityBuilder] 配置;在 lambda 内用 `+Component` 添加组件、`tag(...)` 打标签
     */
    fun createEntity(
        parent: Entity? = null,
        block: EntityBuilder.() -> Unit = {}
    ): Entity {
        val id = EntityId(nextId++)
        val entity = Entity(id, this)
        entities[id] = entity
        // 先落到空 archetype(空类型集),再由 EntityBuilder 的 +Component 逐个迁移到目标 archetype。
        val empty = getOrCreateArchetype(emptySet())
        val row = empty.appendRow(id, emptyMap())
        entity.location = Archetype.Location(empty, row)
        if (parent != null) {
            entity.parent = parent
            parent.children.add(entity)
        }
        EntityBuilder(entity).block()
        return entity
    }

    /** 顶层创建实体的便捷别名。 */
    fun entity(block: EntityBuilder.() -> Unit): Entity = createEntity(null, block)

    /** 添加一个 [System],立即触发其 [System.onAdded]。 */
    fun addSystem(system: System): System {
        systems.add(system)
        system.onAdded(this)
        return system
    }

    /** 移除一个 [System],触发其 [System.onRemoved]。 */
    fun removeSystem(system: System) {
        if (systems.remove(system)) system.onRemoved(this)
    }

    /** 通过 DSL 内联定义并注册一个 [FunctionalSystem]。 */
    fun system(block: SystemScope.() -> Unit): System {
        val scope = SystemScope().apply(block)
        return addSystem(
            FunctionalSystem(
                onInit = scope.init ?: {},
                onUpdate = scope.update ?: { _, _ -> },
                onFixedUpdate = scope.fixedUpdate ?: { _, _ -> }
            )
        )
    }

    /** 创建一个新的 [Query]。 */
    fun query(): Query = Query(this)

    // ============ 组件变更(由 Entity 委托调用)============

    internal fun addComponent(entity: Entity, component: Component) {
        val loc = entity.location ?: error("实体缺少 location: $entity")
        val oldArch = loc.archetype
        val type = component::class
        // 若旧签名已含该类型,直接替换列内组件(不迁移)
        if (type in oldArch.typeSet) {
            oldArch.columns[type]!![loc.row] = component
            return
        }
        // 否则迁移到组件集更大的 archetype
        migrate(entity, oldArch, oldArch.typeSet + type) { comps ->
            comps[type] = component
        }
    }

    internal fun <T : Component> removeComponent(entity: Entity, type: KClass<out T>): T? {
        val loc = entity.location ?: return null
        val oldArch = loc.archetype
        if (type !in oldArch.typeSet) return null
        @Suppress("UNCHECKED_CAST")
        val removed = oldArch.columns[type]!![loc.row] as? T
        migrate(entity, oldArch, oldArch.typeSet - type) {
            // 不复制被移除的类型,其它类型由 migrate 默认复制
        }
        return removed
    }

    /**
     * 将 [entity] 从 [oldArch] 迁移到签名为 [newSignature] 的 archetype。
     *
     * [configure] 可在复制的基础上追加/覆盖组件(用于 add)。remove 时留空。
     */
    private inline fun migrate(
        entity: Entity,
        oldArch: Archetype,
        newSignature: Set<KClass<out Component>>,
        configure: (MutableMap<KClass<*>, Component>) -> Unit
    ) {
        val loc = entity.location!!
        val oldRow = loc.row
        val newArch = getOrCreateArchetype(newSignature)
        // 复制旧组件(仅保留存在于新签名的类型)
        val comps = HashMap<KClass<*>, Component>()
        for (t in oldArch.typeSet) {
            if (t in newSignature) comps[t] = oldArch.columns[t]!![oldRow]
        }
        configure(comps)
        val newRow = newArch.appendRow(entity.id, comps)
        // 从旧 archetype swap-remove,修正被搬移实体的 row
        val removed = oldArch.removeRow(oldRow)
        if (removed.movedId != null) {
            entities[removed.movedId]?.location?.row = removed.newRow
        }
        entity.location = Archetype.Location(newArch, newRow)
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <T : Component> getComponent(entity: Entity, type: KClass<out T>): T? {
        val loc = entity.location ?: return null
        return loc.archetype[loc.row, type]
    }

    internal fun hasComponent(entity: Entity, type: KClass<*>): Boolean {
        val loc = entity.location ?: return false
        return type in loc.archetype.typeSet
    }

    internal fun entityById(id: EntityId): Entity? = entities[id]

    /** 供 [Query] 遍历所有 archetype。 */
    internal fun archetypes(): Collection<Archetype> = archetypes.values

    // ============ 主循环驱动 ============

    /**
     * 推进世界状态。
     *
     * 1. 累加 [dt] 到累加器,以 [fixedStep] 为单位分发 [System.fixedUpdate]
     *    (受 [maxFixedStepsPerUpdate] 限制,避免死亡螺旋);
     * 2. 调用每个 [System.update];
     * 3. 清理已标记销毁的实体(级联剪除子树)。
     *
     * @param dt 距上次调用的实际秒数
     */
    fun update(dt: Float) {
        // 1) 固定步长
        accumulator += dt
        var steps = 0
        while (accumulator >= fixedStep && steps < maxFixedStepsPerUpdate) {
            for (sys in systems) sys.fixedUpdate(this, fixedStep)
            accumulator -= fixedStep
            steps++
        }
        // 若已达上限,丢弃残余以防无限累积
        if (steps >= maxFixedStepsPerUpdate) accumulator = 0f

        // 2) 帧更新
        for (sys in systems) sys.update(this, dt)

        // 3) 清理
        cleanupDestroyed()
    }

    /**
     * 移除所有 `alive=false` 的实体。
     *
     * 从 archetype 中 swap-remove,并修正被搬移实体的 [Archetype.Location.row];
     * 父子关系同步解除。
     */
    internal fun cleanupDestroyed() {
        val dead = entities.values.filterTo(ArrayList()) { !it.alive }
        if (dead.isEmpty()) return
        for (e in dead) {
            val loc = e.location ?: continue
            val removed = loc.archetype.removeRow(loc.row)
            if (removed.movedId != null) {
                entities[removed.movedId]?.location?.row = removed.newRow
            }
            entities.remove(e.id)
            e.parent?.children?.remove(e)
            e.parent = null
            e.location = null
        }
    }

    /**
     * 调试/统计:返回各 archetype 类型签名 → 实体数。
     */
    fun stats(): Map<Set<KClass<out Component>>, Int> =
        archetypes.values.associate { it.typeSet to it.size }

    private fun getOrCreateArchetype(signature: Set<KClass<out Component>>): Archetype =
        archetypes.getOrPut(signature) { Archetype(signature) }
}

// ============ DSL 入口 ============

/**
 * 世界构建 DSL 作用域。
 */
@EcsDsl
class WorldBuilder @PublishedApi internal constructor(val world: World) {

    /** 创建实体。 */
    fun entity(block: EntityBuilder.() -> Unit) {
        world.entity(block)
    }

    /** 内联定义并注册系统。 */
    fun system(block: SystemScope.() -> Unit) {
        world.system(block)
    }

    /** 添加已实例化的系统。 */
    fun system(system: System) {
        world.addSystem(system)
    }

    /** 设置固定步长(秒)。 */
    fun fixedStep(seconds: Float) {
        world.fixedStep = seconds
    }

    /** 设置单次 update 内最大固定步数。 */
    fun maxFixedStepsPerUpdate(n: Int) {
        world.maxFixedStepsPerUpdate = n
    }
}

/**
 * 顶层入口:创建并配置一个 [World]。
 *
 * ```kotlin
 * val world = world {
 *     fixedStep(1f / 30f)
 *     system { onFixedUpdate { w, dt -> /* ... */ } }
 *     entity { +Position(0f, 0f); tag("player") }
 * }
 * ```
 */
fun world(block: WorldBuilder.() -> Unit): World {
    val world = World()
    WorldBuilder(world).block()
    return world
}

/**
 * 全局单例组件快捷访问。
 *
 * 假设 [T] 类型的组件在世界上只有一个实例(如全局状态、配置)。
 * 多实例时返回第一个。
 */
inline fun <reified T : Component> World.singleton(): T? = firstComponent<T>()
