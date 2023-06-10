package forpleuvoir.ibuki_gourd.gui.widget

import forpleuvoir.ibuki_gourd.gui.common.IPositionElement
import net.minecraft.client.MinecraftClient
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
open class WidgetText(x: Int, y: Int, width: Int, height: Int, text: Text? = null) :
	TextFieldWidget(MinecraftClient.getInstance().textRenderer, x, y, width, height, text), IPositionElement {

	init {
		setMaxLength(65535)
	}

	var unFocusedCallback: ((WidgetText) -> Unit)? = null

	override fun setFocusUnlocked(focused: Boolean) {
		if (this.isFocused || !focused)
			unFocusedCallback?.invoke(this)
		super.setFocusUnlocked(focused)
	}

	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		if (!isActive) return false
		if (InputUtil.GLFW_KEY_ENTER == keyCode || keyCode == InputUtil.GLFW_KEY_KP_ENTER) {
			setFocusUnlocked(!isFocused)
			return true
		}
		return super.keyPressed(keyCode, scanCode, modifiers)
	}

	override fun setPosition(x: Int, y: Int) {
		val deltaX = x - this.x
		val deltaY = y - this.y
		this.x = x
		this.y = y
		onPositionChanged?.invoke(deltaX, deltaY, x, y)
	}

	override fun deltaPosition(deltaX: Int, deltaY: Int) {
		this.x += deltaX
		this.y += deltaY
		onPositionChanged?.invoke(deltaX, deltaY, this.x, this.y)
	}

	override var onPositionChanged: ((deltaX: Int, deltaY: Int, x: Int, y: Int) -> Unit)? = null

}