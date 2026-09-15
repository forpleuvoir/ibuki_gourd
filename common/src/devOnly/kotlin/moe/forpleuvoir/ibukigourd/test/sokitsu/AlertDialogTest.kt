package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import moe.forpleuvoir.ibukigourd.test.CenterBox
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.AlertDialog
import moe.forpleuvoir.ibukigourd.ui.sokitsu.AlertDialogDefaults
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Button
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.SimpleAlertDialog
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TextButton

/**
 * 提示对话框测试屏：覆盖内容槽位组合、按钮回调、Modal 行为、宽度约束与出现 / 消失动画。
 *
 * 弹窗照 M3 的写法用 `if` 控制组合；退场动画由平台 `Dialog` 播完（图层快照重放），
 * 所以按钮里直接把自己那个 state 置 false 也能看到完整退场。
 *
 * 验证点：
 * 1. 基础 —— 标题 / 正文 / 确认 + 取消，按钮回调与关闭；
 * 2. 图标 —— icon 槽位与标题的间距；
 * 3. 单按钮 —— 无标题时面板不出现空占位；
 * 4. [SimpleAlertDialog] 校验 —— 确认回调返回 false 时对话框保持打开；
 * 5. 关闭策略 —— 禁用遮罩点击与 Esc 后只能由按钮关闭；
 * 6. 宽度 —— 长正文在 maxWidth 内换行，不把面板撑满屏幕；
 * 7. 动画 —— 放慢时长与加大位移，肉眼看"下方滑入 + 淡入 / 反向加速淡出"，含遮罩同步淡入淡出。
 *
 * 背景按钮的点击计数用于验证模态：对话框打开时 scrim 应阻断主场景点击（计数不变）。
 */
@Composable
fun AlertDialogTestContent() {
    var lastAction by remember { mutableStateOf("（还没操作）") }
    var backgroundClicks by remember { mutableStateOf(0) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("最近操作：$lastAction")
        Text("背景点击次数：$backgroundClicks（弹窗打开时点不动才对）")
        Button({ backgroundClicks++ }) { Text("背景按钮（模态验证）") }

        // 1. 基础：标题 + 正文 + 确认 / 取消
        var basic by remember { mutableStateOf(false) }
        Button({ basic = true }) { Text("1. 基础：标题 + 正文 + 取消") }
        if (basic) {
            AlertDialog(
                onDismissRequest = { basic = false; lastAction = "1. 遮罩 / Esc 关闭" },
                confirmButton = { TextButton({ basic = false; lastAction = "1. 确认" }, "确认") },
                dismissButton = { TextButton({ basic = false; lastAction = "1. 取消" }, "取消") },
                title = { Text("提示") },
                text = { Text("标题取主题 subtitle 字号，正文取 body 字号，按钮行右对齐。") },
            )
        }

        // 2. 图标：icon + 标题 + 正文 + 单按钮
        var withIcon by remember { mutableStateOf(false) }
        Button({ withIcon = true }) { Text("2. 带图标") }
        if (withIcon) {
            AlertDialog(
                onDismissRequest = { withIcon = false },
                confirmButton = { TextButton({ withIcon = false; lastAction = "2. 知道了" }, "知道了") },
                icon = { Icon(Icons.Delete) },
                title = { Text("删除这一项？") },
                text = { Text("图标默认按像素放大倍率取尺寸（16×16 素材 → 48 逻辑像素）。") },
            )
        }

        // 3. 无标题 / 单按钮：验证空槽位不留占位
        var minimal by remember { mutableStateOf(false) }
        Button({ minimal = true }) { Text("3. 只有正文 + 单按钮") }
        if (minimal) {
            AlertDialog(
                onDismissRequest = { minimal = false },
                confirmButton = { TextButton({ minimal = false }, "关闭") },
                text = { Text("没有 title / icon / dismissButton，面板上方不应出现多余留白。") },
            )
        }

        // 4. SimpleAlertDialog：确认回调返回 false 时保持打开
        var simple by remember { mutableStateOf(false) }
        var attempts by remember { mutableStateOf(0) }
        Button({ simple = true; attempts = 0 }) { Text("4. Simple：确认前校验") }
        if (simple) {
            SimpleAlertDialog(
                onDismissRequest = { simple = false; lastAction = "4. 已关闭" },
                onConfirmRequest = {
                    attempts++
                    val pass = attempts >= 3
                    lastAction = "4. 第 $attempts 次确认 → 返回 $pass"
                    pass
                },
                title = { Text("校验演示") },
                content = { Text("前两次点确认返回 false，对话框不关闭（已尝试 $attempts 次）；第三次才关闭。") },
            )
        }

        // 5. 关闭策略：遮罩点击与 Esc 均失效，只能点按钮
        var strict by remember { mutableStateOf(false) }
        Button({ strict = true }) { Text("5. 禁用遮罩 / Esc 关闭") }
        if (strict) {
            AlertDialog(
                onDismissRequest = { strict = false; lastAction = "5. 关闭请求（遮罩/Esc 已禁用，不应触发）" },
                confirmButton = { TextButton({ strict = false; lastAction = "5. 确认关闭" }, "确认") },
                title = { Text("强制选择") },
                text = { Text("properties 关掉了 dismissOnClickOutside 与 dismissOnBackPress。") },
                properties = DialogProperties(
                    dismissOnClickOutside = false,
                    dismissOnBackPress = false,
                ),
            )
        }

        // 6. 长正文：验证 maxWidth 换行
        var longText by remember { mutableStateOf(false) }
        Button({ longText = true }) { Text("6. 长正文（maxWidth 换行）") }
        if (longText) {
            AlertDialog(
                onDismissRequest = { longText = false },
                confirmButton = { TextButton({ longText = false }, "好") },
                title = { Text("长正文") },
                text = {
                    Text(
                        "这段正文刻意写得很长，用来验证面板宽度受 maxWidth 约束后文本自动换行，" +
                                "而不是把面板一路撑到屏幕边缘。按钮行应始终右对齐在面板内容区的右边缘，" +
                                "标题与正文左对齐，三者共享同一组内边距。"
                    )
                },
            )
        }

        // 7. 动画观察：拉长时长、加大位移
        var slow by remember { mutableStateOf(false) }
        Button({ slow = true }) { Text("7. 动画观察（时长 ×4 / 位移 32dp）") }
        if (slow) {
            AlertDialog(
                onDismissRequest = { slow = false },
                confirmButton = { TextButton({ slow = false }, "关闭") },
                title = { Text("慢速动画") },
                text = { Text("入场 640ms：从下方 32dp 处滑入并淡入；关闭时反向滑回下方并加速淡出，遮罩同步淡出。") },
                enterAnimation = AlertDialogDefaults.enterAnimation.copy(durationMillis = 640, offset = 32.dp),
                exitAnimation = AlertDialogDefaults.exitAnimation.copy(durationMillis = 480, offset = 32.dp),
            )
        }
    }
}

fun AlertDialogTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        CenterBox {
            AlertDialogTestContent()
        }
    }
}
