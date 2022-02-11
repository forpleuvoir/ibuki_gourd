package forpleuvoir.ibuki_gourd.mod.compat.ibuki_gourd

import forpleuvoir.ibuki_gourd.api.screen.IbukiGourdScreenApi
import forpleuvoir.ibuki_gourd.gui.screen.ScreenTab
import forpleuvoir.ibuki_gourd.mod.config.gui.IbukiGourdScreen
import net.minecraft.client.gui.screen.Screen

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.mod.compat.ibuki_gourd

 * 文件名 ScreenImpl

 * 创建时间 2022/2/11 22:20

 * @author forpleuvoir

 */
class ScreenImpl : IbukiGourdScreenApi {
	override fun getScreenTabFactory(): (Screen?) -> ScreenTab {
		return {
			IbukiGourdScreen.current().apply { parent = it }
		}
	}
}