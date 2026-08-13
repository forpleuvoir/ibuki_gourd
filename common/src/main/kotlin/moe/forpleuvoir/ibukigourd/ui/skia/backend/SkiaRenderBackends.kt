package moe.forpleuvoir.ibukigourd.ui.skia.backend

import moe.forpleuvoir.ibukigourd.platform.RenderBackend

/**
 * [SkiaRenderBackend] 选择工厂。
 *
 * 依据 Minecraft 实际生效的图形后端选择对应的 Skia 渲染后端。
 */
object SkiaRenderBackends {

    /**
     * 当前使用的 Skia 渲染后端。
     *
     * Vulkan 图形后端下本模组的 UI 功能已被禁用，因此正常情况下不会走到
     * Vulkan 分支；待 skiko 官方支持 Vulkan（Graphite）后在此接入
     * VulkanSkiaRenderBackend 实现。
     */
    val current: SkiaRenderBackend by lazy {
        when {
            RenderBackend.isVulkan -> TODO("待 skiko 支持 Vulkan（Graphite）后实现 VulkanSkiaRenderBackend")
            else -> OpenGLSkiaRenderBackend
        }
    }
}
