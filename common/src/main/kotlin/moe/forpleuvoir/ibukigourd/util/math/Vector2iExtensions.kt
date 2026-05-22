package moe.forpleuvoir.ibukigourd.util.math

import moe.forpleuvoir.nebula.serialization.codec.Codec
import org.joml.*

fun Vector2ic.isEmpty() = this.x() == 0 && this.y() == 0

fun Vector2ic.isNotEmpty() = this.x() != 0 && this.y() != 0

fun Vector3ic.asVector2ic(): Vector2ic = Vector2i(x(), y())

fun Vector2dc.asInt(): Vector2ic = Vector2i(x().toInt(), y().toInt())

operator fun Vector2ic.component1(): Int = x()

operator fun Vector2ic.component2(): Int = y()

fun Vector2ic.coerceIn(min: Vector2ic, max: Vector2ic): Vector2ic = Vector2i(x.coerceIn(min.x(), max.x()), y.coerceIn(min.y(), max.y()))

fun Vector2i.coerceInOf(min: Vector2ic, max: Vector2ic): Vector2ic {
    x = x.coerceIn(min.x(), max.x())
    y = y.coerceIn(min.y(), max.y())
    return this
}

/**
 * 创建一个向量[Vector2i]对象
 *
 * @param x Number
 * @param y Number
 * @return [Vector2i]
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Vector2i(x: Number = 0f, y: Number = 0f): Vector2i = org.joml.Vector2i(x.toInt(), y.toInt())

/**
 * 复制一个向量
 * @receiver [Vector2i]
 * @param x Number 用于覆盖的[Vector2i.x]值
 * @param y Number 用于覆盖的[Vector2i.y]值
 * @return [Vector2i]
 */
@JvmOverloads
fun Vector2i.copy(x: Number = this.x, y: Number = this.y): Vector2i = Vector2i(x.toInt(), y.toInt())

/**
 * 复制一个向量
 * @receiver [Vector2ic]
 * @param x Number 用于覆盖的[Vector2ic.x]值
 * @param y Number 用于覆盖的[Vector2ic.y]值
 * @return [Vector2i]
 */
@JvmOverloads
fun Vector2ic.copy(x: Number = this.x(), y: Number = this.y()): Vector2ic = Vector2i(x.toInt(), y.toInt())

/**
 * 获取[Vector2i.x]值
 */
val Vector2ic.x: Int
    get() = this.x()

/**
 * 获取[Vector2i.y]值

 */
val Vector2ic.y: Int
    get() = this.y()

val Codec.Companion.vector2ic: Codec<Vector2ic> by lazy {
    context(Codec.int) {
        Codec.create<Vector2ic>()
            .field<Int>("x").getter(Vector2ic::x).codec
            .field<Int>("y").getter(Vector2ic::y).codec
            .build(::Vector2i)
    }
}

fun Codec.Companion.vector2ic(start: Vector2ic, end: Vector2ic) = Codec.create<Vector2ic>()
    .field<Int>("x").getter(Vector2ic::x).codec(Codec.int(start.x..end.x))
    .field<Int>("y").getter(Vector2ic::y).codec(Codec.int(start.y..end.y))
    .build(::Vector2i)


/**
 * 将两个向量[vector2fc]相加返回一个新的副本
 * @receiver [vector2fc]
 * @param vector2fc 目标向量[vector2fc]
 * @return [Vector2i]
 */
operator fun Vector2ic.plus(vector2fc: Vector2ic): Vector2ic = this.copy(this.x() + vector2fc.x(), this.y() + vector2fc.y())

operator fun Vector2ic.plus(value: Number): Vector2ic = this.copy(this.x() + value.toInt(), this.y() + value.toInt())

/**
 * 将两个向量[Vector2ic]相加并赋值给自身
 * @receiver [vector2f]
 * @param vector2f 目标向量[Vector2ic]
 */
operator fun Vector2i.plusAssign(vector2f: Vector2ic) {
    this.add(vector2f)
}

fun Vector2i.plusAssign(value: Number) {
    this.add(value.toInt(), value.toInt())
}

/**
 * 将两个向量[vector2fc]相减返回一个新的副本
 * @receiver [vector2fc]
 * @param vector2fc 目标向量[vector2fc]
 * @return [Vector2i]
 */
operator fun Vector2ic.minus(vector2fc: Vector2ic): Vector2ic = this.copy(this.x() - vector2fc.x(), this.y() - vector2fc.y())

operator fun Vector2ic.minus(value: Number): Vector2ic = this.copy(this.x() - value.toInt(), this.y() - value.toInt())

/**
 * 将两个向量[Vector2ic]相减并赋值给自身
 * @receiver [vector2f]
 * @param vector2f 目标向量[Vector2ic]
 */
operator fun Vector2i.minusAssign(vector2f: Vector2ic) {
    this.sub(vector2f)
}

operator fun Vector2i.minusAssign(value: Number) {
    this.sub(value.toInt(), value.toInt())
}

/**
 * 将两个向量[vector2fc]相乘返回一个新的副本
 * @receiver [vector2fc]
 * @param vector2fc 目标向量[vector2fc]
 * @return [Vector2i]
 */
operator fun Vector2ic.times(vector2fc: Vector2ic): Vector2ic = this.copy(this.x() * vector2fc.x(), this.y() * vector2fc.y())

operator fun Vector2ic.times(value: Number): Vector2ic = this.copy(this.x() * value.toInt(), this.y() * value.toInt())

/**
 * 将两个向量[vector2fc]相乘并赋值给自身
 * @receiver [Vector2i]
 * @param vector2fc 目标向量[vector2fc]
 */
operator fun Vector2i.timesAssign(vector2fc: Vector2ic) {
    this.mul(vector2fc)
}

operator fun Vector2i.timesAssign(value: Number) {
    this.mul(value.toInt())
}

/**
 * 将两个向量[vector2fc]相除返回一个新的副本
 * @receiver [vector2fc]
 * @param vector2fc 目标向量[vector2fc]
 * @return [Vector2i]
 */
operator fun Vector2ic.div(vector2fc: Vector2ic): Vector2ic = this.copy(this.x() / vector2fc.x(), this.y() / vector2fc.y())

operator fun Vector2ic.div(value: Number): Vector2ic = this.copy(this.x() / value.toInt(), this.y() / value.toInt())

/**
 * 将两个向量[vector2fc]相除并赋值给自身
 * @receiver [Vector2i]
 * @param vector2fc 目标向量[vector2fc]
 */
operator fun Vector2i.divAssign(vector2fc: Vector2ic) {
    this.div(vector2fc)
}

operator fun Vector2i.divAssign(value: Number) {
    this.div(value.toInt())
}

/**
 * 将两个向量[vector2fc]取余返回一个新的副本
 * @receiver [vector2fc]
 * @param vector2fc 目标向量[vector2fc]
 * @return [Vector2i]
 */
operator fun Vector2ic.rem(vector2fc: Vector2ic): Vector2ic = this.copy(this.x() % vector2fc.x(), this.y() % vector2fc.y())

operator fun Vector2ic.rem(value: Number): Vector2ic = this.copy(this.x() % value.toInt(), this.y() % value.toInt())

/**
 * 将两个向量[vector2fc]取余并赋值给自身
 * @receiver [Vector2i]
 * @param vector2fc 目标向量[vector2fc]
 */
operator fun Vector2i.remAssign(vector2fc: Vector2ic) {
    this.x %= vector2fc.x()
    this.y %= vector2fc.y()
}

operator fun Vector2i.remAssign(value: Number) {
    this.x %= value.toInt()
    this.y %= value.toInt()
}