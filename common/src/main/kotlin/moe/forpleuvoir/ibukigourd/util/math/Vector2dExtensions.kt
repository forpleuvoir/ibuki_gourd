package moe.forpleuvoir.ibukigourd.util.math

import moe.forpleuvoir.nebula.serialization.codec.Codec
import org.joml.Vector2d
import org.joml.Vector2dc
import org.joml.Vector2fc
import org.joml.Vector2ic
import org.joml.Vector3dc

fun Vector2dc.isEmpty() = this.x() == 0.0 && this.y() == 0.0

fun Vector2dc.isNotEmpty() = this.x() != 0.0 || this.y() != 0.0

fun Vector3dc.asVector2dc(): Vector2dc = Vector2d(x(), y())

fun Vector2fc.asDouble(): Vector2dc = Vector2d(x().toDouble(), y().toDouble())

fun Vector2ic.asDouble(): Vector2dc = Vector2d(x.toDouble(), y.toDouble())

operator fun Vector2dc.component1(): Double = x()

operator fun Vector2dc.component2(): Double = y()

fun Vector2dc.coerceIn(min: Vector2dc, max: Vector2dc): Vector2dc =
    Vector2d(x().coerceIn(min.x(), max.x()), y().coerceIn(min.y(), max.y()))

fun Vector2d.coerceInOf(min: Vector2dc, max: Vector2dc): Vector2dc {
    x = x.coerceIn(min.x(), max.x())
    y = y.coerceIn(min.y(), max.y())
    return this
}

/**
 * 创建一个向量[Vector2d]对象
 *
 * @param x Number
 * @param y Number
 * @return [Vector2d]
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Vector2d(x: Number = 0.0, y: Number = 0.0): Vector2d =
    Vector2d(x.toDouble(), y.toDouble())

/**
 * 复制一个向量
 * @receiver [Vector2d]
 * @param x Number 用于覆盖的[Vector2d.x]值
 * @param y Number 用于覆盖的[Vector2d.y]值
 * @return [Vector2d]
 */
@JvmOverloads
fun Vector2d.copy(x: Number = this.x, y: Number = this.y): Vector2d =
    Vector2d(x.toDouble(), y.toDouble())

/**
 * 复制一个向量
 * @receiver [Vector2dc]
 * @param x Number 用于覆盖的[Vector2dc.x]值
 * @param y Number 用于覆盖的[Vector2dc.y]值
 * @return [Vector2d]
 */
@JvmOverloads
fun Vector2dc.copy(x: Number = this.x(), y: Number = this.y()): Vector2dc =
    Vector2d(x.toDouble(), y.toDouble())

/**
 * 获取[Vector2d.x]值
 */
val Vector2dc.x: Double
    get() = this.x()

/**
 * 获取[Vector2d.y]值
 */
val Vector2dc.y: Double
    get() = this.y()

val Codec.Companion.vector2dc: Codec<Vector2dc> by lazy {
    context(Codec.double) {
        Codec.create<Vector2dc>()
            .field<Double>("x").getter(Vector2dc::x).codec
            .field<Double>("y").getter(Vector2dc::y).codec
            .build(::Vector2d)
    }
}

fun Codec.Companion.vector2dc(start: Vector2dc, end: Vector2dc) = Codec.create<Vector2dc>()
    .field<Double>("x").getter(Vector2dc::x).codec(Codec.double(start.x..end.x))
    .field<Double>("y").getter(Vector2dc::y).codec(Codec.double(start.y..end.y))
    .build(::Vector2d)

/**
 * 将两个向量[Vector2dc]相加返回一个新的副本
 * @receiver [Vector2dc]
 * @param vector2dc 目标向量[Vector2dc]
 * @return [Vector2d]
 */
operator fun Vector2dc.plus(vector2dc: Vector2dc): Vector2dc =
    this.copy(this.x() + vector2dc.x(), this.y() + vector2dc.y())

/**
 * 将两个向量[Vector2dc]相加返回一个新的副本
 * @receiver [Vector2dc]
 * @param value 目标向量[Vector2dc]
 * @return [Vector2d]
 */
operator fun Vector2dc.plus(value: Number): Vector2dc =
    this.copy(this.x() + value.toDouble(), this.y() + value.toDouble())

/**
 * 将两个向量[Vector2dc]相加并赋值给自身
 * @receiver [Vector2d]
 * @param vector2dc 目标向量[Vector2dc]
 */
operator fun Vector2d.plusAssign(vector2dc: Vector2dc) {
    this.add(vector2dc)
}

operator fun Vector2d.plusAssign(value: Number) {
    this.add(value.toDouble(), value.toDouble())
}

/**
 * 将两个向量[Vector2dc]相减返回一个新的副本
 * @receiver [Vector2dc]
 * @param vector2dc 目标向量[Vector2dc]
 * @return [Vector2d]
 */
operator fun Vector2dc.minus(vector2dc: Vector2dc): Vector2dc =
    this.copy(this.x() - vector2dc.x(), this.y() - vector2dc.y())

operator fun Vector2dc.minus(value: Number): Vector2dc =
    this.copy(this.x() - value.toDouble(), this.y() - value.toDouble())

/**
 * 将两个向量[Vector2dc]相减并赋值给自身
 * @receiver [Vector2d]
 * @param vector2dc 目标向量[Vector2dc]
 */
operator fun Vector2d.minusAssign(vector2dc: Vector2dc) {
    this.sub(vector2dc)
}

operator fun Vector2d.minusAssign(value: Number) {
    this.sub(value.toDouble(), value.toDouble())
}

/**
 * 将两个向量[Vector2dc]相乘返回一个新的副本
 * @receiver [Vector2dc]
 * @param vector2dc 目标向量[Vector2dc]
 * @return [Vector2d]
 */
operator fun Vector2dc.times(vector2dc: Vector2dc): Vector2dc =
    this.copy(this.x() * vector2dc.x(), this.y() * vector2dc.y())

operator fun Vector2dc.times(value: Number): Vector2dc =
    this.copy(this.x() * value.toDouble(), this.y() * value.toDouble())

/**
 * 将两个向量[Vector2dc]相乘并赋值给自身
 * @receiver [Vector2d]
 * @param vector2dc 目标向量[Vector2dc]
 */
operator fun Vector2d.timesAssign(vector2dc: Vector2dc) {
    this.mul(vector2dc)
}

operator fun Vector2d.timesAssign(value: Number) {
    this.mul(value.toDouble())
}

/**
 * 将两个向量[Vector2dc]相除返回一个新的副本
 * @receiver [Vector2dc]
 * @param vector2dc 目标向量[Vector2dc]
 * @return [Vector2d]
 */
operator fun Vector2dc.div(vector2dc: Vector2dc): Vector2dc =
    this.copy(this.x() / vector2dc.x(), this.y() / vector2dc.y())

operator fun Vector2dc.div(value: Number): Vector2dc =
    this.copy(this.x() / value.toDouble(), this.y() / value.toDouble())

/**
 * 将两个向量[Vector2dc]相除并赋值给自身
 * @receiver [Vector2d]
 * @param vector2dc 目标向量[Vector2dc]
 */
operator fun Vector2d.divAssign(vector2dc: Vector2dc) {
    this.div(vector2dc)
}

operator fun Vector2d.divAssign(value: Number) {
    this.div(value.toDouble())
}

/**
 * 将两个向量[Vector2dc]取余返回一个新的副本
 * @receiver [Vector2dc]
 * @param vector2dc 目标向量[Vector2dc]
 * @return [Vector2d]
 */
operator fun Vector2dc.rem(vector2dc: Vector2dc): Vector2dc =
    this.copy(this.x() % vector2dc.x(), this.y() % vector2dc.y())

operator fun Vector2dc.rem(value: Number): Vector2dc =
    this.copy(this.x() % value.toDouble(), this.y() % value.toDouble())

/**
 * 将两个向量[Vector2dc]取余并赋值给自身
 * @receiver [Vector2d]
 * @param vector2dc 目标向量[Vector2dc]
 */
operator fun Vector2d.remAssign(vector2dc: Vector2dc) {
    this.x %= vector2dc.x()
    this.y %= vector2dc.y()
}

operator fun Vector2d.remAssign(value: Number) {
    this.x %= value.toDouble()
    this.y %= value.toDouble()
}

