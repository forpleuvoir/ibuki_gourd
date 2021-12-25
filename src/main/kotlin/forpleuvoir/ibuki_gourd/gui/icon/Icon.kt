package forpleuvoir.ibuki_gourd.gui.icon

import com.mojang.blaze3d.systems.RenderSystem
import forpleuvoir.ibuki_gourd.mod.IbukiGourdMod
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier

const val TEXTURE_WIDTH = 256
const val TEXTURE_HEIGHT = 256
val TEXTURE = Identifier(IbukiGourdMod.modId, "textures/gui/icon/gui_icons.png")

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.icon

 * 文件名 Icon

 * 创建时间 2021/12/24 20:47

 * @author forpleuvoir

 */
enum class Icon(
	override val u: Int,
	override val v: Int,
	override val width: Int = 16,
	override val height: Int = 16,
	override val hoverU: Int = u,
	override val hoverV: Int = v + 16,
	override val textureWidth: Int = TEXTURE_WIDTH,
	override val textureHeight: Int = TEXTURE_HEIGHT
) : IIcon {
	SETTING(0, 0),
	PLUS(16, 0),
	MINUS(32, 0),
	SEARCH(48, 0),
	CLOSE(64, 0),
	SAVE(80, 0),
	;

	override fun render(matrices: MatrixStack, x: Int, y: Int, width: Int, height: Int, hovered: Boolean) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader)
		RenderSystem.setShaderTexture(0, TEXTURE)
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1f)
		RenderSystem.enableBlend()
		RenderSystem.defaultBlendFunc()
		RenderSystem.enableDepthTest()
		if (!hovered)
			DrawableHelper.drawTexture(
				matrices,
				x,
				y,
				width,
				height,
				u.toFloat(),
				v.toFloat(),
				this.width,
				this.height,
				textureWidth,
				textureHeight
			)
		else DrawableHelper.drawTexture(
			matrices,
			x,
			y,
			width,
			height,
			hoverU.toFloat(),
			hoverV.toFloat(),
			this.width,
			this.height,
			textureWidth,
			textureHeight
		)
		RenderSystem.disableTexture()
	}


}