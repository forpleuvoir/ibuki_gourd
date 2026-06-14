package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Switch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import moe.forpleuvoir.ibukigourd.config.item.ConfigKeybind
import moe.forpleuvoir.ibukigourd.config.item.ConfigToggleKeybind
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.ui.preset.KeyCodeSetButton
import moe.forpleuvoir.ibukigourd.ui.preset.KeybindSetButton
import moe.forpleuvoir.ibukigourd.ui.preset.KeybindSettingSetButton
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.pathWithRoot

@Composable
fun KeyCodeConfigWrapper(
    config: Config<KeyCode>,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically
) {
    var value by remember { mutableStateOf(config.getValue()) }

    val interval = ConfigRowWrapper.valuePollInterval
    LaunchedEffect(config.pathWithRoot) {
        while (isActive) {
            val savedValue = value
            delay(interval)
            val newValue = config.getValue()
            if (newValue != value && savedValue == value) {
                value = newValue
            }
        }
    }

    ConfigRowWrapper(
        config,
        modifier,
        horizontalArrangement,
        verticalAlignment,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(ConfigRowWrapper.entrySize)
        ) {
            KeyCodeSetButton(value, { value = it; config.setValue(it) }, Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun KeybindConfigWrapper(
    config: ConfigKeybind,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically
) {
    var version by remember { mutableStateOf(0L) }
    fun update() {
        if (version > 1145141919810) {
            version = 0
        } else {
            version++
        }
    }
    ConfigRowWrapper(
        config,
        modifier,
        horizontalArrangement,
        verticalAlignment,
        onReset = ::update
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.size(ConfigRowWrapper.entrySize)
        ) {
            key(version) {
                KeybindSetButton(config.getValue(), { update() }, Modifier.weight(1f))
                KeybindSettingSetButton(config.getValue().setting, {
                    config.getValue().setFrom(it)
                    update()
                })
            }
        }
    }
}


@Composable
fun ToggleKeybindConfigWrapper(
    config: ConfigToggleKeybind,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically
) {

    var enabled by remember { mutableStateOf(config.enabled) }

    var version by remember { mutableStateOf(0L) }
    fun update() {
        if (version > 1145141919810) {
            version = 0
        } else {
            version++
        }
    }

    val interval = ConfigRowWrapper.valuePollInterval
    LaunchedEffect(config.pathWithRoot) {
        while (isActive) {
            val savedValue = enabled
            delay(interval)
            val newValue = config.enabled
            if (newValue != enabled && savedValue == enabled) {
                enabled = newValue
            }
        }
    }

    ConfigRowWrapper(
        config,
        modifier,
        horizontalArrangement,
        verticalAlignment,
        onReset = ::update
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.size(ConfigRowWrapper.entrySize)
        ) {

            Switch(enabled, { config.enabled = it; enabled = it; update() })
            key(version) {
                KeybindSetButton(config.keybind, { update() }, Modifier.weight(1f))
                KeybindSettingSetButton(config.keybind.setting, {
                    config.keybind.setFrom(it)
                    update()
                })
            }
        }
    }
}