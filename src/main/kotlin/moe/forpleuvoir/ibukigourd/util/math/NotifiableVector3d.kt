package moe.forpleuvoir.ibukigourd.util.math

import org.joml.Vector3d
import org.joml.Vector3dc
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class NotifiableVector3d private constructor(private val vector3d: Vector3d) : Vector3dc by vector3d {

    @JvmOverloads
    constructor(x: Number = 0.0, y: Number = 0.0, z: Number = 0.0) : this(Vector3d(x.toDouble(), y.toDouble(), z.toDouble()))

    constructor(vector3dc: Vector3dc) : this(Vector3d(vector3dc))

    private val subscribers: MutableList<(origin: Vector3dc, current: Vector3dc) -> Unit> = ArrayList()

    fun subscribe(action: (origin: Vector3dc, current: Vector3dc) -> Unit) {
        subscribers.add(action)
    }

    var enableNotify = true

    private inline fun notifyIfChanged(block: () -> Unit) {
        val origin = vector3d.copy()
        block.invoke()
        if (!origin.equals(this)) notify(origin)
    }

    private fun notify(origin: Vector3dc) {
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
        get() = vector3d.x
        set(value) {
            notifyIfChanged {
                vector3d.x = value
            }
        }

    var y: Double
        get() = vector3d.y
        set(value) {
            notifyIfChanged {
                vector3d.y = value
            }
        }

    var z: Double
        get() = vector3d.z
        set(value) {
            notifyIfChanged {
                vector3d.z = value
            }
        }

    @JvmOverloads
    fun set(x: Number = this.x, y: Number = this.y, z: Number = this.z) {
        this.x = x.toDouble()
        this.y = y.toDouble()
        this.z = z.toDouble()
    }

    fun set(vector3dc: Vector3dc) {
        this.x = vector3dc.x
        this.y = vector3dc.y
        this.z = vector3dc.z
    }

    /**
     * 将两个向量[Vector3dc]相加并赋值给自身
     * @param vector3dc 目标向量[Vector3dc]
     */
    operator fun plusAssign(vector3dc: Vector3dc) {
        this.x += vector3dc.x()
        this.y += vector3dc.y()
        this.z += vector3dc.z()
    }

    /**
     * 将两个向量[Vector3dc]相减并赋值给自身
     * @param vector3dc 目标向量[Vector3dc]
     */
    operator fun minusAssign(vector3dc: Vector3dc) {
        this.x -= vector3dc.x()
        this.y -= vector3dc.y()
        this.z -= vector3dc.z()
    }

    /**
     * 将两个向量[Vector3dc]相乘并赋值给自身
     * @param vector3dc 目标向量[Vector3dc]
     */
    operator fun timesAssign(vector3dc: Vector3dc) {
        this.x *= vector3dc.x()
        this.y *= vector3dc.y()
        this.z *= vector3dc.z()
    }

    /**
     * 将两个向量[Vector3dc]相除并赋值给自身
     * @param vector3dc 目标向量[Vector3dc]
     */
    operator fun divAssign(vector3dc: Vector3dc) {
        this.x /= vector3dc.x()
        this.y /= vector3dc.y()
        this.z /= vector3dc.z()
    }

    /**
     * 将两个向量[Vector3dc]取余并赋值给自身
     * @param vector3dc 目标向量[Vector3dc]
     */
    operator fun remAssign(vector3dc: Vector3dc) {
        this.x %= vector3dc.x()
        this.y %= vector3dc.y()
        this.z %= vector3dc.z()
    }
}