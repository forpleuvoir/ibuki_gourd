@file:Suppress("NOTHING_TO_INLINE")

package moe.forpleuvoir.ibukigourd.ui.preset.modifier

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import moe.forpleuvoir.ibukigourd.ui.util.toComposeColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.world.level.block.state.properties.DripstoneThickness
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


fun Modifier.debug(color: Color = Colors.RED, thickness: Float = 2f) = this.drawBehind {
    drawLine(color.toComposeColor, Offset.Zero, Offset(size.width, 0f), thickness)
    drawLine(color.toComposeColor, Offset(size.width, 0f), Offset(size.width, size.height), thickness)
    drawLine(color.toComposeColor, Offset(0f, size.height), Offset(size.width, size.height), thickness)
    drawLine(color.toComposeColor, Offset.Zero, Offset(0f, size.height), thickness)
}