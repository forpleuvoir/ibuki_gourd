package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import moe.forpleuvoir.ibukigourd.config.item.ConfigKeybind
import moe.forpleuvoir.ibukigourd.config.item.ConfigToggleKeybind
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.ui.keybind.KeyCodeSetButton
import moe.forpleuvoir.ibukigourd.ui.keybind.KeybindSetButton
import moe.forpleuvoir.ibukigourd.ui.keybind.KeybindSettingSetButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Switch
import moe.forpleuvoir.nebula.config.Config

/**
 * 单个按键码：只捕获一个按键（滚轮倍率触发键一类）。
 *
 * @param config 按键码配置项
 * @param modifier 作用于整行
 */
@Composable
fun KeyCodeConfigWrapper(config: Config<KeyCode>, modifier: Modifier = Modifier) {
    val value by config.asState()

    ConfigRowWrapper(config, modifier) {
        KeyCodeSetButton(
            value = value,
            onValueChange = { config.setValue(it) },
            modifier = Modifier.width(ConfigControlDefaults.SelectorWidth),
        )
    }
}

/**
 * 组合键：捕获一段按键组合（松开全部按键才写入）。
 *
 * 捕获是**原地修改** keybind 对象后回调，配置的变更通知由 `ConfigKeybind.init` 里挂的
 * `keybind.observe { notifyChange() }` 转发；而显示文本要重画，所以这里用 `key(version)`
 * 在每次捕获 / 重置后重建按钮 —— 与旧版同款做法（旧版另有一段无意义的版本回绕判断，未保留）。
 *
 * 齿轮按钮打开该快捷键的扩展设置（穿透 / 严格 / 环境 / 触发模式 / 长按阈值等），
 * 比旧版只给一个组合键展示多一层编辑入口。
 *
 * @param config 组合键配置项
 * @param modifier 作用于整行
 */
@Composable
fun KeybindConfigWrapper(config: ConfigKeybind, modifier: Modifier = Modifier) {
    var version by remember { mutableIntStateOf(0) }
    val keybind = config.getValue()

    ConfigRowWrapper(config, modifier, onReset = { version++ }) {
        key(version) {
            KeybindSetButton(
                keybind = keybind,
                onValueChange = { version++ },
                modifier = Modifier.width(ConfigControlDefaults.SelectorWidth),
            )
        }
        KeybindSettingSetButton(
            keybindSetting = keybind.setting,
            onValueChange = {
                keybind.setFrom(it)
                version++
            },
        )
    }
}

/**
 * 开关式组合键：开关 + 组合键 + 扩展设置。
 *
 * 开关走 [ConfigToggleKeybind.enabled]（内部经 `toggle()` 触发变更通知），
 * 组合键部分与 [KeybindConfigWrapper] 相同。
 *
 * @param config 开关式组合键配置项
 * @param modifier 作用于整行
 */
@Composable
fun ToggleKeybindConfigWrapper(config: ConfigToggleKeybind, modifier: Modifier = Modifier) {
    val enabled by config.asDerivedState { it.enabled }
    var version by remember { mutableIntStateOf(0) }
    val keybind = config.keybind

    ConfigRowWrapper(config, modifier, onReset = { version++ }) {
        Switch(
            checked = enabled,
            onCheckedChange = {
                config.enabled = it
                version++
            },
        )
        key(version) {
            KeybindSetButton(
                keybind = keybind,
                onValueChange = { version++ },
                modifier = Modifier.width(ConfigControlDefaults.SelectorWidth),
            )
        }
        KeybindSettingSetButton(
            keybindSetting = keybind.setting,
            onValueChange = {
                keybind.setFrom(it)
                version++
            },
        )
    }
}
