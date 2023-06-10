package forpleuvoir.ibuki_gourd.config.options.gui

import forpleuvoir.ibuki_gourd.config.IConfigBase
import forpleuvoir.ibuki_gourd.config.gui.ConfigWrapper
import forpleuvoir.ibuki_gourd.config.options.IConfigColor
import forpleuvoir.ibuki_gourd.gui.common.PositionDrawable
import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.mod.IbukiGourdMod.mc
import forpleuvoir.ibuki_gourd.render.RenderUtil.drawOutlinedBox
import forpleuvoir.ibuki_gourd.utils.color.Color4f
import forpleuvoir.ibuki_gourd.utils.color.Color4i
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.gui.DrawContext


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options.gui

 * 文件名 WrapperColor

 * 创建时间 2022/1/3 16:19

 * @author forpleuvoir

 */
class WrapperColor(config: IConfigBase, private val color: IConfigColor, x: Int, y: Int, width: Int, height: Int) :
	ConfigWrapper(config, x, y, width, height) {

	private val colorBoxPadding = 4
	private val colorBoxSize = this.height - colorBoxPadding

	private val colorBox: PositionDrawable =
		object : PositionDrawable(this.x + 1, this.y + this.colorBoxPadding / 2, colorBoxSize, colorBoxSize) {
			override fun setFocused(focused: Boolean) {
			}

			override fun isFocused(): Boolean {
				return false
			}

			override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
				val bordColor =
					if (isMouseOver(mouseX.toDouble(), mouseY.toDouble())) Color4f.WHITE else Color4i(160, 160, 160)
				drawOutlinedBox(context, this.x, this.y, this.width, this.height, color.getValue(), bordColor)
				if (isMouseOver(mouseX.toDouble(), mouseY.toDouble()) && ScreenBase.isCurrent(parent)) {
					context.drawTooltip(
						mc.textRenderer,
						listOf(
							"§cRed:${color.getValue().red}".text,
							"§aGreen:${color.getValue().green}".text,
							"§9Blue:${color.getValue().blue}".text,
							"§rAlpha:${color.getValue().alpha}".text
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
				config = config,
				color = color
			)
		)
	}
}