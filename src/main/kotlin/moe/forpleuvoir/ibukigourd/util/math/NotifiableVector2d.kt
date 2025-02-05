package moe.forpleuvoir.ibukigourd.util.math

import org.joml.Vector2d
import org.joml.Vector2dc
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class NotifiableVector2d private constructor(private val vector2d: Vector2d) : Vector2dc by vector2d {

    @JvmOverloads
    constructor(x: Number = 0.0, y: Number = 0.0) : this(Vector2d(x.toDouble(), y.toDouble()))

    constructor(vector2dc: Vector2dc) : this(Vector2d(vector2dc))

    private val subscribers: MutableList<(origin: Vector2dc, current: Vector2dc) -> Unit> = ArrayList()

    fun subscribe(action: (origin: Vector2dc, current: Vector2dc) -> Unit) {
        subscribers.add(action)
    }

    var enableNotify = true

    private inline fun notifyIfChanged(block: () -> Unit) {
        val origin = vector2d.copy()
        block.invoke()
        if (!origin.equals(this)) notify(origin)
    }

    private fun notify(origin: Vector2dc) {
        if (enableNotify) {
            for (subscriber in subscribers) {
                subscriber(origin, this)
            }
        }
    }

    @OptIn(ExperimentalContracts::class)
    inline fun disableNotify(block: () -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        val temp = enableNotify
        enableNotify = false
        block()
        enableNotify = temp
    }

    var x: Double
        get() = vector2d.x
        set(value) {
            notifyIfChanged {
                vector2d.x = value
            }
        }

    var y: Double
        get() = vector2d.y
        set(value) {
            notifyIfChanged {
                vector2d.y = value
            }
        }

    @JvmOverloads
    fun set(x: Number = this.x, y: Number = this.y) {
        this.x = x.toDouble()
        this.y = y.toDouble()
    }

    fun set(vector2dc: Vector2dc) {
        this.x = vector2dc.x
        this.y = vector2dc.y
    }

    /**
     * 将两个向量[Vector2dc]相加并赋值给自身
     * @param vector2dc 目标向量[Vector2dc]
     */
    operator fun plusAssign(vector2dc: Vector2dc) {
        this.x += vector2dc.x()
        this.y += vector2dc.y()
    }

    /**
     * 将两个向量[Vector2dc]相减并赋值给自身
     * @param vector2dc 目标向量[Vector2dc]
     */
    operator fun minusAssign(vector2dc: Vector2dc) {
        this.x -= vector2dc.x()
        this.y -= vector2dc.y()
    }

    /**
     * 将两个向量[Vector2dc]相乘并赋值给自身
     * @param vector2dc 目标向量[Vector2dc]
     */
    operator fun timesAssign(vector2dc: Vector2dc) {
        this.x *= vector2dc.x()
        this.y *= vector2dc.y()
    }

    /**
     * 将两个向量[Vector2dc]相除并赋值给自身
     * @param vector2dc 目标向量[Vector2dc]
     */
    operator fun divAssign(vector2dc: Vector2dc) {
        this.x /= vector2dc.x()
        this.y /= vector2dc.y()
    }

    /**
     * 将两个向量[Vector2dc]取余并赋值给自身
     * @param vector2dc 目标向量[Vector2dc]
     */
    operator fun remAssign(vector2dc: Vector2dc) {
        this.x %= vector2dc.x()
        this.y %= vector2dc.y()
    }
}