package moe.forpleuvoir.ibukigourd.ui.preset

import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.icon.default.DragHandle
import sh.calvin.reorderable.ReorderableCollectionItemScope

@Composable
fun ReorderableCollectionItemScope.DragHandle(
    hapticFeedback: HapticFeedback,
    handleInteraction: MutableInteractionSource,
    handleHovered: Boolean,
    isDragging: Boolean
) {
    Box(
        Modifier
            .draggableHandle(
                onDragStarted = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                },
                onDragStopped = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                },
            )
            .hoverable(handleInteraction)
            .pointerHoverIcon(PointerIcon.Hand)
            .background(
                if (handleHovered || isDragging) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                CircleShape,
            ).padding(4.dp)
    ) {
        Icon(Icons.DragHandle, contentDescription = null)
    }
}
