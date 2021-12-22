package forpleuvoir.ibuki_gourd.mod.config.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigBase
import forpleuvoir.ibuki_gourd.config.options.ConfigInt
import forpleuvoir.ibuki_gourd.config.gui.IConfigGroup
import forpleuvoir.ibuki_gourd.config.gui.ScreenTabConfig
import forpleuvoir.ibuki_gourd.gui.screen.IScreenTabEntry
import forpleuvoir.ibuki_gourd.gui.screen.ScreenTab
import forpleuvoir.ibuki_gourd.mod.config.IbukiGourdConfigs
import forpleuvoir.ibuki_gourd.mod.config.gui.IbukiGourdScreen.allTabsEntry
import forpleuvoir.ibuki_gourd.mod.config.gui.IbukiGourdScreen.currentEntry
import net.minecraft.text.Text


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.mod.config.gui

 * 文件名 IbukiGourdConfigGroup

 * 创建时间 2021/12/21 16:29

 * @author forpleuvoir

 */
class IbukiGourdConfigGroup(
	override val key: String,
	override val baseTitle: Text,
	override val configs: List<ConfigBase>
) : IConfigGroup {

	override val screen: ScreenTab
		get() = ScreenTabConfig(24, this)

	override val all: List<IScreenTabEntry>
		get() = allTabsEntry

	override val current: IScreenTabEntry
		get() = currentEntry

	override fun changeCurrent(current: IScreenTabEntry) {
		currentEntry = current
	}

}


