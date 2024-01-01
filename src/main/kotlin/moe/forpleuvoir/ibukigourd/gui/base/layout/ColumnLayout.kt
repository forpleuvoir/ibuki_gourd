package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.render.base.Alignment
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.base.PlanarAlignment
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class ColumnLayout(
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

    override var layout: Layout = LinearLayout({ this }, Arrangement.Vertical, alignment)
        @Deprecated("Do not set the layout value of Row") set(@Suppress("UNUSED_PARAMETER") value) {
            throw NotImplementedError("Do not set the layout value of Row")
        }

}


@OptIn(ExperimentalContracts::class)
inline fun ElementContainer.column(
    width: Float? = null,
    height: Float? = null,
    padding: Padding? = Padding(2),
    margin: Margin? = null,
    spacing: Float = 2f,
    noinline alignment: (Arrangement) -> Alignment = PlanarAlignment::Center,
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
    noinline alignment: (Arrangement) -> Alignment = PlanarAlignment::Center,
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
