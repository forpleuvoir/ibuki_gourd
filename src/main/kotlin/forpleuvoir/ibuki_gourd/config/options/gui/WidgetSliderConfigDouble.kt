package forpleuvoir.ibuki_gourd.config.options.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigDouble
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.gui.widget.SliderWidget
import net.minecraft.text.LiteralText

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options.gui

 * 文件名 WidgetSliderConfigDouble

 * 创建时间 2021/12/17 16:58

 * @author forpleuvoir

 */
class WidgetSliderConfigDouble(x: Int, y: Int, width: Int, height: Int, private val config: ConfigDouble) :
	SliderWidget(x, y, width, height, LiteralText.EMPTY, config.getValue() / (config.maxValue - config.minValue)) {

	init {
		updateMessage()
	}

	override fun updateMessage() {
		this.message = String.format("%.2f", config.getValue()).text()
	}

	override fun applyValue() {
		this.config.setValue((config.maxValue - config.minValue) * this.value)
	}
}