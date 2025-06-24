package moe.forpleuvoir.ibukigourd.mod.what.ecs

import kotlin.reflect.KClass

interface ComponentManager {

    fun <T : Any> addComponent(entity: Entity, component: T): T?

    fun <T : Any> getComponent(entity: Entity, componentType: KClass<T>): T?

    fun <T : Any> removeComponent(entity: Entity, componentType: KClass<T>)

    fun removeAllComponent(entity: Entity)

    fun <T : Any> getAllComponents(type: KClass<T>): Map<Entity, T>

    fun <T : Any> getEntityByComponent(componentType: KClass<T>): Set<Entity>

}

fun ComponentManager.queryEntity() = EntityQuery(this)

inline fun <reified T : Any> ComponentManager.removeComponent(entity: Entity) = removeComponent(entity, T::class)

inline fun <reified T : Any> ComponentManager.getComponent(entity: Entity) = getComponent(entity, T::class)

inline fun <reified T : Any> ComponentManager.getAllComponents() = getAllComponents(T::class)

inline fun <reified T : Any> ComponentManager.getEntitiesByComponentType() = getAllComponents<T>().keys

inline fun <reified A : Any> ComponentManager.query() = getAllComponents<A>().asSequence()

inline fun <reified A : Any> ComponentManager.findFirst() = getAllComponents<A>().values.firstOrNull()

inline fun <reified A : Any, reified B : Any> ComponentManager.queryPair() =
    queryEntity().require<A>().require<B>().map { entity ->
        entity to Pair(getComponent<A>(entity)!!, getComponent<B>(entity)!!)
    }.asSequence()

inline fun <reified A : Any, reified B : Any, reified C : Any> ComponentManager.queryTuple() =
    queryEntity().require<A>().require<B>().require<C>().map { entity ->
        entity to Triple(getComponent<A>(entity)!!, getComponent<B>(entity)!!, getComponent<C>(entity)!!)
    }.asSequence()

class DefaultComponentManager : ComponentManager {

    private val stores = mutableMapOf<KClass<*>, MutableMap<Entity, Any>>()

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> storeFor(type: KClass<T>): MutableMap<Int, T> {
        return stores.getOrPut(type) { mutableMapOf() } as MutableMap<Int, T>
    }

    override fun <T : Any> addComponent(entity: Entity, component: T): T {
        val store = storeFor(component::class) as MutableMap<Int, T>
        store[entity] = component
        return component
    }

    override fun <T : Any> getComponent(entity: Entity, componentType: KClass<T>): T? {
        return storeFor(componentType)[entity]
    }

    override fun <T : Any> removeComponent(entity: Entity, componentType: KClass<T>) {
        storeFor(componentType).remove(entity)
    }

    override fun removeAllComponent(entity: Entity) {
        for (type in stores.keys) {
            storeFor(type).remove(entity)
        }
    }

    override fun <T : Any> getAllComponents(type: KClass<T>): Map<Int, T> {
        return storeFor(type).toMap()
    }

    override fun <T : Any> getEntityByComponent(componentType: KClass<T>): Set<Int> {
        return storeFor(componentType).keys
    }
}
