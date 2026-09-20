package moe.forpleuvoir.ibukigourd.ui.curve

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeToken

/**
 * 曲线编辑画布的主题接入声明：只声明该组件映射到主题的哪些语义槽位。
 *
 * 与 sokitsu 组件（[moe.forpleuvoir.ibukigourd.ui.sokitsu.SliderTokens] 等）同构；
 * 颜色走"调用点传参 > 作用域色 > 组件 token > 主题槽位"回退链
 * （见 [moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolve]）。
 */
object BezierCurvePlotTokens {

    /** 画布底（含 y 轴上越界的那两条带）：弱化的容器色。 */
    val Background = ColorSchemeToken.SurfaceVariant

    /** 单位正方形（x / y 均 `0f..1f`）区域：与画布底拉开一档，一眼分出"合法区间"。 */
    val UnitArea = ColorSchemeToken.Surface

    /** 网格线、单位正方形边框、控制点引导线：全局描边色。 */
    val Grid = ColorSchemeToken.Outline

    /** 曲线本体：主色。 */
    val Curve = ColorSchemeToken.Primary

    /** 控制点方块：主色；悬停 / 拖拽时改用 [HandleActive]。 */
    val Handle = ColorSchemeToken.Primary

    /** 控制点方块描边：全局描边色，保证控制点压在曲线上时仍有边界。 */
    val HandleOutline = ColorSchemeToken.Outline

    /** 悬停 / 拖拽中的控制点：次色，与静止态区分。 */
    val HandleActive = ColorSchemeToken.Secondary

    /** 禁用态整体不透明度（绘制端统一压在所有颜色上）。 */
    const val DisabledOpacity: Float = 0.4f
}

/**
 * 曲线编辑画布的尺寸与几何缺省值。
 *
 * **不设 meta 段**：画布尺寸由调用方的 `modifier` 决定（不是主题量），
 * 其余各项都是编辑器的几何常量，资源包覆盖它们没有意义。
 */
object BezierCurvePlotDefaults {

    /** y 轴显示区间：上下各留 0.2 的越界带，回弹 / 过冲曲线也能画全。 */
    val YRange: ClosedFloatingPointRange<Float> = -0.2f..1.2f

    /** 单位正方形内网格的等分数（N×N）。 */
    const val GridDivisions: Int = 4

    /** 网格线在 token 色之上再压的不透明度，避免网格抢过曲线。 */
    const val GridOpacity: Float = 0.5f

    /** 按住 Alt 时控制点的吸附步长（1/20）。 */
    const val SnapStep: Float = 0.05f

    /** 是否启用 Alt 吸附。 */
    const val SnapWithAlt: Boolean = true

    /** 是否启用 Shift 锁角（控制点只沿"自身锚点 → 当前位置"这条直线移动）。 */
    const val LockAngleWithShift: Boolean = true

    /** 是否启用 Ctrl 微调（按住 Ctrl 拖动时，位移按 [SlowMoveFactor] 缩放）。 */
    const val SlowMoveWithControl: Boolean = true

    /** 按住 Ctrl 时控制点位移相对指针位移的比例（1/10）。 */
    const val SlowMoveFactor: Float = 0.1f

    /** 画布四周留白：至少完整容下一个控制点方块（贴到 `x = 0` / `y = 0` 时不被裁掉一半）。 */
    val EdgePadding: Dp = 6.dp

    /** 控制点方块边长。 */
    val HandleSize: Dp = 8.dp

    /** 控制点命中判定在方块外额外扩出的量。 */
    val HandleHitPadding: Dp = 6.dp

    /** 曲线线宽。 */
    val CurveThickness: Dp = 2.dp

    /** 网格 / 边框 / 引导线线宽。 */
    val LineThickness: Dp = 1.dp

    /** 对角参考虚线实线段的长度。 */
    val DashLength: Dp = 4.dp

    /** 对角参考虚线间隔的长度。 */
    val DashGap: Dp = 4.dp
}
