package forpleuvoir.ibuki_gourd.mod.compat.modmenu

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import forpleuvoir.ibuki_gourd.mod.gui.ScreenTest


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.mod.compat.modmenu

 * 文件名 ModMenuImpl

 * 创建时间 2021/12/13 17:48

 * @author forpleuvoir

 */
class ModMenuImpl : ModMenuApi {

	override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
		return ConfigScreenFactory {
			val screen = ScreenTest("测试啊")
			screen.parent = it
			return@ConfigScreenFactory screen
		}
	}
}