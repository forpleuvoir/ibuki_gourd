package forpleuvoir.ibuki_gourd.gui.widget

import forpleuvoir.ibuki_gourd.gui.common.PositionDrawable
import forpleuvoir.ibuki_gourd.render.RenderUtil.drawRect
import forpleuvoir.ibuki_gourd.utils.clamp
import forpleuvoir.ibuki_gourd.utils.color.Color4f
import forpleuvoir.ibuki_gourd.utils.color.Color4i
import forpleuvoir.ibuki_gourd.utils.color.IColor
import net.minecraft.client.gui.DrawContext


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.widget

 * 文件名 Scrollbar

 * 创建时间 2022/1/4 16:36

 * @author forpleuvoir

 */
open class Scrollbar(x: Int, y: Int, width: Int, height: Int, private val maxScroll: () -> Int, private val percent: () -> Double) :
	PositionDrawable(x, y, width, height) {

	var paddingLeft: Int = 1
	var paddingRight: Int = 1
	var paddingTop: Int = 1
	var paddingBottom: Int = 1

	var color: IColor<out Number> = Color4i.WHITE
	var backgroundColor: IColor<out Number> = Color4f.WHITE.apply { alpha = 0.3f }

	var holdVisible: Boolean = false

	private val holdTime: Double = 10.0
	private var hold: Double = 0.0
		set(value) {
			field = value.clamp(0.0, holdTime).toDouble()
		}
	private var opacity: Double = 1.0
		set(value) {
			field = value.clamp(0.0, 1.0).toDouble()
		}

	var amountConsumer: ((Double) -> Unit)? = null
	var amount = 0.0
		set(value) {
			field = value.clamp(0.0, maxScroll.invoke().toDouble()).toDouble()
			opacity += 0.5
			hold = 0.0
			amountConsumer?.invoke(field)
		}

	fun setPadding(padding: Int) {
		paddingLeft = padding
		paddingRight = padding
		paddingTop = padding
		paddingBottom = padding
	}

	open operator fun plus(amount: Number) {
		this.amount += amount.toDouble()
	}

	open operator fun minus(amount: Number) {
		this.amount -= amount.toDouble()
	}

	override fun setFocused(focused: Boolean) {

	}

	override fun isFocused(): Boolean {
		return false
	}

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		if (this.maxScroll.invoke() > 0) {
			if (!holdVisible)
				updateOpacity(delta)
			//draw scrollbar background
			drawRect(context, this.x, this.y, this.width, this.height, backgroundColor.opacity(opacity))
			renderBar(context, mouseX, mouseY, delta)
		}
	}

	protected open fun updateOpacity(delta: Float) {
		if (hold < holdTime) {
			hold += delta
		} else {
			opacity -= delta * 0.05
		}
	}

	protected open fun renderBar(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		val renderWidth: Int = this.width - (this.paddingLeft + paddingRight)
		val height: Int = (percent.invoke() * this.height).toInt()
		val renderHeight: Int = height - (this.paddingBottom + this.paddingTop)
		val maxScrollLength = this.height - height
		val posY: Int = this.y + this.paddingTop + ((this.amount / this.maxScroll.invoke()) * maxScrollLength).toInt()
		val posX: Int = this.x + this.paddingLeft
		drawRect(context, posX, posY, renderWidth, renderHeight, color.opacity(opacity))
	}


}