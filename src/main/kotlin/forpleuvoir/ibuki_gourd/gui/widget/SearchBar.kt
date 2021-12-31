package forpleuvoir.ibuki_gourd.gui.widget

import forpleuvoir.ibuki_gourd.gui.button.ButtonIcon
import forpleuvoir.ibuki_gourd.gui.common.PositionParentWidget
import forpleuvoir.ibuki_gourd.gui.icon.Icon
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.gui.Selectable.SelectionType
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder


/**
 * 搜索条

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.widget

 * 文件名 SearchBar

 * 创建时间 2021/12/30 17:16

 * @author forpleuvoir

 */
class SearchBar(x: Int, y: Int, width: Int, height: Int) : PositionParentWidget(x, y, width, height) {

	private lateinit var button: ButtonIcon
	private lateinit var textField: WidgetText
	val text: String get() = textField.text

	init {
		initButton()
		initText()
	}

	private fun initButton() {
		button = ButtonIcon(this.x, this.y, Icon.SEARCH, this.height, padding = 0, renderBord = true).apply {
			setOnPressAction {
				textField.active = !textField.active
				textField.visible = !textField.visible
				textField.text = ""
			}
		}
		this.addDrawableChild(button)
	}

	private fun initText() {
		textField = WidgetText(this.x + button.width + 1, this.y + 1, this.width - button.width - 2, this.height - 2, "".text).apply {
			active = false
			visible = false
			setMaxLength(1024)
		}
		this.addDrawableChild(textField)
	}

	fun tick() {
		textField.tick()
	}

	override fun appendNarrations(builder: NarrationMessageBuilder) {}

	override fun getType(): SelectionType {
		return SelectionType.NONE
	}
}