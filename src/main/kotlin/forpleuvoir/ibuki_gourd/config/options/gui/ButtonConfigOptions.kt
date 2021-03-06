package forpleuvoir.ibuki_gourd.config.options.gui

import forpleuvoir.ibuki_gourd.config.options.IConfigOptions
import forpleuvoir.ibuki_gourd.gui.button.ButtonBase
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options.gui

 * 文件名 ButtonConfigOptions

 * 创建时间 2021/12/13 16:06

 * @author forpleuvoir

 */
class ButtonConfigOptions(x: Int, y: Int, width: Int, height: Int = 20, private val config: IConfigOptions) :
	ButtonBase<ButtonConfigOptions>(
		x, y, width, height, Text.of(""), null
	) {

	init {
		this.updateText()
		this.config.getValue().getAllItem().forEach {
			this.hoverText.add(it.displayKey.append(" - ").append(it.displayRemark))
		}
	}

	override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		super.render(matrices, mouseX, mouseY, delta)
		this.updateText()
	}

	override fun onPress() {
		super.onPress()
		this.config.toggle()
		this.updateText()
	}

	private fun updateText() {
		message = this.config.getValue().displayKey
	}

}