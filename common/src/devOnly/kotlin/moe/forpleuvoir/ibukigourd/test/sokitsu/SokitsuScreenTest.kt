package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.compose_minecraft.platform.screen.ComposeScreen
import moe.forpleuvoir.ibukigourd.test.CenterBox
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Button
import moe.forpleuvoir.ibukigourd.ui.sokitsu.SokitsuScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.SokitsuScreenDefaults
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalContentColor
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuPixelScale
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.toNebulaColor

/**
 * `SokitsuScreen` 测试屏。
 *
 * 验证点：
 * 1. 主题：内容直接摆 `Surface` / `Button` / `Text`，不需要自己套 `SokitsuTheme`；
 * 2. 缩放读数：窗口像素、`LocalDensity`、`LocalSokitsuPixelScale` 三者应满足
 *    `density = 档位倍率` 且 `pixelScale = 主题 pixelScale × 倍率`；
 *    缩放窗口使宽高跨过 [SokitsuScreenDefaults.threshold] 时读数应在基准档 / 紧凑档之间实时切换；
 * 3. 嵌套：从本屏再开一个 `SokitsuScreen`，父屏进入可复活流程，返回后状态保留
 *    （读数与按钮的悬停/焦点态不重置）。
 */
fun SokitsuScreenTestScreen() = SokitsuScreen.open {
    SokitsuScreenBody("SokitsuScreen 根屏")
}

/**
 * 屏幕内容：读数 + 开子屏 + 关屏。
 *
 * 不套 `SokitsuTheme`、不设 `Modifier.fillMaxSize()` 之外的缩放参数 —— 这两件事由
 * [SokitsuScreen] 的测量包裹负责。
 */
@Composable
private fun SokitsuScreenBody(title: String) {
    Surface(Modifier.fillMaxSize()) {
        CenterBox {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(title)
                ScaleReadout()
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button({
                        SokitsuScreen.open {
                            SokitsuScreenBody("SokitsuScreen 子屏")
                        }
                    }) {
                        Text("打开子屏")
                    }
                    Button({
                        ComposeScreen.closeCurrent()
                    }) {
                        Text("关闭本屏")
                    }
                }
            }
        }
    }
}

/** 当前生效的缩放与主题读数（`density` / `pixelScale` 都取自组合环境，即测量包裹下发的值）。 */
@Composable
private fun ScaleReadout() {
    val density = LocalDensity.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text("窗口 ${mc.window.width}×${mc.window.height}")
        Text("密度 ${density.density}（1dp = ${density.density}px）  像素倍率 ${LocalSokitsuPixelScale.current}")
        Text(
            "阈值 ${SokitsuScreenDefaults.threshold.width}×${SokitsuScreenDefaults.threshold.height}" +
                    "  基准倍率 ${SokitsuScreenDefaults.baseFactor}  紧凑倍率 ${SokitsuScreenDefaults.compactFactor}"
        )
        Text("内容色 ${LocalContentColor.current.toNebulaColor().hexStr}")
    }
}
