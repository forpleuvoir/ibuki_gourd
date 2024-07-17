package moe.forpleuvoir.ibukigourd.gui.base.modifier.widget

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget

fun interface WidgetModifier : Modifier.Element {
    fun applyModify(element: IGWidget)

    override fun tryApplyModify(target: Any) {
        if (target is IGWidget) applyModify(target)
    }

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

fun Modifier.minSize(minWidth: Float?, minHeight: Float?) = this then WidgetModifier { widget ->
    widget.constraints = widget.constraints.copy(
        minWidth = minWidth ?: widget.constraints.minWidth,
        minHeight = minHeight ?: widget.constraints.minHeight
    )
}

fun Modifier.maxSize(maxWidth: Float?, maxHeight: Float?) = this then WidgetModifier { widget ->
    widget.constraints = widget.constraints.copy(
        maxWidth = maxWidth ?: widget.constraints.maxWidth,
        maxHeight = maxHeight ?: widget.constraints.maxHeight
    )
}

/**
 * sets the width of the widget's constraints.
 *
 * @param width the width value to set
 * @return The modified modifier.
 */
fun Modifier.width(width: Float) = this then WidgetModifier { widget ->
    widget.constraints = widget.constraints.copy(minWidth = width, maxWidth = width)
}

fun Modifier.minWidth(minWidth: Float) = this then WidgetModifier { widget ->
    widget.constraints = widget.constraints.copy(minWidth = minWidth)
}

fun Modifier.maxWidth(maxWidth: Float) = this then WidgetModifier { widget ->
    widget.constraints = widget.constraints.copy(maxWidth = maxWidth)
}


/**
 * sets the height of the widget's constraints.
 *
 * @param height the height value to set
 * @return The modified modifier.
 */
fun Modifier.height(height: Float) = this then WidgetModifier { widget ->
    widget.constraints = widget.constraints.copy(minHeight = height, maxHeight = height)
}

fun Modifier.minHeight(minHeight: Float) = this then WidgetModifier { widget ->
    widget.constraints = widget.constraints.copy(minHeight = minHeight)
}

fun Modifier.maxHeight(maxHeight: Float) = this then WidgetModifier { widget ->
    widget.constraints = widget.constraints.copy(maxHeight = maxHeight)
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

//------------ Drawable ------------\\

//fun Modifier.renderBackground(action: IGWidget.(IGDrawContext, Float, Float, Float) -> Unit) = this then WidgetModifier { widget ->
//    widget.renderBackground = { context, mouseX, mouseY, delta ->
//        widget.action(context, mouseX, mouseY, delta)
//    }
//}
//
//fun Modifier.render(action: IGWidget.(IGDrawContext, Float, Float, Float) -> Unit) = this then WidgetModifier { widget ->
//    widget.render = { context, mouseX, mouseY, delta ->
//        widget.action(context, mouseX, mouseY, delta)
//    }
//}
//
//fun Modifier.renderOverlay(action: IGWidget.(IGDrawContext, Float, Float, Float) -> Unit) = this then WidgetModifier { widget ->
//    widget.renderOverlay = { context, mouseX, mouseY, delta ->
//        widget.action(context, mouseX, mouseY, delta)
//    }
//}