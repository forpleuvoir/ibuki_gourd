package moe.forpleuvoir.ibukigourd.mod.ui

import moe.forpleuvoir.ibukigourd.lang.IGLang
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

/**
 * Vulkan 图形后端下替代 Compose 配置界面的纯 Minecraft 提示屏幕。
 */
class UnsupportedBackendScreen(
    private val parentScreen: Screen?,
) : Screen(Component.literal("IbukiGourd")) {

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick)
        graphics.centeredText(
            font,
            IGLang.Misc.uiDisabled,
            this.width / 2,
            this.height / 2,
            0xFFFFFF
        )
    }

    override fun onClose() {
        minecraft.gui.setScreen(parentScreen)
    }
}
