package forpleuvoir.ibuki_gourd.config.gui

import forpleuvoir.ibuki_gourd.config.options.IConfigMap
import forpleuvoir.ibuki_gourd.gui.button.ButtonIcon
import forpleuvoir.ibuki_gourd.gui.icon.Icon
import forpleuvoir.ibuki_gourd.gui.widget.WidgetList
import forpleuvoir.ibuki_gourd.gui.widget.WidgetListEntry
import forpleuvoir.ibuki_gourd.gui.widget.WidgetText
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.gui.DrawContext


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.gui

 * 文件名 WidgetListMapEntry

 * 创建时间 2021/12/25 12:57

 * @author forpleuvoir

 */
class WidgetListMapEntry(
	private val map: IConfigMap,
	private var oldKey: String,
	parent: WidgetList<*>,
	x: Int,
	y: Int,
	width: Int,
	height: Int
) : WidgetListEntry<WidgetListMapEntry>(parent, x, y, width, height) {

	private val key: WidgetText = WidgetText(0, 0, (this.width * 0.17).toInt(), this.height - 8, oldKey.text).also {
		it.text = oldKey
	}
	private val value: WidgetText =
		WidgetText(0, 0, (this.width * 0.67).toInt(), this.height - 8, map.getValue()[oldKey]?.text).also {
			it.text = map.getValue()[oldKey]
		}
	private val remove: ButtonIcon = ButtonIcon(0, 0, Icon.MINUS, iconSize = this.height - 8, renderBord = true) {
		map.remove(key.text)
	}

	private val save: ButtonIcon = ButtonIcon(0, 0, Icon.SAVE, iconSize = this.height - 8, renderBord = true) {
		map.reset(oldKey, key.text, value.text)
		oldKey = key.text
	}.apply { setHoverText(listOf(IbukiGourdLang.Save.tText())) }

	init {
		addDrawableChild(save)
		addDrawableChild(key)
		addDrawableChild(value)
		addDrawableChild(remove)
		initPosition()
	}

	override fun resize() {
		value.width = (this.width * 0.67).toInt()
		key.width = (this.width * 0.17).toInt()
	}

	override fun initPosition() {
		save.setPosition(
			this.x + 5,
			this.y + this.height / 2 - save.height / 2
		)
		key.setPosition(
			this.save.x + this.save.width + 5,
			this.y + 4
		)
		value.setPosition(
			this.key.x + this.key.width + 5,
			this.y + 4
		)
		remove.setPosition(
			this.value.x + this.value.width + 5,
			this.y + this.height / 2 - remove.height / 2
		)
	}

	override fun renderEntry(drawContext: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
	}

	override fun tick() {
		key.tick()
		value.tick()
	}
}