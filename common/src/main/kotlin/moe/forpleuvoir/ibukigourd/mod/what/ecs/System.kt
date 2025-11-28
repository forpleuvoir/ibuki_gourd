package moe.forpleuvoir.ibukigourd.mod.what.ecs

fun interface System {
    fun tick(world: World)
}

interface SystemManager {

    fun addSystem(system: System)

    fun removeSystem(system: System)

    fun tick(world: World)

}

class DefaultSystemManager : SystemManager {

    private val systems = mutableListOf<System>()

    override fun addSystem(system: System) {
        systems.add(system)
    }

    override fun removeSystem(system: System) {
        systems.remove(system)
    }

    override fun tick(world: World) {
        systems.forEach { it.tick(world) }
    }

}