package forpleuvoir.ibuki_gourd.mod.config.gui

import forpleuvoir.ibuki_gourd.common.ModInfo
import forpleuvoir.ibuki_gourd.gui.screen.IScreenTabEntry
import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.gui.screen.ScreenTab
import forpleuvoir.ibuki_gourd.mod.IbukiGourdMod
import forpleuvoir.ibuki_gourd.mod.config.IbukiGourdConfigs
import forpleuvoir.ibuki_gourd.mod.gui.ScreenStinger
import forpleuvoir.ibuki_gourd.mod.gui.ScreenTest
import net.minecraft.client.gui.screen.Screen
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

	private val setting =
		IbukiGourdConfigGroup("ibuki_gourd.config.setting", baseTitle, IbukiGourdConfigs.Setting.CONFIGS)

	private val testConfig = IbukiGourdConfigGroup("测试", baseTitle, IbukiGourdConfigs.Test.CONFIGS, 180)

	private val stinger = object : IScreenTabEntry {
		override val key: String
			get() = "§6?§b?§d?"
		override val remark: String
			get() = "§kStinger"
		override val baseTitle: Text
			get() = this@IbukiGourdScreen.baseTitle
		override val screen: ScreenTab
			get() = ScreenStinger(this)
		override val all: List<IScreenTabEntry>
			get() = allTabsEntry
		override val current: IScreenTabEntry
			get() = currentEntry

		override fun changeCurrent(current: IScreenTabEntry) {
			currentEntry = current
		}

		override val currentMod: ModInfo
			get() = IbukiGourdMod
	}

	private val test = object : IScreenTabEntry {
		override val key: String
			get() = "test"
		override val remark: String
			get() = "test"
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

		override val currentMod: ModInfo
			get() = IbukiGourdMod
	}


	val allTabsEntry: List<IScreenTabEntry> = listOf(
		setting, stinger, testConfig, test
	)

	var currentEntry: IScreenTabEntry = setting

	fun current(): ScreenTab {
		return currentEntry.screen
	}

	fun openScreen() {
		ScreenBase.openScreen(current())
	}

	fun openScreen(parent: Screen?) {
		ScreenBase.openScreen(current().apply { this.parent = parent })
	}
}