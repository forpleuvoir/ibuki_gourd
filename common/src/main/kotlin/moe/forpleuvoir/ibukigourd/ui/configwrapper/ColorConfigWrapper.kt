package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.text.InlineStyleText
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.colorpicker.ColorPickButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.util.toComposeColor
import moe.forpleuvoir.ibukigourd.util.toNebulaColor
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.common.color.Color as NebulaColor

/**
 * 颜色：底色即当前色的预览按钮，点击弹出取色器编辑副本。
 *
 * 素材与交互都来自 [ColorPickButton]（HSV / RGB 通道条 + alpha + 色值复制粘贴 + 棋盘），
 * 与旧版基于 Material3 `AssistChip` 的 `ColorAssistChipOuterSetting` 不再相同 —— 本仓既定策略不做 Chip。
 *
 * @param config 颜色配置项
 * @param modifier 作用于整行
 */
@Composable
fun ColorConfigWrapper(config: Config<NebulaColor>, modifier: Modifier = Modifier) {
    val value by config.asState()

    ConfigRowWrapper(config, modifier) {
        ColorPickButton(
            color = value.toComposeColor(),
            onValueChange = { config.setValue(it.toNebulaColor()) },
            title = { Text(InlineStyleText(config.translateText.plainText)) },
            modifier = Modifier.width(ConfigControlDefaults.ColorButtonWidth),
        )
    }
}
