package forpleuvoir.ibuki_gourd.config.options.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigInt
import forpleuvoir.ibuki_gourd.gui.button.ButtonIcon
import forpleuvoir.ibuki_gourd.gui.dialog.DialogSimple
import forpleuvoir.ibuki_gourd.gui.icon.Icon
import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.gui.widget.WidgetSliderNumberParentElement
import forpleuvoir.ibuki_gourd.gui.widget.WidgetTextFieldInt
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
	private val config: ConfigInt,
	private var valueInt: Int = config.getValue()
) :
	WidgetSliderNumberParentElement(
		x,
		y,
		width,
		height,
		{ valueInt },
		config.minValue,
		config.maxValue
	) {
	init {
		number = Supplier {
			this.valueInt
		}
		setConsumer {
			this.valueInt = it.toInt()
		}
		onStopCallback = { _, _ ->
			config.setValue(valueInt)
		}
		config.setOnValueChangedCallback {
			this.valueInt = config.getValue()
			updateMessage()
		}
		val size = 20
		addDrawableChild(
			ButtonIcon(this.x - size - 2, this.y + this.height / 2 - size / 2, Icon.SETTING, padding = 4, renderBord = false, renderBg = true) {
				ScreenBase.openScreen(
					object : DialogSimple(140, 60, config.displayName, current) {
						override fun iniWidget() {
							addDrawableChild(
								WidgetTextFieldInt(
									x = this.x + this.paddingLeft + 5,
									y = this.y + this.paddingTop + ((this.dialogHeight - (this.paddingTop + paddingBottom)) / 2) - size / 2,
									width = this.dialogWidth - (this.paddingLeft + this.paddingRight) - 10,
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