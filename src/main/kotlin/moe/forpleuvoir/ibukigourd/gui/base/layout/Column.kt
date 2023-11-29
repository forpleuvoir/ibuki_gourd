package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.render.base.Alignment
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.base.PlanarAlignment
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class Column(alignment: (Arrangement) -> Alignment = PlanarAlignment::Center) : AbstractElement() {

    override var layout: Layout = LinearLayout({ this }, Arrangement.Vertical, alignment)
        @Deprecated("Do not set the layout value of Row") set(@Suppress("UNUSED_PARAMETER") value) {
            throw NotImplementedError("Do not set the layout value of Row")
        }

}


@OptIn(ExperimentalContracts::class)
fun ElementContainer.column(alignment: (Arrangement) -> Alignment = PlanarAlignment::Center, scope: Column.() -> Unit): Column {
    contract {
        callsInPlace(scope, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }
    return addElement(Column(alignment).apply(scope))
}
