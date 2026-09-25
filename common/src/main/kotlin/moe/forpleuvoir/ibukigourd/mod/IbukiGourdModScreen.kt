package moe.forpleuvoir.ibukigourd.mod

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import moe.forpleuvoir.compose_minecraft.platform.screen.ComposeScreen
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.mod.config.IGConfig
import moe.forpleuvoir.ibukigourd.mod.waht.openEasterEggsScreen
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.style.style
import moe.forpleuvoir.ibukigourd.ui.ModScreen
import moe.forpleuvoir.ibukigourd.ui.ModScreenIcon
import moe.forpleuvoir.ibukigourd.ui.ModScreenTab
import moe.forpleuvoir.ibukigourd.ui.configwrapper.ConfigManagerWrapper
import moe.forpleuvoir.ibukigourd.ui.sokitsu.SokitsuScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuTheme
import moe.forpleuvoir.ibukigourd.util.identifier
import net.minecraft.client.gui.screens.Screen

/**
 * 构造（**不打开**）IbukiGourd 的模组屏幕，交调用方自行 `setScreen` —— ModMenu / NeoForge
 * 模组列表的配置按钮工厂要的就是这种"先给屏幕"的形态。
 *
 * 内容 = sokitsu 主题与缩放 + [IbukiGourdModScreen]。
 *
 * @param parent 返回时回到的父屏
 */
fun ibukiGourdModScreen(parent: Screen? = null): ComposeScreen =
    SokitsuScreen.create(parent = parent) { IbukiGourdModScreen() }

/**
 * 构造并打开 IbukiGourd 的模组屏幕。
 *
 * @param parent 返回时回到的父屏；缺省 = 打开前的当前屏幕
 */
fun openIbukiGourdModScreen(parent: Screen? = null): ComposeScreen =
    SokitsuScreen.open(parent = parent) { IbukiGourdModScreen() }

/**
 * IbukiGourd 的模组屏幕内容（不含主题与缩放，见 [ibukiGourdModScreen]）。
 *
 * 顶栏用模组图标与模组名，图标连点 [EasterEggClickCount] 次开彩蛋屏；页面见 [ibukiGourdModScreenTabs]。
 *
 * @param modifier 作用于整屏
 */
@Composable
fun IbukiGourdModScreen(modifier: Modifier = Modifier) {
    ModScreen(
        tabs = ibukiGourdModScreenTabs,
        modifier = modifier,
        title = { Text(IbukiGourd.MOD_NAME, style = SokitsuTheme.typography.subtitle.copy(fontWeight = FontWeight.Bold),) },
        icon = { ModScreenIcon(identifier("icon.png"), onClick = rememberEasterEggTrigger()) },
    )
}

/**
 * 彩蛋触发：连续点模组图标 [EasterEggClickCount] 次即打开彩蛋屏。
 *
 * 相邻两次超过 [EasterEggClickIntervalMillis] 就重新计数，"连续"才算数。
 */
@Composable
private fun rememberEasterEggTrigger(): () -> Unit {
    var clicks by remember { mutableIntStateOf(0) }
    var lastClickAt by remember { mutableLongStateOf(0L) }
    return {
        val now = System.currentTimeMillis()
        clicks = if (now - lastClickAt <= EasterEggClickIntervalMillis) clicks + 1 else 1
        lastClickAt = now
        if (clicks >= EasterEggClickCount) {
            clicks = 0
            openEasterEggsScreen()
        }
    }
}

/** 彩蛋触发所需的连续点击次数。 */
private const val EasterEggClickCount = 10

/** 相邻两次点击的最长间隔（毫秒）：超过即重新计数。 */
private const val EasterEggClickIntervalMillis = 1500L

/**
 * IbukiGourd 模组屏幕的全部页面：**新增页面往这里加一项** —— 页签条、下标与内容分发都按本表生成。
 */
private val ibukiGourdModScreenTabs: List<ModScreenTab> = listOf(
    ModScreenTab(IGConfig.translateText) {
        ConfigManagerWrapper(IGConfig, modifier = Modifier.fillMaxSize())
    },
)
