@file:Suppress("unused", "MemberVisibilityCanBePrivate")
@file:OptIn(ExperimentalContracts::class)

package moe.forpleuvoir.ibukigourd.gui.base

import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.render.Size
import moe.forpleuvoir.ibukigourd.gui.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.input.MousePosition
import moe.forpleuvoir.ibukigourd.render.math.*
import moe.forpleuvoir.nebula.common.ifc
import org.joml.Vector2f
import org.joml.Vector2fc
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * 变换青春版
 */
class Transform(
    position: Vector2fc = Vector2f(),
    /**
     * 是否为世界坐标轴
     */
    isWorldAxis: Boolean = false,
    width: Float = 0.0f,
    height: Float = 0.0f,
    var parent: () -> Transform? = { null },
) : Box {

    /**
     * 不可变向量
     */
    override val position: Vector2fc = NotifiableVector2f(position)

    /**
     * 可变向量
     */
    private val positionAsNotifiable: NotifiableVector2f get() = position as NotifiableVector2f

    override var width: Float = width
        set(value) {
            notifyIfChanged {
                field = value
            }
        }

    override var height: Float = height
        set(value) {
            notifyIfChanged {
                field = value
            }
        }

    val depth: Int
        get() = parent()?.let { it.depth + 1 } ?: 0

    private val resizeCallbackSubscribers: MutableList<(origin: Size<Float>, current: Size<Float>) -> Unit> = ArrayList()

    private fun notify(origin: Size<Float>) {
        for (subscriber in resizeCallbackSubscribers) {
            subscriber(origin, this)
        }
    }

    private inline fun notifyIfChanged(block: () -> Unit) {
        val origin = Size(width, height)
        block.invoke()
        if (origin != this) notify(origin)
    }

    fun subscribePositionChange(action: (origin: Vector2fc, current: Vector2fc) -> Unit) = positionAsNotifiable.subscribe(action)

    fun subscribeSizeChange(action: (origin: Size<Float>, current: Size<Float>) -> Unit) {
        resizeCallbackSubscribers.add(action)
    }

    fun subscribeChange(sizeChangedAction: (origin: Size<Float>, current: Size<Float>) -> Unit, positionChangedAction: (origin: Vector2fc, current: Vector2fc) -> Unit) {
        resizeCallbackSubscribers.add(sizeChangedAction)
        positionAsNotifiable.subscribe(positionChangedAction)
    }

    override val vertexes: Array<out Vector2fc>
        get() = arrayOf(
            position,
            position.copy(y = y + height),
            position.copy(x + width, y + height),
            position.copy(x + width)
        )

    var isWorldAxis: Boolean = isWorldAxis
        set(value) {
            field = value
            if (value) parent()?.let { positionAsNotifiable += it.worldPosition }
        }

    val localPosition: Vector2fc by this::position

    val worldPosition: Vector2fc
        get() {
            if (isWorldAxis) return position
            var pos = position
            parent()?.let { pos = it.worldPosition + position }
            return pos
        }

    val asWorldBox: Box get() = Box(worldPosition, width, height)

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

    override val top: Float get() = y

    val worldTop: Float get() = worldY

    override val bottom: Float get() = top + height

    val worldBottom: Float get() = worldTop + height

    override val left: Float get() = x

    val worldLeft: Float get() = worldX

    override val right: Float get() = left + width

    val worldRight: Float get() = worldLeft + width

    override var center: Vector2fc
        get() = Vector2f(x + this.halfWidth, y + this.halfHeight)
        set(value) {
            translate(value.x - center.x, value.y - center.y)
        }

    val worldCenter: Vector2fc get() = Vector2f(worldX + this.halfWidth, worldY + this.halfHeight)

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

    fun translate(vector2fc: Vector2fc) {
        positionAsNotifiable += vector2fc
    }

    fun translate(x: Number = 0, y: Number = 0) {
        positionAsNotifiable += Vector2f(x.toFloat(), y.toFloat())
    }

    fun translateTo(vector2fc: Vector2fc) {
        positionAsNotifiable.set(vector2fc)
    }

    fun translateTo(x: Number = position.x, y: Number = position.y) {
        positionAsNotifiable.set(x, y)
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
 * 当鼠标位于此元素内容矩形[Element.contentBox]时调用
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
    val contentRect = contentBox(true)
    screen().let {
        return it.mousePosition.x in contentRect.left..contentRect.right && it.mousePosition.y in contentRect.top..contentRect.bottom
    }
}

fun Element.mouseHoverContent(mousePosition: MousePosition): Boolean {
    val contentRect = contentBox(true)
    return mousePosition.x in contentRect.left..contentRect.right && mousePosition.y in contentRect.top..contentRect.bottom
}
