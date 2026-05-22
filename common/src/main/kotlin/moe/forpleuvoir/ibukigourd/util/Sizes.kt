package moe.forpleuvoir.ibukigourd.util

data class SizeFloat(val width: Float, val height: Float)
data class SizeInt(val width: Int, val height: Int)

fun Size(width: Int, height: Int): SizeFloat = SizeFloat(width.toFloat(), height.toFloat())
fun Size(width: Float, height: Float): SizeFloat = SizeFloat(width, height)
