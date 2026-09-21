package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Button
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IntSlider
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TabStrip
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TabStripPlacement
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TabStripTab
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text

/** 面板宽度的下限：再窄下去箭头槽位就吃满宽度，没有实际意义。 */
private const val MinPanelWidth = 240

/** 本屏对应的 TabStrip 修订标记：改完刷新后应能在屏上看到同一串，便于确认跑的是最新代码。 */
private const val TabStripRev = "rev 2026-09-21 选中随数量收敛"

/**
 * 页签条溢出测试屏（原型：占位方块绘制）。
 *
 * 面板宽度默认取**当前屏幕可用宽度**（减内边距），与真实配置页的形态接近；滑条上限同样是可用宽度，
 * 因此只有主动往左拖才会进入溢出场景。
 *
 * 验证点：
 * 1. 宽度充足时全部页签铺开、两端不出现箭头；
 * 2. 收窄面板宽度：页签整体隐藏（不显示半个）、两端常驻箭头槽位、箭头按该侧是否真有隐藏项启用；
 * 3. 点箭头 = 只平移窗口（不改选中），并带左右滑动 + 进入页签的透明渐入；该侧没有隐藏页签时箭头整块隐藏；
 *    "首个 / 中间 / 末尾 / +5"按钮用于把选中项抛到隐藏区，观察窗口如何回填；
 * 4. 点页签本身 = 选中该页签（悬停有手型与变色、按下有音效）；
 * 5. 选中项本身比可用区还宽时不被隐藏，按可用宽钳制并省略标签（"非常长的页签名字"那一档）；
 * 6. 页签行与面板同宽 —— 页签永远不会落到面板右缘之外。
 */
fun TabStripTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.fillMaxSize().padding(16.dp)) {
            val available = maxWidth.value.toInt().coerceAtLeast(MinPanelWidth)
            var count by remember { mutableIntStateOf(8) }
            var panelWidth by remember(available) { mutableIntStateOf(available) }
            var selected by remember { mutableIntStateOf(0) }
            var placement by remember { mutableStateOf(TabStripPlacement.Top) }
            val labels = remember(count) { tabStripTestLabels(count) }
            val current = selected.coerceIn(0, count - 1)

            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("页签数：")
                    listOf(3, 8, 20, 40).forEach { size ->
                        Button({
                            // 先按新数量收敛选中、再写数量：写入过程中选中始终落在 [0, 新数量)
                            selected = selected.coerceIn(0, size - 1)
                            count = size
                        }) {
                            Text("$size")
                        }
                    }
                    Text(TabStripRev)
                    Text("朝向：")
                    TabStripPlacement.entries.forEach { side ->
                        Button({
                            // 换朝向不涉及页签集，选中保持不变
                            placement = side
                        }) {
                            Text(if (side == TabStripPlacement.Top) "上方" else "下方")
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("面板宽度：${panelWidth}dp（可用 ${available}dp）")
                    IntSlider(
                        value = panelWidth,
                        onValueChange = { panelWidth = it },
                        valueRange = MinPanelWidth..available,
                        modifier = Modifier.weight(1f),
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("选中：#${current + 1}「${labels[current]}」")
                    Button({ selected = 0 }) { Text("首个") }
                    Button({ selected = count / 2 }) { Text("中间") }
                    Button({ selected = count - 1 }) { Text("末尾") }
                    Button({ selected = (current + 5).coerceAtMost(count - 1) }) { Text("+5") }
                }

                Box(Modifier.width(panelWidth.dp)) {
                    TabStrip(
                        selectedTab = current,
                        placement = placement,
                        tabs = {
                            labels.forEachIndexed { index, label ->
                                TabStripTab(
                                    selected = index == current,
                                    onClick = { selected = index },
                                ) {
                                    Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        },
                    ) {
                        // 面板内容也吃渐变过渡：切页签时整块 Crossfade 淡入淡出。
                        // 色带用横向渐变、每个页签一档配色，淡入淡出看得更清楚。
                        // 点箭头只翻窗口、不改选中，所以不会触发它。
                        Crossfade(targetState = current, animationSpec = tween(220)) { tab ->
                            // Crossfade 动画期间 tab 是**旧值**：页签集缩小后旧下标会越界（labels 已经变短），
                            // 所以这里按当前列表把动画值钳进范围再用。
                            val index = tab.coerceIn(0, labels.lastIndex.coerceAtLeast(0))
                            val accent = TabStripTestAccents[index % TabStripTestAccents.size]
                            Column(Modifier.padding(12.dp)) {
                                Text("面板内容：第 ${index + 1} 个页签「${labels[index]}」")
                                Text("切页签 → 这段内容淡入淡出；箭头翻页不改选中，不触发")
                                Spacer(Modifier.height(8.dp))
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(24.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(accent, accent.copy(alpha = 0.2f)),
                                            ),
                                        ),
                                )
                                Spacer(Modifier.height(150.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/** 每个页签一档强调色（面板色带的渐变起点）：一页一个色，Crossfade 的淡入淡出才看得出来。 */
private val TabStripTestAccents = listOf(
    Color(0xFF8A63FF),
    Color(0xFF4FA3FF),
    Color(0xFF43C59E),
    Color(0xFFFFB454),
    Color(0xFFFF6B8A),
)

/** 测试标签：长度参差，含一档超长标签用于验证"选中页签豁免隐藏"。 */
private fun tabStripTestLabels(count: Int): List<String> = (0 until count).map { index ->
    when (index % 5) {
        0    -> "页签 ${index + 1}"
        1    -> "设置 ${index + 1}"
        2    -> "一般功能 ${index + 1}"
        3    -> "非常长的页签名字 ${index + 1}"
        else -> "S${index + 1}"
    }
}
