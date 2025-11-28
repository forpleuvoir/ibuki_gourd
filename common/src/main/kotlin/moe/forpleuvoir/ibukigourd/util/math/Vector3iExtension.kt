@file:Suppress("UNUSED")

package moe.forpleuvoir.ibukigourd.util.math

import moe.forpleuvoir.nebula.serialization.Deserializer
import moe.forpleuvoir.nebula.serialization.base.SerializeArray
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.extensions.checkType
import moe.forpleuvoir.nebula.serialization.extensions.serializeObject
import org.joml.Vector2ic
import org.joml.Vector3i
import org.joml.Vector3ic

fun Vector3ic.isEmpty() = this.x() == 0 && this.y() == 0 && this.z() == 0

fun Vector3ic.isNotEmpty() = this.x() != 0 || this.y() != 0 || this.z() != 0

fun Vector2ic.asVector3ic(): Vector3ic = Vector3i(x, y, 0)

operator fun Vector3ic.component1(): Int = x()

operator fun Vector3ic.component2(): Int = y()

operator fun Vector3ic.component3(): Int = z()

fun Vector3ic.coerceIn(min: Vector3ic, max: Vector3ic): Vector3ic =
    Vector3i(
        x().coerceIn(min.x(), max.x()),
        y().coerceIn(min.y(), max.y()),
        z().coerceIn(min.z(), max.z())
    )

fun Vector3i.coerceInOf(min: Vector3ic, max: Vector3ic): Vector3ic {
    x = x.coerceIn(min.x(), max.x())
    y = y.coerceIn(min.y(), max.y())
    z = z.coerceIn(min.z(), max.z())
    return this
}

/**
 * 创建一个向量[Vector3i]对象
 *
 * @param x Number
 * @param y Number
 * @param z Number
 * @return [Vector3i]
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Vector3i(x: Number = 0, y: Number = 0, z: Number = 0): Vector3i =
    Vector3i(x.toInt(), y.toInt(), z.toInt())

/**
 * 复制一个向量
 * @receiver [Vector3i]
 * @param x Number 用于覆盖的[Vector3i.x]值
 * @param y Number 用于覆盖的[Vector3i.y]值
 * @param z Number 用于覆盖的[Vector3i.z]值
 * @return [Vector3i]
 */
@JvmOverloads
fun Vector3i.copy(x: Number = this.x, y: Number = this.y, z: Number = this.z): Vector3i =
    Vector3i(x.toInt(), y.toInt(), z.toInt())

/**
 * 复制一个向量
 * @receiver [Vector3ic]
 * @param x Number 用于覆盖的[Vector3ic.x]值
 * @param y Number 用于覆盖的[Vector3ic.y]值
 * @param z Number 用于覆盖的[Vector3ic.z]值
 * @return [Vector3i]
 */
@JvmOverloads
fun Vector3ic.copy(x: Number = this.x(), y: Number = this.y(), z: Number = this.z()): Vector3ic =
    Vector3i(x.toInt(), y.toInt(), z.toInt())

/**
 * 获取[Vector3i.x]值
 */
val Vector3ic.x: Int
    get() = this.x()

/**
 * 获取[Vector3i.y]值
 */
val Vector3ic.y: Int
    get() = this.y()

/**
 * 获取[Vector3i.z]值
 */
val Vector3ic.z: Int
    get() = this.z()

/**
 * 将[Vector3i]序列化
 * @receiver [Vector3ic]
 * @return [SerializeElement]
 */
fun Vector3ic.serialization(): SerializeElement = serializeObject {
    "x" to x()
    "y" to y()
    "z" to z()
}

object Vector3icDeserializer : Deserializer<Vector3ic> {
    override fun deserialization(serializeElement: SerializeElement): Vector3ic {
        return serializeElement.checkType<Vector3ic>()
            .check<SerializeArray> {
                Vector3i(it[0].asInt, it[1].asInt, it[2].asInt)
            }.check<SerializeObject> {
                Vector3i(it["x"]!!.asInt, it["y"]!!.asInt, it["z"]!!.asInt)
            }.getOrThrow()
    }

}

/**
 * 将[Vector3i]反序列化
 * @receiver [Vector3i]
 * @param element [SerializeElement]
 */
fun Vector3i.deserialization(element: SerializeElement) {
    element.checkType<Unit>()
        .check<SerializeArray> {
            this.x = it[0].asInt
            this.y = it[1].asInt
            this.z = it[2].asInt
        }.check<SerializeObject> {
            this.x = it["x"]!!.asInt
            this.y = it["y"]!!.asInt
            this.z = it["z"]!!.asInt
        }.getOrThrow()
}

/**
 * 将两个向量[Vector3ic]相加返回一个新的副本
 * @receiver [Vector3ic]
 * @param vector3ic 目标向量[Vector3ic]
 * @return [Vector3i]
 */
operator fun Vector3ic.plus(vector3ic: Vector3ic): Vector3ic =
    this.copy(this.x() + vector3ic.x(), this.y() + vector3ic.y(), this.z() + vector3ic.z())

/**
 * 将两个向量[Vector3ic]相加并赋值给自身
 * @receiver [Vector3i]
 * @param vector3ic 目标向量[Vector3ic]
 */
operator fun Vector3i.plusAssign(vector3ic: Vector3ic) {
    this.add(vector3ic)
}

/**
 * 将两个向量[Vector3ic]相减返回一个新的副本
 * @receiver [Vector3ic]
 * @param vector3ic 目标向量[Vector3ic]
 * @return [Vector3i]
 */
operator fun Vector3ic.minus(vector3ic: Vector3ic): Vector3ic =
    this.copy(this.x() - vector3ic.x(), this.y() - vector3ic.y(), this.z() - vector3ic.z())

/**
 * 将两个向量[Vector3ic]相减并赋值给自身
 * @receiver [Vector3i]
 * @param vector3ic 目标向量[Vector3ic]
 */
operator fun Vector3i.minusAssign(vector3ic: Vector3ic) {
    this.sub(vector3ic)
}

/**
 * 将两个向量[Vector3ic]相乘返回一个新的副本
 * @receiver [Vector3ic]
 * @param vector3ic 目标向量[Vector3ic]
 * @return [Vector3i]
 */
operator fun Vector3ic.times(vector3ic: Vector3ic): Vector3ic =
    this.copy(this.x() * vector3ic.x(), this.y() * vector3ic.y(), this.z() * vector3ic.z())

/**
 * 将两个向量[Vector3ic]相乘并赋值给自身
 * @receiver [Vector3i]
 * @param vector3ic 目标向量[Vector3ic]
 */
operator fun Vector3i.timesAssign(vector3ic: Vector3ic) {
    this.mul(vector3ic)
}

/**
 * 将两个向量[Vector3ic]相除返回一个新的副本
 * @receiver [Vector3ic]
 * @param vector3ic 目标向量[Vector3ic]
 * @return [Vector3i]
 */
operator fun Vector3ic.div(vector3ic: Vector3ic): Vector3ic =
    this.copy(this.x() / vector3ic.x(), this.y() / vector3ic.y(), this.z() / vector3ic.z())

/**
 * 将两个向量[Vector3ic]相除并赋值给自身
 * @receiver [Vector3i]
 * @param vector3ic 目标向量[Vector3ic]
 */
operator fun Vector3i.divAssign(vector3ic: Vector3ic) {
    this.div(vector3ic)
}

/**
 * 将两个向量[Vector3ic]取余返回一个新的副本
 * @receiver [Vector3ic]
 * @param vector3ic 目标向量[Vector3ic]
 * @return [Vector3i]
 */
operator fun Vector3ic.rem(vector3ic: Vector3ic): Vector3ic =
    this.copy(this.x() % vector3ic.x(), this.y() % vector3ic.y(), this.z() % vector3ic.z())

/**
 * 将两个向量[Vector3ic]取余并赋值给自身
 * @receiver [Vector3i]
 * @param vector3ic 目标向量[Vector3ic]
 */
operator fun Vector3i.remAssign(vector3ic: Vector3ic) {
    this.x %= vector3ic.x()
    this.y %= vector3ic.y()
    this.z %= vector3ic.z()
}