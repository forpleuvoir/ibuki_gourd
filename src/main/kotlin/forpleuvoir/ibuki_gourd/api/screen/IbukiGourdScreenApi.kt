package forpleuvoir.ibuki_gourd.api.screen

import forpleuvoir.ibuki_gourd.gui.screen.ScreenTab
import net.minecraft.client.gui.screen.Screen

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.api.screen

 * 文件名 IbukiGourdScreenApi

 * 创建时间 2022/2/11 22:13

 * @author forpleuvoir

 */
interface IbukiGourdScreenApi {
	fun getScreenTabFactory(): (Screen?) -> ScreenTab
}