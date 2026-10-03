package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import moe.forpleuvoir.compose_minecraft.platform.screen.ComposeScreen
import moe.forpleuvoir.compose_minecraft.platform.screen.ComposeScreenDefaults
import moe.forpleuvoir.compose_minecraft.platform.screen.ScreenAnimation
import moe.forpleuvoir.ibukigourd.mod.config.IGConfig
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
        pauseGame: Boolean = IGConfig.Gui.Screen.pauseGame,
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
        pauseGame: Boolean = IGConfig.Gui.Screen.pauseGame,
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
 * Sokitsu 屏幕内容根：读取**窗口像素**尺寸 → 解析缩放档 → 下发密度与主题。
 *
 * 尺寸取自 [LocalWindowInfo] 的 `containerSize`（平台把它作为快照状态写入，组合期读取
 * 即建立依赖）：窗口尺寸变化在**同一帧的组合阶段**就失效重算，密度与尺寸同帧生效。
 *
 * 层级：`Box` → [CompositionLocalProvider] 覆盖 `LocalDensity` → [SokitsuTheme] 套主题。
 *
 * **在内容根覆盖 `LocalDensity`**（平台另有运行期可写的 `ComposeScreen.density`）：`LayoutNode`
 * 的密度取自组合里的 `LocalDensity`，在内容根覆盖既让布局 / 字号生效，又不需要写状态、
 * 不额外多一次组合。`Popup` / `Dialog` 图层会继承注册处的组合环境
 * （`PopupHostOverlay` 渲染时重新注入调用处的 `CompositionLocalContext`），弹层因此同样
 * 吃到这里的密度。
 *
 * 不向场景传 `density` 意味着场景自身密度恒为 1f，而内容为档位密度；两者只影响
 * `Owner.density` 这类场景级读取（布局 / 字号 / 输入坐标都走内容密度或像素，不受影响）。
 *
 * **不适用于 `Dialog` / `Popup` 图层内容**：需要同款缩放的弹层应从所在屏幕读
 * `LocalSokitsuPixelScale`，而不是自己再包一层。
 *
 * @param modifier 默认 [fillMaxSize]；容器尺寸不参与分档（窗口像素取自场景快照）
 */
@Composable
fun SokitsuScreenRoot(
    modifier: Modifier = Modifier.fillMaxSize(),
    content: @Composable () -> Unit,
) {
    val windowPx = LocalWindowInfo.current.containerSize
    // 首帧组合发生在平台写入窗口尺寸之前（containerSize 仍为 0），按基准档起手，
    // 同帧尺寸落地后重算，不会画出错误档位
    val scale = if (windowPx.width > 0 && windowPx.height > 0) {
        SokitsuScreenDefaults.resolver.resolve(
            windowPx = windowPx,
            basePixelScale = SokitsuThemeMeta.pixelScale,
        )
    } else {
        SokitsuScreenScale(1f, SokitsuThemeMeta.pixelScale)
    }
    Box(modifier) {
        CompositionLocalProvider(LocalDensity provides Density(scale.density)) {
            SokitsuTheme(pixelScale = scale.pixelScale, content = content)
        }
    }
}
