package forpleuvoir.ibuki_gourd.config.options.gui

import forpleuvoir.ibuki_gourd.config.gui.ConfigWrapper
import forpleuvoir.ibuki_gourd.config.options.ConfigBase
import forpleuvoir.ibuki_gourd.config.options.ConfigDouble
import forpleuvoir.ibuki_gourd.config.options.ConfigInt
import forpleuvoir.ibuki_gourd.gui.button.ButtonIcon
import forpleuvoir.ibuki_gourd.gui.icon.Icon
import forpleuvoir.ibuki_gourd.gui.widget.WidgetSliderNumber
import net.minecraft.client.gui.widget.TextFieldWidget


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options.gui

 * 文件名 WrapperNumber

 * 创建时间 2022/1/3 16:41

 * @author forpleuvoir

 */
abstract class WrapperNumber(
	config: ConfigBase,
	x: Int,
	y: Int,
	width: Int,
	height: Int
) : ConfigWrapper(config, x, y, width, height) {

	private lateinit var switchButton: ButtonIcon

	abstract val slider: WidgetSliderNumber
	abstract val field: TextFieldWidget

	override fun initWidget() {
		val field = field
		val slider = slider
		switchButton = ButtonIcon(this.x, this.y, Icon.SWITCH, this.height, renderBg = true, renderBord = false) {
			slider.active = !slider.active
			slider.visible = !slider.visible
			field.active = !field.active
			field.visible = !field.visible
			if (config is ConfigInt) {
				field.text = config.getValue().toString()
			} else if (config is ConfigDouble) {
				field.text = config.getValue().toString()
			}
		}
		field.active = false
		field.visible = false
		addDrawableChild(switchButton)
		addDrawableChild(slider.apply {
			this.x = this@WrapperNumber.x + switchButton.width + 2
			this.y = this@WrapperNumber.y
			this.width = this@WrapperNumber.width - switchButton.width - 2
		})
		addDrawableChild(field.apply {
			this.x = this@WrapperNumber.x + switchButton.width + 3
			this.y = this@WrapperNumber.y + 1
			this.width = this@WrapperNumber.width - switchButton.width - 4
		})
	}

	override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
		children().forEach {
			if (it is WidgetSliderNumber)
				if (it.mouseReleased(mouseX, mouseY, button)) return true
		}
		return super.mouseReleased(mouseX, mouseY, button)
	}

	override fun tick() {
		children().forEach { if (it is TextFieldWidget) it.tick() }
	}
}