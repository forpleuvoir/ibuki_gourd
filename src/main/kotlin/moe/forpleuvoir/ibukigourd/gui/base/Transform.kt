@file:Suppress("unused", "MemberVisibilityCanBePrivate")
@file:OptIn(ExperimentalContracts::class)

package moe.forpleuvoir.ibukigourd.gui.base

import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.input.MousePosition
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3f
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex
import moe.forpleuvoir.ibukigourd.render.graphics.rectangle.Rectangle
import moe.forpleuvoir.ibukigourd.render.graphics.rectangle.rect
import moe.forpleuvoir.nebula.common.ifc
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * 变换青春版
 */
class Transform(
    override val position: Vector3f = Vector3f(),
    /**
     * 是否为世界坐标轴
     */
    isWorldAxis: Boolean = false,
    width: Float = 0.0f,
    height: Float = 0.0f,
    var parent: () -> Transform? = { null },
) : Rectangle<Vector3f> {

    override var width: Float = width
        set(value) {
            if (value != field) {
                resizeCallback?.invoke(value, height)
            }
            field = value
        }

    override var height: Float = height
        set(value) {
            if (value != field) {
                resizeCallback?.invoke(width, value)
            }
            field = value
        }

    var resizeCallback: ((width: Float, height: Float) -> Unit)? = null

    var positionChangeCallback: ((x: Float, y: Float, z: Float) -> Unit)? by position::changeCallback

    override val vertexes: Array<Vector3f>
        get() = arrayOf(
            position,
            position.y(y + height),
            position.xyz(x + width, y + height),
            position.x(x + width)
        )

    var isWorldAxis: Boolean = isWorldAxis
        set(value) {
            field = value
            if (value) parent()?.let { position += it.worldPosition }
        }

    val worldPosition: Vector3f
        get() {
            if (isWorldAxis) return position
            var pos = position
            parent()?.let { pos = it.worldPosition + position }
            return pos
        }

    val asWorldRect: Rectangle<Vector3<Float>> get() = rect(worldPosition, width, height)

    override var x
        get() = position.x
        set(value) {
            position.x = value
        }

    var worldX
        get() = worldPosition.x
        set(value) {
            if (isWorldAxis) position.x = value
            else {
                val delta = value - worldPosition.x
                position.x += delta
            }
        }

    override var y
        get() = position.y
        set(value) {
            position.y = value
        }

    var worldY
        get() = worldPosition.y
        set(value) {
            if (isWorldAxis) position.y = value
            else {
                val delta = value - worldPosition.y
                position.y += delta
            }
        }

    override var z
        get() = position.z
        set(value) {
            position.z = value
        }

    var worldZ
        get() = worldPosition.z
        set(value) {
            if (isWorldAxis) position.z = value
            else {
                val delta = value - worldPosition.z
                position.z += delta
            }
        }

    override val top: Float get() = y

    val worldTop: Float get() = worldY

    override val bottom: Float get() = top + height

    val worldBottom: Float get() = worldTop + height

    override val left: Float get() = x

    val worldLeft: Float get() = worldX

    override val right: Float get() = left + width

    val worldRight: Float get() = worldLeft + width

    override var center: Vector3<Float>
        get() = vertex(x + this.halfWidth, y + this.halfHeight, z)
        set(value) {
            translate(value.x - center.x, value.y - center.y, value.z - center.z)
        }

    val worldCenter: Vector3f get() = Vector3f(worldX + this.halfWidth, worldY + this.halfHeight, worldZ)


    /**
     * 鼠标是否在此元素[Transform]内部
     * @param mouseX Number
     * @param mouseY Number
     * @return Boolean
     */
    fun isMouseOvered(mouseX: Number, mouseY: Number): Boolean =
        mouseX.toFloat() in worldLeft..worldRight && mouseY.toFloat() in worldTop..worldBottom

    /**
     * 鼠标是否在此元素[Transform]内部
     * @param mousePosition MousePosition
     * @return Boolean
     */
    fun isMouseOvered(mousePosition: MousePosition): Boolean =
        mousePosition.x in worldLeft..worldRight && mousePosition.y in worldTop..worldBottom

    fun translate(vector3: Vector3<out Number>) {
        position += Vector3f(vector3)
    }

    fun translate(x: Number = 0, y: Number = 0, z: Number = 0) {
        position += Vector3f(x.toFloat(), y.toFloat(), z.toFloat())
    }

    fun translateTo(vector3: Vector3<out Number>) {
        position.set(Vector3f(vector3))
    }

    fun translateTo(x: Number = position.x, y: Number = position.y, z: Number = position.z) {
        position.set(Vector3f(x.toFloat(), y.toFloat(), z.toFloat()))
    }

}

/**
 * 当鼠标位于此元素[Transform]内部时调用
 * @receiver Transform
 * @param mouseX Number
 * @param mouseY Number
 * @param block Transform.() -> Unit
 */
inline fun Transform.mouseHover(mouseX: Number, mouseY: Number, block: Transform.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    isMouseOvered(mouseX, mouseY).ifc { block() }
}

/**
 * 当鼠标位于此元素[Transform]内部时调用
 * @receiver Transform
 * @param mousePosition MousePosition
 * @param block Transform.() -> Unit
 */
inline fun Transform.mouseHover(mousePosition: MousePosition, block: Transform.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    isMouseOvered(mousePosition).ifc { block() }
}


/**
 * 当鼠标位于此元素内部时调用
 * @param block [@kotlin.ExtensionFunctionType] Function1<Element, Unit>
 */
inline fun Element.mouseHover(block: Element.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    screen().let {
        transform.isMouseOvered(it.mousePosition.x, it.mousePosition.y).ifc { block() }
    }
}

/**
 * 当鼠标位于此元素内容矩阵[Element.contentRect]"true"时调用
 * @receiver Element
 * @param block Element.() -> Unit
 */
inline fun Element.mouseHoverContent(block: Element.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    this.mouseHoverContent().ifc { block() }
}

/**
 * 鼠标是否在此元素内
 * @receiver T
 * @return Boolean
 */
fun Element.mouseHover(): Boolean = screen().let { transform.isMouseOvered(it.mousePosition.x, it.mousePosition.y) }

/**
 * 鼠标是否在此元素内
 * @receiver T
 * @return Boolean
 */
fun Element.mouseHover(mousePosition: MousePosition): Boolean = transform.isMouseOvered(mousePosition)

fun Element.mouseHoverContent(): Boolean {
    val contentRect = contentRect(true)
    screen().let {
        return it.mousePosition.x in contentRect.left..contentRect.right && it.mousePosition.y in contentRect.top..contentRect.bottom
    }
}

fun Element.mouseHoverContent(mousePosition: MousePosition): Boolean {
    val contentRect = contentRect(true)
    return mousePosition.x in contentRect.left..contentRect.right && mousePosition.y in contentRect.top..contentRect.bottom
}
