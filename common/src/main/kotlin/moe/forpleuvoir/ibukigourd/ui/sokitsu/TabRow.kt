package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalColorScheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalContentColor
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.fromToken
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolve

/**
 * 页签行：固定等宽标签 + 选中指示器。
 *
 * 标签必须经由 [Tab] 放入 [tabs] 槽。每一格的最终宽度由页签行统一测量，标签的内容由 [Tab] 承担。
 *
 * @param selectedTabIndex 当前选中的标签下标
 * @param containerColor 页签行背景色；未指定时按 [TabRowTokens.Container] 解析
 * @param contentColor 选中标签的内容色；未指定时按 [TabRowTokens.ContentSelected] 解析
 * @param indicator 指示器内容，运行在 [TabIndicatorScope] 中；默认使用整格宽度的 Primary 指示器
 * @param divider 页签行底部分割线；传空内容可关闭
 * @param tabs 标签槽；按顺序写若干 [Tab]
 */
@Composable
fun TabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    indicator: @Composable TabIndicatorScope.() -> Unit = {
        TabRowDefaults.PrimaryIndicator(
            modifier = Modifier.tabIndicatorOffset().width(tabWidth),
        )
    },
    divider: @Composable () -> Unit = { HorizontalDivider() },
    tabs: @Composable () -> Unit,
) {
    PrimaryTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        indicator = indicator,
        divider = divider,
        tabs = tabs,
    )
}

/**
 * Primary 页签行：使用整格宽度的底部指示器。
 */
@Composable
fun PrimaryTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    indicator: @Composable TabIndicatorScope.() -> Unit = {
        TabRowDefaults.PrimaryIndicator(
            modifier = Modifier.tabIndicatorOffset().width(tabWidth),
        )
    },
    divider: @Composable () -> Unit = { HorizontalDivider() },
    tabs: @Composable () -> Unit,
) {
    TabRowLayout(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        indicator = indicator,
        divider = divider,
        tabs = tabs,
    )
}

/**
 * Secondary 页签行：使用居中于标签的短指示器。
 */
@Composable
fun SecondaryTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    indicator: @Composable TabIndicatorScope.() -> Unit = {
        TabRowDefaults.SecondaryIndicator(
            modifier = Modifier.tabIndicatorOffset(Alignment.CenterHorizontally),
        )
    },
    divider: @Composable () -> Unit = { HorizontalDivider() },
    tabs: @Composable () -> Unit,
) {
    TabRowLayout(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        indicator = indicator,
        divider = divider,
        tabs = tabs,
    )
}

private val LocalTabRowUnselectedContentColor = compositionLocalOf { Color.Unspecified }

private enum class TabRowSlot {
    Tabs,
    Indicator,
    Divider,
}

@Stable
private class TabIndicatorScopeImpl(
    override val selectedTabIndex: Int,
    override val tabCount: Int,
    override val tabWidth: Dp,
    override val tabGap: Dp,
    private val animatedTabIndexState: State<Float>,
) : TabIndicatorScope {

    override val animatedTabIndex: Float get() = animatedTabIndexState.value
}

@Composable
private fun TabRowLayout(
    selectedTabIndex: Int,
    modifier: Modifier,
    containerColor: Color,
    contentColor: Color,
    indicator: @Composable TabIndicatorScope.() -> Unit,
    divider: @Composable () -> Unit,
    tabs: @Composable () -> Unit,
) {
    val resolvedContainer = if (containerColor.isSpecified) containerColor else TabRowDefaults.containerColor()
    val resolvedContent = if (contentColor.isSpecified) contentColor else TabRowDefaults.contentColor()
    val resolvedUnselectedContent =
        if (contentColor.isSpecified) contentColor else TabRowDefaults.unselectedContentColor()
    val density = LocalDensity.current
    val animatedTabIndex = animateFloatAsState(
        targetValue = selectedTabIndex.coerceAtLeast(0).toFloat(),
        animationSpec = tween(TabRowDefaults.IndicatorAnimationDurationMillis),
    )

    CompositionLocalProvider(
        LocalContentColor provides resolvedContent,
        LocalTabRowUnselectedContentColor provides resolvedUnselectedContent,
    ) {
        SubcomposeLayout(
            modifier = modifier
                .fillMaxWidth()
                .background(resolvedContainer)
                .selectableGroup(),
        ) { constraints ->
            val minHeightPx = TabRowDefaults.minHeight.roundToPx()
            val minTabWidthPx = TabRowDefaults.minTabWidth.roundToPx()
            val maxTabWidthPx = TabRowDefaults.maxTabWidth.roundToPx()
                .coerceAtLeast(minTabWidthPx)
            val tabGapPx = TabRowDefaults.tabGap.roundToPx()
            val tabMeasurables = subcompose(TabRowSlot.Tabs) { tabs() }
            val tabCount = tabMeasurables.size
            val boundedWidth = constraints.hasBoundedWidth
            val totalTabGapPx = tabGapPx * (tabCount - 1).coerceAtLeast(0)

            val tabWidthPx: Int
            if (tabCount == 0) {
                tabWidthPx = 0
            } else if (boundedWidth) {
                val equalWidth = (constraints.maxWidth - totalTabGapPx).coerceAtLeast(0) / tabCount
                tabWidthPx = if (equalWidth >= minTabWidthPx) {
                    equalWidth.coerceAtMost(maxTabWidthPx)
                } else {
                    equalWidth
                }.coerceAtLeast(1)
            } else {
                tabWidthPx = (tabMeasurables.maxOfOrNull { it.maxIntrinsicWidth(Constraints.Infinity) } ?: 0)
                    .coerceIn(minTabWidthPx, maxTabWidthPx)
                    .coerceAtLeast(1)
            }

            val rowWidthPx = if (boundedWidth) {
                constraints.maxWidth
            } else {
                (tabWidthPx * tabCount + totalTabGapPx).coerceAtLeast(constraints.minWidth)
            }
            val selected = selectedTabIndex.coerceIn(0, (tabCount - 1).coerceAtLeast(0))
            val indicatorScope = TabIndicatorScopeImpl(
                selectedTabIndex = selected,
                tabCount = tabCount,
                tabWidth = with(density) { tabWidthPx.toDp() },
                tabGap = TabRowDefaults.tabGap,
                animatedTabIndexState = animatedTabIndex,
            )
            val indicatorMeasurable = subcompose(TabRowSlot.Indicator) {
                indicatorScope.indicator()
            }.firstOrNull()
            val dividerMeasurable = subcompose(TabRowSlot.Divider) {
                divider()
            }.firstOrNull()
            val indicatorPlaceable = indicatorMeasurable?.measure(
                Constraints(
                    minWidth = 0,
                    maxWidth = rowWidthPx,
                    minHeight = 0,
                    maxHeight = constraints.maxHeight,
                ),
            )
            val dividerPlaceable = dividerMeasurable?.measure(
                Constraints(
                    minWidth = 0,
                    maxWidth = rowWidthPx,
                    minHeight = 0,
                    maxHeight = constraints.maxHeight,
                ),
            )

            val bottomBandHeightPx = maxOf(
                indicatorPlaceable?.height ?: 0,
                dividerPlaceable?.height ?: 0,
            )
            val tabDividerGapPx = TabRowDefaults.tabDividerGap.roundToPx()
            // 先取 intrinsic 高度算标签内容高，再结合底部指示器 / 分割线区域算整行高度；
            // 每个 Measurable 只在最后做一次正式 measure。
            val contentHeightPx = if (tabCount == 0) 0 else {
                tabMeasurables.maxOfOrNull { it.maxIntrinsicHeight(tabWidthPx) } ?: 0
            }
            val rowHeightPx = maxOf(
                minHeightPx,
                contentHeightPx + bottomBandHeightPx + tabDividerGapPx,
            ).coerceIn(constraints.minHeight, constraints.maxHeight)
            val tabHeightPx = (rowHeightPx - bottomBandHeightPx - tabDividerGapPx).coerceAtLeast(0)
            val measuredTabs = if (tabCount == 0) {
                emptyList()
            } else {
                tabMeasurables.map { measurable ->
                    measurable.measure(
                        Constraints(
                            minWidth = tabWidthPx,
                            maxWidth = tabWidthPx,
                            minHeight = tabHeightPx,
                            maxHeight = tabHeightPx,
                        ),
                    )
                }
            }

            layout(rowWidthPx, rowHeightPx) {
                measuredTabs.forEachIndexed { index, placeable ->
                    placeable.placeRelative(index * (tabWidthPx + tabGapPx), 0)
                }
                dividerPlaceable?.placeRelative(0, rowHeightPx - dividerPlaceable.height)
                indicatorPlaceable?.placeRelative(0, rowHeightPx - indicatorPlaceable.height)
            }
        }
    }
}

/**
 * 页签行中的单个标签。
 *
 * 标签底座委托给 [FlatButton]，因此按下 / 聚焦纹理、点击音效、内容色状态修正与
 * [FlatButtonDefaults] 的尺寸规则保持一致。文字与图标都是可选内容槽。
 *
 * @param selected 是否选中
 * @param onClick 点击回调
 * @param enabled 是否可交互
 * @param text 文字内容槽
 * @param icon 图标内容槽
 * @param selectedContentColor 选中态内容色；未指定时取当前内容色
 * @param unselectedContentColor 未选中态内容色；未指定时取当前页签行下发的未选中内容色
 */
@Composable
fun Tab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    selectedContentColor: Color = Color.Unspecified,
    unselectedContentColor: Color = Color.Unspecified,
    interactionSource: MutableInteractionSource? = null,
) {
    val contentColor = if (selected) {
        selectedContentColor.takeOrElse { LocalContentColor.current }
    } else {
        unselectedContentColor.takeOrElse {
            LocalTabRowUnselectedContentColor.current
                .takeOrElse { LocalColorScheme.current.fromToken(TabRowTokens.Content) }
        }
    }
    // 底色不区分选中：选中由底部指示器与内容色表达
    val tone = TabRowTokens.TabContainer

    FlatButton(
        onClick = onClick,
        modifier = modifier.semantics { this.selected = selected },
        enabled = enabled,
        colors = FlatButtonColors(
            color = Color.Unspecified.resolve(tone),
            contentColor = contentColor,
        ),
        sprite = TabRowDefaults.tabSprite(),
        contentPadding = FlatButtonDefaults.contentPadding,
        minSize = TabRowDefaults.tabMinSize,
        role = Role.Tab,
        interactionSource = interactionSource,
    ) {
        TabContent(text = text, icon = icon)
    }
}

@Composable
private fun TabContent(
    text: @Composable (() -> Unit)?,
    icon: @Composable (() -> Unit)?,
) {
    if (text == null && icon == null) return
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        icon?.invoke()
        if (icon != null && text != null) {
            Spacer(Modifier.height(TabRowDefaults.iconSpacing))
        }
        text?.invoke()
    }
}
