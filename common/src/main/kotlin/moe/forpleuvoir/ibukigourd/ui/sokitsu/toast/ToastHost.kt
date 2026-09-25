package moe.forpleuvoir.ibukigourd.ui.sokitsu.toast

import androidx.compose.ui.InternalComposeUiApi
import moe.forpleuvoir.compose_minecraft.platform.screen.MinecraftComposeScene
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuTheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.currentSokitsuColorScheme
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds

/**
 * 提示的**常驻宿主**：持有一个不隶属于任何屏幕的 Compose 场景，把 [ToastContainer]
 * 持续渲染到 GUI 之上。
 *
 * 与 [moe.forpleuvoir.compose_minecraft.platform.screen.ComposeScreen] 的关系：
 * 屏幕只是 [MinecraftComposeScene] 的驱动者之一（它每帧从 `extractRenderState` 调用
 * `renderFrame()`）；本宿主在**自己的帧钩子**里驱动同款场景，因而提示能在原版界面、
 * 无界面（HUD）、Compose 界面之上照常显示。
 *
 * 渲染器不注册到 `ComposeGuiRenderer.active`：那个槽位是**单值**的
 * （`register` 覆盖、`unregister` 清空），屏幕场景占用它；本宿主直接持有
 * [MinecraftComposeScene.renderer] 并自行提交，两者互不干扰。
 *
 * 生命周期：场景**懒创建**（首条提示出现时），此后常驻复用 —— 窗口尺寸由
 * `renderFrame()` 内部按当前窗口同步，无需外部通知。空闲帧（既无活动提示也无排队提示）
 * 直接返回，不产生重组与绘制开销。
 *
 * 线程约束：仅允许在渲染线程（原版 GUI 渲染阶段）调用 [onFrame]。
 */
object ToastHost {

    private var scene: MinecraftComposeScene? = null

    private var lastFrameNanos = 0L

    /**
     * 单帧时间步上限：渲染停顿（加载界面、窗口最小化、调试器断点）后恢复时，
     * 直接用真实间隔会让所有提示瞬间过期，故按上限截断。
     */
    private val MAX_FRAME_STEP = 100.milliseconds

    /**
     * 帧钩子入口（每帧原版 GUI 渲染阶段调用一次）：
     * 推进提示计时 → 重组/布局/绘制到收集器 → 提交。
     *
     * 计时先行：`tick` 修改的剩余时长在同一帧的重组中即被读到。
     */
    fun onFrame() {
        val now = System.nanoTime()
        val elapsed = if (lastFrameNanos == 0L) Duration.ZERO else (now - lastFrameNanos).nanoseconds
        lastFrameNanos = now

        if (ToastHandler.isIdle) return

        val delta = elapsed.coerceAtMost(MAX_FRAME_STEP)
        val current = ensureScene()
        ToastHandler.tick(delta)
        current.renderFrame()
        current.renderer.render()
    }

    /**
     * 建立常驻场景（幂等）。
     *
     * 场景根只提供主题：`MinecraftComposeScene.setContent` 已自动挂载弹层宿主
     * （`LocalPopupHost` + `PopupHostOverlay`），[SokitsuTheme] 自带像素放大倍率，
     * `density` 固定 `1f`（1dp == 1 像素，与 [MinecraftComposeScene] 的默认语义一致）。
     *
     * 配色取 [ToastHandler.activeScheme]，为 `null` 时跟随当前主题
     * （[currentSokitsuColorScheme]）——"提示是否跟界面走"由配置决定。
     */
    @OptIn(InternalComposeUiApi::class)
    private fun ensureScene(): MinecraftComposeScene = scene ?: MinecraftComposeScene(
        width = 1,
        height = 1,
        density = 1f,
    ).apply {
        setContent {
            // 配色：提示自己的那份（activeScheme）优先，否则跟随当前主题
            SokitsuTheme(colorScheme = ToastHandler.activeScheme ?: currentSokitsuColorScheme()) {
                ToastContainer()
            }
        }
        scene = this
    }
}
