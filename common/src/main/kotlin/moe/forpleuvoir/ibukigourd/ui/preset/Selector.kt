package moe.forpleuvoir.ibukigourd.ui.preset

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.text.translateComment
import moe.forpleuvoir.ibukigourd.text.translateText

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> Selector(
    selected: T,
    onSelect: (T) -> Unit,
    items: List<T>,
    itemEquals: (T, T) -> Boolean = { a, b -> a == b },
    content: @Composable (T) -> Unit,
    labelPosition: TextFieldLabelPosition = TextFieldLabelPosition.Attached(),
    label: @Composable (() -> Unit)? = null,
    itemContent: @Composable (T, Boolean) -> Unit,
    enabled: Boolean = true,
    enabledSearch: Boolean = false,
    searchFilter: ((String, T) -> Boolean) = { _, _ -> true },
    modifier: Modifier = Modifier,
    itemLeadingIcon: ((Boolean) -> (@Composable (T) -> Unit)?)? = null,
    itemTrailingIcon: ((Boolean) -> (@Composable (T) -> Unit)?)? = null,
    textStyle: TextStyle = LocalTextStyle.current,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
    contentPadding: PaddingValues = OutlinedTextFieldDefaults.contentPadding(),
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.defaultMinSize(minWidth = 160.dp, minHeight = 46.dp)
    ) {
        val searchState = rememberTextFieldState()
        OutlinedTextField(
            state = searchState,
            readOnly = !enabledSearch,
            enabled = enabled,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            labelPosition = labelPosition,
            label = label?.let { { it() } },
            prefix = {
                if (searchState.text.isBlank()) content(selected)
            },
            textStyle = textStyle,
            interactionSource = interactionSource,
            shape = shape,
            colors = colors,
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxWidth().pointerHoverIcon(PointerIcon.Default, !enabledSearch)
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled)
        )
        ExposedDropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            items.filter { searchFilter(searchState.text.toString(), it) }.forEach { item ->
                val isSelected = itemEquals(selected, item)
                DropdownMenuItem(
                    modifier = if (isSelected) Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) else Modifier,
                    leadingIcon = itemLeadingIcon?.let { predicate ->
                        predicate(isSelected)?.let { { it.invoke(item) } }
                    },
                    text = { itemContent(item, isSelected) },
                    trailingIcon = itemTrailingIcon?.let { predicate ->
                        predicate(isSelected)?.let { { it.invoke(item) } }
                    },
                    onClick = { onSelect(item); expanded = false; searchState.clearText() },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StringSelector(
    selected: String,
    onSelect: (String) -> Unit,
    items: List<String>,
    itemEquals: (String, String) -> Boolean = { a, b -> a == b },
    content: @Composable (String) -> Unit = {
        Text(it)
    },
    labelPosition: TextFieldLabelPosition = TextFieldLabelPosition.Attached(),
    label: @Composable (() -> Unit)? = null,
    itemContent: @Composable (String, Boolean) -> Unit = { item, _ ->
        Text(item)
    },
    enabled: Boolean = true,
    enabledSearch: Boolean = false,
    searchFilter: ((String, String) -> Boolean) = { _, _ -> true },
    modifier: Modifier = Modifier,
    itemLeadingIcon: ((Boolean) -> (@Composable (String) -> Unit)?)? = null,
    itemTrailingIcon: ((Boolean) -> (@Composable (String) -> Unit)?)? = null,
    textStyle: TextStyle = LocalTextStyle.current,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
    contentPadding: PaddingValues = OutlinedTextFieldDefaults.contentPadding(),
) = Selector(
    selected = selected,
    onSelect = onSelect,
    items = items,
    itemEquals = itemEquals,
    content = content,
    labelPosition = labelPosition,
    label = label,
    itemContent = itemContent,
    enabled = enabled,
    enabledSearch = enabledSearch,
    searchFilter = searchFilter,
    modifier = modifier,
    itemLeadingIcon = itemLeadingIcon,
    itemTrailingIcon = itemTrailingIcon,
    textStyle = textStyle,
    interactionSource = interactionSource,
    shape = shape,
    colors = colors,
    contentPadding = contentPadding
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun <E : Enum<E>> EnumSelector(
    selected: E,
    onSelect: (E) -> Unit,
    items: List<E> = selected::class.java.enumConstants.toList(),
    itemEquals: (E, E) -> Boolean = { a, b -> a == b },
    content: @Composable (E) -> Unit = {
        Text(it.translateText)
    },
    labelPosition: TextFieldLabelPosition = TextFieldLabelPosition.Attached(),
    label: @Composable (() -> Unit)? = null,
    itemContent: @Composable (E, Boolean) -> Unit = { item, _ ->
        TipBox({
            Text(item.translateComment)
        }) {
            Text(item.translateText)
        }
    },
    enabled: Boolean = true,
    enabledSearch: Boolean = false,
    searchFilter: ((String, E) -> Boolean) = { _, _ -> true },
    modifier: Modifier = Modifier,
    itemLeadingIcon: ((Boolean) -> (@Composable (E) -> Unit)?)? = null,
    itemTrailingIcon: ((Boolean) -> (@Composable (E) -> Unit)?)? = null,
    textStyle: TextStyle = LocalTextStyle.current,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
    contentPadding: PaddingValues = OutlinedTextFieldDefaults.contentPadding(),
) = Selector(
    selected = selected,
    onSelect = onSelect,
    items = items,
    itemEquals = itemEquals,
    content = content,
    labelPosition = labelPosition,
    label = label,
    itemContent = itemContent,
    enabled = enabled,
    enabledSearch = enabledSearch,
    searchFilter = searchFilter,
    modifier = modifier,
    itemLeadingIcon = itemLeadingIcon,
    itemTrailingIcon = itemTrailingIcon,
    textStyle = textStyle,
    interactionSource = interactionSource,
    shape = shape,
    colors = colors,
    contentPadding = contentPadding
)
