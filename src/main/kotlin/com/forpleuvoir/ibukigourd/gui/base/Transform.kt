package com.forpleuvoir.ibukigourd.gui.base

import com.forpleuvoir.ibukigourd.util.math.Vector3
import com.forpleuvoir.ibukigourd.util.math.Vector3d
import com.forpleuvoir.ibukigourd.util.mouseX
import com.forpleuvoir.ibukigourd.util.mouseY
import com.forpleuvoir.nebula.common.ifc

/**
 * 变换青春版
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
open class Transform(
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

	/**
	 * 鼠标是否在此元素[Element]内部
	 * @param mouseX Number
	 * @param mouseY Number
	 * @return Boolean
	 */
	fun isMouseOvered(mouseX: Number, mouseY: Number): Boolean =
		mouseX.toDouble() in left..right && mouseY.toDouble() in top..bottom

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

/**
 * 当鼠标位于此元素[Element]内部时调用
 * @param action [@kotlin.ExtensionFunctionType] Function1<Element, Unit>
 */
inline fun <T : Transform> T.mouseHover(action: T.() -> Unit) = isMouseOvered(mouseX, mouseY).ifc { action() }

/**
 * 鼠标是否在此元素内
 * @receiver T
 * @return Boolean
 */
fun <T : Transform> T.mouseHover(): Boolean = isMouseOvered(mouseX, mouseY)
