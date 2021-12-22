package forpleuvoir.ibuki_gourd.mod.config.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigInt
import forpleuvoir.ibuki_gourd.gui.screen.IScreenTabEntry
import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.gui.screen.ScreenTab
import forpleuvoir.ibuki_gourd.mod.IbukiGourdMod
import forpleuvoir.ibuki_gourd.mod.config.IbukiGourdConfigs
import forpleuvoir.ibuki_gourd.mod.gui.ScreenTest
import net.minecraft.text.Text


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.mod.config.gui

 * 文件名 IbukiGourdScreen

 * 创建时间 2021/12/22 17:41

 * @author forpleuvoir

 */
object IbukiGourdScreen {

	private val baseTitle = Text.of(IbukiGourdMod.modName)

	private val values = IbukiGourdConfigGroup("ibuki_gourd.config.toggles", baseTitle, IbukiGourdConfigs.Values.CONFIGS)
	private val toggles = IbukiGourdConfigGroup("ibuki_gourd.config.values", baseTitle, ArrayList(IbukiGourdConfigs.Values.CONFIGS).apply {
		for (i in 1..30) {
			this.add(ConfigInt("凑数的", "凑数的", i, 0, 30))
		}
	})
	private val test = object : IScreenTabEntry {
		override val key: String
			get() = "测试的"
		override val baseTitle: Text
			get() = this@IbukiGourdScreen.baseTitle
		override val screen: ScreenTab
			get() = ScreenTest(this)
		override val all: List<IScreenTabEntry>
			get() = allTabsEntry
		override val current: IScreenTabEntry
			get() = currentEntry

		override fun changeCurrent(current: IScreenTabEntry) {
			currentEntry = current
		}

	}

	val allTabsEntry: List<IScreenTabEntry> = listOf(
		values,
		toggles,
		test
	)

	var currentEntry: IScreenTabEntry = values

	fun current(): ScreenTab {
		return currentEntry.screen
	}

	fun openScreen() {
		ScreenBase.openScreen(current())
	}
}