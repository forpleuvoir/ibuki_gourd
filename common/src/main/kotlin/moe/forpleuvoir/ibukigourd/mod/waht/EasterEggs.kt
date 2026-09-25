package moe.forpleuvoir.ibukigourd.mod.waht

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.compose_minecraft.platform.screen.ComposeScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.SokitsuScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.VerticalDivider
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalColorScheme
import net.minecraft.client.gui.screens.Screen

/**
 * 彩蛋屏幕：左列是小游戏列表，右侧铺选中游戏的本体。
 *
 * 入口在模组屏幕顶栏的模组图标上（点一下即开），不是旧版的"小键盘序列"。
 *
 * @param parent 返回时回到的父屏；缺省 = 打开前的当前屏幕
 */
fun openEasterEggsScreen(parent: Screen? = null): ComposeScreen =
    SokitsuScreen.open(parent = parent) { EasterEggsScreen() }

/**
 * 彩蛋屏幕内容（不含主题与缩放，见 [openEasterEggsScreen]）。
 *
 * @param modifier 作用于整屏
 */
@Composable
fun EasterEggsScreen(modifier: Modifier = Modifier) {
    var selected by remember { mutableStateOf(EasterEgg.Snake) }
    val scheme = LocalColorScheme.current

    Row(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.width(EasterEggsDefaults.MenuWidth).fillMaxHeight()
                .padding(EasterEggsDefaults.MenuPadding),
            verticalArrangement = Arrangement.spacedBy(EasterEggsDefaults.MenuSpacing),
        ) {
            EasterEgg.entries.forEach { egg ->
                val active = egg == selected
                Surface(
                    onClick = { selected = egg },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (active) scheme.primaryContainer else Color.Unspecified,
                    contentColor = if (active) scheme.onPrimaryContainer else Color.Unspecified,
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(egg.title)
                }
            }
        }
        VerticalDivider(Modifier.fillMaxHeight())
        Box(Modifier.weight(1f).fillMaxHeight()) {
            when (selected) {
                EasterEgg.Snake       -> SnakeGame(modifier = Modifier.fillMaxSize())
                EasterEgg.GameOfLife  -> GameOfLife(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

/**
 * 彩蛋列表。
 *
 * 标题沿用旧版的英文小写（彩蛋不是正式界面文案，不进语言文件）。
 */
private enum class EasterEgg(val title: String) {
    Snake("snake"),
    GameOfLife("game of life"),
}

/** 彩蛋屏幕的排版常量。 */
object EasterEggsDefaults {

    /** 左侧游戏列表宽度。 */
    val MenuWidth: Dp = 200.dp

    /** 游戏列表内边距。 */
    val MenuPadding: PaddingValues = PaddingValues(8.dp)

    /** 游戏项间距。 */
    val MenuSpacing: Dp = 4.dp
}
