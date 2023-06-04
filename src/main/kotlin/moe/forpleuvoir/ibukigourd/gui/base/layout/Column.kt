package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.render.base.Alignment
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.base.PlanarAlignment

class Column(alignment: (Arrangement) -> Alignment = PlanarAlignment::Center) : AbstractElement() {

	override var layout: Layout = LinearLayout({ this }, Arrangement.Vertical, alignment)
		@Deprecated("Do not set the layout value of Row") set(@Suppress("UNUSED_PARAMETER") value) {
			throw NotImplementedError("Do not set the layout value of Row")
		}

}

fun ElementContainer.column(alignment: (Arrangement) -> Alignment = PlanarAlignment::Center, scope: Column.() -> Unit): Column =
	addElement(Column(alignment).apply(scope))
