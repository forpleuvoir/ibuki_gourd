package forpleuvoir.ibuki_gourd.config.options.gui

import forpleuvoir.ibuki_gourd.config.IConfigBase
import forpleuvoir.ibuki_gourd.config.gui.DialogColorEditor
import forpleuvoir.ibuki_gourd.config.options.IConfigColor
import forpleuvoir.ibuki_gourd.gui.button.ButtonBase
import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.utils.color.IColor
import forpleuvoir.ibuki_gourd.utils.mText
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.util.math.MatrixStack

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options.gui

 * 文件名 ButtonConfigColor

 * 创建时间 2021/12/17 16:05

 * @author forpleuvoir

 */
class ButtonConfigColor(
	x: Int,
	y: Int,
	width: Int,
	height: Int = 20,
	private val config: IConfigBase,
	private val color: IConfigColor
) : ButtonBase<ButtonConfigColor>(
	x,
	y,
	width,
	height,
	message = color.getValue().hexString.text,
	onButtonPress = null
) {

	init {
		setOnPressAction {
			ScreenBase.openScreen(
				@Suppress("unchecked_cast")
				(DialogColorEditor(
					IColor.copy(this.color.getValue()) as IColor<Number>,
					250,
					165,
					this.config.displayName,
					mc.currentScreen!!
				) {
					this.color.setValue(it)
				})
			)
		}
	}

	override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		updateText()
		super.render(matrices, mouseX, mouseY, delta)
	}

	private fun updateText() {
		message = color.getValue().hexString.mText.styled {
			it.withColor(color.getValue().rgba)
		}
	}

	override fun renderButton(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
		super.renderButton(matrices, mouseX, mouseY, delta)
	}

}