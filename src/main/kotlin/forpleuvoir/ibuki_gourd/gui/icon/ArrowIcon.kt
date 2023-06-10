package forpleuvoir.ibuki_gourd.gui.icon

import com.mojang.blaze3d.systems.RenderSystem
import forpleuvoir.ibuki_gourd.utils.color.Color4f
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.GameRenderer
import net.minecraft.util.Identifier

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.icon

 * 文件名 ArrowIcon

 * 创建时间 2022/1/20 15:32

 * @author forpleuvoir

 */

enum class ArrowIcon(
	override val u: Int,
	override val v: Int,
	override val width: Int,
	override val height: Int,
	override val hoverU: Int = u,
	override val hoverV: Int = v,
	override val textureWidth: Int = 256,
	override val textureHeight: Int = 256
) : IIcon {
	Right(12, 5, 22, 22, 12, 37),
	Left(30, 5, 22, 22, 30, 37),
	Up(97, 1, 15, 15, 97, 33),
	Down(65, 16, 15, 15, 65, 48)
	;

	companion object {
		val TEXTURE = Identifier("textures/gui/server_selection.png")
	}

	override fun render(
		content: DrawContext,
		x: Int,
		y: Int,
		width: Int,
		height: Int,
		hovered: Boolean,
		color: Color4f,
		hoveredColor: Color4f
	) {
		RenderSystem.setShader(GameRenderer::getPositionTexProgram)
		if (hovered) RenderSystem.setShaderColor(
			hoveredColor.red,
			hoveredColor.green,
			hoveredColor.blue,
			hoveredColor.alpha
		) else RenderSystem.setShaderColor(color.red, color.green, color.blue, color.alpha)
		RenderSystem.enableBlend()
		RenderSystem.defaultBlendFunc()
		RenderSystem.enableDepthTest()
		if (!hovered)
			content.drawTexture(
				TEXTURE,
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
		else content.drawTexture(
			TEXTURE,
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
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
	}
}