package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.element.GuiRenderableElement
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layoutable
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.nebula.common.util.primitive.either
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

interface GuiWidget : GuiRenderableElement, Layoutable {

    val transform: Transform

    var padding: Padding

    /**
     * 鼠标是否在组件中
     */
    val wasMouseOver: Boolean

    val wasMouseOverContent: Boolean

    /**
     * 组件是否在拖动中
     */
    val wasDragging: Boolean

    val interactableBox: Box

    val interactableContentBox: Box

    val contentWidth: Float get() = transform.width - padding.width

    val contentHeight: Float get() = transform.height - padding.height

    val contentSize: Size<Float> get() = Size(contentWidth, contentHeight)

    fun contentLeft(worldCoordinatesMode: Boolean) =
        worldCoordinatesMode.either(transform.worldLeft, 0f) + padding.left

    fun contentRight(worldCoordinatesMode: Boolean) =
        worldCoordinatesMode.either(transform.worldRight, 0f) + padding.right

    fun contentTop(worldCoordinatesMode: Boolean) =
        worldCoordinatesMode.either(transform.worldTop, 0f) + padding.top

    fun contentBottom(worldCoordinatesMode: Boolean) =
        worldCoordinatesMode.either(transform.worldBottom, 0f) + padding.bottom

    fun contentBox(worldCoordinatesMode: Boolean): Box =
        Box(x = contentLeft(worldCoordinatesMode), y = contentTop(worldCoordinatesMode), width = contentWidth, height = contentHeight)


    //------------ Placeable ------------\\

    override val size: Size<Float>
        get() = transform

    override var margin: Margin

    override fun placeAt(x: Float, y: Float, worldCoordinatesMode: Boolean) {
        if (worldCoordinatesMode) {
            transform.worldX = x
            transform.worldY = y
        } else {
            transform.x = x
            transform.y = y
        }
        placeCompletion()
    }

}

@OptIn(ExperimentalContracts::class)
inline fun GuiWidget.wasMouseOver(block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    if (wasMouseOver) block()
}