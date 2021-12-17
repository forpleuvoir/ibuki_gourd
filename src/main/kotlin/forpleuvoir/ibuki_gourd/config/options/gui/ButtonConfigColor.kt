package forpleuvoir.ibuki_gourd.config.options.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigColor
import forpleuvoir.ibuki_gourd.gui.button.ButtonBase
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.util.math.MatrixStack

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options.gui

 * 文件名 ButtonConfigColor

 * 创建时间 2021/12/17 16:05

 * @author forpleuvoir

 */
class ButtonConfigColor(x: Int, y: Int, width: Int, height: Int = 20, private val config: ConfigColor) :
	ButtonBase<ButtonConfigColor>(x, y, width, height, message = config.getValue().hexString.text(), onButtonPress = null) {

	override fun onPress() {
		super.onPress()
		// TODO: 2021/12/17 打开颜色编辑组件 
	}

	override fun renderButton(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
		super.renderButton(matrices, mouseX, mouseY, delta)

	}
}