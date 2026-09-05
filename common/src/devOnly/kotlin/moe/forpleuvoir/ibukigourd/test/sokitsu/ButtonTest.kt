package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.CompositionLocalProvider
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
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Switch
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuTone
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuTheme


fun ButtonTestScreen() = TestScreen {
    CenterBox(Modifier.background(SokitsuTheme.colorScheme.background.base)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Button(
                    {
                        println("按钮测试1")
                    },
                ) {
                    Text("按钮测试")
                }
                Button(
                    {
                        println("按钮测试1")
                    },
                ) {
                }
                Button(
                    {
                        println("button2 clicked")
                    },
                    enabled = false,
                ) {
                    Text("禁用测试")
                }
                CompositionLocalProvider(
                    LocalSokitsuTone provides SokitsuTheme.colorScheme.secondary
                ) {
                    Button(
                        {
                            println("button3 clicked")
                        },

                        ) {
                        Text("换个颜色")
                    }
                }
            }
            // 测试 Switch：覆盖关闭/开启/禁用三态，验证把手滑动与四态精灵
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                var checked by remember { mutableStateOf(false) }
                Switch(checked = checked, onCheckedChange = { checked = it })
                var checkedOn by remember { mutableStateOf(true) }
                Switch(checked = checkedOn, onCheckedChange = { checkedOn = it })
                var disabledChecked by remember { mutableStateOf(false) }
                Switch(checked = disabledChecked, onCheckedChange = { disabledChecked = it }, enabled = false)
            }
        }
    }
}
