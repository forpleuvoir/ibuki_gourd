package forpleuvoir.ibuki_gourd.config.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigStringList
import forpleuvoir.ibuki_gourd.gui.button.ButtonIcon
import forpleuvoir.ibuki_gourd.gui.icon.Icon
import forpleuvoir.ibuki_gourd.gui.widget.WidgetList
import forpleuvoir.ibuki_gourd.gui.widget.WidgetListEntry
import forpleuvoir.ibuki_gourd.gui.widget.WidgetText
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.util.math.MatrixStack


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.gui

 * 文件名 WidgetListStringConfigEntry

 * 创建时间 2021/12/24 23:04

 * @author forpleuvoir

 */
class WidgetListStringConfigEntry(
	private val config: ConfigStringList,
	parent: WidgetList<*>,
	x: Int,
	y: Int,
	width: Int,
	height: Int
) :
	WidgetListEntry<WidgetListStringConfigEntry>(parent, x, y, width, height) {

	private val textInput: WidgetText = WidgetText( 0, 0, (this.width * 0.835).toInt(), this.height - 8, "".text)
	private val remove: ButtonIcon = ButtonIcon(0, 0, Icon.MINUS, iconSize = this.height - 8, renderBord = true) {
		config.remove(index)
	}
	private val save: ButtonIcon = ButtonIcon(0, 0, Icon.SAVE, iconSize = this.height - 8, renderBord = true) {
		if (hovered)
			config.set(index, textInput.text)
	}

	init {
		textInput.text = config.getValue()[index]
		textInput.setMaxLength(65535)
		addDrawableChild(save)
		addDrawableChild(textInput)
		addDrawableChild(remove)
		initPosition()
	}

	override fun renderEntry(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {

	}

	override fun updateIndex() {
		textInput.text = config.getValue()[index]
	}

	override fun initPosition() {
		save.setPosition(
			this.x + 5,
			this.y + this.height / 2 - save.height / 2
		)

		textInput.setPosition(
			this.save.x + this.save.width + 5,
			this.y + this.height / 2 - textInput.height / 2
		)
		remove.setPosition(
			textInput.x + textInput.width + 5,
			this.y + this.height / 2 - remove.height / 2
		)
	}


	override fun tick() {
		textInput.tick()
	}
}