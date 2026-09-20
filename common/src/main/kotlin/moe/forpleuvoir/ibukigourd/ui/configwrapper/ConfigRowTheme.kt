package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeToken
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * 配置行的主题接入声明：只声明该组件映射到主题的哪些语义槽位。
 *
 * 与 sokitsu 组件（[moe.forpleuvoir.ibukigourd.ui.sokitsu.SliderTokens] 等）同构，
 * 颜色走"调用点传参 > 作用域色 > 组件 token > 主题槽位"回退链。
 *
 * 像素风里没有圆角卡片：行本身**常态完全透明**，只把悬停底色与文字色映射到主题。
 */
object ConfigRowTokens {

    /** 悬停底色：中性容器色（按 [HoverAlpha] 压淡）。 */
    val Container = ColorSchemeToken.SurfaceVariant

    /** 配置名（标题）色。 */
    val Title = ColorSchemeToken.OnSurface

    /** 注释等次要信息色。 */
    val Comment = ColorSchemeToken.OnSurfaceVariant

    /** 重置按钮与分组展开箭头的图标色。 */
    val Icon = ColorSchemeToken.OnSurfaceVariant

    /** 悬停底色不透明度。 */
    const val HoverAlpha: Float = 0.35f

    /** 图标禁用态不透明度。 */
    const val DisabledAlpha: Float = 0.38f
}

/**
 * 配置行的尺寸与几何缺省值。
 *
 * **不设 meta 段**：config GUI 是应用级页面，行高 / 内边距 / 缩进属于页面排版，
 * 不是随主题或资源包变化的量；需要局部调整时用 [ConfigRowWrapper] 的 CompositionLocal 覆盖。
 */
object ConfigRowDefaults {

    /** 行内边距。 */
    val Padding: ConfigRowPadding = ConfigRowPadding()

    /** 名字列与右侧控件列之间的间距。 */
    val Spacing: Dp = 12.dp

    /** 每嵌套一层增加的左缩进。 */
    val Indent: Dp = 16.dp

    /** 注释最多显示行数（超出省略，悬停气泡展示全文）。 */
    const val CommentMaxLines: Int = 1

    /** 图标倍率：素材为 16×16，取 1 即 16dp（配置行是信息密集页面，不随 pixelScale 放大）。 */
    const val IconScale: Int = 1

    /** 重置按钮的点击区尺寸。 */
    val ResetButtonSize: Dp = 28.dp

    /** 重置图标的旋转动画时长。 */
    val ResetAnimation: Duration = 400.milliseconds

    /** 行悬停底色的过渡时长。 */
    val HoverAnimation: Duration = 120.milliseconds

    /** 注释被截断时的悬停气泡延迟。 */
    val TooltipDelay: Duration = 250.milliseconds

    /** 分组子项数少于该值时默认展开。 */
    const val AutoExpandLimit: Int = 10

    /** 重置态轮询间隔：仅用于不实现 `Observable` 的节点。 */
    val ValuePollInterval: Duration = 50.milliseconds
}

/**
 * 配置行右侧控件的常用尺寸。
 *
 * 与 [ConfigRowDefaults] 同样是页面排版量，不做 meta 段。
 */
object ConfigControlDefaults {

    /** 数值框宽度。 */
    val FieldWidth: Dp = 96.dp

    /** 需要更宽输入的框（字符串 / 时长）。 */
    val WideFieldWidth: Dp = 140.dp

    /** 滑条宽度。 */
    val SliderWidth: Dp = 180.dp

    /** 颜色预览按钮宽度。 */
    val ColorButtonWidth: Dp = 120.dp

    /** 枚举选择器宽度。 */
    val SelectorWidth: Dp = 160.dp

    /** 行内小图标按钮尺寸（重置、滑条 ⇄ 数值框切换）。 */
    val IconButtonSize: Dp = 28.dp

    /** 列表 / 映射的「条目数」按钮宽度。 */
    val ListButtonWidth: Dp = 160.dp

    /** 缓动曲线在行内的速览画布边长。 */
    val CurvePreviewSize: Dp = 56.dp

    /** 曲线编辑弹窗的内容内边距。 */
    val CurveDialogPadding: PaddingValues = PaddingValues(8.dp)

    /** 数值滑条可用的区间跨度上限：超过该跨度时只给数值框。 */
    const val SliderSpanLimit: Int = 1000
}

/**
 * 行内边距：显式拆开 start / end，便于嵌套层级在其上叠加缩进。
 *
 * （不直接用 `PaddingValues`：本平台的 `PaddingValues` 不提供读取 start / end 的公开途径，
 * 无法在其上做"加一段缩进"。）
 */
data class ConfigRowPadding(
    val start: Dp = 8.dp,
    val top: Dp = 6.dp,
    val end: Dp = 8.dp,
    val bottom: Dp = 6.dp,
) {

    /** 在左（start）侧再增加一段缩进，用于嵌套层级。 */
    fun indented(indent: Dp): ConfigRowPadding = copy(start = start + indent)
}

/**
 * 列表 / 映射编辑浮层的内容尺寸。
 *
 * 浮层宽度由内容撑开（`usePlatformDefaultWidth = false`），所以这里给内容一个确定宽度，
 * 避免行内控件把浮层挤成一条。
 */
object ConfigDialogDefaults {

    /** 内容区宽度。 */
    val ContentWidth: Dp = 480.dp

    /** 浮层最小宽度。 */
    val MinWidth: Dp = 360.dp
}
