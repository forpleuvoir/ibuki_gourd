package moe.forpleuvoir.ibukigourd.util.math

import org.joml.Vector3i
import org.joml.Vector3ic
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class NotifiableVector3i private constructor(private val vector3i: Vector3i) : Vector3ic by vector3i {

    @JvmOverloads
    constructor(x: Number = 0, y: Number = 0, z: Number = 0) : this(Vector3i(x.toInt(), y.toInt(), z.toInt()))

    constructor(vector3ic: Vector3ic) : this(Vector3i(vector3ic))

    private val subscribers: MutableList<(origin: Vector3ic, current: Vector3ic) -> Unit> = ArrayList()

    fun subscribe(action: (origin: Vector3ic, current: Vector3ic) -> Unit) {
        subscribers.add(action)
    }

    var enableNotify = true

    private inline fun notifyIfChanged(block: () -> Unit) {
        val origin = vector3i.copy()
        block.invoke()
        if (!origin.equals(this)) notify(origin)
    }

    private fun notify(origin: Vector3ic) {
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
        get() = vector3i.x
        set(value) {
            notifyIfChanged {
                vector3i.x = value
            }
        }

    var y: Int
        get() = vector3i.y
        set(value) {
            notifyIfChanged {
                vector3i.y = value
            }
        }

    var z: Int
        get() = vector3i.z
        set(value) {
            notifyIfChanged {
                vector3i.z = value
            }
        }

    @JvmOverloads
    fun set(x: Number = this.x, y: Number = this.y, z: Number = this.z) {
        this.x = x.toInt()
        this.y = y.toInt()
        this.z = z.toInt()
    }

    fun set(vector3ic: Vector3ic) {
        this.x = vector3ic.x
        this.y = vector3ic.y
        this.z = vector3ic.z
    }

    /**
     * 将两个向量[Vector3ic]相加并赋值给自身
     * @param vector3ic 目标向量[Vector3ic]
     */
    operator fun plusAssign(vector3ic: Vector3ic) {
        this.x += vector3ic.x()
        this.y += vector3ic.y()
        this.z += vector3ic.z()
    }

    /**
     * 将两个向量[Vector3ic]相减并赋值给自身
     * @param vector3ic 目标向量[Vector3ic]
     */
    operator fun minusAssign(vector3ic: Vector3ic) {
        this.x -= vector3ic.x()
        this.y -= vector3ic.y()
        this.z -= vector3ic.z()
    }

    /**
     * 将两个向量[Vector3ic]相乘并赋值给自身
     * @param vector3ic 目标向量[Vector3ic]
     */
    operator fun timesAssign(vector3ic: Vector3ic) {
        this.x *= vector3ic.x()
        this.y *= vector3ic.y()
        this.z *= vector3ic.z()
    }

    /**
     * 将两个向量[Vector3ic]相除并赋值给自身
     * @param vector3ic 目标向量[Vector3ic]
     */
    operator fun divAssign(vector3ic: Vector3ic) {
        this.x /= vector3ic.x()
        this.y /= vector3ic.y()
        this.z /= vector3ic.z()
    }

    /**
     * 将两个向量[Vector3ic]取余并赋值给自身
     * @param vector3ic 目标向量[Vector3ic]
     */
    operator fun remAssign(vector3ic: Vector3ic) {
        this.x %= vector3ic.x()
        this.y %= vector3ic.y()
        this.z %= vector3ic.z()
    }
}