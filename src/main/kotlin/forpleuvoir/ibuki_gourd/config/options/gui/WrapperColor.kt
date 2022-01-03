package forpleuvoir.ibuki_gourd.config.options.gui

import forpleuvoir.ibuki_gourd.config.gui.ConfigWrapper
import forpleuvoir.ibuki_gourd.config.options.ConfigColor
import forpleuvoir.ibuki_gourd.gui.common.PositionDrawable
import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.render.RenderUtil
import forpleuvoir.ibuki_gourd.utils.color.Color4f
import forpleuvoir.ibuki_gourd.utils.color.Color4i
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.util.math.MatrixStack


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options.gui

 * 文件名 WrapperColor

 * 创建时间 2022/1/3 16:19

 * @author forpleuvoir

 */
class WrapperColor(config: ConfigColor, x: Int, y: Int, width: Int, height: Int) : ConfigWrapper<ConfigColor>(config, x, y, width, height) {

	private val colorBoxPadding = 4
	private val colorBoxSize = this.height - colorBoxPadding

	private val colorBox: PositionDrawable =
		object : PositionDrawable(this.x + 1, this.y + this.colorBoxPadding / 2, colorBoxSize, colorBoxSize) {
			override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
				RenderUtil.drawOutlinedBox(
					this.x,
					this.y,
					this.width,
					this.height,
					config.getValue(),
					if (isMouseOver(mouseX.toDouble(), mouseY.toDouble())) Color4f.WHITE else Color4i(160,160,160),
					parent?.zOffset ?: 0
				)
				if (isMouseOver(mouseX.toDouble(), mouseY.toDouble()) && ScreenBase.isCurrent(parent)) {
					parent?.renderTooltip(
						matrices,
						listOf(
							"§cRed:${config.getValue().red}".text,
							"§aGreen:${config.getValue().green}".text,
							"§9Blue:${config.getValue().blue}".text,
							"§rAlpha:${config.getValue().alpha}".text
						),
						mouseX,
						mouseY
					)
				}
			}
		}

	override fun initWidget() {
		addDrawableChild(colorBox)
		addDrawableChild(
			ButtonConfigColor(
				x = x + colorBox.width + colorBoxPadding,
				y = y,
				width = width - colorBox.width - colorBoxPadding,
				height = height,
				config = config
			)
		)
	}
}