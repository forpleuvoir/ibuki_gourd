package forpleuvoir.ibuki_gourd.config.options.gui

import forpleuvoir.ibuki_gourd.config.IConfigBase
import forpleuvoir.ibuki_gourd.config.options.IConfigDouble
import forpleuvoir.ibuki_gourd.gui.widget.WidgetSliderNumber
import java.util.function.Supplier

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options.gui

 * 文件名 WidgetSliderConfigDouble

 * 创建时间 2021/12/17 16:58

 * @author forpleuvoir

 */
class WidgetSliderConfigDouble(
	x: Int,
	y: Int,
	width: Int,
	height: Int,
	config: IConfigBase,
	private val double: IConfigDouble,
	private var valueDouble: Double = double.getValue()
) :
	WidgetSliderNumber(x, y, width, height, { valueDouble }, double.minValue, double.maxValue) {

	init {
		number = Supplier {
			this.valueDouble
		}
		setConsumer {
			this.valueDouble = it.toDouble()
		}
		onStopCallback = { _, _ ->
			double.setValue(valueDouble)
		}
		config.setOnValueChangedCallback {
			this.valueDouble = double.getValue()
			updateMessage()
		}
	}


}