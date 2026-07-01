package moe.forpleuvoir.ibukigourd.ui.preset

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider

@Composable
fun TipBox(
    tooltip: @Composable TooltipScope.() -> Unit,
    positionProvider: PopupPositionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
    maxWidth: Dp = 600.dp,
    state: TooltipState = rememberTooltipState(),
    modifier: Modifier = Modifier,
    onDismissRequest: (() -> Unit)? = null,
    focusable: Boolean = false,
    enableUserInput: Boolean = true,
    hasAction: Boolean = false,
    content: @Composable () -> Unit,
) = TooltipBox(
    positionProvider = positionProvider,
    tooltip = { PlainTooltip(maxWidth = maxWidth) { tooltip() } },
    state = state,
    modifier = modifier,
    onDismissRequest = onDismissRequest,
    focusable = focusable,
    enableUserInput = enableUserInput,
    hasAction = hasAction,
    content = content,
)