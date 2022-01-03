package forpleuvoir.ibuki_gourd.config.options.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigHotkey
import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.gui.button.ButtonBase
import forpleuvoir.ibuki_gourd.keyboard.KeyBind
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.util.InputUtil
import net.minecraft.text.LiteralText
import net.minecraft.util.Formatting


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options.gui

 * 文件名 ButtonConfigHotkey

 * 创建时间 2021/12/21 18:06

 * @author forpleuvoir

 */
class ButtonConfigHotkey(x: Int, y: Int, width: Int, height: Int = 20, private val config: ConfigHotkey) :
	ButtonBase<ButtonConfigHotkey>(x, y, width, height, "".text, null) {


	private var selected: Boolean = false
		set(value) {
			field = value
			mc.currentScreen?.let {
				if (it is ScreenBase) {
					it.shouldCloseOnEsc = !field
				}
			}
		}
	private val keyBind: KeyBind = KeyBind()
	private var firstKey: Boolean = true

	init {
		keyBind.copyOf(config.getValue())
		updateText()
		config.setOnValueChangedCallback {
			selected = false
			firstKey = true
			keyBind.copyOf(config.getValue())
			updateText()
		}
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
		config.setValue(keyBind)
		updateText()
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