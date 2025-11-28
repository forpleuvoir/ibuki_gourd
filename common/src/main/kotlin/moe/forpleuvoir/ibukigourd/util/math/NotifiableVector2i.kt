package moe.forpleuvoir.ibukigourd.util.math

import org.joml.Vector2i
import org.joml.Vector2ic
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class NotifiableVector2i private constructor(private val vector2i: Vector2i) : Vector2ic by vector2i {

    @JvmOverloads
    constructor(x: Number = 0, y: Number = 0) : this(Vector2i(x.toInt(), y.toInt()))

    constructor(vector2ic: Vector2ic) : this(Vector2i(vector2ic))

    private val subscribers: MutableList<(origin: Vector2ic, current: Vector2ic) -> Unit> = ArrayList()

    fun subscribe(action: (origin: Vector2ic, current: Vector2ic) -> Unit) {
        subscribers.add(action)
    }

    var enableNotify = true

    private inline fun notifyIfChanged(block: () -> Unit) {
        val origin = vector2i.copy()
        block.invoke()
        if (!origin.equals(this)) notify(origin)
    }

    private fun notify(origin: Vector2ic) {
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

    var x: Int
        get() = vector2i.x
        set(value) {
            notifyIfChanged {
                vector2i.x = value
            }
        }

    var y: Int
        get() = vector2i.y
        set(value) {
            notifyIfChanged {
                vector2i.y = value
            }
        }

    @JvmOverloads
    fun set(x: Number = this.x, y: Number = this.y) {
        this.x = x.toInt()
        this.y = y.toInt()
    }

    fun set(vector2ic: Vector2ic) {
        this.x = vector2ic.x
        this.y = vector2ic.y
    }

    /**
     * 将两个向量[Vector2ic]相加并赋值给自身
     * @param vector2ic 目标向量[Vector2ic]
     */
    operator fun plusAssign(vector2ic: Vector2ic) {
        this.x += vector2ic.x()
        this.y += vector2ic.y()
    }

    /**
     * 将两个向量[Vector2ic]相减并赋值给自身
     * @param vector2ic 目标向量[Vector2ic]
     */
    operator fun minusAssign(vector2ic: Vector2ic) {
        this.x -= vector2ic.x()
        this.y -= vector2ic.y()
    }

    /**
     * 将两个向量[Vector2ic]相乘并赋值给自身
     * @param vector2ic 目标向量[Vector2ic]
     */
    operator fun timesAssign(vector2ic: Vector2ic) {
        this.x *= vector2ic.x()
        this.y *= vector2ic.y()
    }

    /**
     * 将两个向量[Vector2ic]相除并赋值给自身
     * @param vector2ic 目标向量[Vector2ic]
     */
    operator fun divAssign(vector2ic: Vector2ic) {
        this.x /= vector2ic.x()
        this.y /= vector2ic.y()
    }

    /**
     * 将两个向量[Vector2ic]取余并赋值给自身
     * @param vector2ic 目标向量[Vector2ic]
     */
    operator fun remAssign(vector2ic: Vector2ic) {
        this.x %= vector2ic.x()
        this.y %= vector2ic.y()
    }
}