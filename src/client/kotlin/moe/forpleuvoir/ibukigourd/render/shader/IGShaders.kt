package moe.forpleuvoir.ibukigourd.render.shader

import moe.forpleuvoir.ibukigourd.util.identifier
import net.minecraft.client.gl.Defines
import net.minecraft.client.gl.ShaderProgramKey
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats

//@EventSubscriber
object IGShaders {

//    @Subscriber
//    @Suppress("UNUSED_PARAMETER")
//    fun init(event: ModInitializerEvent) {
//        val shaderProgramKeys = ShaderProgramKeys.getAll()
//        REGISTERED_SHADERS.forEach { shaderProgramKey ->
//            shaderProgramKeys.add(shaderProgramKey)
//        }
//    }

    private val REGISTERED_SHADERS = mutableListOf<ShaderProgramKey>()

    private fun shader(path: String, format: VertexFormat, defines: Defines = Defines.EMPTY): ShaderProgramKey =
        ShaderProgramKey(identifier(path), format, defines).apply {
            REGISTERED_SHADERS.add(this)
        }

    val POSITION_HSV_COLOR = shader("core/position_hsv_color", VertexFormats.POSITION_COLOR)


}