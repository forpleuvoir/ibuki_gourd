package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Button
import moe.forpleuvoir.ibukigourd.ui.sokitsu.PrimaryTabRow
import moe.forpleuvoir.ibukigourd.ui.sokitsu.SecondaryTabRow
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Tab
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TabRowDefaults
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text

/** 超出屏幕场景下每个标签的固定宽度。 */
private const val OverflowTabWidth = 112

/** 超出屏幕场景的默认标签数量。 */
private const val DefaultOverflowTabCount = 40

/** 可滚动页签行首尾留白，与 M3 `ScrollableTabRowEdgeStartPadding` 一致。 */
private const val OverflowEdgePadding = 52

/**
 * M3 形态页签行测试屏。
 *
 * 覆盖 Primary / Secondary、文字 / 图标、禁用态、长标签、窄容器，
 * 以及标签总宽度超过屏幕后的水平滚动与选中项回滚。
 */
fun TabRowTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text("PrimaryTabRow")
            var primarySelected by remember { mutableIntStateOf(0) }
            PrimaryTabRow(
                selectedTabIndex = primarySelected,
                modifier = Modifier.fillMaxWidth(),
            ) {
                listOf("概览", "设置", "性能", "外观", "快捷键", "一个非常长的页签").forEachIndexed { index, label ->
                    Tab(
                        selected = primarySelected == index,
                        onClick = { primarySelected = index },
                        text = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    )
                }
            }
            TabRowPanel(primarySelected, "Primary 内容")

            Text("SecondaryTabRow")
            var secondarySelected by remember { mutableIntStateOf(1) }
            SecondaryTabRow(
                selectedTabIndex = secondarySelected,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Tab(
                    selected = secondarySelected == 0,
                    onClick = { secondarySelected = 0 },
                    icon = { Text("[i]") },
                    text = { Text("信息") },
                )
                Tab(
                    selected = secondarySelected == 1,
                    onClick = { secondarySelected = 1 },
                    icon = { Text("[s]") },
                    text = { Text("状态") },
                )
                Tab(
                    selected = secondarySelected == 2,
                    onClick = { secondarySelected = 2 },
                    icon = { Text("[x]") },
                    text = { Text("禁用") },
                    enabled = false,
                )
                Tab(
                    selected = secondarySelected == 3,
                    onClick = { secondarySelected = 3 },
                    text = { Text("无图标") },
                )
                Tab(
                    selected = secondarySelected == 4,
                    onClick = { secondarySelected = 4 },
                    icon = { Text("[?]") },
                    text = { Text("帮助") },
                )
            }
            TabRowPanel(secondarySelected, "Secondary 内容")

            Text("窄容器")
            Box(Modifier.width(220.dp)) {
                var narrowSelected by remember { mutableIntStateOf(0) }
                PrimaryTabRow(
                    selectedTabIndex = narrowSelected,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    listOf("一", "二", "三", "四", "五").forEachIndexed { index, label ->
                        Tab(
                            selected = narrowSelected == index,
                            onClick = { narrowSelected = index },
                            text = { Text(label) },
                        )
                    }
                }
            }

            Text("超出屏幕：水平滚动")
            var overflowSelected by remember { mutableIntStateOf(0) }
            var overflowCount by remember { mutableIntStateOf(DefaultOverflowTabCount) }
            val overflowScroll = rememberScrollState()
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button({ overflowSelected = 0 }) { Text("首个") }
                Button({ overflowSelected = (overflowCount - 1) / 2 }) { Text("中间") }
                Button({ overflowSelected = overflowCount - 1 }) { Text("末尾") }
                Text("当前：#${overflowSelected + 1} / $overflowCount")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("标签数：")
                listOf(16, 40, 80).forEach { count ->
                    Button({
                        overflowSelected = overflowSelected.coerceAtMost(count - 1)
                        overflowCount = count
                    }) {
                        Text("$count")
                    }
                }
            }
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val density = LocalDensity.current
                val tabWidthPx = with(density) { OverflowTabWidth.dp.roundToPx() }
                val tabGapPx = with(density) { TabRowDefaults.tabGap.roundToPx() }
                val tabStridePx = tabWidthPx + tabGapPx
                val edgePaddingPx = with(density) { OverflowEdgePadding.dp.roundToPx() }
                val tabAreaWidthPx = tabWidthPx * overflowCount + tabGapPx * (overflowCount - 1)
                val totalTabRowWidthPx = tabAreaWidthPx + edgePaddingPx * 2
                LaunchedEffect(overflowSelected, overflowCount) {
                    // ScrollState.maxValue 初始为 Int.MAX_VALUE，需等布局写出真实上界后再滚动。
                    val maxScroll = snapshotFlow { overflowScroll.maxValue }
                        .first { it in 1 until Int.MAX_VALUE }
                    // 与 M3 ScrollableTabData.calculateTabOffset 同式。
                    val visibleWidth = totalTabRowWidthPx - maxScroll
                    val tabLeft = edgePaddingPx + overflowSelected * tabStridePx
                    val scrollerCenter = visibleWidth / 2
                    val centeredTabOffset = tabLeft - (scrollerCenter - tabWidthPx / 2)
                    val availableSpace = (totalTabRowWidthPx - visibleWidth).coerceAtLeast(0)
                    val target = centeredTabOffset.coerceIn(0, availableSpace)
                    overflowScroll.animateScrollTo(
                        value = target,
                        animationSpec = tween(TabRowDefaults.IndicatorAnimationDurationMillis),
                    )
                }

                val edgePadding = OverflowEdgePadding.dp
                val tabAreaWidth =
                    OverflowTabWidth.dp * overflowCount + TabRowDefaults.tabGap * (overflowCount - 1)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(overflowScroll),
                ) {
                    Box(
                        modifier = Modifier
                            .width(tabAreaWidth + edgePadding * 2)
                            .padding(horizontal = edgePadding),
                    ) {
                        PrimaryTabRow(
                            selectedTabIndex = overflowSelected,
                            modifier = Modifier.width(tabAreaWidth),
                        ) {
                            repeat(overflowCount) { index ->
                                val label = when (index) {
                                    overflowCount / 5 -> "#${index + 1} 一个非常长的横向标签"
                                    overflowCount / 2 -> "#${index + 1} 配置"
                                    else              -> "#${index + 1}"
                                }
                                Tab(
                                    selected = overflowSelected == index,
                                    onClick = { overflowSelected = index },
                                    text = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                )
                            }
                        }
                    }
                }
            }
            TabRowPanel(overflowSelected, "横向滚动内容")
        }
    }
}

@Composable
private fun TabRowPanel(
    index: Int,
    label: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .background(Color(0x332F80ED)),
    ) {
        Text(
            text = "$label #${index + 1}",
            modifier = Modifier.padding(16.dp),
        )
    }
}
