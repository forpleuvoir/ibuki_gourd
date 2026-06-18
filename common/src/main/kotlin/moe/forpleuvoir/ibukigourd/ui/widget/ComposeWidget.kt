package moe.forpleuvoir.ibukigourd.ui.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.ui.scene.ComposeSceneFactory
import moe.forpleuvoir.ibukigourd.ui.scene.ComposeSceneHost
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent

/**
 * 将 Compose 内容包装为原版 [AbstractWidget]。
 *
 * 目前问题很多不建议使用,暂时不建议使用,或者是只用于渲染
 */
class ComposeWidget(
    content: @Composable BoxScope.() -> Unit,
) : AbstractWidget(0, 0, 0, 0, Text.literal("ComposeWidget")) {

    private val host: ComposeSceneHost = ComposeSceneFactory.create {
        Box(Modifier.onSizeChanged {
            val guiScale = mc.window.guiScale
            this.width = it.width / guiScale
            this.height = it.height / guiScale
        }.onGloballyPositioned {
            val guiScale = mc.window.guiScale
            val pos = it.positionInWindow()
            this.x = (pos.x / guiScale).toInt()
            this.y = (pos.y / guiScale).toInt()
        }) {
            content()
        }
    }

    init {
        current?.dispose()
        current = this
        host.init()
    }

    override fun extractWidgetRenderState(
        graphics: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float,
    ) {
        host.extractRenderState(graphics, mouseX, mouseY, partialTick)
//        graphics.outline(x, y, x + width, y + height, Colors.RED.argb)
    }

    override fun updateWidgetNarration(narration: NarrationElementOutput) {
        defaultButtonNarrationText(narration)
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        isFocused = event.x >= x && event.x < x + width &&
                event.y >= y && event.y < y + height
        return if (isFocused) host.mouseClicked(event) else false
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        return host.mouseReleased(event)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        host.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
        return false
    }

    override fun keyPressed(event: KeyEvent): Boolean =
        if (isFocused) host.keyPressed(event) else false

    override fun keyReleased(event: KeyEvent): Boolean =
        if (isFocused) host.keyReleased(event) else false

    fun dispose() {
        if (current == this) current = null
        host.onClose()
    }

    companion object {
        private var current: ComposeWidget? = null
    }
}
