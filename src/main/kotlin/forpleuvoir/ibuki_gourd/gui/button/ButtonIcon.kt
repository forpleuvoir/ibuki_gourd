package forpleuvoir.ibuki_gourd.gui.button

import forpleuvoir.ibuki_gourd.gui.icon.IIcon
import forpleuvoir.ibuki_gourd.render.RenderUtil
import forpleuvoir.ibuki_gourd.utils.color.Color4f
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.util.math.MatrixStack


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.button

 * 文件名 ButtonIcon

 * 创建时间 2021/12/24 21:08

 * @author forpleuvoir

 */
class ButtonIcon(
	x: Int,
	y: Int,
	private val icon: IIcon,
	private val iconSize: Int = 20,
	private val padding: Int = 2,
	width: Int = iconSize,
	height: Int = iconSize,
	private val renderBord: Boolean = false,
	private val renderBg: Boolean = false,
	onButtonPress: ((ButtonIcon) -> Unit)? = null
) :
	ButtonBase<ButtonIcon>(x, y, width, height, "".text, onButtonPress) {


	override fun renderButton(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		if (renderBg) super.renderButton(matrices, mouseX, mouseY, delta)
		else if (this.isHovered) {
			renderTooltip(matrices, mouseX, mouseY)
			if (renderBord)
				RenderUtil.drawOutline(
					this.x,
					this.y,
					this.width,
					this.height,
					1,
					Color4f.WHITE,
					parent?.zOffset?.times(2.0) ?: 5
				)
		}
		matrices.translate(0.0, 0.0, parent?.zOffset?.times(2.0) ?: 0.0)
		icon.render(
			matrices,
			this.x + padding,
			this.y + padding,
			iconSize - padding * 2,
			iconSize - padding * 2,
			isHovered
		)
	}


}