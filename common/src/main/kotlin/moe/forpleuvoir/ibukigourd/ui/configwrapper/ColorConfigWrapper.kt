@file:Suppress("DuplicatedCode", "DuplicatedCode", "DuplicatedCode", "DuplicatedCode", "DuplicatedCode", "DuplicatedCode", "DuplicatedCode")

package moe.forpleuvoir.ibukigourd.ui.configwrapper

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.text.InlineStyleText
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.platformcontext.MinecraftClipboard
import moe.forpleuvoir.ibukigourd.ui.preset.Checkerboard
import moe.forpleuvoir.ibukigourd.ui.preset.ColorAssistChipOuterSetting
import moe.forpleuvoir.ibukigourd.ui.preset.ColorSettingButton
import moe.forpleuvoir.ibukigourd.ui.preset.Text
import moe.forpleuvoir.ibukigourd.ui.preset.TipBox
import moe.forpleuvoir.ibukigourd.ui.preset.modifier.background
import moe.forpleuvoir.ibukigourd.ui.toast.ToastContent
import moe.forpleuvoir.ibukigourd.ui.toast.ToastHandler
import moe.forpleuvoir.ibukigourd.ui.util.toComposeColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.config.Config

@Composable
fun ColorConfigWrapper(
    config: Config<Color>,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically
) = ConfigRowWrapper(config, modifier, horizontalArrangement, verticalAlignment) {

    val value by config.asState()
    ColorAssistChipOuterSetting(
        value = value,
        onValueChange = { config.setValue(it) },
        editorTitle = { Text(InlineStyleText(config.translateText.plainText)) },
        modifier = Modifier.size(ConfigRowWrapper.entrySize),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
    )
}
