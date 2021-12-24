package forpleuvoir.ibuki_gourd.gui.widget

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.widget

 * 文件名 WidgetText

 * 创建时间 2021/12/25 0:10

 * @author forpleuvoir

 */
open class WidgetText(textRenderer: TextRenderer?, x: Int, y: Int, width: Int, height: Int, text: Text?) :
	TextFieldWidget(textRenderer, x, y, width, height, text) {
	var unFocusedCallback: ((WidgetText) -> Unit)? = null

	override fun setTextFieldFocused(focused: Boolean) {
		if (this.isFocused || !focused)
			unFocusedCallback?.invoke(this)
		super.setTextFieldFocused(focused)
	}

	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		if (!isActive) return false
		if (InputUtil.GLFW_KEY_ENTER == keyCode || keyCode == InputUtil.GLFW_KEY_KP_ENTER) {
			setTextFieldFocused(!isFocused)
			return true
		}
		return super.keyPressed(keyCode, scanCode, modifiers)
	}

}