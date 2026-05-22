@file:Suppress("UNUSED")

package moe.forpleuvoir.ibukigourd.util.math

import moe.forpleuvoir.nebula.serialization.codec.Codec
import org.joml.Vector3f
import org.joml.Vector3fc

fun Vector3fc.isEmpty() = this.x() == 0f && this.y() == 0f && this.z() == 0f

fun Vector3fc.isNotEmpty() = this.x() != 0f && this.y() != 0f && this.z() != 0f

fun Vector3fc.asVector3fc(): Vector3fc = Vector3f(x, y, 0f)

operator fun Vector3fc.component1(): Float = x()

operator fun Vector3fc.component2(): Float = y()

operator fun Vector3fc.component3(): Float = z()

fun Vector3fc.coerceIn(min: Vector3fc, max: Vector3fc): Vector3fc =
    Vector3f(x.coerceIn(min.x(), max.x()), y.coerceIn(min.y(), max.y()), z.coerceIn(min.z(), max.z()))

fun Vector3f.coerceInOf(min: Vector3fc, max: Vector3fc): Vector3fc {
    x = x.coerceIn(min.x(), max.x())
    y = y.coerceIn(min.y(), max.y())
    y = y.coerceIn(min.z(), max.z())
    return this
}

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

val Codec.Companion.vector3fc: Codec<Vector3fc> by lazy {
    context(Codec.float) {
        Codec.create<Vector3fc>()
            .field<Float>("x").getter(Vector3fc::x).codec
            .field<Float>("y").getter(Vector3fc::y).codec
            .field<Float>("z").getter(Vector3fc::z).codec
            .build(::Vector3f)
    }
}

fun Codec.Companion.vector3fc(start: Vector3fc, end: Vector3fc) = Codec.create<Vector3fc>()
    .field<Float>("x").getter(Vector3fc::x).codec(Codec.float(start.x..end.x))
    .field<Float>("y").getter(Vector3fc::y).codec(Codec.float(start.y..end.y))
    .field<Float>("z").getter(Vector3fc::z).codec(Codec.float(start.z..end.z))
    .build(::Vector3f)


/**
 * 将两个向量[Vector3fc]相加返回一个新的副本
 * @receiver [Vector3fc]
 * @param vector3fc 目标向量[Vector3fc]
 * @return [Vector3f]
 */
operator fun Vector3fc.plus(vector3fc: Vector3fc): Vector3fc = this.copy(this.x() + vector3fc.x(), this.y() + vector3fc.y(), this.z() + vector3fc.z())

operator fun Vector3fc.plus(value: Number): Vector3fc = this.copy(this.x() + value.toFloat(), this.y() + value.toFloat(), this.z() + value.toFloat())

/**
 * 将两个向量[Vector3fc]相加并赋值给自身
 * @receiver [Vector3f]
 * @param vector3f 目标向量[Vector3fc]
 */
operator fun Vector3f.plusAssign(vector3f: Vector3fc) {
    this.add(vector3f)
}

operator fun Vector3f.plusAssign(value: Number) {
    this.add(value.toFloat(), value.toFloat(), value.toFloat())
}

/**
 * 将两个向量[Vector3fc]相减返回一个新的副本
 * @receiver [Vector3fc]
 * @param vector3fc 目标向量[Vector3fc]
 * @return [Vector3f]
 */
operator fun Vector3fc.minus(vector3fc: Vector3fc): Vector3fc = this.copy(this.x() - vector3fc.x(), this.y() - vector3fc.y(), this.z() - vector3fc.z())

operator fun Vector3fc.minus(value: Number): Vector3fc = this.copy(this.x() - value.toFloat(), this.y() - value.toFloat(), this.z() - value.toFloat())

/**
 * 将两个向量[Vector3fc]相减并赋值给自身
 * @receiver [Vector3f]
 * @param vector3f 目标向量[Vector3fc]
 */
operator fun Vector3f.minusAssign(vector3f: Vector3fc) {
    this.sub(vector3f)
}

operator fun Vector3f.minusAssign(value: Number) {
    this.sub(value.toFloat(), value.toFloat(), value.toFloat())
}

/**
 * 将两个向量[Vector3fc]相乘返回一个新的副本
 * @receiver [Vector3fc]
 * @param vector3fc 目标向量[Vector3fc]
 * @return [Vector3f]
 */
operator fun Vector3fc.times(vector3fc: Vector3fc): Vector3fc = this.copy(this.x() * vector3fc.x(), this.y() * vector3fc.y(), this.z() * vector3fc.z())

operator fun Vector3fc.times(value: Number): Vector3fc = this.copy(this.x() * value.toFloat(), this.y() * value.toFloat(), this.z() * value.toFloat())

/**
 * 将两个向量[Vector3fc]相乘并赋值给自身
 * @receiver [Vector3f]
 * @param vector3fc 目标向量[Vector3fc]
 */
operator fun Vector3f.timesAssign(vector3fc: Vector3fc) {
    this.mul(vector3fc)
}

operator fun Vector3f.timesAssign(value: Number) {
    this.mul(value.toFloat())
}

/**
 * 将两个向量[Vector3fc]相除返回一个新的副本
 * @receiver [Vector3fc]
 * @param vector3fc 目标向量[Vector3fc]
 * @return [Vector3f]
 */
operator fun Vector3fc.div(vector3fc: Vector3fc): Vector3fc = this.copy(this.x() / vector3fc.x(), this.y() / vector3fc.y(), this.z() / vector3fc.z())

operator fun Vector3fc.div(value: Number): Vector3fc = this.copy(this.x() / value.toFloat(), this.y() / value.toFloat(), this.z() / value.toFloat())

/**
 * 将两个向量[Vector3fc]相除并赋值给自身
 * @receiver [Vector3f]
 * @param vector3fc 目标向量[Vector3fc]
 */
operator fun Vector3f.divAssign(vector3fc: Vector3fc) {
    this.div(vector3fc)
}

operator fun Vector3f.divAssign(value: Number) {
    this.div(value.toFloat())
}

/**
 * 将两个向量[Vector3fc]取余返回一个新的副本
 * @receiver [Vector3fc]
 * @param vector3fc 目标向量[Vector3fc]
 * @return [Vector3f]
 */
operator fun Vector3fc.rem(vector3fc: Vector3fc): Vector3fc = this.copy(this.x() % vector3fc.x(), this.y() % vector3fc.y(), this.z() % vector3fc.z())

operator fun Vector3fc.rem(value: Number): Vector3fc = this.copy(this.x() % value.toFloat(), this.y() % value.toFloat(), this.z() % value.toFloat())

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

operator fun Vector3f.remAssign(value: Number) {
    this.x %= value.toFloat()
    this.y %= value.toFloat()
    this.z %= value.toFloat()
}