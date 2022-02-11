package forpleuvoir.ibuki_gourd.config.gui

import forpleuvoir.ibuki_gourd.config.IConfigBase
import forpleuvoir.ibuki_gourd.config.options.IConfigMap
import forpleuvoir.ibuki_gourd.gui.widget.WidgetList
import net.minecraft.client.gui.screen.Screen


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.gui

 * 文件名 WidgetListMap

 * 创建时间 2021/12/25 13:27

 * @author forpleuvoir

 */
class WidgetListMap(
	config: IConfigBase,
	private val map: IConfigMap,
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
) : WidgetList<WidgetListMapEntry>(parent, x, y, pageSize, itemHeight, width, leftPadding, rightPadding, topPadding, bottomPadding) {
	init {
		initData()
		config.setOnValueChangedCallback { initData() }
	}

	private fun initData() {
		clearEntries()
		map.getValue().forEach {
			addEntry(WidgetListMapEntry(map, it.key, this, 0, 0, this.contentWidth, this.itemHeight))
		}
	}
}