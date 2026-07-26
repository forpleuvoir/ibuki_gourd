@file:Suppress("NOTHING_TO_INLINE")

package moe.forpleuvoir.ibukigourd.ui.preset.modifier

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.util.toComposeColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import androidx.compose.ui.graphics.Color as ComposeColor

/**
 * @see androidx.compose.foundation.background
 */
@Stable
inline fun Modifier.background(color: Color, shape: Shape = RectangleShape): Modifier =
    background(
        color = color.toComposeColor,
        shape = shape
    )


@Composable
fun Modifier.hoverBackground(
    hoverColor: ComposeColor,
    defaultColor: ComposeColor,
    shape: Shape = RectangleShape
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val color by animateColorAsState(
        (if (isHovered) hoverColor else defaultColor),
        label = "hoverBackground"
    )
    return this
        .hoverable(interactionSource)
        .background(color, shape)
}


fun Modifier.debug(color: Color = Colors.RED, width: Dp = 2.dp, shape: Shape = RectangleShape) = this.border(width, color.toComposeColor, shape)