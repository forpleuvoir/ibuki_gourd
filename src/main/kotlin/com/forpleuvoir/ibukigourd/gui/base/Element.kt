package com.forpleuvoir.ibukigourd.gui.base

import org.joml.Vector3d

interface Element {

	val position: Vector3d

	var x: Double
		get() = position.x
		set(value) {
			position.x = value
		}

	var y: Double
		get() = position.y
		set(value) {
			position.y = value
		}

	var z: Double
		get() = position.z
		set(value) {
			position.z = value
		}

	val localPosition: Vector3d

	val width: Double

	val height: Double


}