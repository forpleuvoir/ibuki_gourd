package forpleuvoir.ibuki_gourd.gui.screen

import forpleuvoir.ibuki_gourd.common.ModInfo
import forpleuvoir.ibuki_gourd.common.mText
import forpleuvoir.ibuki_gourd.common.tText
import net.minecraft.text.MutableText
import net.minecraft.text.Text

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.screen

 * 文件名 IScreenTabEntry

 * 创建时间 2021/12/22 15:12

 * @author forpleuvoir

 */
interface IScreenTabEntry {
	val key: String

	val displayKey: MutableText
		get() = key.tText().mText

	val remark: String
		get() = "$key.remark"

	val baseTitle: Text

	val displayRemark: MutableText
		get() = remark.tText().mText

	val buttonHeight: Int get() = 20

	val screen: ScreenTab

	val all: List<IScreenTabEntry>

	val current: IScreenTabEntry

	fun changeCurrent(current: IScreenTabEntry)

	val currentMod: ModInfo

	val buttonPress: (IScreenTabEntry) -> Unit
		get() = {
			changeCurrent(this)
			ScreenBase.openScreen(this.screen.apply {
				this.parent = if (ScreenBase.current is ScreenBase) (ScreenBase.current as ScreenBase).parent else null
			})
		}
}