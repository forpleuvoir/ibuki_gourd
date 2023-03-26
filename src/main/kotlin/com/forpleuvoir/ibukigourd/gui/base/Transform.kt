package com.forpleuvoir.ibukigourd.gui.base

import com.forpleuvoir.ibukigourd.render.base.Rectangle
import com.forpleuvoir.ibukigourd.render.base.math.Vector3
import com.forpleuvoir.ibukigourd.render.base.math.Vector3f
import com.forpleuvoir.ibukigourd.render.base.vertex.VertexImpl
import com.forpleuvoir.ibukigourd.util.mouseX
import com.forpleuvoir.ibukigourd.util.mouseY
import com.forpleuvoir.nebula.common.ifc

/**
 * 变换青春版
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
open class Transform(
	val position: Vector3f = Vector3f(),
	val width: Float = 0.0f,
	val height: Float = 0.0f,
	var parent: Transform? = null,
	var root: Transform? = null
) {

	val asRect: Rectangle get() = Rectangle(VertexImpl(position), width, height)

	val top: Float get() = position.y

	val bottom: Float get() = position.y + height

	val left: Float get() = position.x

	val right: Float get() = position.x + width

	/**
	 * 鼠标是否在此元素[Element]内部
	 * @param mouseX Number
	 * @param mouseY Number
	 * @return Boolean
	 */
	fun isMouseOvered(mouseX: Number, mouseY: Number): Boolean =
		mouseX.toDouble() in left..right && mouseY.toDouble() in top..bottom

	fun move(vector3: Vector3<out Number>) {
		position += Vector3f(vector3)
	}

	fun move(x: Number, y: Number, z: Number) {
		position += Vector3f(x, y, z)
	}

	fun moveTo(vector3: Vector3<out Number>) {
		position.set(Vector3f(vector3))
	}

	fun moveTo(x: Number, y: Number, z: Number) {
		position.set(Vector3f(x, y, z))
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
