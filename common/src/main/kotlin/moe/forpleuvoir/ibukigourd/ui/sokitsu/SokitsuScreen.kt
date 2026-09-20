package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import moe.forpleuvoir.compose_minecraft.platform.screen.ComposeScreen
import moe.forpleuvoir.compose_minecraft.platform.screen.ComposeScreenDefaults
import moe.forpleuvoir.compose_minecraft.platform.screen.ScreenAnimation
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuTheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import net.minecraft.client.gui.screens.Screen

/**
 * Sokitsu 屏幕：[ComposeScreen] 的薄包装，只补两件基础设施，界面内容仍由调用方给。
 *
 * 1. **主题**：内容外层套 [SokitsuTheme]（配色 / 字体 / 像素放大倍率 / 指示器 / 右键菜单）；
 * 2. **缩放**：内容外层套 [SokitsuScreenRoot]，按窗口分辨率选一档 [SokitsuScreenScale]，
 *    下发 `LocalDensity` 与主题 `pixelScale` —— 窗口小于阈值时整体收缩，
 *    避免固定 dp 尺寸的界面在窄窗口里挤爆。
 *
 * 其余屏幕能力（父子屏交叉过渡、进出场动画、世界渲染开关、关闭流程）全部沿用
 * [ComposeScreen]，本对象只是把构造参数原样透传，不新增语义。
 *
 * 需要"先构造、由调用方自行 `setScreen`"的场合（如 ModMenu / NeoForge 模组列表的
 * 配置按钮工厂）用 [create]；直接打开用 [open]。
 */
object SokitsuScreen {

    /**
     * 构造（**不打开**）一个 Sokitsu 屏幕，交调用方自行 `setScreen`。
     *
     * 参数与 [ComposeScreen] 构造器一一对应，见 [open]。
     */
    fun create(
        parent: Screen? = null,
        renderParentScreen: Boolean = false,
        disableWorldRender: Boolean = ComposeScreenDefaults.disableWorldRenderByDefault,
        pauseGame: Boolean = true,
        closeOnEsc: Boolean = true,
        animation: ScreenAnimation = ComposeScreenDefaults.animation,
        exitParentOnOpen: Boolean = true,
        content: @Composable () -> Unit,
    ): ComposeScreen = ComposeScreen(
        parent = parent,
        renderParentScreen = renderParentScreen,
        disableWorldRender = disableWorldRender,
        pauseGame = pauseGame,
        closeOnEsc = closeOnEsc,
        animation = animation,
        exitParentOnOpen = exitParentOnOpen,
    ) {
        SokitsuScreenRoot(content = content)
    }

    /**
     * 构造并打开一个 Sokitsu 屏幕（等价于原版 `minecraft.gui.setScreen`）。
     *
     * [parent] 缺省 = 打开前的当前屏幕，关闭流程走完后自动返回它；父屏为 [ComposeScreen] 时
     * 由平台自动标记可复活。其余参数语义同 [ComposeScreen.open]。
     *
     * 注意不向 [ComposeScreen] 传 `density`：场景密度没有运行期 setter，固定死在开屏时刻会
     * 让窗口缩放后不跟随；这里改由 [SokitsuScreenRoot] 在内容根覆盖 `LocalDensity`（见其 KDoc）。
     *
     * @return 创建的 [ComposeScreen] 实例（可运行时切换 `renderParentScreen` / `disableWorldRender`）
     */
    fun open(
        parent: Screen? = null,
        renderParentScreen: Boolean = false,
        disableWorldRender: Boolean = ComposeScreenDefaults.disableWorldRenderByDefault,
        pauseGame: Boolean = true,
        closeOnEsc: Boolean = true,
        animation: ScreenAnimation = ComposeScreenDefaults.animation,
        exitParentOnOpen: Boolean = true,
        content: @Composable () -> Unit,
    ): ComposeScreen = ComposeScreen.open(
        parent = parent,
        renderParentScreen = renderParentScreen,
        disableWorldRender = disableWorldRender,
        pauseGame = pauseGame,
        closeOnEsc = closeOnEsc,
        animation = animation,
        exitParentOnOpen = exitParentOnOpen,
    ) {
        SokitsuScreenRoot(content = content)
    }
}

/**
 * Sokitsu 屏幕内容根（测量包裹）：测量可用**像素**尺寸 → 解析缩放档 → 下发密度与主题。
 *
 * 层级为 `BoxWithConstraints`（在密度覆盖**之外**，所以测到的是真实屏幕像素）→
 * [CompositionLocalProvider] 覆盖 `LocalDensity` → [SokitsuTheme] 套主题。
 *
 * **用 `LocalDensity` 而不是 `ComposeScreen(density = ...)`**：场景密度在构造时固定、
 * 无运行期 setter；而 `LayoutNode` 的密度取自组合里的 `LocalDensity`（见其
 * `compositionLocalMap` 赋值），在内容根覆盖既能让布局 / 字号生效，又能随窗口尺寸变化
 * 重新解析档位。`Popup` / `Dialog` 图层会继承注册处的组合环境
 * （`PopupHostOverlay` 渲染时重新注入调用处的 `CompositionLocalContext`），弹层因此同样
 * 吃到这里的密度。
 *
 * 不向场景传 `density` 意味着场景自身密度恒为 1f，而内容为档位密度；两者只影响
 * `Owner.density` 这类场景级读取（布局 / 字号 / 输入坐标都走内容密度或像素，不受影响）。
 *
 * **不适用于 `Dialog` / `Popup` 图层内容**：那些图层拿到的是图层约束（对话框还会被
 * `usePlatformDefaultWidth` 收窄），测出来不是窗口分辨率，分档会偏小。需要同款缩放的弹层
 * 应从所在屏幕读 `LocalSokitsuPixelScale`，而不是自己再包一层。
 *
 * @param modifier 必须让本包裹覆盖**整个窗口**才测得到真实分辨率（默认 [fillMaxSize]）；
 *   包在更小的容器里会按容器尺寸分档
 */
@Composable
fun SokitsuScreenRoot(
    modifier: Modifier = Modifier.fillMaxSize(),
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier = modifier) {
        val scale = SokitsuScreenDefaults.resolver.resolve(
            windowPx = IntSize(constraints.maxWidth, constraints.maxHeight),
            basePixelScale = SokitsuThemeMeta.pixelScale,
        )
        CompositionLocalProvider(LocalDensity provides Density(scale.density)) {
            SokitsuTheme(pixelScale = scale.pixelScale, content = content)
        }
    }
}
