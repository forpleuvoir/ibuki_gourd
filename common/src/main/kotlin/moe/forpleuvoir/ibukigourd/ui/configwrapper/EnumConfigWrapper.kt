package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
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
    Box(Modifier.height(ConfigRowWrapper.entrySize.height).widthIn(max = ConfigRowWrapper.entrySize.width), contentAlignment = Alignment.Center) {
        val textMeasurer = rememberTextMeasurer()
        val density = LocalDensity.current
        val maxWidth = remember {
            with(density) {
                value::class.java.enumConstants.maxOf {
                    textMeasurer.measure(it.translateText.toAnnotatedString()).size.width
                }.toDp() + 20.dp
            }
        }
        EnumSelector(
            value,
            { value = it; config.setValue(it) },
            modifier = modifier,
            selectedLabel = {
                Text(it.translateText, Modifier.width(maxWidth), overflow = TextOverflow.Ellipsis)
            },
        )
    }
}
