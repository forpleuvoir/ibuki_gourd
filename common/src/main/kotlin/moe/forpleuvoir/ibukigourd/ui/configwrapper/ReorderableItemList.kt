package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

private val SHIFT_ANIM = tween<Float>(150)

@Composable
fun ReorderableItemList(
    itemCount: Int,
    onMove: (from: Int, to: Int) -> Unit,
    modifier: Modifier = Modifier,
    version: Int = 0,
    content: @Composable (index: Int, dragHandleModifier: Modifier) -> Unit,
) {
    var draggedIndex by remember { mutableStateOf(-1) }
    var dragOffset by remember { mutableStateOf(0f) }
    var dragStartIndex by remember { mutableStateOf(-1) }
    val itemHeights = remember { mutableStateListOf<Int>() }

    @Suppress("UNUSED_EXPRESSION")
    version

    LaunchedEffect(itemCount) {
        val diff = itemCount - itemHeights.size
        if (diff > 0) repeat(diff) { itemHeights.add(0) }
        else if (diff < 0) repeat(-diff) { itemHeights.removeLast() }
    }

    val targetIndex = if (dragStartIndex >= 0) {
        val height = itemHeights
            .getOrElse(dragStartIndex) { itemHeights.getOrElse(draggedIndex) { 0 } }
            .toFloat()
        if (height > 0) {
            val delta = (dragOffset / height).roundToInt()
            (dragStartIndex + delta).coerceIn(0, itemCount - 1)
        } else {
            dragStartIndex
        }
    } else {
        -1
    }

    val currentViewConfig = LocalViewConfiguration.current
    val shortLongPress = remember(currentViewConfig) {
        object : ViewConfiguration by currentViewConfig {
            override val longPressTimeoutMillis: Long = 150L
        }
    }

    Column(modifier) {
        CompositionLocalProvider(LocalViewConfiguration provides shortLongPress) {
        for (i in 0 until itemCount) {
            val isDragging = draggedIndex == i

            val gapItemHeight = itemHeights
                .getOrElse(dragStartIndex) { itemHeights.getOrElse(draggedIndex) { 0 } }
                .toFloat()

            val shiftTarget = if (!isDragging && draggedIndex >= 0 && targetIndex >= 0 && targetIndex != dragStartIndex && gapItemHeight > 0) {
                when {
                    dragStartIndex < targetIndex && i in (dragStartIndex + 1)..targetIndex -> -gapItemHeight
                    dragStartIndex > targetIndex && i in targetIndex until dragStartIndex -> gapItemHeight
                    else -> 0f
                }
            } else 0f

            val animatedShift = remember { Animatable(0f) }
            LaunchedEffect(shiftTarget, draggedIndex) {
                if (draggedIndex >= 0) {
                    animatedShift.animateTo(shiftTarget, SHIFT_ANIM)
                } else {
                    animatedShift.snapTo(shiftTarget)
                }
            }

            val itemModifier = Modifier
                .onSizeChanged { size ->
                    if (i < itemHeights.size && draggedIndex < 0) {
                        itemHeights[i] = size.height
                    }
                }
                .then(
                    when {
                        isDragging -> Modifier
                            .zIndex(1f)
                            .graphicsLayer {
                                translationY = dragOffset
                                scaleX = 1.05f
                                scaleY = 1.05f
                            }
                        animatedShift.value != 0f -> Modifier.graphicsLayer { translationY = animatedShift.value }
                        else -> Modifier
                    }
                )

            val dragModifier = Modifier.pointerInput(i) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        draggedIndex = i
                        dragStartIndex = i
                        dragOffset = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount.y
                    },
                    onDragEnd = {
                        val from = dragStartIndex
                        draggedIndex = -1
                        if (from >= 0) {
                            val height = itemHeights
                                .getOrElse(from) { itemHeights.getOrElse(i) { 0 } }
                                .toFloat()
                            if (height > 0) {
                                val delta = (dragOffset / height).roundToInt()
                                val to = (from + delta).coerceIn(0, itemCount - 1)
                                if (from != to) {
                                    onMove(from, to)
                                }
                            }
                        }
                        dragOffset = 0f
                        dragStartIndex = -1
                    },
                    onDragCancel = {
                        draggedIndex = -1
                        dragOffset = 0f
                        dragStartIndex = -1
                    }
                )
            }

            key(i, version) {
                Box(itemModifier) {
                    content(i, dragModifier)
                }
            }
            }
        }
    }
}
