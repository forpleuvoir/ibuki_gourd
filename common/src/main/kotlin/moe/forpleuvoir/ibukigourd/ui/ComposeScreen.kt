package moe.forpleuvoir.ibukigourd.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.util.fastRoundToInt
import moe.forpleuvoir.ibukigourd.mixin.client.ScreenAccessor
import moe.forpleuvoir.ibukigourd.mod.config.IGConfig
import moe.forpleuvoir.ibukigourd.platform.isDevEnv
import moe.forpleuvoir.ibukigourd.ui.preset.LocalInheritedAlpha
import moe.forpleuvoir.ibukigourd.ui.scene.ComposeSceneFactory
import moe.forpleuvoir.ibukigourd.ui.scene.ComposeSceneHost
import moe.forpleuvoir.ibukigourd.ui.toast.ToastHandler
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import kotlin.time.TimeSource

/**
 * 将 JetBrains Compose UI 嵌入 Minecraft 屏幕的桥接类。
 *
 * 接收一个 [content] 可组合函数，通过 [ComposeSceneHost] 渲染 Compose 内容，并将鼠标/键盘输入事件
 * 委托至 Compose 场景。支持控制游戏暂停、父屏幕渲染以及世界层级背景渲染。
 *
 * [content] 直接作为 Compose 场景的根内容，不附加任何动画或装饰。
 * 如需要入场动画等效果，请在传递 [content] 前自行包裹，
 * 可使用 [DefaultAnimatedScreenEntry] 作为默认入场动画。
 *
 * @param pauseGame          是否暂停游戏（[isPauseScreen] 返回值）。
 * @param renderParent       是否在 extractRenderState 中渲染父屏幕。
 * @param parentScreen       关闭时回退到的父屏幕，默认 null。
 * @param shouldRenderLevel  (实验性功能,暂时不建议修改,可能会导致游戏卡死) 是否持续渲染世界层级背景。
 *                            若为 false，[renderingLevel] 初始为 true，经过 [IGConfig.Gui.Screen.fadeInDuration]
 *                            后会被置为 false（世界背景仅作为淡入效果，淡入完成后停止渲染）；若为 true 则始终渲染。
 * @param content            Compose 可组合内容。
 */
class ComposeScreen(
    val pauseGame: Boolean,
    val renderParent: Boolean,
    val parentScreen: Screen?,
    val shouldRenderLevel: (ComposeScreen) -> Boolean,
    content: @Composable () -> Unit,
) : Screen(Component.literal("Compose Screen")) {
    companion object {
        private val logger = logger()
    }

    constructor(
        pauseGame: Boolean = IGConfig.Gui.Screen.pauseGame,
        renderParent: Boolean = false,
        parentScreen: Screen? = mc.screen,
        shouldRenderLevel: (ComposeScreen) -> Boolean = { true },
        entryAnimation: Boolean = true,
        content: @Composable () -> Unit,
    ) : this(pauseGame, renderParent, parentScreen, shouldRenderLevel, {
        if (entryAnimation) DefaultAnimatedScreenEntry(content)
        else content()
    })

    private val mark by lazy { TimeSource.Monotonic.markNow() }
    private val host: ComposeSceneHost by lazy { ComposeSceneFactory.create(content) }

    var renderingLevel: Boolean = true
        private set

    private var onClose: (() -> Unit)? = null

    fun onClose(block: () -> Unit) {
        onClose = block
    }

    private var onInit: (() -> Unit)? = null

    fun onInit(block: () -> Unit) {
        onInit = block
    }

    private var onResize: ((width: Int, height: Int) -> Unit)? = null

    fun onResize(block: (width: Int, height: Int) -> Unit) {
        onResize = block
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        onResize?.invoke(width, height)
    }

    override fun init() {
        runCatching {
            mark
            host.init()
            onInit?.invoke()
        }.onFailure {
            logger.error(it)
            ToastHandler.showContent {
                Text("ComposeScreen Error: ${it.message}", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    override fun onClose() {
        this.minecraft.setScreen(parentScreen)
        onClose?.invoke()
        host.onClose()
    }

    var fadeInDuration = IGConfig.Gui.Screen.fadeInDuration

    private var init = false

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (renderParent) {
            parentScreen?.extractRenderState(graphics, -500, -500, partialTick)
        }
        if (!shouldRenderLevel(this) && renderingLevel && mark.elapsedNow() > fadeInDuration) {
            renderingLevel = false
        }
        host.extractRenderState(graphics, mouseX, mouseY, partialTick)
        if (isDevEnv && !init) {
            logger.devInfo("first frame time: ${mark.elapsedNow()}")
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

fun Screen.initScreen() {
    (this as ScreenAccessor).`ibukigourd$Init`()
}

fun Screen.rebuildWidgets() {
    (this as ScreenAccessor).`ibukigourd$rebuildWidgets`()
}

fun <S : Screen> S.open(): S {
    mc.execute { mc.setScreen(this) }
    return this
}

//TODO 关闭ComposeScreen时 需要播放动画, 实现思路 向屏幕发送关闭信号,接收到开始执行关闭流程
fun closeScreen() {
    mc.execute { mc.screen?.onClose() }
}

fun Screen?.isComposeScreen() =
    this is ComposeScreen

fun openComposeScreen(
    pauseGame: Boolean = IGConfig.Gui.Screen.pauseGame,
    renderParent: Boolean = false,
    parentScreen: Screen? = mc.screen,
    shouldRenderLevel: (ComposeScreen) -> Boolean = { true },
    entryAnimation: Boolean = true,
    content: @Composable () -> Unit
) = ComposeScreen(
    pauseGame,
    renderParent,
    parentScreen,
    shouldRenderLevel,
    entryAnimation,
    content
).open()

@Composable
fun DefaultAnimatedScreenEntry(content: @Composable () -> Unit) {
    val enterEasing = CubicBezierEasing(0f, 0f, 0.2f, 1f)
    val duration = IGConfig.Gui.Screen.fadeInDuration.inWholeMilliseconds.toInt()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> (fullHeight * IGConfig.Gui.Screen.fadeInOffset).fastRoundToInt() },
            animationSpec = tween(duration, easing = enterEasing)
        ) + fadeIn(animationSpec = tween(duration, easing = enterEasing)),
    ) {
        val animProgress by this.transition.animateFloat(label = "toastProgress") {
            when (it) {
                EnterExitState.PreEnter -> 0f
                EnterExitState.Visible  -> 1f
                EnterExitState.PostExit -> 0f
            }
        }
        CompositionLocalProvider(LocalInheritedAlpha provides animProgress) {
            content()
        }
    }
}
