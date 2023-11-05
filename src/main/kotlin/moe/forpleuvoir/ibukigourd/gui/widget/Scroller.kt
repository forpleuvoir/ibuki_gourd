package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.mouseHover
import moe.forpleuvoir.ibukigourd.gui.texture.IbukiGourdTextures.SCROLLER_BACKGROUND
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.ButtonThemes
import moe.forpleuvoir.ibukigourd.gui.widget.button.button
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.helper.renderTexture
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.ibukigourd.util.mouseX
import moe.forpleuvoir.ibukigourd.util.mouseY
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.clamp
import kotlin.math.abs

open class Scroller(
	length: Float,
	thickness: Float = 10f,
	var amountStep: () -> Float,
	var totalAmount: () -> Float,
	/**
	 * 滚动条所占总长度的百分比 Range(0f..1f)
	 */
	var barLength: () -> Float,
	private val arrangement: Arrangement = Arrangement.Vertical,
	var color: () -> ARGBColor = { Colors.WHITE },
	barColor: () -> ARGBColor = { Colors.WHITE },
) : ClickableElement() {

	val bar: Button = button(color = barColor, pressOffset = 0f, theme = ButtonThemes.ScrollerBar) {
		fixed = true
		playClickSound = false
	}

	override var visible: Boolean = true

	init {
		transform.fixedWidth = true
		transform.fixedHeight = true
		transform.resizeCallback = { width, height ->
			arrangement.switch({
				bar.transform.width = width
			}, {
				bar.transform.height = height
			})
		}
		arrangement.switch({
			transform.width = thickness
			transform.height = length
		}, {
			transform.width = length
			transform.height = thickness
		})
	}

	var amount: Float
		get() = (totalAmount() * progress).clamp(0f..totalAmount())
		set(value) {
			val fixedValue = value.clamp(0f..totalAmount())
			val barPosition = scrollerLength * if (totalAmount() == 0f) 0f else fixedValue / totalAmount()
			arrangement.switch({
				bar.transform.y = barPosition
			}, {
				bar.transform.x = barPosition
			})
			amountReceiver?.invoke(fixedValue)
		}

	var amountReceiver: ((amount: Float) -> Unit)? = null

	open val scrollerLength: Float
		get() = arrangement.switch({
			this.transform.height - (barLength() * this.transform.height)
		}, {
			this.transform.width - (barLength() * this.transform.width)
		})

	val barPositionRange: ClosedFloatingPointRange<Float> get() = 0f..scrollerLength

	/**
	 * 进度 Range(0.0f..1.0f)
	 */
	var progress: Float
		set(value) {
			val fixed = value.clamp(0f..1f)
			amount = (totalAmount() * fixed).clamp(0f..totalAmount())
		}
		get() {
			if (scrollerLength == 0f) return 1f
			return (arrangement.switch({ bar.transform.y }, { bar.transform.x }) / scrollerLength).clamp(0f..1f)
		}


	private fun calcBarLength() {
		arrangement.switch({
			bar.transform.height = barLength() * this.transform.height
		}, {
			bar.transform.width = barLength() * this.transform.width
		})
	}

	override fun tick() {
		super.tick()
		if (pressed && mouseHover() && visible) {
			setFromMouse(mouseX.toFloat(), mouseY.toFloat())
		}
	}

	protected open fun setFromMouse(mouseX: Float, mouseY: Float) {
		if (bar.mouseHover()) return
		arrangement.switch({
			val a = mouseY - bar.transform.worldY
			if (a > 0) {
				bar.transform.y = (bar.transform.y + amountStep().coerceAtMost(abs(a))).clamp(barPositionRange)
			} else {
				bar.transform.y = (bar.transform.y - amountStep().coerceAtMost(abs(a))).clamp(barPositionRange)
			}
		}, {
			val a = mouseX - bar.transform.worldX
			if (a > 0) {
				bar.transform.x = (bar.transform.x + amountStep().coerceAtMost(abs(a))).clamp(barPositionRange)
			} else {
				bar.transform.x = (bar.transform.x - amountStep().coerceAtMost(abs(a))).clamp(barPositionRange)
			}
		})
		amountReceiver?.invoke(amount)
	}

	override fun onMouseDragging(mouseX: Float, mouseY: Float, button: Mouse, deltaX: Float, deltaY: Float): NextAction {
		if (!active || !dragging || !visible) return NextAction.Continue
		arrangement.switch({
			bar.transform.y = (bar.transform.y + deltaY).clamp(barPositionRange)
		}, {
			bar.transform.x = (bar.transform.x + deltaX).clamp(barPositionRange)
		})
		amountReceiver?.invoke(amount)
		return super.onMouseDragging(mouseX, mouseY, button, deltaX, deltaY)
	}

	override fun onMouseClick(mouseX: Float, mouseY: Float, button: Mouse): NextAction {
		if (!visible) return NextAction.Continue
		mouseHover {
			setFromMouse(mouseX, mouseY)
		}
		return super.onMouseClick(mouseX, mouseY, button)
	}

	override fun onMouseScrolling(mouseX: Float, mouseY: Float, amount: Float): NextAction {
		mouseHover {
			this@Scroller.amount -= amountStep() * amount
		}
		return super.onMouseScrolling(mouseX, mouseY, amount)
	}


	override fun onRenderBackground(renderContext: RenderContext) {
		calcBarLength()
		renderTexture(renderContext.matrixStack, this.transform, SCROLLER_BACKGROUND, color())
	}

}

fun ElementContainer.scroller(
	length: Float,
	thickness: Float = 10f,
	amountStep: () -> Float,
	totalAmount: () -> Float,
	barLength: () -> Float,
	arrangement: Arrangement = Arrangement.Vertical,
	color: () -> ARGBColor = { Colors.WHITE },
	barColor: () -> ARGBColor = { Colors.WHITE },
	scope: Scroller.() -> Unit = {}
): Scroller = this.addElement(Scroller(length, thickness, amountStep, totalAmount, barLength, arrangement, color, barColor).apply(scope))
