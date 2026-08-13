package moe.forpleuvoir.ibukigourd.ui.skia.backend

import com.mojang.blaze3d.GpuFormat
import com.mojang.blaze3d.opengl.GlConst.GL_COLOR_ATTACHMENT0
import com.mojang.blaze3d.opengl.GlConst.GL_FRAMEBUFFER
import com.mojang.blaze3d.opengl.GlConst.GL_RGBA8
import com.mojang.blaze3d.opengl.GlConst.GL_TEXTURE_2D
import com.mojang.blaze3d.opengl.GlStateManager
import com.mojang.blaze3d.opengl.GlTexture
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.GpuTexture
import com.mojang.blaze3d.textures.GpuTextureView
import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.BackendTexture
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.Image
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.WGL
import java.util.concurrent.atomic.AtomicInteger

/**
 * OpenGL（Ganesh）渲染后端。
 *
 * 管理一个与主渲染上下文共享显示列表的隐藏 OpenGL 上下文，
 * 并通过 GL FBO + [BackendRenderTarget.makeGL] 创建绑定到 Minecraft
 * 纹理的 Skia 表面。
 */
object OpenGLSkiaRenderBackend : SkiaRenderBackend {

    override val name: String = "OpenGL"

    private val idCounter = AtomicInteger()

    private val SURFACE_USAGE = GpuTexture.USAGE_COPY_DST or
        GpuTexture.USAGE_COPY_SRC or
        GpuTexture.USAGE_TEXTURE_BINDING or
        GpuTexture.USAGE_RENDER_ATTACHMENT

    private var contextId: Long = 0

    override val sharedContext: DirectContext by lazy {
        DirectContext.makeGL()
    }

    override fun init() {
        if (contextId != 0L) return
        contextId = WGL.wglCreateContext(null, WGL.wglGetCurrentDC())
        if (contextId == 0L) return

        val current = WGL.wglGetCurrentContext(null)
        WGL.wglShareLists(null, current, contextId)
    }

    override fun submit(task: () -> Unit) {
        val previous = WGL.wglGetCurrentContext(null)
        val dc = WGL.wglGetCurrentDC()
        WGL.wglMakeCurrent(null, dc, contextId)
        try {
            task()
        } finally {
            WGL.wglMakeCurrent(null, dc, previous)
        }
    }

    override fun createSurfaceTarget(width: Int, height: Int): SkiaRenderTarget =
        createTarget(width, height, adoptImage = false)

    override fun createAtlasTarget(width: Int, height: Int): SkiaRenderTarget =
        createTarget(width, height, adoptImage = true)

    private fun createTarget(width: Int, height: Int, adoptImage: Boolean): SkiaRenderTarget {
        val device = RenderSystem.getDevice()
        val label = "Skia Surface ${idCounter.getAndIncrement()}"
        val gpuTexture = device.createTexture(label, SURFACE_USAGE, GpuFormat.RGBA8_UNORM, width, height, 1, 1)
        val textureView = device.createTextureView(gpuTexture)
        val glId = (gpuTexture as GlTexture).glId()

        val fboId = GlStateManager.glGenFramebuffers()
        GlStateManager._glBindFramebuffer(GL_FRAMEBUFFER, fboId)
        GlStateManager._glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, glId, 0)
        GlStateManager._glBindFramebuffer(GL_FRAMEBUFFER, 0)

        if (adoptImage) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, glId)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE)
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
        }

        var skiaSurface: Surface? = null
        var renderTarget: BackendRenderTarget? = null
        submit {
            val bt = BackendRenderTarget.makeGL(width, height, 0, 8, fboId, GL_RGBA8)
            renderTarget = bt
            skiaSurface = Surface.makeFromBackendRenderTarget(
                sharedContext, bt,
                SurfaceOrigin.TOP_LEFT,
                SurfaceColorFormat.RGBA_8888,
                ColorSpace.sRGB
            ) ?: throw RuntimeException("Failed to create Skia surface")
        }

        val image = if (adoptImage) {
            var adopted: Image? = null
            submit {
                // 与 FBO 共用同一 GL 纹理，Skia 接管该纹理所有权（关闭 Image 时删除纹理）
                adopted = Image.adoptTextureFrom(
                    sharedContext,
                    BackendTexture.makeGL(width, height, false, glId, GL11.GL_TEXTURE_2D, GL_RGBA8),
                    SurfaceOrigin.TOP_LEFT,
                    ColorType.RGBA_8888
                )
            }
            adopted
        } else {
            null
        }

        return OpenGLSkiaRenderTarget(
            width, height, fboId, gpuTexture, textureView,
            skiaSurface!!, renderTarget!!, image
        )
    }

    override fun dispose() {
        if (contextId != 0L) {
            WGL.wglDeleteContext(null, contextId)
            contextId = 0L
        }
    }
}

/**
 * OpenGL 离屏渲染表面（GpuFrame 的替代）。
 */
private class OpenGLSkiaRenderTarget(
    override val width: Int,
    override val height: Int,
    private val fboId: Int,
    override val gpuTexture: GpuTexture,
    override val textureView: GpuTextureView,
    override val skiaSurface: Surface,
    private val renderTarget: BackendRenderTarget,
    override val textureImage: Image?,
) : SkiaRenderTarget {

    override fun flushAndSubmit() = skiaSurface.flushAndSubmit()

    override fun close() {
        OpenGLSkiaRenderBackend.submit {
            skiaSurface.close()
            renderTarget.close()
        }
        // textureImage 持有 GL 纹理所有权（Skia 关闭时删除纹理）；
        // 先于 gpuTexture 关闭，避免 id 复用后误删其它纹理（gpuTexture.close 对已删 id 为 no-op）
        textureImage?.close()
        textureView.close()
        gpuTexture.close()
        GlStateManager._glDeleteFramebuffers(fboId)
    }
}
