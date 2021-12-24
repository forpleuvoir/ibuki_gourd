package forpleuvoir.ibuki_gourd.config.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigStringList
import forpleuvoir.ibuki_gourd.gui.button.ButtonIcon
import forpleuvoir.ibuki_gourd.gui.icon.Icon
import forpleuvoir.ibuki_gourd.gui.widget.WidgetList
import forpleuvoir.ibuki_gourd.gui.widget.WidgetListEntry
import forpleuvoir.ibuki_gourd.gui.widget.WidgetText
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.util.math.MatrixStack
import java.util.function.Consumer
import java.util.function.Supplier


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.gui

 * 文件名 WidgetListStringEntry

 * 创建时间 2021/12/24 23:04

 * @author forpleuvoir

 */
class WidgetListStringEntry(
	private val config: ConfigStringList,
	parent: WidgetList<*>,
	x: Int,
	y: Int,
	width: Int,
	height: Int
) :
	WidgetListEntry<WidgetListStringEntry>(parent, x, y, width, height) {

	private val textInput: WidgetText = WidgetText(textRenderer, 0, 0, this.width - 40, this.height - 8, "".text)
	private val remove: ButtonIcon = ButtonIcon(0, 0, Icon.MINUS, iconSize = this.height - 16, renderBord = true) {
		config.remove(index)
	}

	init {
		textInput.unFocusedCallback = {
			config.set(index, it.text)
		}
		textInput.setMaxLength(65535)
		addDrawableChild(textInput)
		addDrawableChild(remove)
		onPositionChanged()
	}

	override fun renderEntry(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {

	}

	override fun onPositionChanged() {
		textInput.y = this.y + this.height / 2 - textInput.height / 2
		textInput.x = this.x + 10
		textInput.text = config.getValue()[index]

		remove.x = textInput.x + textInput.width + 5
		remove.y = this.y + this.height / 2 - remove.height / 2
	}


	override fun tick() {
		textInput.tick()
	}
}