package moe.forpleuvoir.ibukigourd.ui.platformcontext

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.window.rememberPopupPositionProviderAtPosition
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.icon.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalComposeUiApi::class)
val Material3ContextMenuRepresentation = object : ContextMenuRepresentation {

    @Composable
    override fun Representation(
        state: ContextMenuState,
        items: () -> List<ContextMenuItem>
    ) {
        val status = state.status
        if (status is ContextMenuState.Status.Open) {
            var focusManager: FocusManager? by remember { mutableStateOf(null) }
            var inputModeManager: InputModeManager? by remember { mutableStateOf(null) }
            Popup(
                properties = PopupProperties(focusable = true),
                onDismissRequest = { state.status = ContextMenuState.Status.Closed },
                popupPositionProvider = rememberPopupPositionProviderAtPosition(status.rect.center),
                onKeyEvent = {
                    if (it.type == KeyEventType.KeyDown) {
                        when (it.key) {
                            Key.DirectionDown, Key.NumPadDirectionUp -> {
                                inputModeManager?.requestInputMode(InputMode.Keyboard)
                                focusManager?.moveFocus(FocusDirection.Next)
                                true
                            }

                            Key.DirectionUp, Key.NumPadDirectionDown -> {
                                inputModeManager?.requestInputMode(InputMode.Keyboard)
                                focusManager?.moveFocus(FocusDirection.Previous)
                                true
                            }

                            else                                     -> false
                        }
                    } else {
                        false
                    }
                },
            ) {
                focusManager = LocalFocusManager.current
                inputModeManager = LocalInputModeManager.current
                Surface(
                    shape = MenuDefaults.groupShape(0, 1).shape,
                    color = MenuDefaults.groupStandardContainerColor,
                    tonalElevation = MenuDefaults.TonalElevation,
                    shadowElevation = MenuDefaults.ShadowElevation,
                ) {
                    Column(
                        modifier = Modifier.padding(0.dp, 4.dp)
                            .width(IntrinsicSize.Max)
                    ) {
                        items().forEachIndexed { index, item ->
                            val shapes = MenuDefaults.itemShape(index, items().size)
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        when (index) {
                                            0    -> IGLang.Misc.copy.plainText
                                            1    -> IGLang.Misc.cut.plainText
                                            2    -> IGLang.Misc.paste.plainText
                                            3    -> IGLang.Misc.selectAll.plainText
                                            else -> item.label
                                        }
                                    )
                                },
                                enabled = item.enabled,
                                shape = shapes.shape,
                                onClick = {
                                    item.onClick()
                                    state.status = ContextMenuState.Status.Closed
                                },
                                leadingIcon = {
                                    val icon = when (index) {
                                        0    -> Icons.ContentCopy
                                        1    -> Icons.ContentCut
                                        2    -> Icons.ContentPaste
                                        3    -> Icons.SelectAll
                                        else -> null
                                    }
                                    icon?.let { Icon(it, contentDescription = null) }
                                },
                            )
                        }
                    }
                }
            }
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompositionTextContextProvider(
    content: @Composable () -> Unit
) = CompositionLocalProvider(
    LocalClipboard provides MinecraftClipboard,
    LocalContextMenuRepresentation provides Material3ContextMenuRepresentation,
    content = content
)