package moe.forpleuvoir.ibukigourd.ui.preset

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
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
    itemContent: @Composable (T, Boolean) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    itemLeadingIcon: ((Boolean) -> (@Composable (T) -> Unit)?)? = null,
    itemTrailingIcon: ((Boolean) -> (@Composable (T) -> Unit)?)? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.defaultMinSize(minWidth = 160.dp, minHeight = 46.dp)
    ) {
        Box(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.fillMaxWidth().pointerHoverIcon(PointerIcon.Default, true)
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled)
            )
            Row(
                Modifier.matchParentSize().padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                content(selected)
            }
        }
        ExposedDropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
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
                    onClick = { onSelect(item); expanded = false },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StringSelector(
    selected: String,
    onSelect: (String) -> Unit,
    items: List<String>,
    itemEquals: (String, String) -> Boolean = { a, b -> a == b },
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable (String) -> Unit = {
        Text(it)
    },
    itemContent: @Composable (String, Boolean) -> Unit = { item, _ ->
        Text(item)
    },
    itemLeadingIcon: ((Boolean) -> (@Composable (String) -> Unit)?)? = null,
    itemTrailingIcon: ((Boolean) -> (@Composable (String) -> Unit)?)? = null,
) = Selector(
    selected = selected,
    onSelect = onSelect,
    items = items,
    itemEquals = itemEquals,
    enabled = enabled,
    modifier = modifier,
    content = content,
    itemContent = itemContent,
    itemLeadingIcon = itemLeadingIcon,
    itemTrailingIcon = itemTrailingIcon
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <E : Enum<E>> EnumSelector(
    selected: E,
    onSelect: (E) -> Unit,
    items: List<E> = selected::class.java.enumConstants.toList(),
    itemEquals: (E, E) -> Boolean = { a, b -> a == b },
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable (E) -> Unit = {
        Text(it.translateText)
    },
    itemContent: @Composable (E, Boolean) -> Unit = { item, _ ->
        TipBox({
            Text(item.translateComment)
        }) {
            Text(item.translateText)
        }
    },
    itemLeadingIcon: ((Boolean) -> (@Composable (E) -> Unit)?)? = null,
    itemTrailingIcon: ((Boolean) -> (@Composable (E) -> Unit)?)? = null,
) = Selector(
    selected = selected,
    onSelect = onSelect,
    items = items,
    itemEquals = itemEquals,
    enabled = enabled,
    modifier = modifier,
    content = content,
    itemContent = itemContent,
    itemLeadingIcon = itemLeadingIcon,
    itemTrailingIcon = itemTrailingIcon
)
