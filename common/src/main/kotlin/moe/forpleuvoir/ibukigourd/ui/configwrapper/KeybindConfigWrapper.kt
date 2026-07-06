package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Switch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import moe.forpleuvoir.ibukigourd.config.item.ConfigKeybind
import moe.forpleuvoir.ibukigourd.config.item.ConfigToggleKeybind
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.ui.preset.KeyCodeSetButton
import moe.forpleuvoir.ibukigourd.ui.preset.KeybindSetButton
import moe.forpleuvoir.ibukigourd.ui.preset.KeybindSettingSetButton
import moe.forpleuvoir.nebula.config.Config

@Composable
fun KeyCodeConfigWrapper(
    config: Config<KeyCode>,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically
) {
    val value by config.asState()

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
            KeyCodeSetButton(value, { config.setValue(it) }, Modifier.fillMaxWidth())
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
    val enabled by config.asDerivedState { it.enabled }

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

            Switch(enabled, { config.enabled = it; update() })
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