package forpleuvoir.ibuki_gourd.mod.compat.modmenu

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import forpleuvoir.ibuki_gourd.mod.config.gui.IbukiGourdScreen


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
			return@ConfigScreenFactory IbukiGourdScreen.current().apply { parent = it }
		}
	}
}