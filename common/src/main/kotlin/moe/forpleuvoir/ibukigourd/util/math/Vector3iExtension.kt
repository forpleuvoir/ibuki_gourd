@file:Suppress("UNUSED")

package moe.forpleuvoir.ibukigourd.util.math

import moe.forpleuvoir.nebula.serialization.codec.Codec
import org.joml.Vector3i
import org.joml.Vector3ic

fun Vector3ic.isEmpty() = this.x() == 0 && this.y() == 0 && this.z() == 0

fun Vector3ic.isNotEmpty() = this.x() != 0 || this.y() != 0 || this.z() != 0

fun Vector3ic.asVector3ic(): Vector3ic = Vector3i(x, y, 0)

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
inline val Vector3ic.x: Int
    get() = this.x()

/**
 * 获取[Vector3i.y]值
 */
inline val Vector3ic.y: Int
    get() = this.y()

/**
 * 获取[Vector3i.z]值
 */
inline val Vector3ic.z: Int
    get() = this.z()

val Codec.Companion.vector3ic: Codec<Vector3ic> by lazy {
    context(Codec.int) {
        Codec.create<Vector3ic>()
            .field<Int>("x").getter(Vector3ic::x).codec
            .field<Int>("y").getter(Vector3ic::y).codec
            .field<Int>("z").getter(Vector3ic::z).codec
            .build(::Vector3i)
    }
}

fun Codec.Companion.vector3ic(start: Vector3ic, end: Vector3ic) = Codec.create<Vector3ic>()
    .field<Int>("x").getter(Vector3ic::x).codec(Codec.int(start.x..end.x))
    .field<Int>("y").getter(Vector3ic::y).codec(Codec.int(start.y..end.y))
    .field<Int>("z").getter(Vector3ic::z).codec(Codec.int(start.z..end.z))
    .build(::Vector3i)

/**
 * 将两个向量[Vector3ic]相加返回一个新的副本
 * @receiver [Vector3ic]
 * @param vector3ic 目标向量[Vector3ic]
 * @return [Vector3i]
 */
operator fun Vector3ic.plus(vector3ic: Vector3ic): Vector3ic =
    this.copy(this.x() + vector3ic.x(), this.y() + vector3ic.y(), this.z() + vector3ic.z())

operator fun Vector3ic.plus(value: Number): Vector3ic =
    this.copy(this.x() + value.toInt(), this.y() + value.toInt(), this.z() + value.toInt())

/**
 * 将两个向量[Vector3ic]相加并赋值给自身
 * @receiver [Vector3i]
 * @param vector3ic 目标向量[Vector3ic]
 */
operator fun Vector3i.plusAssign(vector3ic: Vector3ic) {
    this.add(vector3ic)
}

operator fun Vector3i.plusAssign(value: Number) {
    this.add(value.toInt(), value.toInt(), value.toInt())
}

/**
 * 将两个向量[Vector3ic]相减返回一个新的副本
 * @receiver [Vector3ic]
 * @param vector3ic 目标向量[Vector3ic]
 * @return [Vector3i]
 */
operator fun Vector3ic.minus(vector3ic: Vector3ic): Vector3ic =
    this.copy(this.x() - vector3ic.x(), this.y() - vector3ic.y(), this.z() - vector3ic.z())

operator fun Vector3ic.minus(value: Number): Vector3ic =
    this.copy(this.x() - value.toInt(), this.y() - value.toInt(), this.z() - value.toInt())

/**
 * 将两个向量[Vector3ic]相减并赋值给自身
 * @receiver [Vector3i]
 * @param vector3ic 目标向量[Vector3ic]
 */
operator fun Vector3i.minusAssign(vector3ic: Vector3ic) {
    this.sub(vector3ic)
}

operator fun Vector3i.minusAssign(value: Number) {
    this.sub(value.toInt(), value.toInt(), value.toInt())
}

/**
 * 将两个向量[Vector3ic]相乘返回一个新的副本
 * @receiver [Vector3ic]
 * @param vector3ic 目标向量[Vector3ic]
 * @return [Vector3i]
 */
operator fun Vector3ic.times(vector3ic: Vector3ic): Vector3ic =
    this.copy(this.x() * vector3ic.x(), this.y() * vector3ic.y(), this.z() * vector3ic.z())

operator fun Vector3ic.times(value: Number): Vector3ic =
    this.copy(this.x() * value.toInt(), this.y() * value.toInt(), this.z() * value.toInt())

/**
 * 将两个向量[Vector3ic]相乘并赋值给自身
 * @receiver [Vector3i]
 * @param vector3ic 目标向量[Vector3ic]
 */
operator fun Vector3i.timesAssign(vector3ic: Vector3ic) {
    this.mul(vector3ic)
}

operator fun Vector3i.timesAssign(value: Number) {
    this.mul(value.toInt())
}

/**
 * 将两个向量[Vector3ic]相除返回一个新的副本
 * @receiver [Vector3ic]
 * @param vector3ic 目标向量[Vector3ic]
 * @return [Vector3i]
 */
operator fun Vector3ic.div(vector3ic: Vector3ic): Vector3ic =
    this.copy(this.x() / vector3ic.x(), this.y() / vector3ic.y(), this.z() / vector3ic.z())

operator fun Vector3ic.div(value: Number): Vector3ic =
    this.copy(this.x() / value.toInt(), this.y() / value.toInt(), this.z() / value.toInt())

/**
 * 将两个向量[Vector3ic]相除并赋值给自身
 * @receiver [Vector3i]
 * @param vector3ic 目标向量[Vector3ic]
 */
operator fun Vector3i.divAssign(vector3ic: Vector3ic) {
    this.div(vector3ic)
}

operator fun Vector3i.divAssign(value: Number) {
    this.div(value.toInt())
}

/**
 * 将两个向量[Vector3ic]取余返回一个新的副本
 * @receiver [Vector3ic]
 * @param vector3ic 目标向量[Vector3ic]
 * @return [Vector3i]
 */
operator fun Vector3ic.rem(vector3ic: Vector3ic): Vector3ic =
    this.copy(this.x() % vector3ic.x(), this.y() % vector3ic.y(), this.z() % vector3ic.z())

operator fun Vector3ic.rem(value: Number): Vector3ic =
    this.copy(this.x() % value.toInt(), this.y() % value.toInt(), this.z() % value.toInt())

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

operator fun Vector3i.remAssign(value: Number) {
    this.x %= value.toInt()
    this.y %= value.toInt()
    this.z %= value.toInt()
}