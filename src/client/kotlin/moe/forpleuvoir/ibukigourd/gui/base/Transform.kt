@file:Suppress("unused", "MemberVisibilityCanBePrivate")
@file:OptIn(ExperimentalContracts::class)

package moe.forpleuvoir.ibukigourd.gui.base

import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.SizeInt
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.input.MousePosition
import moe.forpleuvoir.ibukigourd.input.mousePosition
import moe.forpleuvoir.ibukigourd.render.math.*
import moe.forpleuvoir.ibukigourd.render.math.bezier.NotifiableVector2i
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.nebula.common.ifc
import org.joml.Vector2i
import org.joml.Vector2ic
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * 变换青春版
 */
class Transform(
    position: Vector2ic = Vector2i(),
    width: Int = 0,
    height: Int = 0,
    /**
     * 是否为世界坐标轴
     */
    isWorldAxis: Boolean = false,
    var parent: () -> Transform? = { null },
) : SizeInt {

    /**
     * 不可变向量
     */
    val position: Vector2ic = NotifiableVector2i(position)

    /**
     * 可变向量
     */
    private val positionAsNotifiable: NotifiableVector2i get() = position as NotifiableVector2i

    override var width: Int = width
        set(value) {
            notifyIfChanged {
                field = value
            }
        }

    override var height: Int = height
        set(value) {
            notifyIfChanged {
                field = value
            }
        }

    val depth: Int
        get() = parent()?.let { it.depth + 1 } ?: 0

    private val resizeCallbackSubscribers: MutableList<(origin: Size<Int>, current: Size<Int>) -> Unit> = ArrayList()

    private fun notify(origin: Size<Int>) {
        for (subscriber in resizeCallbackSubscribers) {
            subscriber(origin, this)
        }
    }

    private inline fun notifyIfChanged(block: () -> Unit) {
        val origin = Size(width, height)
        block.invoke()
        if (!Size.equals(origin, this)) notify(origin)
    }

    fun subscribePositionChange(action: (origin: Vector2ic, current: Vector2ic) -> Unit) = positionAsNotifiable.subscribe(action)

    fun subscribeSizeChange(action: (origin: Size<Int>, current: Size<Int>) -> Unit) {
        resizeCallbackSubscribers.add(action)
    }

    fun subscribeChange(
        sizeChangedAction: (origin: Size<Int>, current: Size<Int>) -> Unit,
        positionChangedAction: (origin: Vector2ic, current: Vector2ic) -> Unit
    ) {
        resizeCallbackSubscribers.add(sizeChangedAction)
        positionAsNotifiable.subscribe(positionChangedAction)
    }

    val vertexes: Array<out Vector2ic>
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

    val localPosition: Vector2ic by this::position

    val worldPosition: Vector2ic
        get() {
            if (isWorldAxis) return position
            return position + (parent()?.worldPosition ?: Vector2i(0, 0))
        }

    val asWorldBox: Box get() = Box(worldPosition.asFloat(), width.toFloat(), height.toFloat())

    val asBox: Box get() = Box(position.asFloat(), width.toFloat(), height.toFloat())

    var x
        get() = position.x()
        set(value) {
            positionAsNotifiable.x = value
        }

    var localX
        get() = localPosition.x
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

    var y
        get() = position.y
        set(value) {
            positionAsNotifiable.y = value
        }

    var localY
        get() = localPosition.y
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

    val top: Int get() = y

    val worldTop: Int get() = worldY

    val bottom: Int get() = top + height

    val worldBottom: Int get() = worldTop + height

    val left: Int get() = x

    val worldLeft: Int get() = worldX

    val right: Int get() = left + width

    val worldRight: Int get() = worldLeft + width

    var center: Vector2ic
        get() = Vector2i(x + this.halfWidth, y + this.halfHeight)
        set(value) {
            translate(value.x - center.x, value.y - center.y)
        }

    val worldCenter: Vector2ic get() = Vector2i(worldX + this.halfWidth, worldY + this.halfHeight)


    operator fun contains(position: MousePosition): Boolean {
        return isMouseOvered(position)
    }

    /**
     * 鼠标是否在此元素[Transform]内部
     * @param mouseX Number
     * @param mouseY Number
     * @return Boolean
     */
    fun isMouseOvered(mouseX: Number, mouseY: Number): Boolean =
        mouseX.toInt() in worldLeft..worldRight && mouseY.toInt() in worldTop..worldBottom

    /**
     * 鼠标是否在此元素[Transform]内部
     * @param mousePosition MousePosition
     * @return Boolean
     */
    fun isMouseOvered(mousePosition: MousePosition): Boolean =
        (mousePosition.x in (worldLeft.toFloat()..worldRight.toFloat())) && (mousePosition.y in (worldTop.toFloat()..worldBottom.toFloat()))

    fun translate(vector2fc: Vector2ic) {
        positionAsNotifiable += vector2fc
    }

    fun translate(x: Number = 0, y: Number = 0) {
        positionAsNotifiable += Vector2i(x.toInt(), y.toInt())
    }

    fun translateTo(vector2fc: Vector2ic) {
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
inline fun Transform.mouseHover(
    mouseX: Number,
    mouseY: Number,
    block: Transform.() -> Unit
) {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    isMouseOvered(mouseX, mouseY).ifc { block() }
}

/**
 * 当鼠标位于此元素[Transform]内部时调用
 * @receiver Transform
 * @param mousePosition MousePosition
 * @param block Transform.() -> Unit
 */
inline fun Transform.mouseHover(
    mousePosition: MousePosition,
    block: Transform.() -> Unit
) {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    if (mousePosition in this) block()
}


/**
 * 当鼠标位于此元素内部时调用
 * @param block Element.() -> Unit
 */
inline fun IGWidget.mouseHover(block: IGWidget.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    if (mouseHover()) block()
}

/**
 * 当鼠标位于此元素内容矩形[Element.contentBox]时调用
 * @receiver Element
 * @param block Element.() -> Unit
 */
inline fun IGWidget.mouseHoverContent(block: IGWidget.() -> Unit) {
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
fun IGWidget.mouseHover(): Boolean = mc.mousePosition in transform

/**
 * 鼠标是否在此元素内
 * @receiver T
 * @return Boolean
 */
fun IGWidget.mouseHover(mousePosition: MousePosition): Boolean = transform.isMouseOvered(mousePosition)

fun IGWidget.mouseHoverContent(): Boolean = mc.mousePosition in contentBox(true)

fun IGWidget.mouseHoverContent(mousePosition: MousePosition): Boolean = mousePosition in contentBox(true)
