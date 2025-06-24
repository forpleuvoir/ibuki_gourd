package moe.forpleuvoir.ibukigourd.mod.what.ecs

import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import kotlin.reflect.KClass

interface World : EntityManager, ComponentManager, SystemManager, RenderSystemManager {

    companion object {

        operator fun invoke(scope: WorldDSL.() -> Unit = {}): World = DefaultWorld().apply { WorldDSL(this).scope() }
    }

    fun tick() {
        this.tick(this)
    }

    fun render(context: IGDrawContext, deltaTime: Float) {
        this.render(this, context, deltaTime)
    }
}

class DefaultWorld(
    private val entityManager: EntityManager = DefaultEntityManager(),
    private val componentManager: ComponentManager = DefaultComponentManager(),
    private val systemManager: SystemManager = DefaultSystemManager(),
    private val renderSystemManager: RenderSystemManager = DefaultRenderSystemManager(),
) : World,
    EntityManager by entityManager,
    ComponentManager by componentManager,
    SystemManager by systemManager,
    RenderSystemManager by renderSystemManager {

    override fun destroyEntity(entity: Entity): Boolean {
        return if (entityManager.destroyEntity(entity)) {
            removeAllComponent(entity)
            true
        } else false
    }

    override fun <T : Any> addComponent(entity: Entity, component: T): T? {
        return if (isEntityAlive(entity))
            componentManager.addComponent(entity, component)
        else null
    }

    override fun <T : Any> getComponent(entity: Entity, componentType: KClass<T>): T? {
        return if (isEntityAlive(entity)) componentManager.getComponent(entity, componentType) else null
    }

}

@ECSDsl
class WorldDSL(val world: World) {

    inline operator fun <reified T : System> T.unaryPlus() {
        world.addSystem(this)
    }

    inline operator fun <reified T : RenderSystem> T.unaryPlus() {
        world.addRenderSystem(this)
    }

    fun createEntity(block: EntityDSL.() -> Unit): Entity {
        val entity = world.createEntity()
        EntityDSL(entity, world).block()
        return entity
    }
}

fun World.createEntity(block: EntityDSL.() -> Unit): Entity {
    val entity = this.createEntity()
    EntityDSL(entity, this).block()
    return entity
}
