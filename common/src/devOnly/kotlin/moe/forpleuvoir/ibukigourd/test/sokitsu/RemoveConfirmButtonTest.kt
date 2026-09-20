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
import moe.forpleuvoir.ibukigourd.test.CenterBox
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Button
import moe.forpleuvoir.ibukigourd.ui.sokitsu.RemoveButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.RemoveConfirmButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text

/**
 * 删除按钮测试屏：确认流程、快速动作跳过确认、附加正文与直接删除。
 *
 * 验证点：
 * 1. [RemoveConfirmButton] 点击弹确认，取消不计数、确认才计数；
 * 2. 快速动作判定在**点击时**求值 —— 开关打开后点击应直接执行、不弹确认；
 * 3. 确认对话框的补充正文槽位；
 * 4. [RemoveButton] 无确认直接执行。
 */
@Composable
fun RemoveConfirmButtonTestContent() {
    var lastAction by remember { mutableStateOf("（还没操作）") }
    var deletions by remember { mutableStateOf(0) }
    var quick by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("最近操作：$lastAction")
        Text("删除计数：$deletions")

        Text("1. 二次确认：取消不计数")
        RemoveConfirmButton("条目 1", onConfirm = { deletions++; lastAction = "1. 已确认删除" })

        Text("2. 快速动作：${if (quick) "开（点击跳过确认）" else "关（点击弹确认）"}")
        Button({ quick = !quick }) { Text("切换快速动作") }
        RemoveConfirmButton(
            message = "条目 2",
            onConfirm = { deletions++; lastAction = "2. 跳过确认直接删除" },
            quickAction = { quick },
        )

        Text("3. 确认对话框带补充正文")
        RemoveConfirmButton(
            message = "条目 3",
            onConfirm = { deletions++; lastAction = "3. 已确认删除" },
            content = { Text("删除后不可恢复。") },
        )

        Text("4. RemoveButton：无确认")
        RemoveButton(onClick = { deletions++; lastAction = "4. 直接删除" })
    }
}

fun RemoveConfirmButtonTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        CenterBox {
            RemoveConfirmButtonTestContent()
        }
    }
}
