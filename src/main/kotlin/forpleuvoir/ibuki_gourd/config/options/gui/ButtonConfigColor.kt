package forpleuvoir.ibuki_gourd.config.options.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigColor
import forpleuvoir.ibuki_gourd.gui.button.ButtonBase
import forpleuvoir.ibuki_gourd.render.RenderUtil
import forpleuvoir.ibuki_gourd.utils.color.Color4i
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralText

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options.gui

 * 文件名 ButtonConfigColor

 * 创建时间 2021/12/17 16:05

 * @author forpleuvoir

 */
class ButtonConfigColor(x: Int, y: Int, width: Int, height: Int = 20, private val config: ConfigColor) :
	ButtonBase<ButtonConfigColor>(x, y, width, height, message = config.getValue().hexString.text(), onButtonPress = null) {
	private val colorBoxPadding = 4
	private val colorBoxSize = this.height - colorBoxPadding
	override fun onPress() {
		super.onPress()
		// TODO: 2021/12/17 打开颜色编辑组件 
	}

	override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
		updateText()
		super.render(matrices, mouseX, mouseY, delta)
		RenderUtil.isMouseHovered(
			this.x - this.colorBoxSize - colorBoxPadding,
			this.y + colorBoxPadding / 2,
			colorBoxSize,
			colorBoxSize,
			mouseX,
			mouseY
		)
		{
			mc.currentScreen?.renderTooltip(
				matrices,
				listOf(
					"§cRED:${config.getValue().red}".text(),
					"§aGREEN:${config.getValue().green}".text(),
					"§9BLUE:${config.getValue().blue}".text(),
					"§rALPHA:${config.getValue().alpha}".text()
				),
				mouseX,
				mouseY
			)
		}
	}

	private fun updateText() {
		message = LiteralText(config.getValue().hexString).styled {
			it.withColor(config.getValue().rgb)
		}
	}

	override fun renderButton(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
		super.renderButton(matrices, mouseX, mouseY, delta)
		RenderUtil.drawOutlinedBox(
			this.x - this.colorBoxSize - colorBoxPadding,
			this.y + colorBoxPadding / 2,
			colorBoxSize,
			colorBoxSize,
			Color4i().fromInt(config.getValue().rgb),
			Color4i.WHITE
		)
	}

}