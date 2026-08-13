package moe.forpleuvoir.ibukigourd.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.util.fastRoundToInt
import moe.forpleuvoir.ibukigourd.mixin.client.ScreenAccessor
import moe.forpleuvoir.ibukigourd.mod.config.IGConfig
import moe.forpleuvoir.ibukigourd.platform.isDevEnv
import moe.forpleuvoir.ibukigourd.task.scheduleStartTick
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
        parentScreen: Screen? = mc.gui.screen(),
        shouldRenderLevel: (ComposeScreen) -> Boolean = { true },
        entryAnimation: Boolean = true,
        content: @Composable () -> Unit,
    ) : this(pauseGame, renderParent, parentScreen, shouldRenderLevel, {
        if (entryAnimation) DefaultAnimatedScreenEntry(content)
        else content()
    })

    private val mark by lazy { TimeSource.Monotonic.markNow() }
    private val closeController = ComposeScreenCloseController()
    private val host: ComposeSceneHost by lazy {
        ComposeSceneFactory.create {
            CompositionLocalProvider(LocalComposeScreenCloseController provides closeController) {
                content()
            }
        }
    }

    var renderingLevel: Boolean = true
        private set

    private var closeCallback: (() -> Unit)? = null

    @Deprecated("Use onClosed instead", ReplaceWith("onClosed(block)"))
    fun onClose(block: () -> Unit) {
        closeCallback = block
    }

    fun onClosed(block: () -> Unit) {
        closeCallback = block
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
            // 屏幕被重新展示（如弹窗关闭后回到父屏幕）时重置关闭状态，使场景重建后恢复可用
            closeController.reset()
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
        renderedSinceClose = false
        requestClose()
        // 屏幕被替换后若无任何一帧渲染本屏（退出动画无法推进），
        // 在若干 tick 后强制完成关闭流程，避免 Compose 场景与 GPU 资源泄漏。
        mc.scheduleStartTick(3) { _, _ ->
            if (!renderedSinceClose && !closeController.completed && minecraft.gui.screen() !== this) {
                closeController.finishNow()
            }
        }
    }

    fun requestClose() {
        closeController.requestClose()
    }

    private var cleanedUp = false

    private fun cleanup() {
        if (cleanedUp) return
        cleanedUp = true
        // 先销毁 Compose 场景（组合与重组合器），避免其与新屏幕创建
        // （setScreen → init → setContent）在同一帧内重叠，触发 Compose 运行时无效化竞态；
        // GPU 表面资源由 SceneLifecycle 延迟到下一帧释放，防止同帧已入队的 blit 引用已回收纹理。
        try {
            host.onClose()
        } catch (e: Exception) {
            logger.error("Error closing Compose scene host", e)
        }
        try {
            if (minecraft.gui.screen() === this) minecraft.gui.setScreen(parentScreen)
        } catch (e: Exception) {
            logger.error("Error returning to parent screen", e)
        }
        try {
            closeCallback?.invoke()
        } catch (e: Exception) {
            logger.error("Error in close callback", e)
        }
    }

    var fadeInDuration = IGConfig.Gui.Screen.fadeInDuration

    private var init = false

    private var renderedSinceClose = false

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderedSinceClose = true
        if (closeController.completed) {
            cleanup()
            return
        }
        if (renderParent) {
            val closing = closeController.isClosing
            parentScreen?.extractRenderState(
                graphics,
                if (closing) mouseX else -500,
                if (closing) mouseY else -500,
                partialTick
            )
        }
        if (!shouldRenderLevel(this) && renderingLevel && mark.elapsedNow() > fadeInDuration) {
            renderingLevel = closeController.isClosing
        }
        host.extractRenderState(graphics, mouseX, mouseY, partialTick)
        if (closeController.completed) {
            cleanup()
        }
        if (isDevEnv && !init) {
            logger.devInfo("first time: ${mark.elapsedNow()}")
            init = true
        }
    }

    private val isOpen: Boolean get() = closeController.state == ComposeScreenCloseState.Open

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean =
        !isOpen || host.mouseClicked(event) || super.mouseClicked(event, doubleClick)


    override fun mouseReleased(event: MouseButtonEvent): Boolean =
        closeController.isClosed || host.mouseReleased(event) || (isOpen && super.mouseReleased(event))


    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean =
        !isOpen || host.mouseScrolled(mouseX, mouseY, scrollX, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)


    override fun keyPressed(event: KeyEvent): Boolean =
        !isOpen || host.keyPressed(event) || super.keyPressed(event)


    override fun keyReleased(event: KeyEvent): Boolean =
        closeController.isClosed || host.keyReleased(event) || (isOpen && super.keyReleased(event))


    override fun isPauseScreen(): Boolean = pauseGame
}

fun Screen.initScreen() {
    (this as ScreenAccessor).`ibukigourd$Init`()
}

fun Screen.rebuildWidgets() {
    (this as ScreenAccessor).`ibukigourd$rebuildWidgets`()
}

fun <S : Screen> S.open(): S {
    mc.execute { mc.gui.setScreen(this) }
    return this
}

fun closeScreen() {
    mc.execute {
        when (val screen = mc.gui.screen()) {
            is ComposeScreen -> screen.requestClose()
            else             -> screen?.onClose()
        }
    }
}

fun Screen?.isComposeScreen() =
    this is ComposeScreen

fun openComposeScreen(
    pauseGame: Boolean = IGConfig.Gui.Screen.pauseGame,
    renderParent: Boolean = false,
    parentScreen: Screen? = mc.gui.screen(),
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
        val animProgress by this.transition.animateFloat(label = "screenAnimProgress") {
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
