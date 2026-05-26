@file:Suppress("NOTHING_TO_INLINE")

package moe.forpleuvoir.ibukigourd.ui.preset.modifier

import androidx.compose.foundation.background
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import moe.forpleuvoir.ibukigourd.ui.util.toComposeColor
import moe.forpleuvoir.nebula.common.color.Color

/**
 * @see androidx.compose.foundation.background
 */
@Stable
inline fun Modifier.background(color: Color, shape: Shape = RectangleShape): Modifier =
    background(
        color = color.toComposeColor,
        shape = shape
    )
