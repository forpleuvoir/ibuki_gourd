package moe.forpleuvoir.ibukigourd.platform

import com.mojang.blaze3d.systems.RenderSystem

/**
 * 当前 Minecraft 实际生效的图形后端。
 *
 * 检测使用 [RenderSystem.tryGetDevice] 的 [com.mojang.blaze3d.systems.DeviceInfo.backendName]，
 * 反映的是运行时实际创建成功的后端（Vulkan 初始化失败回退 OpenGL 时也会正确返回 OpenGL），
 * 而不是用户配置。
 */
object RenderBackend {

    /**
     * 当前是否为 Vulkan 后端。
     *
     * 设备尚未初始化（游戏启动早期）时视为 false，避免误禁用。
     */
    val isVulkan: Boolean
        get() = RenderSystem.tryGetDevice()
            ?.deviceInfo
            ?.backendName()
            ?.contains("vulkan", ignoreCase = true) == true
}
