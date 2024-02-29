package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementDimension.Companion.WRAP_CONTENT
import moe.forpleuvoir.ibukigourd.render.base.Alignment
import moe.forpleuvoir.ibukigourd.render.base.Orientation
import moe.forpleuvoir.ibukigourd.render.base.PlanarAlignment
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class RowLayout(
    width: Float = WRAP_CONTENT,
    height: Float = WRAP_CONTENT,
    padding: Padding? = null,
    margin: Margin? = null,
    alignment: (Orientation) -> Alignment = PlanarAlignment::Center
) : AbstractElement(width, height) {

    init {
        padding?.let(::padding)
        margin?.let(::margin)
    }

    override val layout: Layout = LinearLayout({ this }, Orientation.Horizontal, alignment)

}

@OptIn(ExperimentalContracts::class)
inline fun ElementContainer.row(
    width: Float = WRAP_CONTENT,
    height: Float = WRAP_CONTENT,
    padding: Padding? = Padding(2),
    margin: Margin? = null,
    spacing: Float = 2f,
    noinline alignment: (Orientation) -> Alignment = PlanarAlignment::Center,
    scope: RowLayout.() -> Unit
): RowLayout {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return addElement(Row(width, height, padding, margin, spacing, alignment, scope))
}

@OptIn(ExperimentalContracts::class)
inline fun Row(
    width: Float = WRAP_CONTENT,
    height: Float = WRAP_CONTENT,
    padding: Padding? = Padding(2),
    margin: Margin? = null,
    spacing: Float = 2f,
    noinline alignment: (Orientation) -> Alignment = PlanarAlignment::Center,
    scope: RowLayout.() -> Unit
): RowLayout {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return RowLayout(width, height, padding, margin, alignment).apply {
        this.spacing = spacing
        scope()
    }
}
