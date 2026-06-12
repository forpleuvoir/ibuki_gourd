package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.plus
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.icon.default.KeyboardArrowDown
import moe.forpleuvoir.nebula.config.ConfigGroup
import moe.forpleuvoir.nebula.config.ConfigNode

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
            val rotation by animateFloatAsState(if (expanded) 180f else 0f)
            Icon(Icons.KeyboardArrowDown, null, Modifier.rotate(rotation))
        }
        AnimatedVisibility(expanded) {
            ConfigsWrapper(config.children)
        }
    }
}

@Composable
fun ConfigsWrapper(
    configs: Iterable<ConfigNode>,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
) = Column(modifier, verticalArrangement, horizontalAlignment) {
    configs.forEach {
        CompositionLocalProvider(
            ConfigRowWrapper.LocalConfigRowWrapperPadding provides ConfigRowWrapper.padding + PaddingValues(
                start = 24.dp,
                end = 8.dp
            )
        ) {
            ConfigUiWrapper(it)
        }
        HorizontalDivider()
    }
}
