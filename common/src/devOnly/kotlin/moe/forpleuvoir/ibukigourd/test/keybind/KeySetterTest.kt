package moe.forpleuvoir.ibukigourd.test.keybind

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.input.Keybind
import moe.forpleuvoir.ibukigourd.test.CenterBox
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.ui.keybind.KeyCodeSetButton
import moe.forpleuvoir.ibukigourd.ui.keybind.KeybindSetButton
import moe.forpleuvoir.ibukigourd.ui.keybind.KeybindSettingSetButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text

/**
 * 按键绑定测试屏。
 *
 * 验证点：
 * 1. 单键捕获 —— 点击后按任意键写入，Esc / Ctrl + Backspace 取消；
 * 2. 组合键捕获 —— 按住若干键（按按下顺序显示）后全部松开写入；清空绑定同样走取消路径；
 * 3. 冲突高亮 —— 屏内自注册两个同键绑定，按钮应换成主题 `error` 配色，悬浮提示列出冲突项；
 * 4. 设置弹窗 —— 穿透 / 严格开关、环境与触发模式选择，长按阈值与重复间隔按触发模式显隐；
 * 5. 捕获期间按键被通配绑定吞掉，不会传给游戏与其它绑定。
 */
@Composable
fun KeySetterTestContent() {
    var single by remember { mutableStateOf<KeyCode>(Keyboard.G) }

    val keybind = remember {
        Keybind(Keyboard.LEFT_CONTROL, Keyboard.G).apply { name = Literal("测试绑定 A") }
    }
    val conflict = remember {
        Keybind(Keyboard.LEFT_CONTROL, Keyboard.G).apply { name = Literal("测试绑定 B（与 A 同键）") }
    }

    DisposableEffect(Unit) {
        val first = InputHandler.register(keybind)
        val second = InputHandler.register(conflict)
        onDispose {
            first.dispose()
            second.dispose()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("单键捕获：当前 ${single.keyName}")
        KeyCodeSetButton(single, { single = it })

        Text("组合键捕获：与自注册的冲突绑定同键 → 按钮变红，悬浮提示列出冲突")
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            KeybindSetButton(keybind)
            KeybindSettingSetButton(keybind.setting, { keybind.setFrom(it) })
        }

        InputHandler.keybindVersion
        Text(
            "当前绑定：${keybind.keys.joinToString(" + ") { it.keyName }}" +
                    "　环境 ${keybind.setting.env.key}" +
                    "　触发 ${keybind.setting.trigger.key}" +
                    "　阈值 ${keybind.setting.longPressThreshold}" +
                    "　间隔 ${keybind.setting.repeatInterval}"
        )
    }
}

fun KeySetterTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        CenterBox {
            KeySetterTestContent()
        }
    }
}
