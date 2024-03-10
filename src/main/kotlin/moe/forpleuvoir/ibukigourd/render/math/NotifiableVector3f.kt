package moe.forpleuvoir.ibukigourd.render.math

import org.joml.Vector3f
import org.joml.Vector3fc
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class NotifiableVector3f private constructor(private val vector3f: Vector3f) : Vector3fc by vector3f {

    @JvmOverloads
    constructor(x: Number = 0.0f, y: Number = 0.0f, z: Number = 0.0f) : this(Vector3f(x, y, z))

    constructor(vector3fc: Vector3fc) : this(Vector3f(vector3fc))

    private val subscribers: MutableList<(origin: Vector3fc, current: Vector3fc) -> Unit> = ArrayList()

    fun subscribe(action: (origin: Vector3fc, current: Vector3fc) -> Unit) {
        subscribers.add(action)
    }

    var enableNotify = true

    private inline fun notifyIfChanged(block: () -> Unit) {
        val origin = vector3f.copy()
        if (origin.equals(this)) block.invoke()
        notify(origin)
    }

    private fun notify(origin: Vector3fc) {
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
        get() = vector3f.x
        set(value) {
            notifyIfChanged {
                vector3f.x = value
            }
        }

    var y: Float
        get() = vector3f.y
        set(value) {
            notifyIfChanged {
                vector3f.y = value
            }
        }

    var z: Float
        get() = vector3f.z
        set(value) {
            notifyIfChanged {
                vector3f.z = value
            }
        }

    @JvmOverloads
    fun set(x: Number = this.x, y: Number = this.y, z: Number = this.z) {
        this.x = x.toFloat()
        this.y = y.toFloat()
        this.z = z.toFloat()
    }

    fun set(vector3fc: Vector3fc) {
        this.x = vector3fc.x
        this.y = vector3fc.y
        this.z = vector3fc.z
    }


    /**
     * 将两个向量[Vector3fc]相加并赋值给自身
     * @param vector3f 目标向量[Vector3fc]
     */
    operator fun plusAssign(vector3fc: Vector3fc) {
        this.x += vector3fc.x()
        this.y += vector3fc.y()
        this.z += vector3fc.z()
    }

    /**
     * 将两个向量[Vector3fc]相减并赋值给自身
     * @param vector3f 目标向量[Vector3fc]
     */
    operator fun minusAssign(vector3fc: Vector3fc) {
        this.x -= vector3fc.x()
        this.y -= vector3fc.y()
        this.z -= vector3fc.z()
    }

    /**
     * 将两个向量[Vector3fc]相乘并赋值给自身
     * @param vector3fc 目标向量[Vector3fc]
     */
    operator fun timesAssign(vector3fc: Vector3fc) {
        this.x *= vector3fc.x()
        this.y *= vector3fc.y()
        this.z *= vector3fc.z()
    }

    /**
     * 将两个向量[Vector3fc]相除并赋值给自身
     * @param vector3fc 目标向量[Vector3fc]
     */
    operator fun divAssign(vector3fc: Vector3fc) {
        this.x /= vector3fc.x()
        this.y /= vector3fc.y()
        this.z /= vector3fc.z()
    }

    /**
     * 将两个向量[Vector3fc]取余并赋值给自身
     * @param vector3fc 目标向量[Vector3fc]
     */
    operator fun remAssign(vector3fc: Vector3fc) {
        this.x %= vector3fc.x()
        this.y %= vector3fc.y()
        this.z %= vector3fc.z()
    }
}
