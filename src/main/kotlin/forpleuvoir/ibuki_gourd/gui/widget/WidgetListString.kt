package forpleuvoir.ibuki_gourd.gui.widget

import net.minecraft.client.gui.screen.Screen


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.widget

 * 文件名 WidgetListString

 * 创建时间 2021/12/28 19:13

 * @author forpleuvoir

 */
class WidgetListString(
	private val list: List<String>,
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
) : WidgetList<WidgetListStringEntry>(parent, x, y, pageSize, itemHeight, width, leftPadding, rightPadding, topPadding, bottomPadding) {


	init {
		initData()
	}

	private fun initData() {
		clearEntries()
		list.forEach {
			addEntry(WidgetListStringEntry(it, this, 0, 0, this.contentWidth, this.itemHeight))
		}
	}
}
