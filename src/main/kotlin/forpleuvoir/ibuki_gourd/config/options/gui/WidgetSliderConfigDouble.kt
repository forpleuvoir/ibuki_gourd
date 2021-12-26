package forpleuvoir.ibuki_gourd.config.options.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigDouble
import forpleuvoir.ibuki_gourd.gui.button.ButtonIcon
import forpleuvoir.ibuki_gourd.gui.dialog.DialogSimple
import forpleuvoir.ibuki_gourd.gui.icon.Icon
import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.gui.widget.WidgetSliderNumberParentElement
import forpleuvoir.ibuki_gourd.gui.widget.WidgetTextFieldDouble
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
	private val config: ConfigDouble,
	private var valueDouble: Double = config.getValue()
) :
	WidgetSliderNumberParentElement(x, y, width, height, { valueDouble }, config.minValue, config.maxValue) {

	init {
		number = Supplier {
			this.valueDouble
		}
		setConsumer {
			this.valueDouble = it.toDouble()
		}
		onStopCallback = { _, _ ->
			config.setValue(valueDouble)
		}
		config.setOnValueChangedCallback {
			this.valueDouble = config.getValue()
			updateMessage()
		}
		val size = 20
		addDrawableChild(
			ButtonIcon(this.x - size - 2, this.y + this.height / 2 - size / 2, Icon.SETTING, padding = 4, renderBord = false, renderBg = true) {
				ScreenBase.openScreen(
					object : DialogSimple(140, 60, config.displayName, current) {
						override fun iniWidget() {
							addDrawableChild(
								WidgetTextFieldDouble(
									x = this.x + this.dialogWidth / 2 - 60,
									y = this.y + 30,
									width = 120,
									height = size,
									config.getValue()
								).apply {
									setConsumer {
										config.setValue(it ?: config.defaultValue)
									}
								}
							)
						}
					}
				)
			}
		)
	}


}