package moe.forpleuvoir.ibukigourd.ui.preset

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.text.translateText
import moe.forpleuvoir.ibukigourd.ui.icon.Check
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.icon.KeyboardArrowDown
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> Selector(
    selected: T,
    items: List<T>,
    selectedLabel: @Composable RowScope.(T) -> Unit,
    itemLabel: @Composable (T, T, () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.outlinedShape,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = ButtonDefaults.outlinedButtonBorder(enabled),
    contentPadding: PaddingValues = PaddingValues(18.dp, 4.dp, 12.dp, 4.dp),
    interactionSource: MutableInteractionSource? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    var buttonWidth by remember { mutableIntStateOf(0) }

    var buttonTopPx by remember { mutableFloatStateOf(0f) }
    var buttonBottomPx by remember { mutableFloatStateOf(0f) }

    var menuWidth by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val windowHeightPx = LocalWindowInfo.current.containerSize.height.toFloat()
    Box {
        OutlinedButton(
            onClick = { expanded = !expanded },
            modifier = Modifier
                .then(modifier)
                .onSizeChanged { buttonWidth = it.width }
                .onGloballyPositioned {
                    buttonTopPx = it.positionInWindow().y
                    buttonBottomPx = it.positionInWindow().y + it.size.height
                },
            shape = shape,
            colors = colors,
            elevation = elevation,
            border = border,
            contentPadding = contentPadding,
            interactionSource = interactionSource,
        ) {
            selectedLabel(selected)
            val rotation = remember { Animatable(0f) }
            LaunchedEffect(expanded) {
                rotation.animateTo(if (expanded) 180f else 0f, tween(200))
            }
            Icon(Icons.KeyboardArrowDown, null, modifier = Modifier.rotate(rotation.value))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .heightIn(max = with(density) {
                    ((buttonTopPx.coerceAtLeast(windowHeightPx - buttonBottomPx)).toDp() - 50.dp).coerceAtLeast(0.dp)
                })
                .onSizeChanged { menuWidth = it.width }
        ) {
            items.forEach { t ->
                itemLabel(selected, t) { expanded = false }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StringSelector(
    selected: String,
    onSelect: (String) -> Unit,
    options: List<String>,
    selectedLabel: @Composable RowScope.(String) -> Unit = {
        Text(it, modifier = Modifier)
    },
    itemLabel: @Composable (String, String, () -> Unit) -> Unit = { selected, item, close ->
        DropdownMenuItem(
            selected = item == selected,
            onClick = { onSelect(item); close() },
            text = { Text(item) },
            shapes = MenuDefaults.itemShapes(),
            selectedLeadingIcon = { Icon(Icons.Check, null) },
            interactionSource = remember { MutableInteractionSource() }
        )
    },
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.outlinedShape,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = ButtonDefaults.outlinedButtonBorder(enabled),
    contentPadding: PaddingValues = PaddingValues(18.dp, 4.dp, 12.dp, 4.dp),
    interactionSource: MutableInteractionSource? = null
) {
    Selector(
        selected = selected,
        items = options,
        selectedLabel = selectedLabel,
        itemLabel = itemLabel,
        modifier = modifier,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <E : Enum<E>> EnumSelector(
    selected: E,
    onSelect: (E) -> Unit,
    items: List<E> = selected::class.java.enumConstants.toList(),
    selectedLabel: @Composable RowScope.(E) -> Unit = {
        Text(it.translateText, modifier = Modifier)
    },
    itemLabel: @Composable (E, E, () -> Unit) -> Unit = { selected, item, close ->
        DropdownMenuItem(
            selected = item == selected,
            onClick = { onSelect(item); close() },
            text = { Text(item.translateText) },
            shapes = MenuDefaults.itemShapes(),
            selectedLeadingIcon = { Icon(Icons.Check, null) },
            interactionSource = remember { MutableInteractionSource() }
        )
    },
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.outlinedShape,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = ButtonDefaults.outlinedButtonBorder(enabled),
    contentPadding: PaddingValues = PaddingValues(18.dp, 4.dp, 12.dp, 4.dp),
    interactionSource: MutableInteractionSource? = null
) {
    Selector(
        selected = selected,
        items = items,
        selectedLabel = selectedLabel,
        itemLabel = itemLabel,
        modifier = modifier,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource
    )
}
