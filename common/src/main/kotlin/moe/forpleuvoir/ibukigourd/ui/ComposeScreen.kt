package moe.forpleuvoir.ibukigourd.ui

import androidx.compose.runtime.Composable
import moe.forpleuvoir.ibukigourd.platform.isDevEnv
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.ui.scene.ComposeSceneFactory
import moe.forpleuvoir.ibukigourd.ui.scene.ComposeSceneHost
import moe.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import kotlin.time.TimeSource

open class ComposeScreen(
    val pauseGame: Boolean = false,
    private val renderParent: Boolean = true,
    private val parentScreen: Screen? = null,
    content: @Composable () -> Unit,
) : Screen(Text.literal("Compose Screen")) {
    private val mark = TimeSource.Monotonic.markNow()
    private val host: ComposeSceneHost = ComposeSceneFactory.create(content)

    override fun init() {
        host.init()
    }

    override fun onClose() {
        this.minecraft.setScreen(parentScreen)
        host.onClose()
    }

    private var init = false

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (renderParent) {
            parentScreen?.extractRenderState(graphics, -500, -500, partialTick)
        }
        host.extractRenderState(graphics, mouseX, mouseY, partialTick)
        if (!init && isDevEnv) {
            println("第一帧耗时${mark.elapsedNow()}")
            init = true
        }
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean =
        host.mouseClicked(event) || super.mouseClicked(event, doubleClick)

    override fun mouseReleased(event: MouseButtonEvent): Boolean =
        host.mouseReleased(event) || super.mouseReleased(event)

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean =
        host.mouseScrolled(mouseX, mouseY, scrollX, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)

    override fun keyPressed(event: KeyEvent): Boolean =
        host.keyPressed(event) || super.keyPressed(event)

    override fun keyReleased(event: KeyEvent): Boolean =
        host.keyReleased(event) || super.keyReleased(event)

    override fun isPauseScreen(): Boolean = pauseGame
}

fun Screen.open() {
    mc.setScreen(this)
}

fun closeScreen() {
    mc.screen?.onClose()
}

fun openComposeScreen(
    pauseGame: Boolean = false,
    renderParent: Boolean = true,
    parentScreen: Screen? = mc.screen,
    content: @Composable () -> Unit
) {
    ComposeScreen(pauseGame, renderParent, parentScreen, content = content).open()
}

fun isComposeScreen() =
    mc.screen is ComposeScreen
