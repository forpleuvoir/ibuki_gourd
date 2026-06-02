package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.plus
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.icon.KeyboardArrowDown
import moe.forpleuvoir.ibukigourd.ui.icon.KeyboardArrowUp
import moe.forpleuvoir.nebula.config.ConfigGroup

@Composable
fun ConfigGroupWrapper(
    config: ConfigGroup,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) {
    var expanded by remember { mutableStateOf(true) }
    Column {
        ConfigRowWrapper(
            config,
            modifier,
            horizontalArrangement,
            verticalAlignment,
            false,
            { expanded = !expanded }
        ) {
            Icon(if (expanded) Icons.KeyboardArrowUp else Icons.KeyboardArrowDown, null)
        }
        AnimatedVisibility(expanded) {
            Column {
                config.children.forEach {
                    HorizontalDivider()
                    CompositionLocalProvider(ConfigRowWrapper.LocalConfigRowWrapperPadding provides ConfigRowWrapper.padding + PaddingValues(start = 24.dp, end = 8.dp)) {
                        UiWrapper(it)
                    }
                }
            }
        }

    }
}
