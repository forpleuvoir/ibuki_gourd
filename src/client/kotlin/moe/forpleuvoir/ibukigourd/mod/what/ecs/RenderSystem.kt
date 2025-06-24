package moe.forpleuvoir.ibukigourd.mod.what.ecs

import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext

fun interface RenderSystem {

    fun render(world: World, context: IGDrawContext, deltaTime: Float)

}

interface RenderSystemManager {

    fun addRenderSystem(system: RenderSystem)

    fun removeRenderSystem(system: RenderSystem)

    fun render(world: World, context: IGDrawContext, deltaTime: Float)

}

class DefaultRenderSystemManager : RenderSystemManager {

    private val systems = mutableListOf<RenderSystem>()

    override fun addRenderSystem(system: RenderSystem) {
        systems.add(system)
    }

    override fun removeRenderSystem(system: RenderSystem) {
        systems.remove(system)
    }

    override fun render(world: World, context: IGDrawContext, deltaTime: Float) {
        systems.forEach { it.render(world, context, deltaTime) }
    }

}