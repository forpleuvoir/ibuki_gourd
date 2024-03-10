package moe.forpleuvoir.ibukigourd.render.math

import org.joml.Vector2f
import org.joml.Vector2fc
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class NotifiableVector2f private constructor(private val vector2f: Vector2f) : Vector2fc by vector2f {

    @JvmOverloads
    constructor(x: Number = 0.0f, y: Number = 0.0f) : this(Vector2f(x, y))

    constructor(vector2fc: Vector2fc) : this(Vector2f(vector2fc))

    private val subscribers: MutableList<(origin: Vector2fc, current: Vector2fc) -> Unit> = ArrayList()

    fun subscribe(action: (origin: Vector2fc, current: Vector2fc) -> Unit) {
        subscribers.add(action)
    }

    var enableNotify = true

    private inline fun notifyIfChanged(block: () -> Unit) {
        val origin = vector2f.copy()
        if (origin.equals(this)) block.invoke()
        notify(origin)
    }

    private fun notify(origin: Vector2fc) {
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

    var x: Float
        get() = vector2f.x
        set(value) {
            notifyIfChanged {
                vector2f.x = value
            }
        }

    var y: Float
        get() = vector2f.y
        set(value) {
            notifyIfChanged {
                vector2f.y = value
            }
        }

    @JvmOverloads
    fun set(x: Number = this.x, y: Number = this.y) {
        this.x = x.toFloat()
        this.y = y.toFloat()
    }

    fun set(vector2fc: Vector2fc) {
        this.x = vector2fc.x
        this.y = vector2fc.y
    }


    /**
     * 将两个向量[Vector2fc]相加并赋值给自身
     * @param vector2fc 目标向量[Vector2fc]
     */
    operator fun plusAssign(vector2fc: Vector2fc) {
        this.x += vector2fc.x()
        this.y += vector2fc.y()
    }

    /**
     * 将两个向量[Vector2fc]相减并赋值给自身
     * @param vector2fc 目标向量[Vector2fc]
     */
    operator fun minusAssign(vector2fc: Vector2fc) {
        this.x -= vector2fc.x()
        this.y -= vector2fc.y()
    }

    /**
     * 将两个向量[Vector2fc]相乘并赋值给自身
     * @param vector2fc 目标向量[Vector2fc]
     */
    operator fun timesAssign(vector2fc: Vector2fc) {
        this.x *= vector2fc.x()
        this.y *= vector2fc.y()
    }

    /**
     * 将两个向量[Vector2fc]相除并赋值给自身
     * @param vector2fc 目标向量[Vector2fc]
     */
    operator fun divAssign(vector2fc: Vector2fc) {
        this.x /= vector2fc.x()
        this.y /= vector2fc.y()
    }

    /**
     * 将两个向量[Vector2fc]取余并赋值给自身
     * @param vector2fc 目标向量[Vector2fc]
     */
    operator fun remAssign(vector2fc: Vector2fc) {
        this.x %= vector2fc.x()
        this.y %= vector2fc.y()
    }
}
