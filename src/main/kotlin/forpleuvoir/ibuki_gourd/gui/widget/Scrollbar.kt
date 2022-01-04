package forpleuvoir.ibuki_gourd.gui.widget

import forpleuvoir.ibuki_gourd.gui.common.PositionDrawable
import forpleuvoir.ibuki_gourd.utils.color.Color4f
import forpleuvoir.ibuki_gourd.utils.color.Color4i
import forpleuvoir.ibuki_gourd.utils.color.IColor
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.MathHelper


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

	var amountConsumer: ((Double) -> Unit)? = null
	var amount = 0.0
		set(value) {
			field = MathHelper.clamp(value, 0.0, maxScroll.invoke().toDouble())
			amountConsumer?.invoke(field)
		}

	fun setPadding(padding: Int) {
		paddingLeft = padding
		paddingRight = padding
		paddingTop = padding
		paddingBottom = padding
	}

	override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		if (this.maxScroll.invoke() > 0) {
			matrices.translate(0.0, 0.0, parent?.zOffset?.times(2.0) ?: 0.0)
			//draw scrollbar background
			DrawableHelper.fill(matrices, this.x, this.y, this.x + width, this.y + this.height, backgroundColor.rgba)
			renderBar(matrices, mouseX, mouseY, delta)
		}
	}

	protected open fun renderBar(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		val renderWidth: Int = this.width - (this.paddingLeft + paddingRight)
		val height: Int = (percent.invoke() * this.height).toInt()
		val renderHeight: Int = height - (this.paddingBottom + this.paddingTop)
		val maxScrollLength = this.height - height
		val posY: Int = this.y + this.paddingTop + ((this.amount / this.maxScroll.invoke()) * maxScrollLength).toInt()
		val posX: Int = this.x + this.paddingLeft
		DrawableHelper.fill(matrices, posX, posY, posX + renderWidth, posY + renderHeight, color.rgba)
	}
}