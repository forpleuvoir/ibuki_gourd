package moe.forpleuvoir.ibukigourd.mod.what.ecs

typealias Entity = Int

interface EntityManager {

    fun createEntity(): Entity

    fun destroyEntity(entity: Entity): Boolean

    fun isEntityAlive(entity: Entity): Boolean

}

class DefaultEntityManager : EntityManager {

    private var nextId = 0

    private val entities = mutableListOf<Int>()

    override fun createEntity(): Entity {
        val result = nextId
        nextId++
        entities.add(result)
        return result
    }

    override fun destroyEntity(entity: Entity): Boolean {
        return entities.remove(entity)
    }

    override fun isEntityAlive(entity: Entity): Boolean {
        return entity in entities
    }
}

@ECSDsl
class EntityDSL(val entity: Entity, val world: World) {

    inline operator fun <reified T : Any> T.unaryPlus() {
        world.addComponent(entity, this)
    }

}