@file:Suppress("UNUSED")

package moe.forpleuvoir.ibukigourd.util.math

import moe.forpleuvoir.nebula.serialization.codec.Codec
import org.joml.Vector3d
import org.joml.Vector3dc

fun Vector3dc.isEmpty() = this.x() == 0.0 && this.y() == 0.0 && this.z() == 0.0

fun Vector3dc.isNotEmpty() = this.x() != 0.0 || this.y() != 0.0 || this.z() != 0.0

fun Vector3dc.asVector3dc(): Vector3dc = Vector3d(x(), y(), 0.0)

operator fun Vector3dc.component1(): Double = x()

operator fun Vector3dc.component2(): Double = y()

operator fun Vector3dc.component3(): Double = z()

fun Vector3dc.coerceIn(min: Vector3dc, max: Vector3dc): Vector3dc =
    Vector3d(
        x().coerceIn(min.x(), max.x()),
        y().coerceIn(min.y(), max.y()),
        z().coerceIn(min.z(), max.z())
    )

fun Vector3d.coerceInOf(min: Vector3dc, max: Vector3dc): Vector3dc {
    x = x.coerceIn(min.x(), max.x())
    y = y.coerceIn(min.y(), max.y())
    z = z.coerceIn(min.z(), max.z())
    return this
}

/**
 * 创建一个向量[Vector3d]对象
 *
 * @param x Number
 * @param y Number
 * @param z Number
 * @return [Vector3d]
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Vector3d(x: Number = 0.0, y: Number = 0.0, z: Number = 0.0): Vector3d =
    Vector3d(x.toDouble(), y.toDouble(), z.toDouble())

/**
 * 复制一个向量
 * @receiver [Vector3d]
 * @param x Number 用于覆盖的[Vector3d.x]值
 * @param y Number 用于覆盖的[Vector3d.y]值
 * @param z Number 用于覆盖的[Vector3d.z]值
 * @return [Vector3d]
 */
@JvmOverloads
fun Vector3d.copy(x: Number = this.x, y: Number = this.y, z: Number = this.z): Vector3d =
    Vector3d(x.toDouble(), y.toDouble(), z.toDouble())

/**
 * 复制一个向量
 * @receiver [Vector3dc]
 * @param x Number 用于覆盖的[Vector3dc.x]值
 * @param y Number 用于覆盖的[Vector3dc.y]值
 * @param z Number 用于覆盖的[Vector3dc.z]值
 * @return [Vector3d]
 */
@JvmOverloads
fun Vector3dc.copy(x: Number = this.x(), y: Number = this.y(), z: Number = this.z()): Vector3dc =
    Vector3d(x.toDouble(), y.toDouble(), z.toDouble())

/**
 * 获取[Vector3d.x]值
 */
val Vector3dc.x: Double
    get() = this.x()

/**
 * 获取[Vector3d.y]值
 */
val Vector3dc.y: Double
    get() = this.y()

/**
 * 获取[Vector3d.z]值
 */
val Vector3dc.z: Double
    get() = this.z()

val Codec.Companion.vector3dc: Codec<Vector3dc> by lazy {
    context(Codec.double) {
        Codec.create<Vector3dc>()
            .field<Double>("x").getter(Vector3dc::x).codec
            .field<Double>("y").getter(Vector3dc::y).codec
            .field<Double>("z").getter(Vector3dc::z).codec
            .build(::Vector3d)
    }
}

fun Codec.Companion.vector3dc(start: Vector3dc, end: Vector3dc) = Codec.create<Vector3dc>()
    .field<Double>("x").getter(Vector3dc::x).codec(Codec.double(start.x..end.x))
    .field<Double>("y").getter(Vector3dc::y).codec(Codec.double(start.y..end.y))
    .field<Double>("z").getter(Vector3dc::z).codec(Codec.double(start.z..end.z))
    .build(::Vector3d)

/**
 * 操作符重载实现 (+, -, *, /, %)，并生成新向量
 */
operator fun Vector3dc.plus(vector3dc: Vector3dc): Vector3dc =
    this.copy(this.x() + vector3dc.x(), this.y() + vector3dc.y(), this.z() + vector3dc.z())

operator fun Vector3dc.plus(value: Number): Vector3dc =
    this.copy(this.x() + value.toDouble(), this.y() + value.toDouble(), this.z() + value.toDouble())

operator fun Vector3d.plusAssign(vector3dc: Vector3dc) {
    this.add(vector3dc)
}

operator fun Vector3d.plusAssign(value: Number) {
    this.add(value.toDouble(), value.toDouble(), value.toDouble())
}

operator fun Vector3dc.minus(vector3dc: Vector3dc): Vector3dc =
    this.copy(this.x() - vector3dc.x(), this.y() - vector3dc.y(), this.z() - vector3dc.z())

operator fun Vector3dc.minus(value: Number): Vector3dc =
    this.copy(this.x() - value.toDouble(), this.y() - value.toDouble(), this.z() - value.toDouble())

operator fun Vector3d.minusAssign(vector3dc: Vector3dc) {
    this.sub(vector3dc)
}

operator fun Vector3d.minusAssign(value: Number) {
    this.sub(value.toDouble(), value.toDouble(), value.toDouble())
}

operator fun Vector3dc.times(vector3dc: Vector3dc): Vector3dc =
    this.copy(this.x() * vector3dc.x(), this.y() * vector3dc.y(), this.z() * vector3dc.z())

operator fun Vector3dc.times(value: Number): Vector3dc =
    this.copy(this.x() * value.toDouble(), this.y() * value.toDouble(), this.z() * value.toDouble())

operator fun Vector3d.timesAssign(vector3dc: Vector3dc) {
    this.mul(vector3dc)
}

operator fun Vector3d.timesAssign(value: Number) {
    this.mul(value.toDouble())
}

operator fun Vector3dc.div(vector3dc: Vector3dc): Vector3dc =
    this.copy(this.x() / vector3dc.x(), this.y() / vector3dc.y(), this.z() / vector3dc.z())

operator fun Vector3dc.div(value: Number): Vector3dc =
    this.copy(this.x() / value.toDouble(), this.y() / value.toDouble(), this.z() / value.toDouble())

operator fun Vector3d.divAssign(vector3dc: Vector3dc) {
    this.div(vector3dc)
}

operator fun Vector3d.divAssign(value: Number) {
    this.div(value.toDouble())
}

operator fun Vector3dc.rem(vector3dc: Vector3dc): Vector3dc =
    this.copy(this.x() % vector3dc.x(), this.y() % vector3dc.y(), this.z() % vector3dc.z())

operator fun Vector3dc.rem(value: Number): Vector3dc =
    this.copy(this.x() % value.toDouble(), this.y() % value.toDouble(), this.z() % value.toDouble())


operator fun Vector3d.remAssign(vector3dc: Vector3dc) {
    this.x %= vector3dc.x()
    this.y %= vector3dc.y()
    this.z %= vector3dc.z()
}

operator fun Vector3d.remAssign(value: Number) {
    this.x %= value.toDouble()
    this.y %= value.toDouble()
    this.z %= value.toDouble()
}