package com.forpleuvoir.ibukigourd.gui.tip

import com.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
import com.forpleuvoir.ibukigourd.gui.base.layout.Layout
import com.forpleuvoir.ibukigourd.gui.base.layout.LinearLayout
import com.forpleuvoir.ibukigourd.render.base.PlanarAlignment

abstract class Tip : AbstractElement() {

	override var fixed: Boolean = true

	override var layout: Layout = LinearLayout({ this }, alignment = PlanarAlignment::CenterLeft)

	fun postToRenderList() {
		screen.tipList.add(this)
	}

	fun removeFromRenderList() {
		screen.tipList.remove(this)
	}

}