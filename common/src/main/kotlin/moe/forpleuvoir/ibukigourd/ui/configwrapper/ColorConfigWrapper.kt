@file:Suppress("DuplicatedCode", "DuplicatedCode", "DuplicatedCode", "DuplicatedCode", "DuplicatedCode", "DuplicatedCode", "DuplicatedCode")

package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.ui.platformcontext.MinecraftClipboard
import moe.forpleuvoir.ibukigourd.ui.preset.Checkerboard
import moe.forpleuvoir.ibukigourd.ui.preset.ColorSettingButton
import moe.forpleuvoir.ibukigourd.ui.preset.Text
import moe.forpleuvoir.ibukigourd.ui.preset.TipBox
import moe.forpleuvoir.ibukigourd.ui.toast.ToastContent
import moe.forpleuvoir.ibukigourd.ui.toast.ToastHandler
import moe.forpleuvoir.ibukigourd.ui.util.toComposeColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.pathWithRoot

@Composable
fun ColorConfigWrapper(
    config: Config<Color>,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically
) = ConfigRowWrapper(config, modifier, horizontalArrangement, verticalAlignment) {
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

    Row(
        modifier = Modifier.size(ConfigRowWrapper.entrySize),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
    ) {
        AssistChip(
            onClick = {
                MinecraftClipboard.setClipboardText(value.hexStr)
                ToastHandler.show {
                    ToastContent { Text(IGLang.Color.copyColorSuccess(value)) }
                }
            },
            label = {
                TipBox({
                    Text(IGLang.Color.clickCopyColor(value))
                }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val shape = MaterialTheme.shapes.extraSmall
                        var titleSize by remember { mutableStateOf(5.dp) }
                        val density = LocalDensity.current
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp).size(28.dp)
                                .onSizeChanged { size ->
                                    titleSize = with(density) { (size.height / 3).toDp() }
                                }
                                .border(0.5.dp, value.reverse(false).toComposeColor, shape)
                        ) {
                            Checkerboard(titleSize, modifier = Modifier.fillMaxSize().clip(shape))
                            Box(Modifier.fillMaxSize().background(value.toComposeColor, shape))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(value.hexStr, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        )
        ColorSettingButton(value, { value = it; config.setValue(value) }, title = {
            Text(config.translateText)
        })
    }

}
