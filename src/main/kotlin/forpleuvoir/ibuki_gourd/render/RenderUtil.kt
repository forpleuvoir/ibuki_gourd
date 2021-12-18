package forpleuvoir.ibuki_gourd.render

import com.mojang.blaze3d.platform.GlStateManager.DstFactor
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor
import com.mojang.blaze3d.systems.RenderSystem
import forpleuvoir.ibuki_gourd.utils.color.Color4i
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat.DrawMode
import net.minecraft.client.render.VertexFormats


/**
 * 渲染工具

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.render

 * 文件名 RenderUtil

 * 创建时间 2021/12/16 21:54

 * @author forpleuvoir

 */
object RenderUtil {

	//from malilib
	fun drawOutlinedBox(x: Int, y: Int, width: Int, height: Int, colorBg: Color4i, colorBorder: Color4i, zLevel: Double = 0.0) {
		drawRect(x, y, width, height, colorBg, zLevel)
		drawOutline(x - 1, y - 1, width + 2, height + 2, colorBorder = colorBorder, zLevel = zLevel)
	}

	//from malilib
	fun drawOutline(x: Int, y: Int, width: Int, height: Int, borderWidth: Int = 1, colorBorder: Color4i, zLevel: Double = 0.0) {
		drawRect(x, y, borderWidth, height, colorBorder, zLevel)
		drawRect(x + width - borderWidth, y, borderWidth, height, colorBorder, zLevel)
		drawRect(x + borderWidth, y, width - 2 * borderWidth, borderWidth, colorBorder, zLevel)
		drawRect(
			x + borderWidth,
			y + height - borderWidth,
			width - 2 * borderWidth,
			borderWidth,
			colorBorder,
			zLevel
		)
	}

	//from malilib
	fun drawRect(x: Int, y: Int, width: Int, height: Int, color: Color4i, zLevel: Double = 0.0) {
		val a = color.alpha
		val r = color.red
		val g = color.green
		val b = color.blue
		RenderSystem.setShader { GameRenderer.getPositionColorShader() }
		RenderSystem.applyModelViewMatrix()
		val tessellator = Tessellator.getInstance()
		val buffer = tessellator.buffer
		setupBlend()
		buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR)
		buffer.vertex(x.toDouble(), y.toDouble(), zLevel).color(r, g, b, a).next()
		buffer.vertex(x.toDouble(), (y + height).toDouble(), zLevel).color(r, g, b, a).next()
		buffer.vertex((x + width).toDouble(), (y + height).toDouble(), zLevel).color(r, g, b, a).next()
		buffer.vertex((x + width).toDouble(), y.toDouble(), zLevel).color(r, g, b, a).next()
		tessellator.draw()
		RenderSystem.disableBlend()
	}

	/**
	 * 绘制渐变矩形
	 * @param left Int
	 * @param top Int
	 * @param right Int
	 * @param bottom Int
	 * @param zLevel Double
	 * @param startColor Int
	 * @param endColor Int
	 */
	fun drawGradientRect(left: Int, top: Int, right: Int, bottom: Int, startColor: Color4i, endColor: Color4i, zLevel: Double = 0.0) {
		val sa = startColor.alpha
		val sr = startColor.red
		val sg = startColor.green
		val sb = startColor.blue
		val ea = endColor.alpha
		val er = endColor.red
		val eg = endColor.green
		val eb = endColor.blue
		RenderSystem.disableTexture()
		setupBlend()
		RenderSystem.setShader { GameRenderer.getPositionColorShader() }
		RenderSystem.applyModelViewMatrix()
		val tessellator = Tessellator.getInstance()
		val buffer = tessellator.buffer
		buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR)
		buffer.vertex(right.toDouble(), top.toDouble(), zLevel).color(sr, sg, sb, sa).next()
		buffer.vertex(left.toDouble(), top.toDouble(), zLevel).color(sr, sg, sb, sa).next()
		buffer.vertex(left.toDouble(), bottom.toDouble(), zLevel).color(er, eg, eb, ea).next()
		buffer.vertex(right.toDouble(), bottom.toDouble(), zLevel).color(er, eg, eb, ea).next()
		tessellator.draw()
		RenderSystem.disableBlend()
		RenderSystem.enableTexture()
	}

	fun setupBlend() {
		RenderSystem.enableBlend()
		RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA, SrcFactor.ONE, DstFactor.ZERO)
	}

	fun isMouseHovered(x: Int, y: Int, width: Int, height: Int, mouseX: Int, mouseY: Int): Boolean {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height
	}

	inline fun isMouseHovered(x: Int, y: Int, width: Int, height: Int, mouseX: Int, mouseY: Int, callback: () -> Unit) {
		if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) callback.invoke()
	}

	fun isMouseHovered(x: Int, y: Int, width: Int, height: Int, mouseX: Double, mouseY: Double): Boolean {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height
	}

	inline fun isMouseHovered(x: Int, y: Int, width: Int, height: Int, mouseX: Double, mouseY: Double, callback: () -> Unit) {
		if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) callback.invoke()
	}
}