package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.config.translateComment
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.icon.default.Replay
import moe.forpleuvoir.nebula.common.api.Resettable
import moe.forpleuvoir.nebula.config.ConfigNode
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

object ConfigRowWrapper {

    val padding: PaddingValues
        @Composable @ReadOnlyComposable get() = LocalConfigRowWrapperPadding.current

    val valuePollInterval: Duration
        @Composable @ReadOnlyComposable get() = LocalConfigPollInterval.current

    val entrySize: DpSize
        @Composable @ReadOnlyComposable get() = LocalConfigEntrySize.current

    val spacing: Dp
        @Composable @ReadOnlyComposable get() = LocalConfigRowSpacing.current

    val iconAnimationDuration: Duration
        @Composable @ReadOnlyComposable get() = LocalConfigRowIconAnimationDuration.current

    val LocalConfigRowSpacing = staticCompositionLocalOf { 12.dp }

    val LocalConfigPollInterval = compositionLocalOf { 50.milliseconds }

    val LocalConfigRowWrapperPadding = staticCompositionLocalOf {
        PaddingValues(12.dp, 8.dp)
    }

    val LocalConfigEntrySize = staticCompositionLocalOf { DpSize(320.dp, 64.dp) }

    val LocalConfigRowIconAnimationDuration = compositionLocalOf { 400.milliseconds }

}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConfigRowWrapper(
    config: ConfigNode,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    resettable: Boolean = true,
    onClick: (() -> Unit)? = null,
    onReset: () -> Unit = {},
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val hoveredColor = (if (isHovered) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f) else Color.Transparent)
    Row(
        modifier = modifier
            .hoverable(interactionSource)
            .background(hoveredColor)
            .onClick { onClick?.invoke() }
            .fillMaxWidth()
            .padding(ConfigRowWrapper.padding),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
    ) {
        ConfigName(config, modifier = Modifier.weight(1f, false))
        Row(
            horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content()
            if (resettable) ResetButton(config, onReset = { onReset() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigName(
    config: ConfigNode,
    modifier: Modifier = Modifier
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = {
            PlainTooltip {
                Text(config.translateComment.plainText)
            }
        },
        state = rememberTooltipState(),
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = config.translateText.plainText,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = config.translateComment.plainText,
                modifier = Modifier.widthIn(max = 512.dp),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Resettable> ResetButton(
    resettable: T,
    onReset: ((T) -> Unit)? = null,
) {
    var state by remember { mutableStateOf(resettable.isDefault()) }
    val interval = ConfigRowWrapper.valuePollInterval

    LaunchedEffect(resettable) {
        while (isActive) {
            state = resettable.isDefault()
            delay(interval)
        }
    }

    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val duration = ConfigRowWrapper.iconAnimationDuration.inWholeMilliseconds.toInt()

    IconButton(
        onClick = {
            resettable.resetDefault()
            onReset?.invoke(resettable)
            scope.launch {
                rotation.animateTo(
                    targetValue = rotation.value - 360f,
                    animationSpec = tween(durationMillis = duration)
                )
            }
        },
        modifier = Modifier.size(32.dp),
        enabled = !state
    ) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
            tooltip = {
                PlainTooltip {
                    Text(IGLang.Misc.reset.plainText)
                }
            },
            state = rememberTooltipState(),
        ) {
            Icon(
                Icons.Replay,
                contentDescription = IGLang.Misc.reset.plainText,
                modifier = Modifier.rotate(rotation.value)
            )
        }
    }
}
