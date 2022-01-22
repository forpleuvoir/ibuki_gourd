package forpleuvoir.ibuki_gourd.config.options.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigString
import forpleuvoir.ibuki_gourd.gui.widget.WidgetText
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.util.math.MatrixStack

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options.gui

 * 文件名 WidgetConfigString

 * 创建时间 2021/12/18 16:58

 * @author forpleuvoir

 */
class WidgetConfigString(x: Int, y: Int, width: Int, height: Int, private val config: ConfigString) :
	WidgetText(x, y, width, height, "".text) {

	init {
		this.setMaxLength(65535)
		updateText()
		unFocusedCallback = {
			config.setValue(it.text)
		}
		config.setOnValueChangedCallback {
			updateText()
		}
	}

	override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		super.render(matrices, mouseX, mouseY, delta)
	}

	private fun updateText() {
		this.text = config.getValue()
	}
}