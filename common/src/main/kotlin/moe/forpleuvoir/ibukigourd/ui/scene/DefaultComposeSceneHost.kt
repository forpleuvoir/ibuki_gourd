@file:OptIn(InternalComposeUiApi::class, ExperimentalComposeUiApi::class)

package moe.forpleuvoir.ibukigourd.ui.scene

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.scene.CanvasLayersComposeScene
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
 *
 * 场景上下文按需创建：首次 [init] 时构建，[onClose] 后置空。
 * 若屏幕被重新展示（如弹窗关闭后回到父屏幕）再次调用 [init]，
 * 会重建场景与组合，恢复可用状态。
 */
open class DefaultComposeSceneHost(
    private val content: @Composable () -> Unit,
) : ComposeSceneHost {

    /** 平台服务绑定 */
    private val binding = MinecraftPlatformContext()

    private var ctx: SceneContext? = null
    private var bootstrapper: SceneLifecycle? = null
    private var renderer: SceneRenderer? = null
    private var inputBridge: SceneInputBridge? = null

    /** 创建（或按需重建）内部场景上下文 */
    private fun ensureScene() {
        if (bootstrapper != null) return
        val sceneContext = SceneContext(
            scene = CanvasLayersComposeScene(platformContext = binding),
            platformContext = binding,
        )
        sceneContext.scene.setContent {
            val popupHostState = remember { PopupHostState() }
            IGCompositionLocalProvider(
                LocalSkiaSurface provides sceneContext.surface,
                LocalPopupHost provides popupHostState
            ) {
                content()
                PopupHostOverlay()
            }
        }
        ctx = sceneContext
        bootstrapper = SceneLifecycle(sceneContext)
        renderer = SceneRenderer(sceneContext)
        inputBridge = SceneInputBridge(sceneContext)
    }

    override fun init() {
        ensureScene()
        bootstrapper?.init()
    }

    override fun onClose() {
        bootstrapper?.onClose()
        ctx = null
        bootstrapper = null
        renderer = null
        inputBridge = null
    }

    override fun extractRenderState(
        guiGraphics: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float,
    ) {
        ensureScene()
        renderer?.render(guiGraphics, mouseX, mouseY, partialTick)
    }

    override fun mouseClicked(event: MouseButtonEvent): Boolean {
        ensureScene()
        return inputBridge?.mouseClicked(event) ?: false
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        ensureScene()
        return inputBridge?.mouseReleased(event) ?: false
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        ensureScene()
        return inputBridge?.mouseScrolled(mouseX, mouseY, scrollX, scrollY) ?: false
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        ensureScene()
        return inputBridge?.keyPressed(event) ?: false
    }

    override fun keyReleased(event: KeyEvent): Boolean {
        ensureScene()
        return inputBridge?.keyReleased(event) ?: false
    }
}