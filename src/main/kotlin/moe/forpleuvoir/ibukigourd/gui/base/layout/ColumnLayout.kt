package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.render.base.arrange.Alignment
import moe.forpleuvoir.ibukigourd.render.base.arrange.Orientation
import moe.forpleuvoir.ibukigourd.render.base.arrange.PlanarAlignment
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class ColumnLayout(
    width: Float? = null,
    height: Float? = null,
    padding: Padding? = null,
    margin: Margin? = null,
    alignment: (Orientation) -> Alignment = PlanarAlignment::Center
) : AbstractElement() {

    init {
        width?.let {
            transform.fixedWidth = true
            transform.width = it
        }
        height?.let {
            transform.fixedHeight = true
            transform.height = it
        }
        padding?.let(::padding)
        margin?.let(::margin)
    }

    override val layout: Layout = LinearLayout({ this }, Orientation.Vertical, alignment)

}


@OptIn(ExperimentalContracts::class)
inline fun ElementContainer.column(
    width: Float? = null,
    height: Float? = null,
    padding: Padding? = Padding(2),
    margin: Margin? = null,
    spacing: Float = 2f,
    noinline alignment: (Orientation) -> Alignment = PlanarAlignment::Center,
    scope: ColumnLayout.() -> Unit
): ColumnLayout {
    contract {
        callsInPlace(scope, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }
    return addElement(Column(width, height, padding, margin, spacing, alignment, scope))
}

@OptIn(ExperimentalContracts::class)
inline fun Column(
    width: Float? = null,
    height: Float? = null,
    padding: Padding? = Padding(2),
    margin: Margin? = null,
    spacing: Float = 2f,
    noinline alignment: (Orientation) -> Alignment = PlanarAlignment::Center,
    scope: ColumnLayout.() -> Unit
): ColumnLayout {
    contract {
        callsInPlace(scope, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }
    return ColumnLayout(width, height, padding, margin, alignment).apply {
        this.spacing = spacing
        scope()
    }
}
