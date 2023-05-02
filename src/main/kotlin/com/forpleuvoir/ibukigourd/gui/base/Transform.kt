@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.forpleuvoir.ibukigourd.gui.base

import com.forpleuvoir.ibukigourd.gui.base.element.Element
import com.forpleuvoir.ibukigourd.render.base.math.Vector3
import com.forpleuvoir.ibukigourd.render.base.math.Vector3f
import com.forpleuvoir.ibukigourd.render.base.rectangle.Rectangle
import com.forpleuvoir.ibukigourd.render.base.rectangle.rect
import com.forpleuvoir.ibukigourd.render.base.vertex.vertex
import com.forpleuvoir.ibukigourd.util.mouseX
import com.forpleuvoir.ibukigourd.util.mouseY
import com.forpleuvoir.nebula.common.ifc

/**
 * 变换青春版
 */
class Transform(
	override val position: Vector3f = Vector3f(),
	/**
	 * 是否为世界坐标轴
	 */
	isWorldAxis: Boolean = false,
	override var width: Float = 0.0f,
	override var height: Float = 0.0f,
	var parent: Transform? = null,
) : Rectangle<Vector3f> {

	override val vertexes: Array<Vector3f>
		get() = arrayOf(
			position,
			position.y(y + height),
			position.xyz(x + width, y + height),
			position.x(x + width)
		)

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

	var fixedWidth: Boolean = false

	var fixedHeight: Boolean = false

	val asWorldRect: Rectangle<Vector3<Float>> get() = rect(worldPosition, width, height)

	override var x
		get() = position.x
		set(value) {
			position.x = value
		}

	var worldX
		get() = worldPosition.x
		set(value) {
			if (isWorldAxis) position.x = value
			else {
				val delta = value - worldPosition.x
				position.x += delta
			}
		}

	override var y
		get() = position.y
		set(value) {
			position.y = value
		}

	var worldY
		get() = worldPosition.y
		set(value) {
			if (isWorldAxis) position.y = value
			else {
				val delta = value - worldPosition.y
				position.y += delta
			}
		}

	override var z
		get() = position.z
		set(value) {
			position.z = value
		}

	var worldZ
		get() = worldPosition.z
		set(value) {
			if (isWorldAxis) position.z = value
			else {
				val delta = value - worldPosition.z
				position.z += delta
			}
		}

	override val top: Float get() = y

	val worldTop: Float get() = worldY

	override val bottom: Float get() = top + height

	val worldBottom: Float get() = worldTop + height

	override val left: Float get() = x

	val worldLeft: Float get() = worldX

	override val right: Float get() = left + width

	val worldRight: Float get() = worldLeft + width

	override var center: Vector3<Float>
		get() = vertex(x + this.halfWidth, y + this.halfHeight, z)
		set(value) {
			translate(value.x - center.x, value.y - center.y, value.z - center.z)
		}

	val worldCenter: Vector3f get() = Vector3f(worldX + this.halfWidth, worldY + this.halfHeight, worldZ)


	/**
	 * 鼠标是否在此元素[Transform]内部
	 * @param mouseX Number
	 * @param mouseY Number
	 * @return Boolean
	 */
	fun isMouseOvered(mouseX: Number, mouseY: Number): Boolean =
		mouseX.toDouble() in worldLeft..worldRight && mouseY.toDouble() in worldTop..worldBottom

	fun translate(vector3: Vector3<out Number>) {
		position += Vector3f(vector3)
	}

	fun translate(x: Number = 0, y: Number = 0, z: Number = 0) {
		position += Vector3f(x.toFloat(), y.toFloat(), z.toFloat())
	}

	fun translateTo(vector3: Vector3<out Number>) {
		position.set(Vector3f(vector3))
	}

	fun translateTo(x: Number = position.x, y: Number = position.y, z: Number = position.z) {
		position.set(Vector3f(x.toFloat(), y.toFloat(), z.toFloat()))
	}

}

/**
 * 当鼠标位于此元素[Transform]内部时调用
 * @param action [@kotlin.ExtensionFunctionType] Function1<Element, Unit>
 */
inline fun Transform.mouseHover(action: Transform.() -> Unit) = isMouseOvered(mouseX, mouseY).ifc { action() }

/**
 * 当鼠标位于此元素内部时调用
 * @param action [@kotlin.ExtensionFunctionType] Function1<Element, Unit>
 */
inline fun Element.mouseHover(action: Element.() -> Unit) = transform.isMouseOvered(mouseX, mouseY).ifc { action() }

/**
 * 鼠标是否在此元素内
 * @receiver T
 * @return Boolean
 */
fun Transform.mouseHover(): Boolean = isMouseOvered(mouseX, mouseY)

/**
 * 鼠标是否在此元素内
 * @receiver T
 * @return Boolean
 */
fun Element.mouseHover(): Boolean = transform.isMouseOvered(mouseX, mouseY)