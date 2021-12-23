package forpleuvoir.ibuki_gourd.gui.widget

import forpleuvoir.ibuki_gourd.gui.widget.LabelText.Align.*
import forpleuvoir.ibuki_gourd.render.RenderUtil
import forpleuvoir.ibuki_gourd.utils.color.Color4i
import forpleuvoir.ibuki_gourd.utils.color.IColor
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.Element
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options.gui

 * 文件名 LabelText

 * 创建时间 2021/12/23 17:42

 * @author forpleuvoir

 */
class LabelText(var text: Text, var x: Int, var y: Int, var width: Int, var height: Int, var align: Align = CENTER) : Drawable, Element {

	private val textRenderer = MinecraftClient.getInstance().textRenderer

	private var topPadding: Int = 2
	private var bottomPadding: Int = 2
	private var leftPadding: Int = 2
	private var rightPadding: Int = 2

	private val textWidth: Int
		get() = textRenderer.getWidth(this.text)

	private val textHeight: Int
		get() = textRenderer.fontHeight

	private val centerX: Int
		get() = this.x + this.width / 2

	private val centerY: Int
		get() = this.y + this.height / 2

	var rightToLeft: Boolean = false
	var backgroundColor: IColor<*> = Color4i.WHITE.apply { alpha = 0 }
	var bordColor: IColor<*> = Color4i.WHITE.apply { alpha = 0 }

	private var hoverCallback: ((LabelText) -> Unit)? = null

	fun setHoverCallback(hoverCallback: (LabelText) -> Unit) {
		this.hoverCallback = hoverCallback
	}

	override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		renderBox()
		renderText(matrices)
	}

	private fun renderBox() {
		RenderUtil.drawOutlinedBox(x, y, width, height, backgroundColor, bordColor)
	}


	private fun renderText(matrices: MatrixStack) {
		val textX: Int
		val textY: Int
		when (align) {
			TOP_LEFT      -> {
				textX = this.x + leftPadding
				textY = this.y + topPadding
			}
			TOP_CENTER    -> {
				textX = centerX - textWidth / 2
				textY = this.y + topPadding
			}
			TOP_RIGHT     -> {
				textX = this.x + this.width - textWidth - rightPadding
				textY = this.y + topPadding
			}
			CENTER_LEFT   -> {
				textX = this.x + leftPadding
				textY = centerY - textHeight / 2
			}
			CENTER        -> {
				textX = centerX - textWidth / 2
				textY = centerY - textHeight / 2
			}
			CENTER_RIGHT  -> {
				textX = this.x + this.width - textWidth - rightPadding
				textY = centerY - textHeight / 2
			}
			BOTTOM_LEFT   -> {
				textX = this.x + leftPadding
				textY = this.y - textHeight - bottomPadding
			}
			BOTTOM_CENTER -> {
				textX = centerX - textWidth / 2
				textY = this.y - textHeight - bottomPadding
			}
			BOTTOM_RIGHT  -> {
				textX = this.x + this.width - textWidth - rightPadding
				textY = this.y - textHeight - bottomPadding
			}
		}
		textRenderer.drawWithShadow(
			matrices,
			this.text.string,
			textX.toFloat(),
			textY.toFloat(),
			text.style.color?.rgb ?: Color4i.WHITE.rgba,
			rightToLeft
		)

	}

	override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
		hoverCallback?.invoke(this)
		return super.isMouseOver(mouseX, mouseY)
	}

	enum class Align {
		TOP_LEFT, TOP_CENTER, TOP_RIGHT,
		CENTER_LEFT, CENTER, CENTER_RIGHT,
		BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
	}
}