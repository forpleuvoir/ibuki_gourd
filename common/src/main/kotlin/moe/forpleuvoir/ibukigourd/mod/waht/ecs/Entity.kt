package moe.forpleuvoir.ibukigourd.mod.waht.ecs

import kotlin.reflect.KClass

/**
 * 全局实体标识,创建后不可变,可用于事件载荷或外部弱引用。
 */
@JvmInline
value class EntityId(val value: Long) {
    override fun toString(): String = "Entity($value)"
}

/**
 * 实体句柄。
 *
 * 实体本身是轻量对象,真正数据存储在 [Archetype] 的列数组里。
 * [location] 记录实体当前所处的 archetype 与行号,增删组件时由 [World] 迁移更新。
 *
 * 层级:`parent` / [children] 构成树;[destroy] 会级联销毁整个子树,
 * 实际从 archetype 移除发生在 [World.cleanupDestroyed]。
 */
class Entity internal constructor(
    val id: EntityId,
    val world: World
) {
    /** 当前位置:所在 archetype + 行号。 */
    internal var location: Archetype.Location? = null

    /** 是否存活。置 false 后由 [World.cleanupDestroyed] 真正移除。 */
    var alive: Boolean = true
        internal set

    /** 父实体(可为 null 表示根)。 */
    var parent: Entity? = null
        internal set

    internal val children = mutableListOf<Entity>()

    /** 实体标签集合,用于 [Query] 过滤。 */
    val tags: MutableSet<String> = HashSet()

    // ============ 组件访问(reified 便捷版)============

    /** 添加组件;若组件类型已存在则覆盖旧实例。可能触发到新 archetype 的迁移。 */
    fun <T : Component> add(component: T): T {
        world.addComponent(this, component)
        return component
    }

    /** 移除指定类型组件;不存在返回 null。可能触发到新 archetype 的迁移。 */
    fun <T : Component> remove(type: KClass<out T>): T? = world.removeComponent(this, type)

    /** 获取指定类型组件;不存在返回 null。 */
    fun <T : Component> get(type: KClass<out T>): T? = world.getComponent(this, type)

    /** 是否拥有指定类型组件。 */
    fun has(type: KClass<*>): Boolean = world.hasComponent(this, type)

    inline fun <reified T : Component> get(): T? = get(T::class)

    inline fun <reified T : Component> has(): Boolean = has(T::class)

    inline fun <reified T : Component> remove(): T? = remove(T::class)

    /** DSL 操作符:等价于 [add]。 */
    operator fun <T : Component> plus(component: T): T = add(component)

    // ============ 标签 ============

    /** 添加标签,返回是否为新增。 */
    fun tag(name: String): Boolean = tags.add(name)

    /** 添加多个标签。 */
    fun tags(vararg names: String) {
        for (n in names) tags.add(n)
    }

    fun hasTag(name: String): Boolean = name in tags

    fun untag(name: String): Boolean = tags.remove(name)

    // ============ 层级 ============

    /** 子实体快照(不可变视图)。 */
    fun children(): List<Entity> = children.toList()

    /**
     * 创建一个挂在本实体下的子实体。
     *
     * @param block [EntityBuilder] 的配置 lambda
     */
    fun child(block: EntityBuilder.() -> Unit): Entity =
        world.createEntity(parent = this, block = block)

    /**
     * 标记本实体及所有后代为待销毁。
     *
     * 实际移除发生在下一次 [World.update] 的清理阶段,期间仍可被查询(已死实体会被过滤)。
     */
    fun destroy() {
        if (!alive) return
        alive = false
        for (c in children) c.destroy()
    }

    override fun toString(): String = id.toString()


}

/**
 * 实体构建 DSL 作用域。
 *
 * 在 [World.entity] / [Entity.child] 的 lambda 接收者中使用。
 * 在此作用域内,`+Component` 会调用 [Component.unaryPlus] 将组件添加到目标实体。
 */
@EcsDsl
class EntityBuilder @PublishedApi internal constructor(private val target: Entity) {

    /**
     * DSL:`+Component` 语句形式添加组件。
     *
     * 作为成员扩展定义,使 `+Position(...)` 解析为一元 `unaryPlus`,
     * 比二元 `plus` 更贴合语句式 DSL 的书写习惯。
     */
    operator fun Component.unaryPlus() {
        target.add(this)
    }

    /** 添加组件(函数式写法),等价于 `+component`。 */
    fun <T : Component> add(component: T): T = target.add(component)

    /** 添加单个标签。 */
    fun tag(name: String) {
        target.tag(name)
    }

    /** 添加多个标签。 */
    fun tags(vararg names: String) {
        target.tags(*names)
    }

    /** 创建挂在本实体下的子实体。 */
    fun child(block: EntityBuilder.() -> Unit): Entity = target.child(block)

    /** 访问被构建的实体本身(供需要在构建过程中读取状态的高级用法)。 */
    val entity: Entity get() = target
}
