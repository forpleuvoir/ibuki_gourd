package forpleuvoir.ibuki_gourd.config.options.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigInt
import forpleuvoir.ibuki_gourd.gui.widget.WidgetSliderNumber
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.gui.widget.SliderWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralText

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options.gui

 * 文件名 WidgetSliderConfigInt

 * 创建时间 2021/12/17 16:58

 * @author forpleuvoir

 */
class WidgetSliderConfigInt(x: Int, y: Int, width: Int, height: Int, private val config: ConfigInt) :
	WidgetSliderNumber(
		x,
		y,
		width,
		height,
		{ config.getValue() },
		config.minValue,
		config.maxValue
	) {
	init {
		setConsumer {
			this.config.setValue(it.toInt())
		}
	}

	override fun getNumber(): Int {
		return super.getNumber().toInt()
	}

}