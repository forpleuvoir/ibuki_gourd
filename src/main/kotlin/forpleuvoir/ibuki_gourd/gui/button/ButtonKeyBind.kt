package forpleuvoir.ibuki_gourd.gui.button

import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.keyboard.KeyBind
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.util.InputUtil
import net.minecraft.text.LiteralText
import net.minecraft.util.Formatting

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.button

 * 文件名 ButtonKeyBind

 * 创建时间 2022/1/19 19:49

 * @author forpleuvoir

 */
class ButtonKeyBind(
	x: Int,
	y: Int,
	width: Int,
	height: Int = 20,
	private val keyBind: KeyBind,
	private val saveCallback: (KeyBind) -> Unit = {}
) :
	ButtonBase<ButtonKeyBind>(x, y, width, height, "".text, null) {

	private var selected: Boolean = false
		set(value) {
			field = value
			mc.currentScreen?.let {
				if (it is ScreenBase) {
					it.shouldCloseOnEsc = !field
				}
			}
		}

	private var firstKey: Boolean = true

	init {
		updateText()
		setOnPressAction {
			if (!selected) {
				this.selected = true
				updateText()
			}
		}
	}


	private fun save() {
		firstKey = true
		this.selected = false
		updateText()
		saveCallback.invoke(keyBind)
	}

	private fun addKey(keyCode: Int) {
		if (firstKey) {
			keyBind.clearKey()
			firstKey = false
		}
		keyBind.add(keyCode)
	}

	private fun updateText() {
		val message = LiteralText("")
		keyBind.asTexts.forEachIndexed { index, it ->
			message.append(it.string.uppercase())
			if (index != keyBind.asTexts.lastIndex)
				message.append(" + ")
		}
		if (message.string.isEmpty()) message.append("None")
		if (selected) message.styled { it.withColor(Formatting.GOLD) }
		this.message = message
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (selected) {
			addKey(button)
			updateText()
		}
		return super.mouseClicked(mouseX, mouseY, button)
	}

	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		if (selected) {
			if (keyCode == InputUtil.GLFW_KEY_ESCAPE) {
				mc.currentScreen?.let {
					if (it is ScreenBase) {
						it.shouldCloseOnEsc = !selected
					}
				}
				if (firstKey) keyBind.clearKey()
				save()
			} else {
				addKey(keyCode)
			}
			updateText()
		}
		return true
	}


}