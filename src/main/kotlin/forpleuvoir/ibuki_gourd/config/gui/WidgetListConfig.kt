package forpleuvoir.ibuki_gourd.config.gui

import forpleuvoir.ibuki_gourd.config.IConfigBase
import forpleuvoir.ibuki_gourd.gui.widget.WidgetList
import net.minecraft.client.gui.screen.Screen


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.gui

 * 文件名 WidgetListConfig

 * 创建时间 2021/12/21 13:48

 * @author forpleuvoir

 */
class WidgetListConfig(
	private val configs: Collection<IConfigBase>,
	parent: Screen,
	x: Int,
	y: Int,
	pageSize: Int,
	itemHeight: Int,
	width: Int,
	val wrapperWidth: Int = 140
) : WidgetList<WidgetListConfigEntry>(parent, x, y, pageSize, itemHeight, width) {

	init {
		clearEntries()
		configs.forEach {
			addEntry(WidgetListConfigEntry(it, this, 0, 0, this.contentWidth, this.itemHeight))
		}
	}
}