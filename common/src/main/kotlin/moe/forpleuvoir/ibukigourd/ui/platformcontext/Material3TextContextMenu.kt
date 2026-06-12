package moe.forpleuvoir.ibukigourd.ui.platformcontext

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ContextMenuRepresentation
import androidx.compose.foundation.ContextMenuState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.PlatformLocalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.window.rememberPopupPositionProviderAtPosition
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.icon.default.ContentCopy
import moe.forpleuvoir.ibukigourd.ui.icon.default.ContentCut
import moe.forpleuvoir.ibukigourd.ui.icon.default.ContentPaste
import moe.forpleuvoir.ibukigourd.ui.icon.default.SelectAll

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalComposeUiApi::class)
object Material3ContextMenuRepresentation : ContextMenuRepresentation {

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
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    visible = true
                }
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(
                        initialOffsetY = { -it / 4 },
                        animationSpec = tween(200)
                    ) + fadeIn(animationSpec = tween(150)),
                ) {
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
                                        Text(item.label)
                                    },
                                    enabled = item.enabled,
                                    shape = shapes.shape,
                                    onClick = {
                                        item.onClick()
                                        state.status = ContextMenuState.Status.Closed
                                    },
                                    leadingIcon = {
                                        val icon = when (index) {
                                            0    -> Icons.ContentCut
                                            1    -> Icons.ContentCopy
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

}

object MinecraftPlatformLocalization : PlatformLocalization {
    override val copy: String
        get() = IGLang.Misc.copy.plainText
    override val cut: String
        get() = IGLang.Misc.cut.plainText
    override val paste: String
        get() = IGLang.Misc.paste.plainText
    override val selectAll: String
        get() = IGLang.Misc.selectAll.plainText
}

