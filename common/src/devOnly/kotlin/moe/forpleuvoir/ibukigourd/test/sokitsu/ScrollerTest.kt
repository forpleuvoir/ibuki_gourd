package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.HorizontalScroller
import moe.forpleuvoir.ibukigourd.ui.sokitsu.ScrollerAdapter
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.VerticalScroller
import moe.forpleuvoir.ibukigourd.ui.sokitsu.rememberScrollerAdapter
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuColor
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuTheme

/**
 * 展示类用例用的 adapter：无真实滚动源，位置自持在组件内部。
 *
 * @param maxScrollOffset 可滚行程（内容像素）；0 = 无行程，滑块铺满
 * @param initial 起始位置（内容像素）
 * @param scrollable false = 丢弃全部 scrollTo（只读展示：配色仍按启用态，但推不动）
 */
@Composable
private fun rememberPreviewAdapter(
    maxScrollOffset: Float,
    initial: Float = 0f,
    scrollable: Boolean = true,
): ScrollerAdapter {
    var offset by remember { mutableFloatStateOf(initial) }
    return remember(maxScrollOffset, scrollable) {
        object : ScrollerAdapter {
            override val scrollOffset: Float get() = offset

            override suspend fun scrollTo(containerSize: Int, scrollOffset: Float) {
                if (scrollable) offset = scrollOffset.coerceIn(0f, maxScrollOffset)
            }

            override fun maxScrollOffset(containerSize: Int): Float = maxScrollOffset
        }
    }
}

fun ScrollerTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            VerticalSection()
            HorizontalSection()
        }
    }
}

/**
 * 竖直滚动条用例组：每条上方标出该条的预期行为，便于在游戏内逐条核对。
 *
 * 标签与滚动条是并列的兄弟节点 —— 文字不放进滚动条内部，避免遮挡或撑宽组件。
 */
@Composable
private fun VerticalSection() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 可拖动 + 滚轮 + 点空白连续滚动（按住不放会重复滚动）
        ScrollerCase("拖动·滚轮·点空白按住连滚") {
            VerticalScroller(
                adapter = rememberPreviewAdapter(maxScrollOffset = 370f, initial = 185f),
                modifier = Modifier.height(200.dp),
            )
        }

        // 只读：adapter 丢弃 scrollTo，配色仍按启用态但推不动
        ScrollerCase("只读·不可拖") {
            VerticalScroller(
                adapter = rememberPreviewAdapter(maxScrollOffset = 460f, scrollable = false),
                modifier = Modifier.height(200.dp),
            )
        }
        ScrollerCase("只读·不可拖") {
            VerticalScroller(
                adapter = rememberPreviewAdapter(maxScrollOffset = 460f, initial = 460f, scrollable = false),
                modifier = Modifier.height(200.dp),
            )
        }

        // 禁用：不接收手势
        ScrollerCase("禁用·不可拖") {
            VerticalScroller(
                adapter = rememberPreviewAdapter(maxScrollOffset = 600f, initial = 240f),
                enabled = false,
                modifier = Modifier.height(200.dp),
            )
        }

        // 作用域换色板：轨道与滑块应随 secondary 走
        ScrollerCase("拖动·换色板") {
            CompositionLocalProvider(LocalSokitsuColor provides SokitsuTheme.colorScheme.secondary) {
                VerticalScroller(
                    adapter = rememberPreviewAdapter(maxScrollOffset = 300f, initial = 90f),
                    modifier = Modifier.height(200.dp),
                )
            }
        }

        // 极端占比：行程 0 时滑块铺满，拖不动属预期
        ScrollerCase("铺满·行程0 不可拖") {
            VerticalScroller(
                adapter = rememberPreviewAdapter(maxScrollOffset = 0f),
                modifier = Modifier.height(200.dp),
            )
        }

        // 与真实滚动状态对接：列表滚动带动滑块，拖滑块 / 点空白 / 滚轮也带动列表
        ScrollerCase("拖动·联动列表(48)") {
            VerticalScrollStateDemo(48)
        }

        // 同视口、条目翻倍 → 行程翻倍；对比两组滚一格的数值可判定步长与行程的关系
        ScrollerCase("拖动·联动列表(96)") {
            VerticalScrollStateDemo(96)
        }
    }
}

/** 水平滚动条用例组：与竖直版同规则，主轴换成 x 轴。 */
@Composable
private fun HorizontalSection() {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.Top,
        ) {
            // 滚轮读的是垂直分量（鼠标滚轮只有 y），只是滚动投影到 x 轴
            ScrollerCase("拖动·滚轮·点空白按住连滚") {
                HorizontalScroller(
                    adapter = rememberPreviewAdapter(maxScrollOffset = 450f, initial = 225f),
                    modifier = Modifier.width(240.dp),
                )
            }

            ScrollerCase("只读·不可拖") {
                HorizontalScroller(
                    adapter = rememberPreviewAdapter(maxScrollOffset = 560f, scrollable = false),
                    modifier = Modifier.width(240.dp),
                )
            }
            ScrollerCase("只读·不可拖") {
                HorizontalScroller(
                    adapter = rememberPreviewAdapter(maxScrollOffset = 560f, initial = 560f, scrollable = false),
                    modifier = Modifier.width(240.dp),
                )
            }

            ScrollerCase("禁用·不可拖") {
                HorizontalScroller(
                    adapter = rememberPreviewAdapter(maxScrollOffset = 720f, initial = 288f),
                    enabled = false,
                    modifier = Modifier.width(240.dp),
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.Top,
        ) {
            ScrollerCase("拖动·换色板") {
                CompositionLocalProvider(LocalSokitsuColor provides SokitsuTheme.colorScheme.secondary) {
                    HorizontalScroller(
                        adapter = rememberPreviewAdapter(maxScrollOffset = 360f, initial = 108f),
                        modifier = Modifier.width(240.dp),
                    )
                }
            }

            ScrollerCase("铺满·行程0 不可拖") {
                HorizontalScroller(
                    adapter = rememberPreviewAdapter(maxScrollOffset = 0f),
                    modifier = Modifier.width(240.dp),
                )
            }

            ScrollerCase("拖动·联动列表") {
                HorizontalScrollStateDemo()
            }
        }
    }
}

/** 单条滚动条用例：上方标出该条的预期行为，便于在游戏内逐条核对。 */
@Composable
private fun ScrollerCase(label: String, content: @Composable () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(label)
        content()
    }
}

/**
 * 竖直滚动条与 [ScrollState] 的双向对接示例。
 *
 * 对接只经 [rememberScrollerAdapter]：滚动条与列表共用同一个 `ScrollState`，
 * 滚轮一档两侧走过的内容像素相同，可直接对比。
 *
 * @param items 列表条目数：视口固定 200dp，条目数决定可滚动行程（用于对比步长与行程的关系）
 */
@Composable
private fun VerticalScrollStateDemo(items: Int) {
    val scrollState = rememberScrollState()
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            Modifier
                .width(160.dp)
                .height(200.dp)
                .verticalScroll(scrollState),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(items) { index ->
                    Surface(Modifier.fillMaxWidth()) {
                        Text("条目 $index")
                    }
                }
            }
        }
        VerticalScroller(
            adapter = rememberScrollerAdapter(scrollState),
            modifier = Modifier.height(200.dp),
        )
        // 实时数值：滚一格的变化量可直接读出，用于与列表滚轮对比
        Text("${scrollState.value} / ${scrollState.maxValue}")
    }
}

/** 水平滚动条与 [ScrollState] 的双向对接示例（主轴换成 x 轴）。 */
@Composable
private fun HorizontalScrollStateDemo() {
    val scrollState = rememberScrollState()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            Modifier
                .width(240.dp)
                .height(60.dp)
                .horizontalScroll(scrollState),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // 条目数须撑出远超视口 240dp 的内容宽度，否则 maxValue = 0 无行程可滚
                repeat(24) { index ->
                    Surface(Modifier.height(60.dp)) {
                        Text("条目 $index")
                    }
                }
            }
        }
        HorizontalScroller(
            adapter = rememberScrollerAdapter(scrollState),
            modifier = Modifier.width(240.dp),
        )
        // 实时数值：滚一格的变化量可直接读出，用于与列表滚轮对比
        Text("${scrollState.value} / ${scrollState.maxValue}")
    }
}
