package moe.forpleuvoir.ibukigourd.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.FilterMode
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.ui.render.ComposeRender
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.nebula.common.api.Initializable
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent

@OptIn(InternalComposeUiApi::class)
class ComposeScreen(
    val content: @Composable (ComposeScreen) -> Unit
) : Screen(Text.literal("ComposeScreen")) {

    internal companion object : Initializable {
        override fun init() {

        }
    }

    private val composeRender = ComposeRender()

    private val windowWidth: Int get() = minecraft.window.width
    private val windowHeight: Int get() = minecraft.window.height

    private val uiScale: Float
        get() {
            val scale = minecraft.options.guiScale().get().toFloat()
            if (scale <= 0) return 1f
            return scale / 2
        }

    private val fontScale: Float get() = 1f

    override fun init() {
        composeRender.init(windowWidth, windowHeight, uiScale, fontScale) {
            content(this)
        }
    }

    override fun rebuildWidgets() {
        this.clearWidgets()
        this.clearFocus()
        this.setInitialFocus()
    }

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        val view = composeRender.gpuView ?: return
        graphics.blit(
            view,
            RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR),
            0, 0, width, height,
            0f, 1f, 1f, 0f
        )
    }

    override fun resize(width: Int, height: Int) {
        minecraft.execute {
            composeRender.resize(windowWidth, windowHeight, uiScale, fontScale)
        }
        this.width = width
        this.height = height
    }

    private fun getMouseButton(button: Int) = when (button) {
        0    -> PointerButton.Primary
        1    -> PointerButton.Secondary
        2    -> PointerButton.Tertiary
        3    -> PointerButton.Back
        4    -> PointerButton.Forward
        else -> PointerButton.Primary
    }

    private fun getMousePosition() = Offset(
        minecraft.mouseHandler.xpos().toFloat(),
        minecraft.mouseHandler.ypos().toFloat()
    )

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        composeRender.post {
            it.sendPointerEvent(
                PointerEventType.Move,
                position = getMousePosition()
            )
        }
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        composeRender.post {
            it.sendPointerEvent(
                PointerEventType.Press,
                button = getMouseButton(event.button()),
                position = getMousePosition()
            )
        }
        return true
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        composeRender.post {
            it.sendPointerEvent(
                PointerEventType.Release,
                button = getMouseButton(event.button()),
                position = getMousePosition()
            )
        }
        return true
    }

    override fun mouseScrolled(
        mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double
    ): Boolean {
        composeRender.post {
            it.sendPointerEvent(
                PointerEventType.Scroll,
                button = PointerButton.Tertiary,
                position = getMousePosition(),
                scrollDelta = Offset(horizontalAmount.toFloat(), -verticalAmount.toFloat())
            )
        }
        return true
    }

    override fun onClose() {
        composeRender.destroy()
        mc.setScreen(null)
    }

    override fun isPauseScreen(): Boolean {
        return false
    }

}

fun Screen.open() {
    mc.setScreen(this)
}
