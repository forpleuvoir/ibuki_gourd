package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.DoubleField
import moe.forpleuvoir.ibukigourd.ui.sokitsu.DurationField
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IntField
import moe.forpleuvoir.ibukigourd.ui.sokitsu.PercentField
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 数字输入框测试屏。
 *
 * 重点验证：输入期不回写文本（`-`、`1.` 等中间态可正常输入）、失焦/回车提交回写、越界与非法格式的
 * 错误描边、悬停聚焦后滚轮步进（Shift ×10 / Ctrl ×15 / Alt ×30）、只读与禁用表现。
 *
 * 列内容可滚动：滚轮悬停聚焦在输入框上时应步进数值而**不**滚动本列（步进会消费事件），
 * 未聚焦时滚轮照常滚动本列。
 */
fun NumberFieldTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            var intValue by remember { mutableStateOf(16) }
            IntField(
                value = intValue,
                onValueChange = { intValue = it },
                valueRange = 0..256,
                modifier = Modifier.width(220.dp),
                trailingIcon = { Text("px") },
            )
            Text("IntField = $intValue（范围 0..256；悬停聚焦后滚轮步进，Shift ×10）")

            var doubleValue by remember { mutableStateOf(1.5) }
            DoubleField(
                value = doubleValue,
                onValueChange = { doubleValue = it },
                valueRange = -10.0..10.0,
                valueToText = { "%.2f".format(it) },
                modifier = Modifier.width(220.dp),
            )
            Text("DoubleField = ${"%.2f".format(doubleValue)}（范围 -10..10，两位小数）")

            var percent by remember { mutableStateOf(0.5f) }
            PercentField(
                value = percent,
                onValueChange = { percent = it },
                modifier = Modifier.width(220.dp),
            )
            Text("PercentField = $percent（文本按百分比，输入 50 / 50% 都等于 0.5）")

            var duration by remember { mutableStateOf(90.seconds) }
            DurationField(
                value = duration,
                onValueChange = { duration = it },
                valueRange = Duration.ZERO..(24 * 3600).seconds,
                modifier = Modifier.width(220.dp),
            )
            Text("DurationField = $duration（文本可写 1h 30m / 500ms）")

            // 只读：可聚焦、可选中/复制，改不了值（onValueChange = null 等价）
            IntField(
                value = 42,
                onValueChange = null,
                modifier = Modifier.width(220.dp),
                trailingIcon = { Text("只读") },
            )
            Text("IntField(只读)：能点进来、能选文字，输入不生效")

            // 禁用：不接收任何操作，指针形态也不变
            IntField(
                value = 7,
                onValueChange = {},
                modifier = Modifier.width(220.dp),
                enabled = false,
                trailingIcon = { Text("禁用") },
            )
            Text("IntField(禁用)：整块不响应，指针不变")
        }
    }
}
