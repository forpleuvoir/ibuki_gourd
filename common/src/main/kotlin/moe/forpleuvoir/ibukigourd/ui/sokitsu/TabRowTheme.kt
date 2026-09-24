package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeToken
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolve
import moe.forpleuvoir.ibukigourd.util.codec.dp
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.codec.Codec
import kotlin.math.roundToInt

/**
 * 页签行的颜色 token。
 */
object TabRowTokens {

    /** 页签行的背景色。 */
    val Container = ColorSchemeToken.Surface

    /** 未选中标签的内容色。 */
    val Content = ColorSchemeToken.OnSurfaceVariant

    /** 选中标签的内容色。 */
    val ContentSelected = ColorSchemeToken.OnSurface

    /** 选中指示器颜色。 */
    val Indicator = ColorSchemeToken.Primary

    /** 未选中标签按交互状态绘制底色时的染色基准。 */
    val TabContainer = ColorSchemeToken.OnSurfaceVariant

    /** 选中标签按交互状态绘制底色时的染色基准。 */
    val TabContainerSelected = ColorSchemeToken.Primary
}

/**
 * 页签行的尺寸 meta，单位均为 dp。
 *
 * ```jsonc
 * ui_meta: { tab_row: {
 *   min_height: 48,
 *   min_tab_width: 90,
 *   max_tab_width: 360,
 *   tab_gap: 6,
 *   primary_indicator_height: 6,
 *   secondary_indicator_height: 6,
 *   secondary_indicator_width: 24,
 *   tab_divider_gap: 3,
 *   icon_spacing: 4
 * } }
 * ```
 */
data class TabRowMeta(
    /** 页签行的最小高度。 */
    val minHeight: Dp,
    /** 单个标签的最小宽度。 */
    val minTabWidth: Dp,
    /** 单个标签的最大宽度。 */
    val maxTabWidth: Dp,
    /** 相邻标签之间的间距。 */
    val tabGap: Dp,
    /** Primary 指示器高度。 */
    val primaryIndicatorHeight: Dp,
    /** Secondary 指示器高度。 */
    val secondaryIndicatorHeight: Dp,
    /** Secondary 指示器宽度。 */
    val secondaryIndicatorWidth: Dp,
    /** 标签内容区与底部指示器 / 分割线区域之间的间距。 */
    val tabDividerGap: Dp,
    /** 图标与文字之间的垂直间距。 */
    val iconSpacing: Dp,
) {

    companion object : Codec<TabRowMeta> {

        val default = TabRowMeta(
            minHeight = 48.dp,
            minTabWidth = 90.dp,
            maxTabWidth = 360.dp,
            tabGap = 6.dp,
            primaryIndicatorHeight = 6.dp,
            secondaryIndicatorHeight = 6.dp,
            secondaryIndicatorWidth = 24.dp,
            tabDividerGap = 3.dp,
            iconSpacing = 4.dp,
        )

        private val codec = Codec.create<TabRowMeta>()
            .field(TabRowMeta::minHeight).default(default.minHeight).codec(Codec.dp(1.dp..512.dp))
            .field(TabRowMeta::minTabWidth).default(default.minTabWidth).codec(Codec.dp(1.dp..512.dp))
            .field(TabRowMeta::maxTabWidth).default(default.maxTabWidth).codec(Codec.dp(1.dp..1024.dp))
            .field(TabRowMeta::tabGap).default(default.tabGap).codec(Codec.dp(0.dp..64.dp))
            .field(TabRowMeta::primaryIndicatorHeight).default(default.primaryIndicatorHeight).codec(Codec.dp(1.dp..64.dp))
            .field(TabRowMeta::secondaryIndicatorHeight).default(default.secondaryIndicatorHeight).codec(Codec.dp(1.dp..64.dp))
            .field(TabRowMeta::secondaryIndicatorWidth).default(default.secondaryIndicatorWidth).codec(Codec.dp(1.dp..512.dp))
            .field(TabRowMeta::tabDividerGap).default(default.tabDividerGap).codec(Codec.dp(0.dp..64.dp))
            .field(TabRowMeta::iconSpacing).default(default.iconSpacing).codec(Codec.dp(0.dp..64.dp))
            .build(::TabRowMeta)

        override fun serialization(target: TabRowMeta): SerializeElement = codec.serialization(target)

        override fun deserialization(data: SerializeElement): Result<TabRowMeta> = codec.deserialization(data)
    }
}

/** 主题 meta 的 tab_row 段：缺失 / 解码失败回落 [TabRowMeta] 内置默认。 */
val SokitsuThemeMeta.tabRow: TabRowMeta
    get() = decodeComponent("tab_row", TabRowMeta, TabRowMeta.default)

/**
 * 指示器作用域：[TabRow] 在测量后提供单标签宽度，供自定义指示器计算位置与尺寸。
 *
 * [animatedTabIndex] 是选中下标在切换过程中的连续值；[tabIndicatorOffset] 用它计算水平位移。
 */
@Stable
interface TabIndicatorScope {

    /** 当前选中的标签下标。 */
    val selectedTabIndex: Int

    /** 当前页签数量。 */
    val tabCount: Int

    /** 单个标签的最终宽度。 */
    val tabWidth: Dp

    /** 相邻标签之间的间距。 */
    val tabGap: Dp

    /** 选中下标的动画值。 */
    val animatedTabIndex: Float

    /**
     * 将指示器对齐到当前动画位置的标签内部。
     *
     * @param alignment 指示器在标签宽度内的水平对齐；[Alignment.Start] 为整格起点，
     * [Alignment.CenterHorizontally] 为居中，[Alignment.End] 为整格终点
     */
    fun Modifier.tabIndicatorOffset(
        alignment: Alignment.Horizontal = Alignment.Start,
    ): Modifier = layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val tabWidthPx = tabWidth.roundToPx()
        val tabGapPx = tabGap.roundToPx()
        val animatedIndex = animatedTabIndex.coerceIn(0f, (tabCount - 1).coerceAtLeast(0).toFloat())
        val tabStart = (animatedIndex * (tabWidthPx + tabGapPx)).roundToInt()
        val alignmentOffset = when (alignment) {
            Alignment.CenterHorizontally -> (tabWidthPx - placeable.width) / 2
            Alignment.End                -> tabWidthPx - placeable.width
            else                         -> 0
        }
        layout(constraints.maxWidth, placeable.height) {
            placeable.placeRelative(tabStart + alignmentOffset, 0)
        }
    }
}

/**
 * 页签行的主题桥接与默认值。
 */
object TabRowDefaults {

    /** 当前主题的 tab_row meta。 */
    inline val meta get() = SokitsuThemeMeta.tabRow

    /** 页签行最小高度。 */
    inline val minHeight: Dp get() = meta.minHeight

    /** 单个标签的最小宽度。 */
    inline val minTabWidth: Dp get() = meta.minTabWidth

    /** 单个标签的最大宽度。 */
    inline val maxTabWidth: Dp get() = meta.maxTabWidth

    /** 相邻标签之间的间距。 */
    inline val tabGap: Dp get() = meta.tabGap

    /** Primary 指示器高度。 */
    inline val primaryIndicatorHeight: Dp get() = meta.primaryIndicatorHeight

    /** Secondary 指示器高度。 */
    inline val secondaryIndicatorHeight: Dp get() = meta.secondaryIndicatorHeight

    /** Secondary 指示器宽度。 */
    inline val secondaryIndicatorWidth: Dp get() = meta.secondaryIndicatorWidth

    /** 标签内容区与底部区域的间距。 */
    inline val tabDividerGap: Dp get() = meta.tabDividerGap

    /** 图标与文字的垂直间距。 */
    inline val iconSpacing: Dp get() = meta.iconSpacing

    /** 标签传给 [FlatButton] 的最小尺寸；宽度由页签行测量决定。 */
    val tabMinSize: DpSize get() = DpSize(0.dp, minHeight)

    /** Primary 指示器切换动画时长（毫秒）。 */
    const val IndicatorAnimationDurationMillis = 220

    /** 页签行默认背景色。 */
    @Composable
    fun containerColor(): Color = Color.Unspecified.resolve(TabRowTokens.Container)

    /** 选中标签的默认内容色。 */
    @Composable
    fun contentColor(): Color = Color.Unspecified.resolve(TabRowTokens.ContentSelected)

    /** 未选中标签的默认内容色。 */
    @Composable
    fun unselectedContentColor(): Color = Color.Unspecified.resolve(TabRowTokens.Content)

    /** 选中指示器的默认颜色。 */
    @Composable
    fun indicatorColor(): Color = Color.Unspecified.resolve(TabRowTokens.Indicator)

    /** Primary 指示器：铺满调用方给定的宽度，缺省铺满一行。 */
    @Composable
    fun PrimaryIndicator(
        modifier: Modifier = Modifier,
        height: Dp = primaryIndicatorHeight,
        color: Color = Color.Unspecified,
    ) {
        val resolvedColor = if (color.isSpecified) color else indicatorColor()
        Box(
            modifier
                .fillMaxWidth()
                .height(height)
                .background(resolvedColor),
        )
    }

    /** Secondary 指示器：固定宽度，通常与 [TabIndicatorScope.tabIndicatorOffset] 配合居中。 */
    @Composable
    fun SecondaryIndicator(
        modifier: Modifier = Modifier,
        width: Dp = secondaryIndicatorWidth,
        height: Dp = secondaryIndicatorHeight,
        color: Color = Color.Unspecified,
    ) {
        val resolvedColor = if (color.isSpecified) color else indicatorColor()
        Box(
            modifier
                .width(width)
                .height(height)
                .background(resolvedColor),
        )
    }
}
