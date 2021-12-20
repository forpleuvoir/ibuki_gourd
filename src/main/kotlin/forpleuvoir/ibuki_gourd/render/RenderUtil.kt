package forpleuvoir.ibuki_gourd.render

import com.mojang.blaze3d.platform.GlStateManager.DstFactor
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor
import com.mojang.blaze3d.systems.RenderSystem
import forpleuvoir.ibuki_gourd.utils.color.Color4f
import forpleuvoir.ibuki_gourd.utils.color.Color4i
import forpleuvoir.ibuki_gourd.utils.color.IColor
import net.minecraft.client.gui.widget.ClickableWidget
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
	fun drawOutlinedBox(x: Number, y: Number, width: Number, height: Number, colorBg: IColor<*>, colorBorder: IColor<*>, zLevel: Number = 0.0) {
		drawRect(x, y, width, height, colorBg, zLevel)
		drawOutline(x.toDouble() - 1, y.toDouble() - 1, width.toDouble() + 2, height.toDouble() + 2, borderColor = colorBorder, zLevel = zLevel)
	}


	//from malilib
	fun drawOutline(x: Number, y: Number, width: Number, height: Number, borderWidth: Number = 1, borderColor: IColor<*>, zLevel: Number = 0.0) {
		drawRect(x, y, borderWidth, height, borderColor, zLevel)
		drawRect(x.toDouble() + width.toDouble() - borderWidth.toDouble(), y, borderWidth, height, borderColor, zLevel)
		drawRect(x.toDouble() + borderWidth.toDouble(), y, width.toDouble() - 2 * borderWidth.toDouble(), borderWidth, borderColor, zLevel)
		drawRect(
			x.toDouble() + borderWidth.toDouble(),
			y.toDouble() + height.toDouble() - borderWidth.toDouble(),
			width.toDouble() - 2 * borderWidth.toDouble(),
			borderWidth,
			borderColor,
			zLevel
		)
	}

	fun drawRect(x: Number, y: Number, width: Number, height: Number, color: IColor<*>, zLevel: Number = 0.0) {
		RenderSystem.setShader { GameRenderer.getPositionColorShader() }
		RenderSystem.applyModelViewMatrix()
		val tessellator = Tessellator.getInstance()
		val buffer = tessellator.buffer
		setupBlend()
		buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR)
		buffer.vertex(x.toDouble(), y.toDouble(), zLevel.toDouble()).color(color.rgba).next()
		buffer.vertex(x.toDouble(), (y.toDouble() + height.toDouble()), zLevel.toDouble()).color(color.rgba).next()
		buffer.vertex((x.toDouble() + width.toDouble()), (y.toDouble() + height.toDouble()), zLevel.toDouble()).color(color.rgba).next()
		buffer.vertex((x.toDouble() + width.toDouble()), y.toDouble(), zLevel.toDouble()).color(color.rgba).next()
		tessellator.draw()
		RenderSystem.disableBlend()
	}

	fun drawRect(x: Number, y: Number, width: Number, height: Number, color: Color4f, zLevel: Number = 0.0) {
		drawRect(x, y, width, height, Color4i().fromInt(color.rgba), zLevel)
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
	fun drawGradientRect(
		left: Number,
		top: Number,
		right: Number,
		bottom: Number,
		startColor: IColor<*>,
		endColor: IColor<*>,
		zLevel: Number = 0.0
	) {
		RenderSystem.disableTexture()
		setupBlend()
		RenderSystem.setShader { GameRenderer.getPositionColorShader() }
		RenderSystem.applyModelViewMatrix()
		val tessellator = Tessellator.getInstance()
		val buffer = tessellator.buffer
		buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR)
		buffer.vertex(right.toDouble(), top.toDouble(), zLevel.toDouble()).color(startColor.rgba).next()
		buffer.vertex(left.toDouble(), top.toDouble(), zLevel.toDouble()).color(startColor.rgba).next()
		buffer.vertex(left.toDouble(), bottom.toDouble(), zLevel.toDouble()).color(endColor.rgba).next()
		buffer.vertex(right.toDouble(), bottom.toDouble(), zLevel.toDouble()).color(endColor.rgba).next()
		tessellator.draw()
		RenderSystem.disableBlend()
		RenderSystem.enableTexture()
	}

	fun setupBlend() {
		RenderSystem.enableBlend()
		RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA, SrcFactor.ONE, DstFactor.ZERO)
	}

	fun isMouseHovered(x: Number, y: Number, width: Number, height: Number, mouseX: Number, mouseY: Number): Boolean {
		return mouseX.toDouble() >= x.toDouble() && mouseX.toDouble() <= x.toDouble() + width.toDouble() && mouseY.toDouble() >= y.toDouble() && mouseY.toDouble() <= y.toDouble() + height.toDouble()
	}

	inline fun isMouseHovered(x: Number, y: Number, width: Number, height: Number, mouseX: Number, mouseY: Number, callback: () -> Unit) {
		if (isMouseHovered(x, y, width, height, mouseX, mouseY)) callback.invoke()
	}

	fun isMouseHovered(widget: ClickableWidget, mouseX: Number, mouseY: Number): Boolean {
		return isMouseHovered(widget.x, widget.y, widget.width, widget.height, mouseX, mouseY)
	}

	@Suppress("unchecked_cast")
	inline fun ClickableWidget.isMouseHovered(mouseX: Number, mouseY: Number, callback: (ClickableWidget) -> Unit) {
		if (isMouseHovered(this, mouseX, mouseY)) {
			callback.invoke(this)
		}
	}

	@JvmName("isMouseHovered1")
	@Suppress("unchecked_cast")
	inline fun <W : ClickableWidget> ClickableWidget.isMouseHovered(mouseX: Number, mouseY: Number, callback: (W) -> Unit) {
		if (isMouseHovered(this, mouseX, mouseY)) {
			callback.invoke(this as W)
		}
	}

}