package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.IntSize
import com.mojang.blaze3d.platform.Window
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuPixelScale
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import kotlin.math.roundToInt

/**
 * 一档屏幕缩放：Compose 密度 [density] + Sokitsu 像素放大倍率 [pixelScale]。
 *
 * 两者**同向变化**才能保持像素风的观感，因为它们分别决定两套尺寸：
 * - [density]：布局尺寸（`dp` / `sp` → 屏幕像素）—— 组件 meta 里的宽高、内边距、字号都走它；
 * - [pixelScale]：精灵渲染倍率（1 素材像素 → N 屏幕像素，见 [LocalSokitsuPixelScale]）。
 *
 * 只改其一会让"组件外框"与"框内素材"对不上：密度减半而不动 [pixelScale] 时，
 * 九宫格边框（按 `pixelScale` 折算）相对缩了一半的外框会显得过粗。
 *
 * [density] 尽量取整数（非整数倍会让素材纹素落不到整数屏幕像素上）；倍率小于 1 的档
 * 无法在 Int 的 [pixelScale] 上保持比例，由 [SokitsuScreenDefaults.resolver] 四舍五入。
 */
@Immutable
data class SokitsuScreenScale(
    val density: Float,
    val pixelScale: Int,
)

/**
 * 屏幕缩放分档策略：按**窗口像素尺寸**选一档 [SokitsuScreenScale]。
 *
 * 窗口像素尺寸即 Compose 场景根约束（等于 MC 窗口分辨率，不经原版 `guiScale`），
 * 因为"UI 显得太小"的根因是物理像素密度，与原版 GUI 缩放无关。
 *
 * 不要求实现读 [SokitsuScreenDefaults]——它的默认实现读，替换实现则完全自决。
 */
fun interface SokitsuScreenScaleResolver {

    /**
     * @param windowPx 窗口像素尺寸
     * @param basePixelScale 当前主题的 [SokitsuThemeMeta.pixelScale]（资源包可覆盖）——
     *   应据此换算而非写死倍率，这样资源包改动素材密度时各档一起跟随
     */
    fun resolve(windowPx: IntSize, basePixelScale: Int): SokitsuScreenScale
}

/**
 * 屏幕缩放的全局默认值，与平台的 `ComposeScreenDefaults` 同模式：
 * 字段为 `var`，改后对**之后进入组合**的屏幕生效。
 *
 * 基准档（[baseFactor]）标定在**全屏分辨率**上 —— `density = 1f`、`pixelScale` 取主题值，
 * 即 `SokitsuThemeMeta.pixelScale` 的内置默认；[threshold] 只作"窗口是否已小到需要收缩"
 * 的分界，取自原版 GUI 缩放的分档边界（见该字段）。
 *
 * [resolver] 可整体替换，用于非整数密度、按显示器 PPI 或按原版 `guiScale` 等自定义规则。
 */
object SokitsuScreenDefaults {

    /**
     * 分档阈值（窗口像素）：宽、高**同时**达到则用基准档，否则用紧凑档。
     *
     * 取值照搬原版自动 GUI 缩放的分档边界 ——
     * [com.mojang.blaze3d.platform.Window.calculateScale] 从 1 起逐级自增，条件是
     * ```
     * framebufferWidth / (scale + 1) >= BASE_WIDTH && framebufferHeight / (scale + 1) >= BASE_HEIGHT
     * ```
     * 即"逻辑尺寸不得低于 [Window.BASE_WIDTH]×[Window.BASE_HEIGHT]（320×240）"；
     * 因此 `scale = N` 的窗口下限恰为 `(320N, 240N)`：
     *
     * | N | 窗口下限 | 该档覆盖的典型分辨率 |
     * |---|---|---|
     * | 2 | 640×480 | 854×480 |
     * | 3 | 960×720 | 1280×720、1600×900 |
     * | 4 | 1280×960 | 1920×1080 |
     * | 5 | 1600×1200 | 1920×1200、2560×1400 |
     * | 6 | 1920×1440 | 2560×1440 |
     *
     * 缺省取 `N = 3`（960×720）：2560×1440 全屏落在 `N = 6`，这里取它对折的档位边界，
     * 与"基准档标定在全屏、窗口再小才收缩"的意图一致。
     * 只改这里的 `N` 即可换边界 —— 例如 `N = 4` 会让 1920×1080 及以下都走紧凑档。
     */
    var threshold: IntSize = IntSize(Window.BASE_WIDTH * 3, Window.BASE_HEIGHT * 3)

    /**
     * 基准档（窗口达到 [threshold]）相对主题的倍率：`density = 倍率`、
     * `pixelScale = round(主题 pixelScale × 倍率)`。
     *
     * 缺省 1f → 与主题默认完全一致。
     */
    var baseFactor: Float = 1f

    /**
     * 紧凑档（窗口小于 [threshold]）相对主题的倍率，缺省 0.5f（基准档的一半）。
     *
     * 倍率小于 1 时 `pixelScale` 无法整除（3 × 0.5 = 1.5），按四舍五入取整并下限 1 ——
     * 取值本身可调，需要精确控制请替换 [resolver]。
     */
    var compactFactor: Float = 0.5f

    /**
     * 当前生效的分档策略：默认「窗口宽高同时达到 [threshold] 用 [baseFactor]，
     * 否则用 [compactFactor]」。
     */
    var resolver: SokitsuScreenScaleResolver = SokitsuScreenScaleResolver { windowPx, basePixelScale ->
        val factor = if (windowPx.width >= threshold.width && windowPx.height >= threshold.height)
            baseFactor
        else compactFactor
        SokitsuScreenScale(factor, (basePixelScale * factor).roundToInt().coerceAtLeast(1))
    }
}
