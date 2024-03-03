@file:Suppress("UNUSED")

package moe.forpleuvoir.ibukigourd.render.base.math

import moe.forpleuvoir.nebula.serialization.DeserializationException
import moe.forpleuvoir.nebula.serialization.base.SerializeArray
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.extensions.serializeObject
import org.joml.Vector3f
import org.joml.Vector3fc

/**
 * 创建一个向量[Vector3f]对象
 *
 * @param x Number
 * @param y Number
 * @param z Number
 * @return [Vector3f]
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Vector3f(x: Number = 0f, y: Number = 0f, z: Number = 0f): Vector3f = Vector3f(x.toFloat(), y.toFloat(), z.toFloat())

/**
 * 复制一个向量
 * @receiver [Vector3f]
 * @param x Number 用于覆盖的[Vector3f.x]值
 * @param y Number 用于覆盖的[Vector3f.y]值
 * @param z Number 用于覆盖的[Vector3f.z]值
 * @return [Vector3f]
 */
@JvmOverloads
fun Vector3f.copy(x: Number = this.x, y: Number = this.y, z: Number = this.z): Vector3f = Vector3f(x.toFloat(), y.toFloat(), z.toFloat())

/**
 * 复制一个向量
 * @receiver [Vector3fc]
 * @param x Number 用于覆盖的[Vector3fc.x]值
 * @param y Number 用于覆盖的[Vector3fc.y]值
 * @param z Number 用于覆盖的[Vector3fc.z]值
 * @return [Vector3f]
 */
@JvmOverloads
fun Vector3fc.copy(x: Number = this.x(), y: Number = this.y(), z: Number = this.z()): Vector3fc = Vector3f(x.toFloat(), y.toFloat(), z.toFloat())

/**
 * 获取[Vector3f.x]值
 */
val Vector3fc.x: Float
    get() = this.x()

/**
 * 获取[Vector3f.y]值

 */
val Vector3fc.y: Float
    get() = this.y()

/**
 * 获取[Vector3f.z]值
 */
val Vector3fc.z: Float
    get() = this.z()

/**
 * 将[Vector3f]序列化
 * @receiver [Vector3fc]
 * @return [SerializeElement]
 */
fun Vector3fc.serialization(): SerializeElement = serializeObject {
    "x" to this@serialization.x()
    "y" to this@serialization.y()
    "z" to this@serialization.z()
}

fun vector3fDeserialization(element: SerializeElement): Vector3f {
    return Vector3f().apply { deserialization(element) }
}

/**
 * 将[Vector3f]反序列化
 * @receiver [Vector3f]
 * @param element [SerializeElement]
 */
fun Vector3f.deserialization(element: SerializeElement) {
    when (element) {
        is SerializeArray  -> {
            this.x = element[0].asFloat
            this.y = element[1].asFloat
            this.z = element[2].asFloat
        }

        is SerializeObject -> {
            runCatching {
                this.x = element["x"]!!.asFloat
                this.y = element["y"]!!.asFloat
                this.z = element["z"]!!.asFloat
            }.onFailure {
                throw DeserializationException("Vector3f deserialization error, ${it.message}")
            }
        }

        else               -> throw DeserializationException("SerializeElement type error, expected to be an [SerializeArray] or [SerializeObject], but was [${element.javaClass.name}]")
    }
}

/**
 * 将两个向量[Vector3fc]相加返回一个新的副本
 * @receiver [Vector3fc]
 * @param vector3fc 目标向量[Vector3fc]
 * @return [Vector3f]
 */
operator fun Vector3fc.plus(vector3fc: Vector3fc): Vector3fc = this.copy(this.x() + vector3fc.x(), this.y() + vector3fc.y(), this.z() + vector3fc.z())

/**
 * 将两个向量[Vector3fc]相加并赋值给自身
 * @receiver [Vector3f]
 * @param vector3f 目标向量[Vector3fc]
 */
operator fun Vector3f.plusAssign(vector3f: Vector3fc) {
    this.add(vector3f)
}

/**
 * 将两个向量[Vector3fc]相减返回一个新的副本
 * @receiver [Vector3fc]
 * @param vector3fc 目标向量[Vector3fc]
 * @return [Vector3f]
 */
operator fun Vector3fc.minus(vector3fc: Vector3fc): Vector3fc = this.copy(this.x() - vector3fc.x(), this.y() - vector3fc.y(), this.z() - vector3fc.z())

/**
 * 将两个向量[Vector3fc]相减并赋值给自身
 * @receiver [Vector3f]
 * @param vector3f 目标向量[Vector3fc]
 */
operator fun Vector3f.minusAssign(vector3f: Vector3fc) {
    this.sub(vector3f)
}

/**
 * 将两个向量[Vector3fc]相乘返回一个新的副本
 * @receiver [Vector3fc]
 * @param vector3fc 目标向量[Vector3fc]
 * @return [Vector3f]
 */
operator fun Vector3fc.times(vector3fc: Vector3fc): Vector3fc = this.copy(this.x() * vector3fc.x(), this.y() * vector3fc.y(), this.z() * vector3fc.z())

/**
 * 将两个向量[Vector3fc]相乘并赋值给自身
 * @receiver [Vector3f]
 * @param vector3fc 目标向量[Vector3fc]
 */
operator fun Vector3f.timesAssign(vector3fc: Vector3fc) {
    this.mul(vector3fc)
}

/**
 * 将两个向量[Vector3fc]相除返回一个新的副本
 * @receiver [Vector3fc]
 * @param vector3fc 目标向量[Vector3fc]
 * @return [Vector3f]
 */
operator fun Vector3fc.div(vector3fc: Vector3fc): Vector3fc = this.copy(this.x() / vector3fc.x(), this.y() / vector3fc.y(), this.z() / vector3fc.z())

/**
 * 将两个向量[Vector3fc]相除并赋值给自身
 * @receiver [Vector3f]
 * @param vector3fc 目标向量[Vector3fc]
 */
operator fun Vector3f.divAssign(vector3fc: Vector3fc) {
    this.div(vector3fc)
}

/**
 * 将两个向量[Vector3fc]取余返回一个新的副本
 * @receiver [Vector3fc]
 * @param vector3fc 目标向量[Vector3fc]
 * @return [Vector3f]
 */
operator fun Vector3fc.rem(vector3fc: Vector3fc): Vector3fc = this.copy(this.x() % vector3fc.x(), this.y() % vector3fc.y(), this.z() % vector3fc.z())

/**
 * 将两个向量[Vector3fc]取余并赋值给自身
 * @receiver [Vector3f]
 * @param vector3fc 目标向量[Vector3fc]
 */
operator fun Vector3f.remAssign(vector3fc: Vector3fc) {
    this.x %= vector3fc.x()
    this.y %= vector3fc.y()
    this.z %= vector3fc.z()
}