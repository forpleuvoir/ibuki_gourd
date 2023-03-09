package com.forpleuvoir.ibukigourd.gui.base

import com.forpleuvoir.ibukigourd.util.math.Vector3
import com.forpleuvoir.ibukigourd.util.math.Vector3d

/**
 * 变换青春版
 */
class Transform(
	val position: Vector3d = Vector3d(),
	val width: Double = 0.0,
	val height: Double = 0.0,
	var parent: Transform? = null,
	var root: Transform? = null
) {
	val top: Double get() = position.y

	val bottom: Double get() = position.y + height

	val left: Double get() = position.x

	val right: Double get() = position.x + width

	fun move(vector3: Vector3<out Number>) {
		position += Vector3d(vector3)
	}

	fun move(x: Number, y: Number, z: Number) {
		position += Vector3d(x, y, z)
	}

	fun moveTo(vector3: Vector3<out Number>) {
		position.set(Vector3d(vector3))
	}

	fun moveTo(x: Number, y: Number, z: Number) {
		position.set(Vector3d(x, y, z))
	}

}

