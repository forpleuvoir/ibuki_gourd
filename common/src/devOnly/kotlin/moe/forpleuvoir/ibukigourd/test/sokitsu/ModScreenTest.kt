package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.mod.config.IGConfig
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.style.style
import moe.forpleuvoir.ibukigourd.ui.ModScreen
import moe.forpleuvoir.ibukigourd.ui.ModScreenIcon
import moe.forpleuvoir.ibukigourd.ui.ModScreenTab
import moe.forpleuvoir.ibukigourd.ui.configwrapper.ConfigManagerWrapper
import moe.forpleuvoir.ibukigourd.ui.rememberModScreenState
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Button
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.util.identifier

/**
 * 模组屏幕测试屏：顶栏与页面全部由调用方传参，验证公共 API 能拼出任意模组的屏幕。
 *
 * 验证点：
 * 1. 顶栏：模组图标（可点，点击次数印在动作槽里）、模组名、靠右的动作槽；
 * 2. 页面：配置页（[IGConfig]）+ 若干占位页，页数够多时页签条出溢出箭头；
 * 3. 选中状态由调用方持有：动作槽的"末页"按钮从外部改 `ModScreenState.selectedTab`，
 *    页签条应把窗口滚到末页（而不是只换高亮）。
 */
fun ModScreenTestScreen() = TestScreen {
    val state = rememberModScreenState()
    var iconClicks by remember { mutableIntStateOf(0) }
    val tabs = remember {
        listOf(
            ModScreenTab(IGConfig.translateText) {
                ConfigManagerWrapper(IGConfig, modifier = Modifier.fillMaxSize())
            },
        ) + (1..8).map { index -> ModScreenTab(Literal("占位页 $index")) { PlaceholderPage(index) } }
    }

    ModScreen(
        tabs = tabs,
        title = { Text(component = Literal(IbukiGourd.MOD_NAME).style { bold() }) },
        icon = { ModScreenIcon(identifier(IbukiGourd.MOD_ID, "icon.png"), onClick = { iconClicks++ }) },
        headerActions = {
            Text("图标点击 $iconClicks 次")
            Button({ state.selectedTab = tabs.lastIndex }) { Text("末页") }
        },
        state = state,
    )
}

/** 占位页内容：页面里放什么完全由调用方决定。 */
@Composable
private fun PlaceholderPage(index: Int) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("占位页 $index")
        Text("页面数量、每页的内容与顶栏动作都由调用方给")
    }
}
