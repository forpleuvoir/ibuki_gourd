package forpleuvoir.ibuki_gourd.config.options.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigHotkey
import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.gui.button.ButtonBase
import forpleuvoir.ibuki_gourd.gui.button.ButtonIcon
import forpleuvoir.ibuki_gourd.gui.button.ButtonOption
import forpleuvoir.ibuki_gourd.gui.button.ButtonParentElement
import forpleuvoir.ibuki_gourd.gui.dialog.DialogSimple
import forpleuvoir.ibuki_gourd.gui.icon.Icon
import forpleuvoir.ibuki_gourd.keyboard.KeyBind
import forpleuvoir.ibuki_gourd.keyboard.KeyEnvironment
import forpleuvoir.ibuki_gourd.mod.IbukiGourdMod.mc
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.ParentElement
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
	ButtonParentElement(x, y, width, height, "".text, null) {


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

	private lateinit var setting: ButtonIcon


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
		val size = 20
		setting = ButtonIcon(this.x - size - 2, this.y + this.height / 2 - size / 2, Icon.SETTING, renderBord = true) {
			ScreenBase.openScreen(
				object : DialogSimple(140, 60, IbukiGourdLang.KeyEnvironment.tText(), current) {
					override fun iniWidget() {
						val values = ArrayList<String>()
						KeyEnvironment.values().forEach {
							values.add(it.name)
						}
						addDrawableChild(
							ButtonOption(
								values,
								config.getValue().keyEnvironment.name,
								x = this.x + this.dialogWidth / 2 - 60,
								y = this.y + 30,
								width = 120
							) {
								config.setKeyEnvironment(KeyEnvironment.valueOf(it))
							}
						)
					}
				}
			)
		}
		addDrawableChild(setting)
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