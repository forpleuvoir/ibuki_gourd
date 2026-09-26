@file:OptIn(InternalComposeUiApi::class)

package moe.forpleuvoir.ibukigourd.ui.overlay

import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import moe.forpleuvoir.compose_minecraft.platform.screen.MinecraftComposeScene
import moe.forpleuvoir.ibukigourd.ui.sokitsu.SokitsuScreenRoot
import moe.forpleuvoir.ibukigourd.util.mc
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds

/**
 * 全局覆盖层的**常驻宿主**：持有一个不隶属于任何屏幕的 Compose 场景，每帧驱动
 * [OverlayService] 注册的内容，让它们画在原版 GUI（HUD / 界面）与 Compose 屏幕之上。
 *
 * 与 [moe.forpleuvoir.compose_minecraft.platform.screen.ComposeScreen] 的关系：屏幕只是
 * [MinecraftComposeScene] 的驱动者之一（它每帧从 `extractRenderState` 调 `renderFrame()`）；
 * 本宿主在**自己的帧钩子**里驱动同款场景，因而覆盖层在原版界面、无界面、Compose 界面之上
 * 都照常显示。
 *
 * 渲染器不注册到 `ComposeGuiRenderer.active`：那个槽位是**单值**的（`register` 覆盖、
 * `unregister` 清空），屏幕场景占用它；本宿主直接持有 [MinecraftComposeScene.renderer]
 * 并自行提交，两者互不干扰。
 *
 * 场景**懒创建**（首次出现需要渲染的条目时），此后常驻复用 —— 窗口尺寸由 `renderFrame()`
 * 内部按当前窗口同步，无需外部通知。没有任何条目参与渲染的帧直接返回，不产生组合与绘制开销。
 *
 * 输入：只向场景转发鼠标**移动**（悬停高亮一类只读反馈够用），不转发点击 / 滚轮 ——
 * 需要交互的内容应做成屏幕或弹层。
 *
 * 线程约束：仅允许在渲染线程（原版 GUI 渲染阶段）调用 [onFrame]。
 */
object OverlayHost {

    private var scene: MinecraftComposeScene? = null

    private var lastFrameNanos = 0L

    /**
     * 单帧时间步上限：渲染停顿（加载界面、窗口最小化、调试器断点）后恢复时，
     * 直接用真实间隔会让按帧推进的计时（提示倒计时一类）瞬间走完，故按上限截断。
     */
    private val MAX_FRAME_STEP = 100.milliseconds

    /**
     * 帧钩子入口（每帧原版 GUI 渲染阶段调用一次）：
     * 推进按帧计时的注册者 → 有内容就重组 / 布局 / 绘制 → 提交。
     */
    fun onFrame() {
        val now = System.nanoTime()
        val elapsed = if (lastFrameNanos == 0L) Duration.ZERO else (now - lastFrameNanos).nanoseconds
        lastFrameNanos = now

        OverlayService.runFrameListeners(elapsed.coerceAtMost(MAX_FRAME_STEP))

        if (!OverlayService.hasPresentEntry) return

        val current = ensureScene()
        current.sendPointerEvent(
            eventType = PointerEventType.Move,
            position = Offset(mc.mouseHandler.xpos().toFloat(), mc.mouseHandler.ypos().toFloat()),
        )
        current.renderFrame()
        current.renderer.render()
    }

    /**
     * 建立常驻场景（幂等）。
     *
     * 场景根沿用屏幕那套内容根 [SokitsuScreenRoot]：按**窗口像素尺寸**解析缩放档
     * （[moe.forpleuvoir.ibukigourd.ui.sokitsu.SokitsuScreenDefaults.resolver]），
     * 下发 `LocalDensity` 与主题 `pixelScale` —— 覆盖层的内容与精灵因此和屏幕同档缩放，
     * 窗口变小一起收缩。场景自身 `density` 仍固定 `1f`（1dp == 1 像素，
     * 与 [MinecraftComposeScene] 的默认语义一致），档位密度由内容根覆盖，
     * 与 [moe.forpleuvoir.ibukigourd.ui.sokitsu.SokitsuScreen] 的做法相同。
     *
     * 需要自己那份配色的条目（如提示）在条目内部再套一层主题，
     * 并记得把 `pixelScale` 传成 `LocalSokitsuPixelScale.current` 以免把档位覆盖回主题缺省值。
     */
    private fun ensureScene(): MinecraftComposeScene = scene ?: MinecraftComposeScene(
        width = 1,
        height = 1,
        density = 1f,
    ).apply {
        setContent {
            SokitsuScreenRoot {
                OverlayContainer()
            }
        }
        scene = this
    }
}
