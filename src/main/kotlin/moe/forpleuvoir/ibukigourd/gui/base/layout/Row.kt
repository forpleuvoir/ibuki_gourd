package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.render.base.Alignment
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.base.PlanarAlignment

class Row(alignment: (Arrangement) -> Alignment = PlanarAlignment::Center) : AbstractElement() {

	override var layout: Layout = LinearLayout({ this }, Arrangement.Horizontal, alignment)
		@Deprecated("Do not set the layout value of Row") set(@Suppress("UNUSED_PARAMETER") value) {
			throw NotImplementedError("Do not set the layout value of Row")
		}

}

fun ElementContainer.row(alignment: (Arrangement) -> Alignment = PlanarAlignment::Center, scope: Row.() -> Unit): Row =
	addElement(Row(alignment).apply(scope))
