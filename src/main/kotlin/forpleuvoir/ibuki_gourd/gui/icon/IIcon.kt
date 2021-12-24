package forpleuvoir.ibuki_gourd.gui.icon

import net.minecraft.client.util.math.MatrixStack


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.icon

 * 文件名 IIcon

 * 创建时间 2021/12/24 21:13

 * @author forpleuvoir

 */
interface IIcon {

	val u: Int
	val v: Int
	val width: Int
	val height: Int
	val hoverU: Int
	val hoverV: Int
	val textureWidth: Int
	val textureHeight: Int
	fun render(matrices: MatrixStack, x: Int, y: Int, width: Int, height: Int, hovered: Boolean)
}