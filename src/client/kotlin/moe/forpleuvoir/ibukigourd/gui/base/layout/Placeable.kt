package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.render.Size

interface Placeable {

    val margin: Margin

    val size: Size<Float>

    fun placeAt(x: Float, y: Float, isWorldAxis: Boolean = false)


}