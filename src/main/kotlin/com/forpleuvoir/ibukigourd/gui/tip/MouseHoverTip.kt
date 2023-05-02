package com.forpleuvoir.ibukigourd.gui.tip

import com.forpleuvoir.ibukigourd.gui.base.Direction
import com.forpleuvoir.ibukigourd.gui.base.Direction.*
import com.forpleuvoir.ibukigourd.gui.base.Margin
import com.forpleuvoir.ibukigourd.gui.base.element.Element
import com.forpleuvoir.ibukigourd.gui.base.mouseHover
import com.forpleuvoir.ibukigourd.gui.texture.IbukiGourdTextures.TIP
import com.forpleuvoir.ibukigourd.gui.texture.IbukiGourdTextures.TIP_ARROW_BOTTOM
import com.forpleuvoir.ibukigourd.gui.texture.IbukiGourdTextures.TIP_ARROW_LEFT
import com.forpleuvoir.ibukigourd.gui.texture.IbukiGourdTextures.TIP_ARROW_RIGHT
import com.forpleuvoir.ibukigourd.gui.texture.IbukiGourdTextures.TIP_ARROW_TOP
import com.forpleuvoir.ibukigourd.mod.gui.Theme
import com.forpleuvoir.ibukigourd.mod.gui.Theme.TIP.ARROW_OFFSET
import com.forpleuvoir.ibukigourd.mod.gui.Theme.TIP.DELAY
import com.forpleuvoir.ibukigourd.mod.gui.Theme.TIP.MARGIN
import com.forpleuvoir.ibukigourd.mod.gui.Theme.TIP.PADDING
import com.forpleuvoir.ibukigourd.render.base.math.Vector3
import com.forpleuvoir.ibukigourd.render.base.rectangle.Rectangle
import com.forpleuvoir.ibukigourd.render.base.rectangle.rect
import com.forpleuvoir.ibukigourd.render.base.vertex.vertex
import com.forpleuvoir.ibukigourd.render.renderTexture
import com.forpleuvoir.ibukigourd.util.mc
import com.forpleuvoir.nebula.common.color.Color
import com.forpleuvoir.nebula.common.util.clamp
import net.minecraft.client.util.math.MatrixStack

/**
 * 只有鼠标悬浮在元素上才会显示的Tip
 * @property parent Element
 * @property displayDelay UInt
 * @property backgroundColor Color
 * @property forcedDirection Direction?
 * @property direction Direction
 * @property latestParent Rectangle
 * @property tickCounter UInt
 * @property visible Boolean
 * @property active Boolean
 * @constructor
 */
@Suppress("MemberVisibilityCanBePrivate")
open class MouseHoverTip(
	/**
	 * 父元素
	 */
	final override var parent: Element,
	/**
	 * 延迟显示时间
	 */
	var displayDelay: UInt = DELAY.toUInt(),
	padding: Margin = PADDING,
	margin: Margin = MARGIN,
	var backgroundColor: Color = Theme.TIP.BACKGROUND_COLOR,
	/**
	 * 出现在父元素的位置方向，为空则自动选择合适位置
	 */
	var forcedDirection: Direction? = null
) : Tip() {

	init {
		transform.parent = parent.transform
		padding(padding)
		margin(margin)
	}

	protected open var direction: Direction = if (this.forcedDirection != null) this.forcedDirection!! else Top

	override fun arrange() {
		super.arrange()
		checkDirection()
		calcPosition()
	}

	/**
	 * 父元素最新的状态，如果没有变则不需要更新位置
	 */
	protected var latestParent: Rectangle<Vector3<Float>> = rect(vertex(0, 0, 0), 0, 0)

	protected open var tickCounter: UInt = 0u
		set(value) {
			field = value
			if (visible) {
				postToRenderList()
				if (!Rectangle.equals(latestParent, transform.parent!!.asWorldRect)) {
					checkDirection()
					calcPosition()
					latestParent = transform.parent!!.asWorldRect
				}
			}
		}

	protected open fun checkDirection() {
		val parent = this.transform.parent!!
		val canPlace: (Direction) -> Boolean = { dir ->
			when (dir) {
				Left -> parent.worldLeft - (transform.width + margin.left) > 0
				Right -> parent.worldRight + (transform.width + margin.right) < mc.window.scaledWidth
				Top -> parent.worldTop - (transform.height + margin.top) > 0
				Bottom -> parent.worldBottom - (transform.height + margin.bottom) < mc.window.scaledHeight
			}
		}
		this.direction =
			if (this.forcedDirection != null) this.forcedDirection!!
			else if (canPlace(Top)) Top
			else if (canPlace(Right)) Right
			else if (canPlace(Bottom)) Bottom
			else Left
	}

	protected open fun calcPosition() {
		val parent = this.transform.parent!!
		when (direction) {
			Left -> transform.translateTo(-(transform.width + margin.left), -(transform.halfHeight - parent.halfHeight))
			Right -> transform.translateTo(parent.width + margin.right, -(transform.halfHeight - parent.halfHeight))
			Top -> transform.translateTo(-(transform.halfWidth - parent.halfWidth), -(transform.height + margin.top))
			Bottom -> transform.translateTo(-(transform.halfWidth - parent.halfWidth), parent.height + margin.top)
		}
		transform.worldY = transform.worldY.clamp(0f, mc.window.scaledHeight - transform.height)
		transform.worldX = transform.worldX.clamp(0f, mc.window.scaledWidth - transform.width)
	}

	override val visible: Boolean get() = tickCounter >= displayDelay

	final override var active: Boolean
		get() = tickCounter >= displayDelay
		@Deprecated("Do not set the active value of Tip")
		set(@Suppress("UNUSED_PARAMETER") value) {
			throw NotImplementedError("Do not set the active value of Tip")
		}


	override fun tick() {
		if (transform.parent!!.mouseHover()) {
			tickCounter++
		} else {
			tickCounter = 0u
			removeFromRenderList()
		}
	}

	override fun onRenderBackground(matrixStack: MatrixStack, delta: Float) {
		renderTexture(matrixStack, this.transform, TIP, backgroundColor)
		val arrow = when (direction) {
			Left ->
				TIP_ARROW_LEFT to rect(
					transform.worldRight - ARROW_OFFSET.left,
					transform.parent!!.worldCenter.y - MARGIN.left / 2,
					0f,
					MARGIN.left, MARGIN.left
				)

			Right ->
				TIP_ARROW_RIGHT to rect(
					transform.worldLeft - MARGIN.right + ARROW_OFFSET.right,
					transform.parent!!.worldCenter.y - MARGIN.left / 2,
					0f,
					MARGIN.right, MARGIN.right
				)

			Top ->
				TIP_ARROW_TOP to rect(
					transform.parent!!.worldCenter.x - MARGIN.top / 2,
					transform.worldBottom - ARROW_OFFSET.top,
					0f,
					MARGIN.top, MARGIN.top
				)

			Bottom ->
				TIP_ARROW_BOTTOM to rect(
					transform.parent!!.worldCenter.x - MARGIN.bottom / 2,
					transform.worldTop - MARGIN.bottom + ARROW_OFFSET.bottom,
					0f,
					MARGIN.bottom, MARGIN.bottom
				)
		}
		renderTexture(matrixStack, arrow.second, arrow.first, backgroundColor)
	}

}

inline fun Element.mouseHoverTip(
	displayDelay: UInt,
	padding: Margin,
	margin: Margin,
	color: Color,
	forcedDirection: Direction?,
	scope: MouseHoverTip.() -> Unit
): MouseHoverTip {
	this.tip = MouseHoverTip(this, displayDelay, padding, margin, color, forcedDirection).apply(scope)
	return this.tip as MouseHoverTip
}

inline fun Element.mouseHoverTip(
	scope: MouseHoverTip.() -> Unit
): MouseHoverTip {
	this.tip = MouseHoverTip(this).apply(scope)
	return this.tip as MouseHoverTip
}
