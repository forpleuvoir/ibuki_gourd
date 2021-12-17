package forpleuvoir.ibuki_gourd.gui

import net.minecraft.client.gui.screen.Screen


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui

 * 文件名 IScreenBase

 * 创建时间 2021/12/13 16:58

 * @author forpleuvoir

 */
interface IScreenBase {

	var parent: Screen?

	fun onScreenClose()

}