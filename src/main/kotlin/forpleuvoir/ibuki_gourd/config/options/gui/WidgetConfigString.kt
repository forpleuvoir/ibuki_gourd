package forpleuvoir.ibuki_gourd.config.options.gui

import forpleuvoir.ibuki_gourd.config.IConfigBase
import forpleuvoir.ibuki_gourd.config.options.IConfigString
import forpleuvoir.ibuki_gourd.gui.widget.WidgetText
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.gui.DrawContext

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options.gui

 * 文件名 WidgetConfigString

 * 创建时间 2021/12/18 16:58

 * @author forpleuvoir

 */
class WidgetConfigString(
	x: Int,
	y: Int,
	width: Int,
	height: Int,
	config: IConfigBase,
	private val string: IConfigString
) :
	WidgetText(x, y, width, height, "".text) {

	init {
		this.setMaxLength(65535)
		updateText()
		unFocusedCallback = {
			string.setValue(it.text)
		}
		config.setOnValueChangedCallback {
			updateText()
		}
	}

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		super.render(context, mouseX, mouseY, delta)
	}

	private fun updateText() {
		this.text = string.getValue()
	}
}