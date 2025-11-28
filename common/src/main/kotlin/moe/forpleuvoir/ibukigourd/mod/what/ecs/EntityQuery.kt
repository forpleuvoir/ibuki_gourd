package moe.forpleuvoir.ibukigourd.mod.what.ecs

import kotlin.reflect.KClass

class EntityQuery(val manager: ComponentManager) {

    private val requiredTypes = mutableSetOf<KClass<*>>()
    private var filterPredicate: ((Entity) -> Boolean)? = { true }

    inline fun <reified T : Any> require(): EntityQuery {
        return require(T::class)
    }

    fun <T : Any> require(type: KClass<T>): EntityQuery {
        requiredTypes.add(type)
        return this
    }

    fun filter(predicate: (Entity) -> Boolean): EntityQuery {
        this.filterPredicate = predicate
        return this
    }

    private fun getMatchingEntities(): Sequence<Entity> {
        if (requiredTypes.isEmpty()) return emptySequence()

        val sets = requiredTypes.map { type ->
            manager.getEntityByComponent(type)
        }

        val commonEntities = sets.reduce { acc, set -> acc.intersect(set).toSet() }
        return commonEntities.asSequence().apply {
            if (filterPredicate != null) {
                filter { filterPredicate!!(it) }
            }
        }
    }

    fun forEach(action: (Entity) -> Unit): EntityQuery {
        getMatchingEntities().forEach(action)
        return this
    }

    fun <R> map(transform: (Entity) -> R): List<R> {
        return getMatchingEntities().map(transform).toList()
    }

    fun firstOrNull(): Entity? {
        return getMatchingEntities().firstOrNull()
    }

    fun count(): Int {
        return getMatchingEntities().count()
    }

    fun any(): Boolean {
        return getMatchingEntities().any()
    }

    fun any(predicate: (Entity) -> Boolean): Boolean {
        return getMatchingEntities().any(predicate)
    }

    fun all(predicate: (Entity) -> Boolean): Boolean {
        return getMatchingEntities().all(predicate)
    }

    fun toList(): List<Entity> {
        return getMatchingEntities().toList()
    }
}
