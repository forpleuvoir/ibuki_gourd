@file:Suppress("NOTHING_TO_INLINE")

package moe.forpleuvoir.ibukigourd.ui.preset.modifier

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tooltipped(
    tooltip: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    state: TooltipState = rememberTooltipState(isPersistent = true),
    content: @Composable () -> Unit,
) {
    var bounds by remember { mutableStateOf(IntRect.Zero) }
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.8f) }
    var showPopup by remember { mutableStateOf(false) }

    LaunchedEffect(isHovered) {
        if (isHovered) state.show() else state.dismiss()
    }

    LaunchedEffect(state.isVisible) {
        if (state.isVisible) {
            showPopup = true
            alpha.snapTo(0f)
            scale.snapTo(0.8f)
            alpha.animateTo(1f, tween(150))
            scale.animateTo(1f, tween(150))
        } else if (showPopup) {
            alpha.animateTo(0f, tween(75))
            scale.animateTo(0.8f, tween(75))
            showPopup = false
        }
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                val pos = coordinates.positionInRoot()
                val size = coordinates.size
                bounds = IntRect(
                    left = pos.x.roundToInt(),
                    top = pos.y.roundToInt(),
                    right = (pos.x + size.width).roundToInt(),
                    bottom = (pos.y + size.height).roundToInt(),
                )
            }
            .hoverable(interactionSource),
    ) {
        content()

        if (showPopup && bounds != IntRect.Zero) {
            val popupPositionProvider = remember(bounds) {
                object : PopupPositionProvider {
                    override fun calculatePosition(
                        anchorBounds: IntRect,
                        windowSize: IntSize,
                        layoutDirection: LayoutDirection,
                        popupContentSize: IntSize,
                    ): IntOffset {
                        val x = (bounds.left + (bounds.width - popupContentSize.width) / 2)
                            .coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0))
                        val y = (bounds.top - popupContentSize.height)
                            .coerceAtLeast(0)
                        return IntOffset(x, y)
                    }
                }
            }

            Popup(
                popupPositionProvider = popupPositionProvider,
                onDismissRequest = { state.dismiss() },
                properties = PopupProperties(focusable = false),
            ) {
                Surface(
                    modifier = Modifier.graphicsLayer {
                        this.alpha = alpha.value
                        this.scaleX = scale.value
                        this.scaleY = scale.value
                        this.transformOrigin = TransformOrigin(0.5f, 1f)
                    },
                    color = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    shape = MaterialTheme.shapes.extraSmall,
                    shadowElevation = 2.dp,
                ) {
                    ProvideTextStyle(MaterialTheme.typography.bodySmall) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = TooltipDefaults.richTooltipMaxWidth)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            tooltip()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Modifier.tooltip(
    state: TooltipState = rememberTooltipState(isPersistent = true),
    positionProvider: PopupPositionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
    tooltip: @Composable () -> Unit,
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var bounds by remember { mutableStateOf(IntRect.Zero) }

    LaunchedEffect(isHovered) {
        if (isHovered) state.show() else state.dismiss()
    }

    if (state.isVisible && bounds != IntRect.Zero) {
        val popupPositionProvider = remember(bounds) {
            object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize,
                ): IntOffset {
                    val x = (bounds.left + (bounds.width - popupContentSize.width) / 2)
                        .coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0))
                    val y = (bounds.top - popupContentSize.height)
                        .coerceAtLeast(0)
                    return IntOffset(x, y)
                }
            }
        }

        Popup(
            popupPositionProvider = popupPositionProvider,
            onDismissRequest = { state.dismiss() },
            properties = PopupProperties(focusable = false),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                shape = MaterialTheme.shapes.extraSmall,
                shadowElevation = 2.dp,
            ) {
                ProvideTextStyle(MaterialTheme.typography.bodySmall) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = TooltipDefaults.richTooltipMaxWidth)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        tooltip()
                    }
                }
            }
        }
    }

    this
        .onGloballyPositioned { coordinates ->
            val pos = coordinates.positionInRoot()
            val size = coordinates.size
            bounds = IntRect(
                left = pos.x.roundToInt(),
                top = pos.y.roundToInt(),
                right = (pos.x + size.width).roundToInt(),
                bottom = (pos.y + size.height).roundToInt(),
            )
        }
        .hoverable(interactionSource)
}
