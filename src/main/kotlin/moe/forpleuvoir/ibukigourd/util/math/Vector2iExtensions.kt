package moe.forpleuvoir.ibukigourd.util.math

import moe.forpleuvoir.nebula.serialization.Deserializer
import moe.forpleuvoir.nebula.serialization.base.SerializeArray
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.extensions.checkType
import moe.forpleuvoir.nebula.serialization.extensions.serializeObject
import org.joml.*
import org.joml.Vector2fc
import org.joml.Vector2ic

fun Vector2ic.isEmpty() = this.x() == 0 && this.y() == 0

fun Vector2ic.isNotEmpty() = this.x() != 0 && this.y() != 0

fun Vector3ic.asVector2ic(): Vector2ic = Vector2i(x(), y())

fun Vector2fc.asInt(): Vector2ic = Vector2i(x().toInt(), y().toInt())

fun Vector2dc.asInt(): Vector2ic = Vector2i(x().toInt(), y().toInt())

operator fun Vector2ic.component1(): Int = x()

operator fun Vector2ic.component2(): Int = y()

fun Vector2ic.coerceIn(min: Vector2ic, max: Vector2ic): Vector2ic = Vector2i(x.coerceIn(min.x(), max.x()), y.coerceIn(min.y(), max.y()))

fun Vector2i.coerceIn(min: Vector2ic, max: Vector2ic): Vector2ic {
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
inline fun Vector2i(x: Number = 0f, y: Number = 0f): Vector2i = Vector2i(x.toInt(), y.toInt())

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

/**
 * 将[Vector2i]序列化
 * @receiver [Vector2ic]
 * @return [SerializeElement]
 */
fun Vector2ic.serialization(): SerializeElement = serializeObject {
    "x" to x()
    "y" to y()
}

object Vector2ic : Deserializer<Vector2ic> {
    override fun deserialization(serializeElement: SerializeElement): Vector2ic {
        return serializeElement.checkType<Vector2ic>()
            .check<SerializeArray> {
                Vector2i(it[0].asInt, it[1].asInt)
            }.check<SerializeObject> {
                Vector2i(it["x"]!!.asInt, it["y"]!!.asInt)
            }.getOrThrow()
    }

}

/**
 * 将[Vector2i]反序列化
 * @receiver [Vector2i]
 * @param element [SerializeElement]
 */
fun Vector2i.deserialization(element: SerializeElement) {
    element.checkType<Unit>()
        .check<SerializeArray> {
            this.x = it[0].asInt
            this.y = it[1].asInt
        }.check<SerializeObject> {
            this.x = it["x"]!!.asInt
            this.y = it["y"]!!.asInt
        }.getOrThrow()
}

/**
 * 将两个向量[vector2fc]相加返回一个新的副本
 * @receiver [vector2fc]
 * @param vector2fc 目标向量[vector2fc]
 * @return [Vector2i]
 */
operator fun Vector2ic.plus(vector2fc: Vector2ic): Vector2ic = this.copy(this.x() + vector2fc.x(), this.y() + vector2fc.y())

/**
 * 将两个向量[Vector2ic]相加并赋值给自身
 * @receiver [vector2f]
 * @param vector2f 目标向量[Vector2ic]
 */
operator fun Vector2i.plusAssign(vector2f: Vector2ic) {
    this.add(vector2f)
}

/**
 * 将两个向量[vector2fc]相减返回一个新的副本
 * @receiver [vector2fc]
 * @param vector2fc 目标向量[vector2fc]
 * @return [Vector2i]
 */
operator fun Vector2ic.minus(vector2fc: Vector2ic): Vector2ic = this.copy(this.x() - vector2fc.x(), this.y() - vector2fc.y())

/**
 * 将两个向量[Vector2ic]相减并赋值给自身
 * @receiver [vector2f]
 * @param vector2f 目标向量[Vector2ic]
 */
operator fun Vector2i.minusAssign(vector2f: Vector2ic) {
    this.sub(vector2f)
}

/**
 * 将两个向量[vector2fc]相乘返回一个新的副本
 * @receiver [vector2fc]
 * @param vector2fc 目标向量[vector2fc]
 * @return [Vector2i]
 */
operator fun Vector2ic.times(vector2fc: Vector2ic): Vector2ic = this.copy(this.x() * vector2fc.x(), this.y() * vector2fc.y())

/**
 * 将两个向量[vector2fc]相乘并赋值给自身
 * @receiver [Vector2i]
 * @param vector2fc 目标向量[vector2fc]
 */
operator fun Vector2i.timesAssign(vector2fc: Vector2ic) {
    this.mul(vector2fc)
}

/**
 * 将两个向量[vector2fc]相除返回一个新的副本
 * @receiver [vector2fc]
 * @param vector2fc 目标向量[vector2fc]
 * @return [Vector2i]
 */
operator fun Vector2ic.div(vector2fc: Vector2ic): Vector2ic = this.copy(this.x() / vector2fc.x(), this.y() / vector2fc.y())

/**
 * 将两个向量[vector2fc]相除并赋值给自身
 * @receiver [Vector2i]
 * @param vector2fc 目标向量[vector2fc]
 */
operator fun Vector2i.divAssign(vector2fc: Vector2ic) {
    this.div(vector2fc)
}

/**
 * 将两个向量[vector2fc]取余返回一个新的副本
 * @receiver [vector2fc]
 * @param vector2fc 目标向量[vector2fc]
 * @return [Vector2i]
 */
operator fun Vector2ic.rem(vector2fc: Vector2ic): Vector2ic = this.copy(this.x() % vector2fc.x(), this.y() % vector2fc.y())

/**
 * 将两个向量[vector2fc]取余并赋值给自身
 * @receiver [Vector2i]
 * @param vector2fc 目标向量[vector2fc]
 */
operator fun Vector2i.remAssign(vector2fc: Vector2ic) {
    this.x %= vector2fc.x()
    this.y %= vector2fc.y()
}