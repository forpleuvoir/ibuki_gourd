package forpleuvoir.ibuki_gourd.config.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigStringList
import forpleuvoir.ibuki_gourd.gui.widget.WidgetList
import net.minecraft.client.gui.screen.Screen


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.gui

 * 文件名 WidgetListStringConfig

 * 创建时间 2021/12/24 23:04

 * @author forpleuvoir

 */
class WidgetListStringConfig(
	private val config: ConfigStringList,
	parent: Screen,
	x: Int,
	y: Int,
	pageSize: Int,
	itemHeight: Int,
	width: Int,
	leftPadding: Int = 0,
	rightPadding: Int = 0,
	topPadding: Int = 0,
	bottomPadding: Int = 0
) : WidgetList<WidgetListStringConfigEntry>(parent, x, y, pageSize, itemHeight, width, leftPadding, rightPadding, topPadding, bottomPadding) {
	init {
		initData()
		config.setOnValueChangedCallback { initData() }
	}

	private fun initData(){
		clearEntries()
		config.getValue().forEach { _ ->
			addEntry(WidgetListStringConfigEntry(config, this, 0, 0, this.rowWidth, this.itemHeight))
		}
	}
}