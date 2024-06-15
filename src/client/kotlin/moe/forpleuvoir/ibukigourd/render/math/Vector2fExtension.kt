package moe.forpleuvoir.ibukigourd.render.math

import moe.forpleuvoir.nebula.serialization.base.SerializeArray
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.extensions.checkType
import moe.forpleuvoir.nebula.serialization.extensions.serializeObject
import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector3fc

fun Vector3fc.toVector2fc(): Vector2fc = Vector2f(x, y)

/**
 * 创建一个向量[Vector2f]对象
 *
 * @param x Number
 * @param y Number
 * @return [Vector2f]
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Vector2f(x: Number = 0f, y: Number = 0f): Vector2f = Vector2f(x.toFloat(), y.toFloat())

/**
 * 复制一个向量
 * @receiver [Vector2f]
 * @param x Number 用于覆盖的[Vector2f.x]值
 * @param y Number 用于覆盖的[Vector2f.y]值
 * @return [Vector2f]
 */
@JvmOverloads
fun Vector2f.copy(x: Number = this.x, y: Number = this.y): Vector2f = Vector2f(x.toFloat(), y.toFloat())

/**
 * 复制一个向量
 * @receiver [Vector2fc]
 * @param x Number 用于覆盖的[Vector2fc.x]值
 * @param y Number 用于覆盖的[Vector2fc.y]值
 * @return [Vector2f]
 */
@JvmOverloads
fun Vector2fc.copy(x: Number = this.x(), y: Number = this.y()): Vector2fc = Vector2f(x.toFloat(), y.toFloat())

/**
 * 获取[Vector2f.x]值
 */
val Vector2fc.x: Float
    get() = this.x()

/**
 * 获取[Vector2f.y]值

 */
val Vector2fc.y: Float
    get() = this.y()

/**
 * 将[Vector2f]序列化
 * @receiver [Vector2fc]
 * @return [SerializeElement]
 */
fun Vector2fc.serialization(): SerializeElement = serializeObject {
    "x" to x()
    "y" to y()
}

fun vector2fDeserialization(element: SerializeElement): Vector2f {
    return Vector2f().apply { deserialization(element) }
}

/**
 * 将[Vector2f]反序列化
 * @receiver [Vector2f]
 * @param element [SerializeElement]
 */
fun Vector2f.deserialization(element: SerializeElement) {
    element.checkType<Unit>()
            .check<SerializeArray> {
                this.x = it[0].asFloat
                this.y = it[1].asFloat
            }.check<SerializeObject> {
                this.x = it["x"]!!.asFloat
                this.y = it["y"]!!.asFloat
            }.getOrThrow()
}

/**
 * 将两个向量[vector2fc]相加返回一个新的副本
 * @receiver [vector2fc]
 * @param vector2fc 目标向量[vector2fc]
 * @return [Vector2f]
 */
operator fun Vector2fc.plus(vector2fc: Vector2fc): Vector2fc = this.copy(this.x() + vector2fc.x(), this.y() + vector2fc.y())

/**
 * 将两个向量[Vector2fc]相加并赋值给自身
 * @receiver [vector2f]
 * @param vector2f 目标向量[Vector2fc]
 */
operator fun Vector2f.plusAssign(vector2f: Vector2fc) {
    this.add(vector2f)
}

/**
 * 将两个向量[vector2fc]相减返回一个新的副本
 * @receiver [vector2fc]
 * @param vector2fc 目标向量[vector2fc]
 * @return [Vector2f]
 */
operator fun Vector2fc.minus(vector2fc: Vector2fc): Vector2fc = this.copy(this.x() - vector2fc.x(), this.y() - vector2fc.y())

/**
 * 将两个向量[Vector2fc]相减并赋值给自身
 * @receiver [vector2f]
 * @param vector2f 目标向量[Vector2fc]
 */
operator fun Vector2f.minusAssign(vector2f: Vector2fc) {
    this.sub(vector2f)
}

/**
 * 将两个向量[vector2fc]相乘返回一个新的副本
 * @receiver [vector2fc]
 * @param vector2fc 目标向量[vector2fc]
 * @return [Vector2f]
 */
operator fun Vector2fc.times(vector2fc: Vector2fc): Vector2fc = this.copy(this.x() * vector2fc.x(), this.y() * vector2fc.y())

/**
 * 将两个向量[vector2fc]相乘并赋值给自身
 * @receiver [Vector2f]
 * @param vector2fc 目标向量[vector2fc]
 */
operator fun Vector2f.timesAssign(vector2fc: Vector2fc) {
    this.mul(vector2fc)
}

/**
 * 将两个向量[vector2fc]相除返回一个新的副本
 * @receiver [vector2fc]
 * @param vector2fc 目标向量[vector2fc]
 * @return [Vector2f]
 */
operator fun Vector2fc.div(vector2fc: Vector2fc): Vector2fc = this.copy(this.x() / vector2fc.x(), this.y() / vector2fc.y())

/**
 * 将两个向量[vector2fc]相除并赋值给自身
 * @receiver [Vector2f]
 * @param vector2fc 目标向量[vector2fc]
 */
operator fun Vector2f.divAssign(vector2fc: Vector2fc) {
    this.div(vector2fc)
}

/**
 * 将两个向量[vector2fc]取余返回一个新的副本
 * @receiver [vector2fc]
 * @param vector2fc 目标向量[vector2fc]
 * @return [Vector2f]
 */
operator fun Vector2fc.rem(vector2fc: Vector2fc): Vector2fc = this.copy(this.x() % vector2fc.x(), this.y() % vector2fc.y())

/**
 * 将两个向量[vector2fc]取余并赋值给自身
 * @receiver [Vector2f]
 * @param vector2fc 目标向量[vector2fc]
 */
operator fun Vector2f.remAssign(vector2fc: Vector2fc) {
    this.x %= vector2fc.x()
    this.y %= vector2fc.y()
}