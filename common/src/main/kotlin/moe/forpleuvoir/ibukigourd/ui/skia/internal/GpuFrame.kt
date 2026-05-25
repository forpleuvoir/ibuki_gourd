package moe.forpleuvoir.ibukigourd.ui.skia.internal

import com.mojang.blaze3d.opengl.GlStateManager
import com.mojang.blaze3d.textures.GpuTexture
import com.mojang.blaze3d.textures.GpuTextureView
import moe.forpleuvoir.ibukigourd.ui.skia.SkiaContext
import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.Surface

/**
 * 单个 GPU 帧资源。
 *
 * 封装一个 OpenGL FBO + 纹理 + Skia Surface 的三元组，
 * 是 [moe.forpleuvoir.ibukigourd.ui.skia.SkiaSurface] 渲染目标的底层资源单元。
 */
internal class GpuFrame(
    /** FBO 标识符 */
    val fboId: Int,
    /** Minecraft GPU 纹理 */
    val gpuTexture: GpuTexture,
    /** GPU 纹理视图 */
    val textureView: GpuTextureView,
    /** Skia 渲染表面 */
    val skiaSurface: Surface,
    /** 后端渲染目标 */
    val renderTarget: BackendRenderTarget,
) {

    /**
     * 释放所有 GPU 资源。
     * - Skia Surface 和 BackendRenderTarget 在共享 GL 上下文中释放
     * - 纹理和 FBO 在 Minecraft 渲染线程释放
     */
    fun close() {
        SkiaContext.submit {
            skiaSurface.close()
            renderTarget.close()
        }
        textureView.close()
        gpuTexture.close()
        GlStateManager._glDeleteFramebuffers(fboId)
    }
}
