package moe.forpleuvoir.ibukigourd.ui.render

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.FilterMode
import com.mojang.blaze3d.textures.GpuTexture
import net.minecraft.client.renderer.texture.AbstractTexture

class SkiaTexture(private val gpuTexture: GpuTexture) : AbstractTexture() {
    init {
        this.texture = gpuTexture
        this.textureView = RenderSystem.getDevice().createTextureView(gpuTexture)
        this.sampler = RenderSystem.getSamplerCache()
            .getClampToEdge(FilterMode.LINEAR)
    }

    override fun close() {
        //由SkiaRender管理
    }

}