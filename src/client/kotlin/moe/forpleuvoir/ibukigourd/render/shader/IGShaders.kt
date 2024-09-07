package moe.forpleuvoir.ibukigourd.render.shader

import moe.forpleuvoir.ibukigourd.event.events.ModInitializerEvent
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.nebula.event.EventSubscriber
import moe.forpleuvoir.nebula.event.Subscriber
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback
import net.minecraft.client.gl.ShaderProgram
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.util.Identifier


@EventSubscriber
object IGShaders {

    @Subscriber
    @Suppress("UNUSED_PARAMETER")
    fun init(event: ModInitializerEvent) {
        CoreShaderRegistrationCallback.EVENT.register(identifier("shaders")) { context ->
            REGISTERED_SHADERS.forEach { (identifier, block) ->
                context.register(identifier, block.first, block.second)
            }
        }
    }

    private val REGISTERED_SHADERS = mutableMapOf<Identifier, Pair<VertexFormat, (ShaderProgram) -> Unit>>()

    private fun shader(path: String, format: VertexFormat): () -> ShaderProgram? {
        val identifier = identifier(path)
        var shader: ShaderProgram? = null
        REGISTERED_SHADERS[identifier] = format to { shader = it }
        return { shader }
    }


    val POSITION_HSV_COLOR = shader("position_hsv_color", VertexFormats.POSITION_COLOR)


}