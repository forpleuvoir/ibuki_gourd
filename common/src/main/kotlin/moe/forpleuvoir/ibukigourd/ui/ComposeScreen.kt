package moe.forpleuvoir.ibukigourd.ui

import androidx.compose.runtime.Composable
import moe.forpleuvoir.ibukigourd.mod.config.IGConfig
import moe.forpleuvoir.ibukigourd.platform.isDevEnv
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.ui.scene.ComposeSceneFactory
import moe.forpleuvoir.ibukigourd.ui.scene.ComposeSceneHost
import moe.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/**
 * 将 JetBrains Compose UI 嵌入 Minecraft 屏幕的桥接类。
 *
 * 接收一个 [content] 可组合函数，通过 [ComposeSceneHost] 渲染 Compose 内容，并将鼠标/键盘输入事件
 * 委托至 Compose 场景。支持控制游戏暂停、父屏幕渲染以及世界层级背景渲染。
 *
 * @param pauseGame        是否暂停游戏（[isPauseScreen] 返回值）。
 * @param renderParent     是否在 extractRenderState 中渲染父屏幕。
 * @param parentScreen     关闭时回退到的父屏幕，默认 null。
 * @param _shouldRenderLevel (实验性功能,暂时不建议修改,可能会导致游戏卡死)初始时是否渲染世界层级背景；若为 true，经过 [IGConfig.Gui.Screen.fadeInDuration]
 *                           后 [shouldRenderLevel] 会被置为 false。
 * @param content          Compose 可组合内容。
 */
class ComposeScreen(
    val pauseGame: Boolean = false,
    private val renderParent: Boolean = false,
    private val parentScreen: Screen? = null,
    private val _shouldRenderLevel: Boolean = true,
    content: @Composable () -> Unit,
) : Screen(Text.literal("Compose Screen")) {
    private val mark by lazy { TimeSource.Monotonic.markNow() }
    private val host: ComposeSceneHost = ComposeSceneFactory.create(content)

    var shouldRenderLevel: Boolean = true
        private set

    override fun init() {
        mark
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
        if (!_shouldRenderLevel && shouldRenderLevel && mark.elapsedNow() > IGConfig.Gui.Screen.fadeInDuration) {
            shouldRenderLevel = false
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

/**
 * 快速创建并打开一个 [ComposeScreen]。
 *
 * 封装了 [ComposeScreen] 的构造与 [Screen.open] 调用，提供更便捷的入口。
 *
 * @param pauseGame        是否暂停游戏。
 * @param renderParent     是否在背景渲染父屏幕。
 * @param parentScreen     关闭时回退到的屏幕，默认为当前屏幕。
 * @param shouldRenderLevel (实验性功能,暂时不建议修改,可能会导致游戏卡死) 是否渲染世界层级背景。
 * @param content          Compose 可组合内容。
 */
fun openComposeScreen(
    pauseGame: Boolean = false,
    renderParent: Boolean = false,
    parentScreen: Screen? = mc.screen,
    shouldRenderLevel: Boolean = true,
    content: @Composable () -> Unit
) {
    ComposeScreen(pauseGame, renderParent, parentScreen, shouldRenderLevel, content = content).open()
}

fun isComposeScreen() =
    mc.screen is ComposeScreen
