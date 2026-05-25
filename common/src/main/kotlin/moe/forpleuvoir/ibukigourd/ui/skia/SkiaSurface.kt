package moe.forpleuvoir.ibukigourd.ui.skia

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.asComposeCanvas
import com.mojang.blaze3d.opengl.GlConst.*
import com.mojang.blaze3d.opengl.GlStateManager
import com.mojang.blaze3d.opengl.GlTexture
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.GpuTexture
import com.mojang.blaze3d.textures.TextureFormat
import moe.forpleuvoir.ibukigourd.ui.skia.internal.FrameRetirement
import moe.forpleuvoir.ibukigourd.ui.skia.internal.GpuFrame
import moe.forpleuvoir.ibukigourd.ui.skia.internal.SkiaTexture
import moe.forpleuvoir.ibukigourd.util.identifier
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import org.jetbrains.skia.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * GPU 渲染表面。
 *
 * 管理一个由 OpenGL FBO + 纹理 + Skia Surface 组成的 GPU 渲染目标，
 * 负责将 Compose 场景渲染结果输出到 Minecraft GUI 图形管线。
 *
 * 使用方式：
 * 1. [resize] — 创建/调整 GPU 资源
 * 2. [update] — 每帧将 Skia 渲染结果混合到 [GuiGraphicsExtractor] 缓冲区
 *
 * 旧帧资源通过 [FrameRetirement] 延迟回收，避免 GPU 竞争。
 */
class SkiaSurface {

    companion object {
        private val idCounter = AtomicInteger()
        private const val USAGE = GpuTexture.USAGE_COPY_DST or
                GpuTexture.USAGE_COPY_SRC or
                GpuTexture.USAGE_TEXTURE_BINDING or
                GpuTexture.USAGE_RENDER_ATTACHMENT
    }

    /** Minecraft 纹理管理器中的资源标识符 */
    private val textureId = identifier("render_surface_${idCounter.getAndIncrement()}")

    private var lastWidth: Int = 0
    private var lastHeight: Int = 0

    /** 当前活动的 GPU 帧资源 */
    private var activeFrame: GpuFrame? = null

    /** Minecraft AbstractTexture 包装 */
    private var boundTexture = SkiaTexture()

    /** 是否已向 TextureManager 注册 */
    private var isRegistered = false

    /** 延迟回收队列 */
    private val retirement = FrameRetirement()

    // ── 公开 API ─────────────────────────────────────────────────────────

    /**
     * 调整渲染表面尺寸。
     * 创建新的 FBO + 纹理 + Skia Surface，旧资源进入延迟回收。
     */
    fun resize(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        if (lastWidth == width && lastHeight == height) return

        boundTexture.unbind()

        activeFrame?.let { old -> retirement.retire(old) }
        activeFrame = null

        lastWidth = width
        lastHeight = height

        val device = RenderSystem.getDevice()

        val gpuTexture = device.createTexture(
            "Skia Surface", USAGE,
            TextureFormat.RGBA8, width, height, 1, 1
        )
        val textureView = device.createTextureView(gpuTexture)
        val glId = (gpuTexture as GlTexture).glId()

        val fbo = GlStateManager.glGenFramebuffers()
        GlStateManager._glBindFramebuffer(GL_FRAMEBUFFER, fbo)
        GlStateManager._glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, glId, 0)
        GlStateManager._glBindFramebuffer(GL_FRAMEBUFFER, 0)

        var skiaSurface: Surface? = null
        var renderTarget: BackendRenderTarget? = null
        SkiaContext.submit {
            val bt = BackendRenderTarget.makeGL(width, height, 0, 8, fbo, GL_RGBA8)
            renderTarget = bt
            skiaSurface = Surface.makeFromBackendRenderTarget(
                SkiaContext.sharedContext, bt,
                SurfaceOrigin.TOP_LEFT,
                SurfaceColorFormat.RGBA_8888,
                ColorSpace.sRGB
            ) ?: throw RuntimeException("Failed to create Skia surface")
        }

        activeFrame = GpuFrame(fbo, gpuTexture, textureView, skiaSurface!!, renderTarget!!)
        boundTexture.bindTexture(gpuTexture, textureView)
    }

    private val postRenderDeque: Deque<GuiGraphicsExtractor.() -> Unit> = ArrayDeque()

    fun postRender(block: GuiGraphicsExtractor.() -> Unit) {
        postRenderDeque.addLast(block)
    }

    /**
     * 将 Compose 场景渲染结果呈现到 Minecraft GUI 缓冲区。
     *
     * 1. 回收过期 GPU 资源
     * 2. 注册纹理（首次）
     * 3. 在共享 GL 上下文中渲染 Skia 内容
     * 4. 将结果 blit 到 [GuiGraphicsExtractor]
     *
     * @param guiGraphics  当前帧的 GUI 图形上下文
     * @param renderBlock  实际的 Compose 场景渲染回调
     */
    fun update(guiGraphics: GuiGraphicsExtractor, renderBlock: (Canvas) -> Unit) {
        retirement.update()

        val frame = activeFrame ?: return

        if (!isRegistered) {
            Minecraft.getInstance().textureManager.register(textureId, boundTexture)
            isRegistered = true
        }

        SkiaContext.submit {
            SkiaContext.sharedContext.resetGLAll()
            frame.skiaSurface.canvas.clear(0)
            renderBlock(frame.skiaSurface.canvas.asComposeCanvas())
            frame.skiaSurface.flushAndSubmit()
        }

        guiGraphics.blit(textureId, 0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), 0f, 1f, 0f, 1f)

        while (true) {
            postRenderDeque.poll()?.invoke(guiGraphics) ?: break
        }
    }

    /**
     * 释放所有 GPU 资源并清空回收队列。
     * 在场景销毁时调用。
     */
    fun dispose() {
        activeFrame?.let { it.close(); activeFrame = null }
        retirement.dispose()
        boundTexture.unbind()
        isRegistered = false
    }
}

/**
 * Compose CompositionLocal，向下层组件提供当前 [SkiaSurface] 实例。
 */
val LocalSkiaSurface = staticCompositionLocalOf<SkiaSurface> { error("No RenderSurface provided") }
