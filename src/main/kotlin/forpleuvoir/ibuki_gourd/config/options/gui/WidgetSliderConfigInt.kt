package forpleuvoir.ibuki_gourd.config.options.gui

import forpleuvoir.ibuki_gourd.config.IConfigBase
import forpleuvoir.ibuki_gourd.config.options.IConfigInt
import forpleuvoir.ibuki_gourd.gui.widget.WidgetSliderNumber
import java.util.function.Supplier

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options.gui

 * 文件名 WidgetSliderConfigInt

 * 创建时间 2021/12/17 16:58

 * @author forpleuvoir

 */
class WidgetSliderConfigInt(
	x: Int,
	y: Int,
	width: Int,
	height: Int,
	config: IConfigBase,
	private val int: IConfigInt,
	private var valueInt: Int = int.getValue()
) :
	WidgetSliderNumber(
		x,
		y,
		width,
		height,
		{ valueInt },
		int.minValue,
		int.maxValue
	) {
	init {
		number = Supplier {
			this.valueInt
		}
		setConsumer {
			this.valueInt = it.toInt()
		}
		onStopCallback = { _, _ ->
			int.setValue(valueInt)
		}
		config.setOnValueChangedCallback {
			this.valueInt = int.getValue()
			updateMessage()
		}
	}


}