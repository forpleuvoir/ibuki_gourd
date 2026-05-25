package moe.forpleuvoir.ibukigourd.ui.render

import com.mojang.blaze3d.opengl.GlTexture
import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.ColorTargetState
import com.mojang.blaze3d.pipeline.DepthStencilState
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.CompareOp
import com.mojang.blaze3d.shaders.UniformType
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.FilterMode
import com.mojang.blaze3d.textures.GpuTexture
import com.mojang.blaze3d.textures.GpuTextureView
import com.mojang.blaze3d.textures.TextureFormat
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import kotlinx.coroutines.Runnable
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.renderer.rendertype.RenderSetup
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.client.renderer.texture.AbstractTexture
import net.minecraft.resources.Identifier
import org.jetbrains.skia.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil.NULL

/**
 * 包装已存在的 GpuTexture，供 TextureManager 注册使用
 */
class PreInitTexture(gpuTex: GpuTexture, gpuView: GpuTextureView) : AbstractTexture() {
    init {
        this.texture = gpuTex
        this.textureView = gpuView
        this.sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)
    }
}

class SkiaRender {
    var windowId: Long = -1
        private set
    var frameBufferId = -1
        private set
    var textureId = -1  // 被 GpuDevice 分配，Skia 线程通过 Atomic 读到
        private set
    var gpuView: GpuTextureView? = null
        private set

    lateinit var context: DirectContext; private set

    lateinit var surface: Surface; private set

    val isSurfaceInitialized: Boolean get() = ::surface.isInitialized

    lateinit var renderType: RenderType; private set

    companion object {
        val SKIA_OUTPUT_ID = Identifier.fromNamespaceAndPath(IbukiGourd.MOD_ID, "skia_output")
    }

    /** 必须在 Minecraft 渲染线程调用 */
    fun ensureRenderType(width: Int, height: Int) {
        if (::renderType.isInitialized) return
        RenderSystem.assertOnRenderThread()

        val dev = RenderSystem.getDevice()
        val tex = dev.createTexture("skia_output", 4 or 1, TextureFormat.RGBA8, width, height, 1, 1)
        textureId = (tex as GlTexture).glId()
        gpuView = dev.createTextureView(tex)

        // 注册到 TextureManager，让 RenderSetup.withTexture() 能找到
        mc.textureManager.register(
            SKIA_OUTPUT_ID, PreInitTexture(tex, gpuView!!)
        )

        val pipeline = RenderPipeline.builder()
            .withLocation("custom/cpu_render_compose")
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withVertexShader("core/position_tex")
            .withFragmentShader("core/position_tex")
            .withSampler("Sampler0")
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
            .withColorTargetState(ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, true))
            .withCull(false)
            .build()

        renderType = RenderType.create(
            "cpu_render_compose",
            RenderSetup.builder(pipeline)
                .withTexture("Sampler0", SKIA_OUTPUT_ID)
                .bufferSize(768 * 1024)
                .createRenderSetup()
        )
    }

    fun prepareTexture() {
        val oldWindow = glfwGetCurrentContext()
        glfwMakeContextCurrent(0)
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        windowId = glfwCreateWindow(1, 1, "", NULL, oldWindow)
        glfwMakeContextCurrent(oldWindow)
    }

    fun prepareRender(width: Int, height: Int, skiaTexId: Int) {
        textureId = skiaTexId
        val oldWindow = glfwGetCurrentContext()
        glfwMakeContextCurrent(windowId)
        GL.createCapabilities()

        glDeleteFramebuffers(frameBufferId)   // ← 先删旧的，防止重复
        frameBufferId = glGenFramebuffers()
        glBindFramebuffer(GL_FRAMEBUFFER, frameBufferId)
        glBindTexture(GL_TEXTURE_2D, textureId)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, NULL)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId, 0)

        context = DirectContext.makeGL()
        val renderTarget = BackendRenderTarget.makeGL(width, height, 0, 8, frameBufferId, GL_RGBA8)
        surface = Surface.makeFromBackendRenderTarget(
            context, renderTarget, SurfaceOrigin.BOTTOM_LEFT,
            SurfaceColorFormat.RGBA_8888, ColorSpace.sRGB
        ) ?: throw IllegalStateException("Surface could not be created")

        glfwMakeContextCurrent(oldWindow)
    }

    fun render(renderTask: Runnable) {
        val oldWindow = glfwGetCurrentContext()
        glfwMakeContextCurrent(windowId)
        surface.canvas.clear(Color.TRANSPARENT)
        renderTask.run()
        context.flush()
        glFinish()
        glfwMakeContextCurrent(oldWindow)
    }

    fun resize(width: Int, height: Int) {
        val oldWindow = glfwGetCurrentContext()
        glfwMakeContextCurrent(windowId)

        // 1. 改了纹理大小
        glBindTexture(GL_TEXTURE_2D, textureId)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, NULL)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId, 0)

//        // 2. 标红——如果渲染路径是通的，屏幕应该变红
//        glClearColor(1.0f, 0.0f, 0.0f, 1.0f)
//        glClear(GL_COLOR_BUFFER_BIT)

        surface.close()
        val renderTarget = BackendRenderTarget.makeGL(width, height, 0, 8, frameBufferId, GL_RGBA8)
        surface = Surface.makeFromBackendRenderTarget(
            context, renderTarget, SurfaceOrigin.BOTTOM_LEFT, SurfaceColorFormat.RGBA_8888, ColorSpace.sRGB
        ) ?: throw IllegalStateException("Failed to recreate Surface")

        glfwMakeContextCurrent(oldWindow)

        println("[SkiaRender] resized to $width x $height, textureId=$textureId")
    }


}
