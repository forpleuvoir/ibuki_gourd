@file:Suppress("unused", "MemberVisibilityCanBePrivate")

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
open class Transform(
	val position: Vector3f = Vector3f(),
	/**
	 * 是否为世界坐标轴
	 */
	isWorldAxis: Boolean = false,
	val width: Float = 0.0f,
	val height: Float = 0.0f,
	var parent: Transform? = null,
) {

	var isWorldAxis: Boolean = isWorldAxis
		set(value) {
			field = value
			if (value) parent?.let { position += it.worldPosition }
		}

	val worldPosition: Vector3f
		get() {
			if (isWorldAxis) return position
			var pos = position
			parent?.let { pos = it.worldPosition + position }
			return pos
		}

	val asRect: Rectangle get() = Rectangle(VertexImpl(worldPosition), width, height)

	val x = position.x

	val worldX = worldPosition.x

	val y = position.y

	val worldY = worldPosition.y

	val z = position.z

	val worldZ = worldPosition.z

	val top: Float get() = y

	val worldTop: Float get() = worldY

	val bottom: Float get() = top + height

	val worldBottom: Float get() = worldTop + height

	val left: Float get() = x

	val worldLeft: Float get() = worldX

	val right: Float get() = left + width

	val worldRight: Float get() = worldLeft + width


	val center: Vector3f get() = Vector3f(x + this.width / 2, y + this.height / 2, z)

	val worldCenter: Vector3f get() = Vector3f(worldX + this.width / 2, worldY + this.height / 2, worldZ)


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
