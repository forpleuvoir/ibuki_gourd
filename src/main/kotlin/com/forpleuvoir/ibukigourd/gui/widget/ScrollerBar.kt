package com.forpleuvoir.ibukigourd.gui.widget

import com.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import com.forpleuvoir.ibukigourd.gui.base.mouseHover
import com.forpleuvoir.ibukigourd.gui.texture.IbukiGourdTextures.SCROLLER_BACKGROUND
import com.forpleuvoir.ibukigourd.gui.widget.button.Button
import com.forpleuvoir.ibukigourd.gui.widget.button.ButtonThemes
import com.forpleuvoir.ibukigourd.gui.widget.button.button
import com.forpleuvoir.ibukigourd.input.Mouse
import com.forpleuvoir.ibukigourd.render.base.Arrangement
import com.forpleuvoir.ibukigourd.render.renderTexture
import com.forpleuvoir.ibukigourd.util.NextAction
import com.forpleuvoir.ibukigourd.util.mouseX
import com.forpleuvoir.ibukigourd.util.mouseY
import com.forpleuvoir.nebula.common.color.ARGBColor
import com.forpleuvoir.nebula.common.color.Color
import com.forpleuvoir.nebula.common.color.Colors
import com.forpleuvoir.nebula.common.util.clamp
import net.minecraft.client.util.math.MatrixStack
import kotlin.math.abs

open class ScrollerBar(
	length: Float,
	thickness: Float = 10f,
	var amountStep: () -> Float,
	var totalAmount: () -> Float,
	/**
	 * 滚动条所占总长度的百分比 Range(0f..1f)
	 */
	var barLength: () -> Float,
	private val arrangement: Arrangement = Arrangement.Vertical,
	var color: () -> ARGBColor = { Color(138, 198, 209) },
	barColor: () -> ARGBColor = { Colors.WHITE },
) : ClickableElement() {

	val bar: Button = button(barColor, height = 0f, theme = ButtonThemes.ScrollerBar) {
		fixed = true
	}

	init {
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
			val fixed = value.clamp(0f..totalAmount())
			amountReceiver?.invoke(fixed)
			arrangement.switch({
				bar.transform.y = scrollerLength * fixed / totalAmount()
			}, {
				bar.transform.x = scrollerLength * fixed / totalAmount()
			})
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
		get() = (arrangement.switch({ bar.transform.y }, { bar.transform.x }) / scrollerLength).clamp(0f..1f)


	private fun calcBarLength() {
		arrangement.switch({
			bar.transform.height = barLength() * this.transform.height
		}, {
			bar.transform.width = barLength() * this.transform.width
		})
	}

	override fun tick() {
		super.tick()
		if (pressed && mouseHover()) {
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
		if (!active || !dragging) return NextAction.Continue
		arrangement.switch({
			bar.transform.y = (bar.transform.y + deltaY).clamp(barPositionRange)
		}, {
			bar.transform.x = (bar.transform.x + deltaX).clamp(barPositionRange)
		})
		amountReceiver?.invoke(amount)
		return super.onMouseDragging(mouseX, mouseY, button, deltaX, deltaY)
	}

	override fun onMouseClick(mouseX: Float, mouseY: Float, button: Mouse): NextAction {
		mouseHover {
			setFromMouse(mouseX, mouseY)
		}
		return super.onMouseClick(mouseX, mouseY, button)
	}

	override fun onMouseScrolling(mouseX: Float, mouseY: Float, amount: Float): NextAction {
		mouseHover {
			this@ScrollerBar.amount -= amountStep() * amount
		}
		return super.onMouseScrolling(mouseX, mouseY, amount)
	}


	override fun onRenderBackground(matrixStack: MatrixStack, delta: Float) {
		calcBarLength()
		renderTexture(matrixStack, this.transform, SCROLLER_BACKGROUND, color())
	}

}

fun ElementContainer.scroller(
	length: Float,
	thickness: Float = 10f,
	amountStep: () -> Float,
	totalAmount: () -> Float,
	barLength: () -> Float,
	arrangement: Arrangement = Arrangement.Vertical,
	color: () -> ARGBColor = { Color(138, 198, 209) },
	barColor: () -> ARGBColor = { Colors.WHITE },
	scope: ScrollerBar.() -> Unit = {}
): ScrollerBar = this.addElement(ScrollerBar(length, thickness, amountStep, totalAmount, barLength, arrangement, color, barColor).apply(scope))
