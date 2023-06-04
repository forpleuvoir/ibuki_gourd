package moe.forpleuvoir.ibukigourd.gui.tip

import moe.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layout
import moe.forpleuvoir.ibukigourd.gui.base.layout.LinearLayout
import moe.forpleuvoir.ibukigourd.render.base.PlanarAlignment

abstract class Tip(parent: () -> Element, private val tipHandler: () -> TipHandler) : AbstractElement() {

	init {
		transform.parent = { parent().transform }
	}

	final override var parent: () -> Element? = parent

	override var fixed: Boolean = true

	override var layout: Layout = LinearLayout({ this }, alignment = PlanarAlignment::CenterLeft)

	fun push(): Boolean = tipHandler().pushTip(this)


	fun pop(): Boolean = tipHandler().popTip(this)

}