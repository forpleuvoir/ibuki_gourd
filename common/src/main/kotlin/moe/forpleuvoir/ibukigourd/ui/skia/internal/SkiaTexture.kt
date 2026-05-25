package moe.forpleuvoir.ibukigourd.ui.skia.internal

import com.mojang.blaze3d.textures.GpuTexture
import com.mojang.blaze3d.textures.GpuTextureView
import net.minecraft.client.renderer.texture.AbstractTexture

/**
 * 包装已存在的 [GpuTexture] 和 [GpuTextureView]，
 * 使其可通过 Minecraft [TextureManager][net.minecraft.client.renderer.texture.TextureManager]
 * 注册和查找，用于后续 [GuiGraphicsExtractor][net.minecraft.client.gui.GuiGraphicsExtractor.blit] 操作。
 */
internal class SkiaTexture : AbstractTexture() {

    /**
     * 绑定当前活动的 GPU 纹理。
     * 在 FBO 创建或替换后调用。
     */
    fun bindTexture(tex: GpuTexture?, view: GpuTextureView?) {
        this.texture = tex
        this.textureView = view
    }

    /**
     * 解除绑定的纹理引用。
     * 在旧纹理被替换前调用。
     */
    fun unbind() {
        this.texture = null
        this.textureView = null
    }

    override fun close() {
        unbind()
    }
}
