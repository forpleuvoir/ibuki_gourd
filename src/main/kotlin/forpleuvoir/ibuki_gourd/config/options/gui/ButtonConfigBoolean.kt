package forpleuvoir.ibuki_gourd.config.options.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigBoolean
import forpleuvoir.ibuki_gourd.gui.button.ButtonBase
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.minecraft.util.Formatting


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options.gui

 * 文件名 ButtonConfigBoolean

 * 创建时间 2021/12/13 15:51

 * @author forpleuvoir

 */
class ButtonConfigBoolean(x: Int, y: Int, width: Int, height: Int = 20, private val config: ConfigBoolean) :
	ButtonBase<ButtonConfigBoolean>(x, y, width, height, Text.of("")) {

	init {
		this.updateText()
		setOnPressAction {
			this.config.toggle()
			this.updateText()
		}
	}


	override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
		super.render(matrices, mouseX, mouseY, delta)
		this.updateText()
	}

	private fun updateText() {
		val valueStr = this.config.getValue().toString()
		val formatting = if (this.config.getValue()) Formatting.GREEN else Formatting.RED
		message = Text.of(formatting.toString() + valueStr)
	}
}