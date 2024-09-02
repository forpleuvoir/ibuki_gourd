package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import org.joml.Vector2fc

interface Placeable {

    val margin: Margin

    val size: Size<Float>

    /**
     * [size] with [margin]
     */
    val wrappedSize: Size<Float>
        get() = Size(wrappedWidth, wrappedHeight)

    val wrappedWidth: Float
        get() = this.size.width + margin.width

    val wrappedHeight: Float
        get() = this.size.height + margin.height

    fun placeAt(x: Float, y: Float, isWorldAxis: Boolean = false)

    fun placeAt(vec2f: Vector2fc, isWorldAxis: Boolean = false) {
        placeAt(vec2f.x(), vec2f.y(), isWorldAxis)
    }

    var placeCompletion: () -> Unit

    fun onPlaceCompletion() {}

}