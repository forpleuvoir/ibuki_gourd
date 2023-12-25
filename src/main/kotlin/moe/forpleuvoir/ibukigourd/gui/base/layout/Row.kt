package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.render.base.Alignment
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.base.PlanarAlignment
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class Row(
    width: Float? = null,
    height: Float? = null,
    padding: Padding? = null,
    margin: Margin? = null,
    alignment: (Arrangement) -> Alignment = PlanarAlignment::Center
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

    override var layout: Layout = LinearLayout({ this }, Arrangement.Horizontal, alignment)
        @Deprecated("Do not set the layout value of Row") set(@Suppress("UNUSED_PARAMETER") value) {
            throw NotImplementedError("Do not set the layout value of Row")
        }

}

@OptIn(ExperimentalContracts::class)
inline fun ElementContainer.row(
    width: Float? = null,
    height: Float? = null,
    padding: Padding? = Padding(2),
    margin: Margin? = null,
    noinline alignment: (Arrangement) -> Alignment = PlanarAlignment::Center,
    scope: Row.() -> Unit
): Row {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return addElement(Row(width, height, padding, margin, alignment).apply {
        spacing = 2f
        scope()
    })
}
