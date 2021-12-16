package forpleuvoir.ibuki_gourd.gui.button

import forpleuvoir.ibuki_gourd.config.options.BooleanConfig
import net.minecraft.text.Text
import net.minecraft.util.Formatting


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.button

 * 文件名 ButtonBooleanConfig

 * 创建时间 2021/12/13 15:51

 * @author forpleuvoir

 */
class ButtonBooleanConfig(x: Int, y: Int, width: Int, height: Int = 20, private val config: BooleanConfig) :
	ButtonBase<ButtonBooleanConfig>(x, y, width, height, Text.of(""), null) {

	init {
		this.updateText()
	}

	override fun onPress() {
		super.onPress()
		this.config.toggle()
		this.updateText()
	}

	private fun updateText() {
		val valueStr = this.config.getValue().toString()
		message = Text.of(valueStr)
		message.style.withColor(if (this.config.getValue()) Formatting.GREEN else Formatting.RED)
	}
}