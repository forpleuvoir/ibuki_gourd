package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.compose_minecraft.platform.screen.openDialogComposeScreen
import moe.forpleuvoir.ibukigourd.test.CenterBox
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Button
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text

/**
 * 对话框屏幕（`DialogComposeScreen`）测试屏。
 *
 * 用途：这是 [moe.forpleuvoir.ibukigourd.mod.config.IGConfig.Gui.Dialog] 那组配置
 * （`scrim_color` / `fade_in_duration` / `initial_scale` / `easing` / `easing_custom` /
 * `disable_world_render`）**唯一**的可达验收面 —— sokitsu 的 `AlertDialog` 走的是主题 meta 的
 * `curve` / `direction`，不读这组值。
 *
 * 验证点：
 * 1. 打开：遮罩淡入 + 面板缩放（起始缩放取 `initial_scale`）；关闭：反向播放，Esc 与点击遮罩都能触发；
 * 2. 缓动取 `DialogAnimationDefaults.easing`，即配置里的 `gui.dialog.easing`（`custom` 时取四点）；
 * 3. 遮罩色取 `gui.dialog.scrim_color`（背后父屏会透出，用于确认 `renderParentScreen` 行为）。
 */
fun DialogComposeScreenTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        CenterBox {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("对话框屏幕测试")
                Text("进出场 = 遮罩淡入淡出 + 面板缩放（Esc / 点击遮罩关闭）")
                Button({
                    openDialogComposeScreen(disableWorldRender = true) {
                        DialogBody()
                    }
                }) {
                    Text("打开对话框")
                }
            }
        }
    }
}

/** 对话框内容：给足够大的面板，缩放进出的差异才看得清。 */
@Composable
private fun DialogBody() {
    Surface(Modifier.padding(24.dp)) {
        Column(
            Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("DialogComposeScreen")
            Text("缓动取配置 gui.dialog.easing")
            Text("按 Esc 或点击遮罩关闭，观察出场动画")
        }
    }
}
