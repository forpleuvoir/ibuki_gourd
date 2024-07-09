package moe.forpleuvoir.ibukigourd.render.math

import org.joml.Vector2fc
import org.joml.Vector2i
import org.joml.Vector2ic

fun Vector2fc.asInt(): Vector2ic = Vector2i(x.toInt(), y.toInt())

operator fun Vector2ic.component1(): Int = x()

operator fun Vector2ic.component2(): Int = y()