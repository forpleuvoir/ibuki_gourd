@file:Suppress("unused", "MemberVisibilityCanBePrivate")
@file:OptIn(ExperimentalContracts::class)

package moe.forpleuvoir.ibukigourd.gui.base

import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.render.MutableSizeFloat
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.input.MousePosition
import moe.forpleuvoir.ibukigourd.input.mousePosition
import moe.forpleuvoir.ibukigourd.render.math.*
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.nebula.common.ifc
import org.joml.Vector2fc
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

typealias WidgetPosition = Vector2fc

/**
 * 变换青春版
 */
class Transform(
    position: WidgetPosition = Vector2f(),
    width: Float = 0f,
    height: Float = 0f,
    /**
     * 是否为世界坐标轴
     */
    isWorldAxis: Boolean = false,
    var parent: () -> Transform? = { null },
) : Box, MutableSizeFloat, Placeable {

    /**
     * 不可变向量
     */
    override val position: WidgetPosition = NotifiableVector2f(position)

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
    override var halfWidth: Float
        get() = width / 2
        set(value) {
            width = value * 2
        }

    override var halfHeight: Float
        get() = height / 2
        set(value) {
            height = value * 2
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
        if (!Size.equals(origin, this)) notify(origin)
    }

    fun subscribePositionChange(action: (origin: Vector2fc, current: Vector2fc) -> Unit) = positionAsNotifiable.subscribe(action)

    fun subscribeSizeChange(action: (origin: Size<Float>, current: Size<Float>) -> Unit) {
        resizeCallbackSubscribers.add(action)
    }

    fun subscribeChange(
        sizeChangedAction: (origin: Size<Float>, current: Size<Float>) -> Unit,
        positionChangedAction: (origin: Vector2fc, current: Vector2fc) -> Unit
    ) {
        resizeCallbackSubscribers.add(sizeChangedAction)
        positionAsNotifiable.subscribe(positionChangedAction)
    }

    override fun placeAt(x: Float, y: Float, isWorldAxis: Boolean) {
        if (isWorldAxis) {
            worldX = x
            worldY = y
        } else {
            this.x = x
            this.y = y
        }
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
            return position + (parent()?.worldPosition ?: Vector2f(0, 0))
        }

    val asWorldBox: Box get() = Box(worldPosition, width, height)

    val asBox: Box get() = Box(position, width, height)

    override var x
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

    override var y
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

    override val top: Float get() = y

    val worldTop: Float get() = worldY

    override val bottom: Float get() = top + height

    val worldBottom: Float get() = worldTop + height

    override val left: Float get() = x

    val worldLeft: Float get() = worldX

    override val right: Float get() = left + width

    val worldRight: Float get() = worldLeft + width

    override var center: WidgetPosition
        get() = Vector2f(x + this.halfWidth, y + this.halfHeight)
        set(value) {
            translate(value.x - center.x, value.y - center.y)
        }

    val worldCenter: WidgetPosition get() = Vector2f(worldX + this.halfWidth, worldY + this.halfHeight)


    override operator fun contains(position: MousePosition): Boolean {
        return isMouseOvered(position)
    }


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
        (mousePosition.x in (worldLeft..worldRight)) && (mousePosition.y in (worldTop..worldBottom))

    fun translate(vector2fc: Vector2fc) {
        positionAsNotifiable += vector2fc
    }

    fun translate(x: Number = 0, y: Number = 0) {
        positionAsNotifiable += Vector2f(x, y)
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
 * 当鼠标位于此元素内容矩形[IGWidget.contentBox]时调用
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
