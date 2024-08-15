package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.element.DrawableElement
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layoutable
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.input.MouseCursor
import moe.forpleuvoir.nebula.common.util.primitive.pick
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

interface IGWidget : DrawableElement, Layoutable {

    val transform: Transform

    var padding: Padding

    /**
     * 鼠标是否在组件中
     */
    val wasMouseOver: Boolean

    val mouseOverCursor: MouseCursor.Cursor

    /**
     * 组件是否在拖动中
     */
    val wasDragging: Boolean

    val contentWidth: Float get() = transform.width - padding.width

    val contentHeight: Float get() = transform.height - padding.height

    val contentSize: Size<Float> get() = Size(contentWidth, contentHeight)

    fun contentLeft(isWorldAxis: Boolean) =
        isWorldAxis.pick(transform.worldLeft, 0f) + padding.left

    fun contentRight(isWorldAxis: Boolean) =
        isWorldAxis.pick(transform.worldRight, 0f) + padding.right

    fun contentTop(isWorldAxis: Boolean) =
        isWorldAxis.pick(transform.worldTop, 0f) + padding.top

    fun contentBottom(isWorldAxis: Boolean) =
        isWorldAxis.pick(transform.worldBottom, 0f) + padding.bottom

    fun contentBox(isWorldAxis: Boolean): Box =
        Box(x = contentLeft(isWorldAxis), y = contentTop(isWorldAxis), width = contentWidth, height = contentHeight)


    //------------ Placeable ------------\\

    override val size: Size<Float>
        get() = transform

    override var margin: Margin

    override fun placeAt(x: Float, y: Float, isWorldAxis: Boolean) {
        if (isWorldAxis) {
            transform.worldX = x
            transform.worldY = y
        } else {
            transform.x = x
            transform.y = y
        }
        placeCompleted()
    }

}

@OptIn(ExperimentalContracts::class)
inline fun IGWidget.wasMouseOver(block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    if (wasMouseOver) block()
}