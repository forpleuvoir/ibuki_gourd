package moe.forpleuvoir.ibukigourd.gui.base.modifier.widget

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget

fun interface WidgetModifier : Modifier.Element {

    companion object {

        fun IGWidget.tryApplyModify(modifier: Modifier) {
            if (modifier is WidgetModifier) {
                modifier.applyModifier(this)
            }
        }

    }

    fun applyModifier(widget: IGWidget)

}

//------------ Size ------------\\

/**
 * Sets the size of the widget's constraints.
 *
 * @param width The width of the widget. If null, the width will be unchanged.
 * @param height The height of the widget. If null, the height will be unchanged.
 *
 * @return The modified Modifier.
 */
fun Modifier.size(width: Float?, height: Float?) = this then WidgetModifier { widget ->
    widget.constraints = Constraints(
        width ?: widget.constraints.minHeight,
        width ?: widget.constraints.maxWidth,
        height ?: widget.constraints.minHeight,
        height ?: widget.constraints.maxHeight
    )
}

/**
 * sets the width of the widget's constraints..
 *
 * @param width the width value to set
 * @return The modified modifier.
 */
fun Modifier.width(width: Float) = this then WidgetModifier { widget ->
    widget.constraints = widget.constraints.copy(minWidth = width, maxWidth = width)
}

/**
 * sets the height of the widget's constraints..
 *
 * @param height the height value to set
 * @return The modified modifier.
 */
fun Modifier.height(height: Float) = this then WidgetModifier { widget ->
    widget.constraints = widget.constraints.copy(minHeight = height, maxHeight = height)
}

//------------ Padding ------------\\

/**
 * Adds padding to the widget using the specified [padding].
 *
 * @param padding The padding to apply to the widget.
 * @return The modified [Modifier].
 */
fun Modifier.padding(padding: Padding) = this then WidgetModifier { widget ->
    widget.padding = padding
}

/**
 * Adds a margin to the widget.
 *
 * @param margin The margin to apply to the widget.
 * @return The modified Modifier object.
 */
fun Modifier.margin(margin: Margin) = this then WidgetModifier { widget ->
    widget.margin = margin
}