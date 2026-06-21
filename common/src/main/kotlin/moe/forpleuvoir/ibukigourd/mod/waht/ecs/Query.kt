package moe.forpleuvoir.ibukigourd.mod.waht.ecs

import kotlin.reflect.KClass

/**
 * 实体查询。
 *
 * 基于 archetype 子集匹配:遍历所有 archetype,筛选出类型签名满足条件者,
 * 在其列数组上线性扫描各行,按标签二次过滤。
 *
 * 典型用法见 [forEach] / [World.query] 扩展。
 */
class Query @PublishedApi internal constructor(private val world: World) {

    private val allTypes = HashSet<KClass<*>>()
    private val noneTypes = HashSet<KClass<*>>()
    private val anyTypes = HashSet<KClass<*>>()
    private val allTags = HashSet<String>()

    /** 要求实体拥有列出的**全部**组件类型。提供 1~3 元组的 reified 重载。 */
    @JvmName("all1")
    inline fun <reified A : Component> all(): Query = allRaw(A::class)

    @JvmName("all2")
    inline fun <reified A : Component, reified B : Component> all(): Query = allRaw(A::class, B::class)

    @JvmName("all3")
    inline fun <reified A : Component, reified B : Component, reified C : Component> all(): Query =
        allRaw(A::class, B::class, C::class)

    @PublishedApi
    internal fun allRaw(vararg types: KClass<*>): Query {
        for (t in types) allTypes.add(t)
        return this
    }

    /** 要求实体**不拥有**任一列出的组件类型。 */
    inline fun <reified A : Component> none(): Query = noneRaw(A::class)

    @PublishedApi
    internal fun noneRaw(vararg types: KClass<*>): Query {
        for (t in types) noneTypes.add(t)
        return this
    }

    /** 要求实体拥有列出的**任意一个**组件类型。 */
    inline fun <reified A : Component> any(): Query = anyRaw(A::class)

    @PublishedApi
    internal fun anyRaw(vararg types: KClass<*>): Query {
        for (t in types) anyTypes.add(t)
        return this
    }

    /** 要求实体拥有列出的**全部**标签。 */
    fun tags(vararg names: String): Query {
        for (n in names) allTags.add(n)
        return this
    }

    // ============ 终端操作 ============

    /**
     * 收集所有匹配的存活实体。
     *
     * 注意:返回快照列表,遍历期间对世界的增删不会影响已返回的结果。
     */
    fun entities(): List<Entity> {
        val out = ArrayList<Entity>()
        collectInto(out)
        return out
    }

    /** 遍历所有匹配实体。基于快照,遍历安全。 */
    inline fun each(block: (Entity) -> Unit) {
        for (e in entities()) block(e)
    }

    /** 是否有匹配的存活实体(尽早返回)。 */
    fun any(): Boolean {
        for (archetype in world.archetypes()) {
            if (!matches(archetype)) continue
            for (row in 0 until archetype.size) {
                val id = archetype.rowToId[row]
                val entity = world.entityById(id) ?: continue
                if (!entity.alive) continue
                if (allTags.isNotEmpty() && !entity.tags.containsAll(allTags)) continue
                return true
            }
        }
        return false
    }

    /** 匹配实体计数(无快照分配)。 */
    fun count(): Int {
        var count = 0
        for (archetype in world.archetypes()) {
            if (!matches(archetype)) continue
            for (row in 0 until archetype.size) {
                val id = archetype.rowToId[row]
                val entity = world.entityById(id) ?: continue
                if (!entity.alive) continue
                if (allTags.isNotEmpty() && !entity.tags.containsAll(allTags)) continue
                count++
            }
        }
        return count
    }

    /** 第一个匹配实体,无则抛 [NoSuchElementException]。 */
    fun first(): Entity = firstOrNull() ?: error("Query returned no matching entities")

    /** 第一个匹配实体,无则返回 null。 */
    fun firstOrNull(): Entity? {
        for (archetype in world.archetypes()) {
            if (!matches(archetype)) continue
            for (row in 0 until archetype.size) {
                val id = archetype.rowToId[row]
                val entity = world.entityById(id) ?: continue
                if (!entity.alive) continue
                if (allTags.isNotEmpty() && !entity.tags.containsAll(allTags)) continue
                return entity
            }
        }
        return null
    }

    /**
     * 惰性序列:按 archetype 分批 yield 匹配实体。
     * 遍历期间安全(已快照行号)。
     */
    fun asSequence(): Sequence<Entity> = sequence {
        for (archetype in world.archetypes()) {
            if (!matches(archetype)) continue
            for (row in 0 until archetype.size) {
                val id = archetype.rowToId[row]
                val entity = world.entityById(id) ?: continue
                if (!entity.alive) continue
                if (allTags.isNotEmpty() && !entity.tags.containsAll(allTags)) continue
                yield(entity)
            }
        }
    }

    /**
     * 收集匹配实体的指定类型组件(零 entity 分配)。
     *
     * 直接读取 archetype 列数组,跳过 entity→[Entity.get] 查表。
     * 注意:仅应用组件类型过滤(all/any/none),不处理标签过滤;
     * 如需标签过滤,请使用 [entities] + [Entity.get]。
     */
    inline fun <reified A : Component> components(): List<A> = componentsRaw(A::class)

    @PublishedApi
    internal fun <A : Component> componentsRaw(type: KClass<A>): List<A> {
        val out = ArrayList<A>()
        for (archetype in world.archetypes()) {
            if (!matches(archetype)) continue
            val col = archetype.columns[type] ?: continue
            @Suppress("UNCHECKED_CAST")
            out.addAll(col as List<A>)
        }
        return out
    }

    @PublishedApi
    internal fun collectInto(out: MutableList<Entity>) {
        for (archetype in world.archetypes()) {
            if (!matches(archetype)) continue
            // 在 archetype 行内线性扫描(缓存友好)
            for (row in 0 until archetype.size) {
                val id = archetype.rowToId[row]
                val entity = world.entityById(id) ?: continue
                if (!entity.alive) continue
                if (allTags.isNotEmpty() && !entity.tags.containsAll(allTags)) continue
                out.add(entity)
            }
        }
    }

    private fun matches(archetype: Archetype): Boolean {
        val sig = archetype.typeSet
        // all:签名必须包含全部 allTypes
        if (allTypes.isNotEmpty() && !sig.containsAll(allTypes)) return false
        // none:签名与 noneTypes 交集必须为空
        if (noneTypes.isNotEmpty() && sig.any { it in noneTypes }) return false
        // any:签名与 anyTypes 必须有交集(anyTypes 为空表示无此约束)
        if (anyTypes.isNotEmpty() && sig.none { it in anyTypes }) return false
        return true
    }
}

// ============ World 上的便捷扩展(主推用法)============

/** 构建一个查询(要求拥有指定单个组件类型)。 */
@JvmName("query1")
inline fun <reified A : Component> World.query(): Query = Query(this).all<A>()

/** 构建一个查询(要求同时拥有 [A] 和 [B])。 */
@JvmName("query2")
inline fun <reified A : Component, reified B : Component> World.query(): Query =
    Query(this).all<A, B>()

/** 构建一个查询(要求同时拥有 [A]、[B]、[C])。 */
@JvmName("query3")
inline fun <reified A : Component, reified B : Component, reified C : Component> World.query(): Query =
    Query(this).all<A, B, C>()

/** 查询拥有 [A] 的全部实体。 */
inline fun <reified A : Component> World.forEach(crossinline block: (Entity, A) -> Unit) {
    Query(this).all<A>().each { e ->
        val a = e.get<A>() ?: return@each
        block(e, a)
    }
}

/** 查询同时拥有 [A] 和 [B] 的全部实体,直接解构出组件。 */
inline fun <reified A : Component, reified B : Component> World.forEach(
    crossinline block: (Entity, A, B) -> Unit
) {
    Query(this).all<A, B>().each { e ->
        val a = e.get<A>() ?: return@each
        val b = e.get<B>() ?: return@each
        block(e, a, b)
    }
}

/** 查询同时拥有 [A]、[B]、[C] 的全部实体。 */
inline fun <reified A : Component, reified B : Component, reified C : Component> World.forEach(
    crossinline block: (Entity, A, B, C) -> Unit
) {
    Query(this).all<A, B, C>().each { e ->
        val a = e.get<A>() ?: return@each
        val b = e.get<B>() ?: return@each
        val c = e.get<C>() ?: return@each
        block(e, a, b, c)
    }
}

/**
 * 获取第一个拥有 [T] 的实体的组件实例,无则返回 null。
 *
 * 使用 [Query.components] 路径,零 entity 分配。
 */
inline fun <reified T : Component> World.firstComponent(): T? =
    query<T>().components<T>().firstOrNull()

/**
 * 直接收集世界内所有 [T] 类型组件(零 entity 分配)。
 *
 * 遍历所有 archetype 列,无视实体存活状态(已销毁实体已被 [World.cleanupDestroyed] 移除)。
 * 等效于 `query<T>().components<T>()`,语义等价于"全部组件实例"。
 */
inline fun <reified T : Component> World.allComponents(): List<T> =
    query<T>().components()
