package com.forpleuvoir.ibukigourd.gui.base.layout

import com.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
import com.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import com.forpleuvoir.ibukigourd.render.base.Alignment
import com.forpleuvoir.ibukigourd.render.base.Arrangement
import com.forpleuvoir.ibukigourd.render.base.PlanarAlignment

class Row(alignment: (Arrangement) -> Alignment = PlanarAlignment::Center) : AbstractElement() {

	override var layout: Layout = LinearLayout({ this }, Arrangement.Horizontal, alignment)
		@Deprecated("Do not set the layout value of Row") set(@Suppress("UNUSED_PARAMETER") value) {
			throw NotImplementedError("Do not set the layout value of Row")
		}

}

fun ElementContainer.row(alignment: (Arrangement) -> Alignment = PlanarAlignment::Center, scope: Row.() -> Unit): Row =
	addElement(Row(alignment).apply(scope))
