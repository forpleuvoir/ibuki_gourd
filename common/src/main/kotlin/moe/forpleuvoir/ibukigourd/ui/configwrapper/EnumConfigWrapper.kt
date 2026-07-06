package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import moe.forpleuvoir.ibukigourd.text.translateText
import moe.forpleuvoir.ibukigourd.ui.preset.EnumSelector
import moe.forpleuvoir.ibukigourd.ui.preset.Text
import moe.forpleuvoir.ibukigourd.ui.preset.toAnnotatedString
import moe.forpleuvoir.nebula.config.item.ConfigEnum
import moe.forpleuvoir.nebula.config.pathWithRoot

@Composable
fun <E : Enum<E>> EnumConfigWrapper(
    config: ConfigEnum<E>,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) = ConfigRowWrapper(config, modifier, horizontalArrangement, verticalAlignment) {

    val value by config.asState()

    Box(Modifier.height(ConfigRowWrapper.entrySize.height).widthIn(max = ConfigRowWrapper.entrySize.width), contentAlignment = Alignment.Center) {
        EnumSelector(
            value,
            { config.setValue(it) },
            modifier = Modifier.fillMaxWidth(),
            content = {
                Text(it.translateText, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        )
    }
}
