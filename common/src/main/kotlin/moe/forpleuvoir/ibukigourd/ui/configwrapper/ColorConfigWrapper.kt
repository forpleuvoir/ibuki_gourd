package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import moe.forpleuvoir.ibukigourd.ui.preset.Checkerboard
import moe.forpleuvoir.ibukigourd.ui.preset.ColorSettingButton
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

    Box(
        modifier = modifier.size(ConfigRowWrapper.entrySize),
        contentAlignment = Alignment.CenterEnd
    ) {
        ColorSettingButton(
            value,
            {
                value = it
                config.setValue(it)
            },
            label = {
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
                    Text(it.hexStr, style = MaterialTheme.typography.labelSmall)
                }
            },
            modifier = modifier
        )
    }

}
