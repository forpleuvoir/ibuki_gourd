package moe.forpleuvoir.ibukigourd.gui.tip

import moe.forpleuvoir.ibukigourd.gui.base.Direction
import moe.forpleuvoir.ibukigourd.gui.base.Direction.*
import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.base.mouseHover
import moe.forpleuvoir.ibukigourd.gui.texture.IbukiGourdTextures.TIP
import moe.forpleuvoir.ibukigourd.gui.texture.IbukiGourdTextures.TIP_ARROW_BOTTOM
import moe.forpleuvoir.ibukigourd.gui.texture.IbukiGourdTextures.TIP_ARROW_LEFT
import moe.forpleuvoir.ibukigourd.gui.texture.IbukiGourdTextures.TIP_ARROW_RIGHT
import moe.forpleuvoir.ibukigourd.gui.texture.IbukiGourdTextures.TIP_ARROW_TOP
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.mod.gui.Theme
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TIP.ARROW_OFFSET
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TIP.DELAY
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TIP.MARGIN
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TIP.PADDING
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.rectangle.Rectangle
import moe.forpleuvoir.ibukigourd.render.base.rectangle.rect
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex
import moe.forpleuvoir.ibukigourd.render.renderTexture
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.util.clamp

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
	parent: Element,
	tipHandler: () -> TipHandler = { parent.screen()!! },
	/**
	 * 延迟显示时间
	 */
	var displayDelay: UInt = DELAY.toUInt(),
	padding: Margin = PADDING,
	margin: Margin = MARGIN,
	var backgroundColor: ARGBColor = Theme.TIP.BACKGROUND_COLOR,
	/**
	 * 出现在父元素的位置方向，为空则自动选择合适位置
	 */
	var forcedDirection: Direction? = null
) : Tip({ parent }, tipHandler) {

	init {
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

	protected var keepDisplay: Boolean = false

	protected open var tickCounter: UInt = 0u
		set(value) {
			field = value
			if (tickCounter >= displayDelay) {
				if (field == displayDelay) {
					visible = push()
					if (!visible) field = 0u
				}
				if (!Rectangle.equals(latestParent, transform.parent()!!.asWorldRect)) {
					checkDirection()
					calcPosition()
					latestParent = transform.parent()!!.asWorldRect
				}
			}
		}

	protected open fun checkDirection() {
		val parent = this.transform.parent()!!
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
		val parent = this.transform.parent()!!
		when (direction) {
			Left -> transform.translateTo(-(transform.width + margin.left), -(transform.halfHeight - parent.halfHeight))
			Right -> transform.translateTo(parent.width + margin.right, -(transform.halfHeight - parent.halfHeight))
			Top -> transform.translateTo(-(transform.halfWidth - parent.halfWidth), -(transform.height + margin.top))
			Bottom -> transform.translateTo(-(transform.halfWidth - parent.halfWidth), parent.height + margin.top)
		}
		transform.worldY = transform.worldY.clamp(0f..mc.window.scaledHeight - transform.height)
		transform.worldX = transform.worldX.clamp(0f..mc.window.scaledWidth - transform.width)
	}

	override var visible: Boolean = false

	final override var active: Boolean
		get() = tickCounter >= displayDelay
		@Deprecated("Do not set the active value of Tip")
		set(@Suppress("UNUSED_PARAMETER") value) {
			throw NotImplementedError("Do not set the active value of Tip")
		}


	override fun tick() {
		if (transform.parent()!!.mouseHover()) {
			tickCounter++
		} else if (!keepDisplay) {
			visible = !pop()
			if (!visible) {
				tickCounter = 0u
			}
		}
	}

	override fun onRenderBackground(renderContext: RenderContext) {
		renderTexture(renderContext.matrixStack, this.transform, TIP, backgroundColor)
		val arrow = when (direction) {
			Left ->
				TIP_ARROW_LEFT to rect(
					transform.worldRight - ARROW_OFFSET.left,
					transform.parent()!!.worldCenter.y - MARGIN.left / 2,
					0f,
					MARGIN.left, MARGIN.left
				)

			Right ->
				TIP_ARROW_RIGHT to rect(
					transform.worldLeft - MARGIN.right + ARROW_OFFSET.right,
					transform.parent()!!.worldCenter.y - MARGIN.left / 2,
					0f,
					MARGIN.right, MARGIN.right
				)

			Top ->
				TIP_ARROW_TOP to rect(
					transform.parent()!!.worldCenter.x - MARGIN.top / 2,
					transform.worldBottom - ARROW_OFFSET.top,
					0f,
					MARGIN.top, MARGIN.top
				)

			Bottom ->
				TIP_ARROW_BOTTOM to rect(
					transform.parent()!!.worldCenter.x - MARGIN.bottom / 2,
					transform.worldTop - MARGIN.bottom + ARROW_OFFSET.bottom,
					0f,
					MARGIN.bottom, MARGIN.bottom
				)
		}
		renderTexture(renderContext.matrixStack, arrow.second, arrow.first, backgroundColor)
	}

	override fun onKeyPress(keyCode: KeyCode): NextAction {
		if (keyCode == Keyboard.LEFT_CONTROL) {
			keepDisplay = true
		}
		return super.onKeyPress(keyCode)
	}

	override fun onKeyRelease(keyCode: KeyCode): NextAction {
		if (keyCode == Keyboard.LEFT_CONTROL) {
			keepDisplay = false
		}
		return super.onKeyRelease(keyCode)
	}

}

inline fun Element.tip(
	displayDelay: UInt = DELAY.toUInt(),
	padding: Margin = PADDING,
	margin: Margin = MARGIN,
	color: ARGBColor = Theme.TIP.BACKGROUND_COLOR,
	forcedDirection: Direction? = null,
	scope: MouseHoverTip.() -> Unit
): MouseHoverTip {
	this.tip = MouseHoverTip(this, { this.screen()!! }, displayDelay, padding, margin, color, forcedDirection).apply(scope)
	return this.tip as MouseHoverTip
}

inline fun Element.tip(
	scope: MouseHoverTip.() -> Unit
): MouseHoverTip {
	this.tip = MouseHoverTip(this).apply(scope)
	return this.tip as MouseHoverTip
}