@file:OptIn(InternalComposeUiApi::class, ExperimentalComposeUiApi::class)

package moe.forpleuvoir.ibukigourd.ui.scene

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.scene.CanvasLayersComposeScene
import androidx.compose.ui.util.fastRoundToInt
import moe.forpleuvoir.ibukigourd.mod.config.IGConfig
import moe.forpleuvoir.ibukigourd.ui.platformcontext.IGCompositionLocalProvider
import moe.forpleuvoir.ibukigourd.ui.platformcontext.MinecraftPlatformContext
import moe.forpleuvoir.ibukigourd.ui.scene.internal.SceneContext
import moe.forpleuvoir.ibukigourd.ui.scene.internal.SceneInputBridge
import moe.forpleuvoir.ibukigourd.ui.scene.internal.SceneLifecycle
import moe.forpleuvoir.ibukigourd.ui.scene.internal.SceneRenderer
import moe.forpleuvoir.ibukigourd.ui.skia.LocalSkiaSurface
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent

/**
 * [ComposeSceneHost] 的默认实现。
 *
 * 内部按职责委托给三个独立组件：
 * - [SceneLifecycle] — 生命周期（init / onClose）
 * - [SceneRenderer]      — 每帧渲染（extractRenderState）
 * - [SceneInputBridge]  — 输入事件路由（mouse / key）
 *
 * 共享状态通过 [SceneContext] 统一管理，组件之间不直接耦合。
 */
@OptIn(ExperimentalFoundationApi::class)
open class DefaultComposeSceneHost(
    private val content: @Composable () -> Unit,
) : ComposeSceneHost {

    /** 平台服务绑定 */
    private val binding = MinecraftPlatformContext()

    /** 内部共享上下文 */
    private val ctx = SceneContext(
        scene = CanvasLayersComposeScene(platformContext = binding),
        platformContext = binding,
    )

    /** 生命周期控制器 */
    private val bootstrapper = SceneLifecycle(ctx)

    /** 渲染器 */
    private val renderer = SceneRenderer(ctx)

    /** 输入桥接器 */
    private val inputBridge = SceneInputBridge(ctx)

    init {
        ctx.scene.setContent {
            IGCompositionLocalProvider(
                LocalSkiaSurface provides ctx.surface,
            ) {
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { visible = true }
                val enterEasing = CubicBezierEasing(0f, 0f, 0.2f, 1f)
                val duration = IGConfig.Gui.Screen.fadeInDuration.inWholeMilliseconds.toInt()
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(
                        initialOffsetY = { fullHeight -> (fullHeight * IGConfig.Gui.Screen.fadeInOffset).fastRoundToInt() },
                        animationSpec = tween(duration, easing = enterEasing)
                    ) + fadeIn(animationSpec = tween(duration, easing = enterEasing)),
                ) {
                    content()
                }
            }
        }
    }

    override fun init() = bootstrapper.init()

    override fun onClose() = bootstrapper.onClose()

    override fun extractRenderState(
        guiGraphics: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float,
    ) = renderer.render(guiGraphics, mouseX, mouseY, partialTick)

    override fun mouseClicked(event: MouseButtonEvent) =
        inputBridge.mouseClicked(event)

    override fun mouseReleased(event: MouseButtonEvent) =
        inputBridge.mouseReleased(event)

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double) =
        inputBridge.mouseScrolled(mouseX, mouseY, scrollX, scrollY)

    override fun keyPressed(event: KeyEvent) =
        inputBridge.keyPressed(event)

    override fun keyReleased(event: KeyEvent) =
        inputBridge.keyReleased(event)
}

