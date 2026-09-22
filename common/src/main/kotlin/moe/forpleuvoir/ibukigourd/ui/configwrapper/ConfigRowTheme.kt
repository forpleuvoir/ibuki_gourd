package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuAtlasManager
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeToken
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuPixelScale
import moe.forpleuvoir.ibukigourd.util.identifier
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

    /**
     * 行悬停底：一笔圆角面板素材 `ui/surface/flat_1x_round`。
     *
     * 只做**透明度**动画（0 → 1），不做颜色渐变 —— 之前用 `animateColorAsState` 在
     * `Color.Transparent` 与带色值之间插值，会经过黑色，肉眼就是"闪一下"。
     */
    val HoverSprite: SokitsuSprite
        get() = SokitsuAtlasManager.sprite(
            SokitsuAtlasManager.UI_ATLAS_ID,
            identifier("ui/surface/flat_1x_round"),
        )

    /** 悬停底的染色：中性容器色。 */
    val Container = ColorSchemeToken.SurfaceVariant

    /** 配置名（标题）色。 */
    val Title = ColorSchemeToken.OnSurface

    /** 注释等次要信息色。 */
    val Comment = ColorSchemeToken.OnSurfaceVariant

    /** 重置按钮与分组展开箭头的图标色。 */
    val Icon = ColorSchemeToken.OnSurfaceVariant

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

    /**
     * 控件区宽度：**所有单控件行统一用这个宽度**。
     *
     * 行骨架的排布是「名称列（weight 1f）+ 控件 + 重置按钮」，控件区宽度一致时
     * 各行控件的左右边缘才会对齐；宽度参差会让整个页面看起来是斜的。
     */
    val ControlWidth: Dp = 320.dp

    /** 缓动曲线在行内的速览画布边长。 */
    val CurvePreviewSize: Dp = 96.dp

    /** 曲线编辑弹窗的内容内边距。 */
    val CurveDialogPadding: PaddingValues = PaddingValues(16.dp)

    /**
     * 配置 GUI 里图标按钮的内边距：调小让按钮贴合图标（按钮尺寸 = `max(minSize, 图标 + 2×padding)`，
     * 取对称值才能保持正方形）。
     */
    val IconButtonPadding: PaddingValues = PaddingValues(4.dp)

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

    fun toPadding() = PaddingValues(start = start, top = top, end = end, bottom = bottom)
}

/**
 * 列表 / 映射编辑浮层的内容尺寸。
 *
 * 浮层宽度由内容撑开（`usePlatformDefaultWidth = false`），所以这里给内容一个确定宽度，
 * 避免行内控件把浮层挤成一条。
 */
object ConfigDialogDefaults {

    /** 内容区宽度。 */
    val ContentWidth: Dp = 960.dp

    /** 浮层最小宽度。 */
    val MinWidth: Dp = 720.dp

    /** 内容区最小高度：条目很少时浮层不缩成一条。 */
    val MinHeight: Dp = 560.dp
}

/**
 * 配置 GUI 的图标倍率：**跟随 sokitsu 像素缩放**（缺省 3，即 16×16 素材 → 48dp）。
 *
 * 行内图标原先按 [moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon] 的缺省倍率 2 画（32dp），
 * 在配置页里显得偏小；配置页信息密度低、以辨识为主，因此这里改用像素缩放倍率。
 */
@Composable
fun configIconScale(): Int = LocalSokitsuPixelScale.current
