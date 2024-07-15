package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.render.Size

interface Placeable : Size<Float> {

    fun placeAt(x: Float, y: Float, isWorldAxis: Boolean = false)


}