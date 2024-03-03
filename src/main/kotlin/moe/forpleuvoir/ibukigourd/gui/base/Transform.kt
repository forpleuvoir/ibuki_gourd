@file:Suppress("unused", "MemberVisibilityCanBePrivate")
@file:OptIn(ExperimentalContracts::class)

package moe.forpleuvoir.ibukigourd.gui.base

import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.input.MousePosition
import moe.forpleuvoir.ibukigourd.render.base.math.*
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex
import moe.forpleuvoir.ibukigourd.render.shape.rectangle.Rectangle
import moe.forpleuvoir.ibukigourd.render.shape.rectangle.rect
import moe.forpleuvoir.nebula.common.ifc
import org.joml.Vector3f
import org.joml.Vector3fc
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * 变换青春版
 */
class Transform(
    position: Vector3fc = Vector3f(),
    /**
     * 是否为世界坐标轴
     */
    isWorldAxis: Boolean = false,
    width: Float = 0.0f,
    height: Float = 0.0f,
    var parent: () -> Transform? = { null },
) : Rectangle {

    override val position: Vector3fc = NotifiableVector3f(position)

    private val positionAsNotifiable: NotifiableVector3f get() = position as NotifiableVector3f

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

    fun subscribePositionChange(action: (origin: Vector3fc, current: Vector3fc) -> Unit) = positionAsNotifiable.subscribe(action)

    override val vertexes: Array<Vector3fc>
        get() = arrayOf(
            position,
            position.copy(y = y + height),
            position.copy(x + width, y + height),
            position.copy(x + width)
        )

    var isWorldAxis: Boolean = isWorldAxis
        set(value) {
            field = value
            if (value) parent()?.let { position as Vector3f += it.worldPosition }
        }

    val worldPosition: Vector3fc
        get() {
            if (isWorldAxis) return position
            var pos = position
            parent()?.let { pos = it.worldPosition + position }
            return pos
        }

    val asWorldRect: Rectangle get() = rect(worldPosition, width, height)

    override var x
        get() = position.x()
        set(value) {
            positionAsNotifiable.x = value
        }

    var worldX
        get() = worldPosition.x
        set(value) {
            if (isWorldAxis) positionAsNotifiable.x = value
            else {
                val delta = value - worldPosition.x
                positionAsNotifiable.x += delta
            }
        }

    override var y
        get() = position.y
        set(value) {
            positionAsNotifiable.y = value
        }

    var worldY
        get() = worldPosition.y
        set(value) {
            if (isWorldAxis) positionAsNotifiable.y = value
            else {
                val delta = value - worldPosition.y
                positionAsNotifiable.y += delta
            }
        }

    override var z
        get() = position.z
        set(value) {
            positionAsNotifiable.z = value
        }

    var worldZ
        get() = worldPosition.z
        set(value) {
            if (isWorldAxis) positionAsNotifiable.z = value
            else {
                val delta = value - worldPosition.z
                positionAsNotifiable.z += delta
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

    override var center: Vector3fc
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

    /**
     * @see [isMouseOvered]
     * @param mousePosition MousePosition
     * @return Boolean
     */
    operator fun contains(mousePosition: MousePosition): Boolean =
        mousePosition.x in worldLeft..worldRight && mousePosition.y in worldTop..worldBottom

    fun translate(vector3: Vector3fc) {
        positionAsNotifiable += Vector3f(vector3)
    }

    fun translate(x: Number = 0, y: Number = 0, z: Number = 0) {
        positionAsNotifiable += Vector3f(x.toFloat(), y.toFloat(), z.toFloat())
    }

    fun translateTo(vector3: Vector3f) {
        positionAsNotifiable.set(Vector3f(vector3))
    }

    fun translateTo(x: Number = position.x, y: Number = position.y, z: Number = position.z) {
        positionAsNotifiable.set(Vector3f(x.toFloat(), y.toFloat(), z.toFloat()))
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
    if (mousePosition in this) block()
}


/**
 * 当鼠标位于此元素内部时调用
 * @param block Element.() -> Unit
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
 * 当鼠标位于此元素内容矩形[Element.contentRect]时调用
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
