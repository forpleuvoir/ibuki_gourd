@file:Suppress("unused")

package com.forpleuvoir.ibukigourd.gui.base

import com.forpleuvoir.ibukigourd.gui.base.HorizontalAlign.*
import com.forpleuvoir.ibukigourd.util.text.Text
import com.forpleuvoir.ibukigourd.util.textRenderer
import com.forpleuvoir.nebula.common.color.Color
import com.forpleuvoir.nebula.common.color.Colors
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.ShaderProgram
import net.minecraft.client.render.*
import net.minecraft.client.render.LightmapTextureManager.MAX_LIGHT_COORDINATE
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import java.util.function.Supplier

fun setShader(shaderSupplier: Supplier<ShaderProgram>) = RenderSystem.setShader(shaderSupplier)

fun setShader(shaderSupplier: (() -> ShaderProgram)) = RenderSystem.setShader(shaderSupplier)

fun setShaderTexture(texture: Identifier) = RenderSystem.setShaderTexture(0, texture)

fun lineWidth(width: Number) = RenderSystem.lineWidth(width.toFloat())

fun enableTexture() = RenderSystem.enableTexture()

fun disableTexture() = RenderSystem.disableTexture()

fun setShaderColor(color: Color) = RenderSystem.setShaderColor(color.redF, color.greenF, color.blueF, color.alphaF)

fun enablePolygonOffset() = RenderSystem.enablePolygonOffset()

fun polygonOffset(factor: Number, units: Number) = RenderSystem.polygonOffset(factor.toFloat(), units.toFloat())

fun disablePolygonOffset() = RenderSystem.disablePolygonOffset()

fun enableBlend() = RenderSystem.enableBlend()

fun defaultBlendFunc() = RenderSystem.defaultBlendFunc()

fun disableBlend() = RenderSystem.disableBlend()

fun enableDepthTest() = RenderSystem.enableDepthTest()

fun disableDepthTest() = RenderSystem.disableDepthTest()

fun enableScissor(x: Number, y: Number, width: Number, height: Number) {
	val window = MinecraftClient.getInstance().window
	val framebufferHeight = window.framebufferHeight
	val scale = window.scaleFactor
	val x1 = x.toDouble() * scale
	val y1 = framebufferHeight.toDouble() - (y.toDouble() + height.toDouble()) * scale
	val width1 = width.toDouble() * scale
	val height1 = height.toDouble() * scale
	RenderSystem.enableScissor(
		x1.toInt(),
		y1.toInt(),
		0.coerceAtLeast(width1.toInt()),
		0.coerceAtLeast(height1.toInt())
	)
}

fun enableScissor(transform: Transform) {
	enableScissor(transform.position.x, transform.position.y, transform.width, transform.height)
}

fun disableScissor() = RenderSystem.disableScissor()

/**
 * 绘制线
 * @param matrixStack MatrixStack
 * @param x Number
 * @param y Number
 * @param x2 Number
 * @param y2 Number
 * @param color Color
 * @param color2 Color
 * @param lineWidth Number
 * @param zOffset Number
 * @param zOffset2 Number
 */
fun drawLine(
	matrixStack: MatrixStack,
	x: Number,
	y: Number,
	x2: Number,
	y2: Number,
	color: Color,
	color2: Color,
	lineWidth: Number,
	zOffset: Number = 0,
	zOffset2: Number = 0
) {
	setShader { GameRenderer.getRenderTypeLinesProgram()!! }
	enableBlend()
	defaultBlendFunc()
	disableTexture()
	lineWidth(lineWidth)
	val buffer = Tessellator.getInstance().buffer
	val matrix4f = matrixStack.peek().positionMatrix
	buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES)
	buffer.vertex(matrix4f, x.toFloat(), y.toFloat(), zOffset.toFloat()).color(color.argb).next()
	buffer.vertex(matrix4f, x2.toFloat(), y2.toFloat(), zOffset2.toFloat()).color(color2.argb).next()
	BufferRenderer.drawWithGlobalProgram(buffer.end())
	lineWidth(1f)
	enableTexture()
	disableBlend()
}

/**
 * 绘制矩形
 * @param matrixStack MatrixStack
 * @param x Number
 * @param y Number
 * @param width Number
 * @param height Number
 * @param color Color
 * @param zOffset Number
 */
fun drawRect(
	matrixStack: MatrixStack,
	x: Number,
	y: Number,
	width: Number,
	height: Number,
	color: Color,
	zOffset: Number = 0
) {
	setShader { GameRenderer.getPositionColorProgram()!! }
	enableBlend()
	defaultBlendFunc()
	disableTexture()
	val buffer = Tessellator.getInstance().buffer
	val matrix4f = matrixStack.peek().positionMatrix
	buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
	buffer.vertex(matrix4f, x.toFloat(), y.toFloat(), zOffset.toFloat()).color(color.argb).next()
	buffer.vertex(matrix4f, x.toFloat(), (y.toFloat() + height.toFloat()), zOffset.toFloat()).color(color.argb).next()
	buffer.vertex(matrix4f, (x.toFloat() + width.toFloat()), (y.toFloat() + height.toFloat()), zOffset.toFloat())
		.color(color.argb).next()
	buffer.vertex(matrix4f, (x.toFloat() + width.toFloat()), y.toFloat(), zOffset.toFloat()).color(color.argb).next()
	BufferRenderer.drawWithGlobalProgram(buffer.end())
	enableTexture()
	disableBlend()
}

/**
 * @see drawRect
 * @param matrixStack MatrixStack
 * @param transform Transform
 * @param color Color
 */
fun drawRect(matrixStack: MatrixStack, transform: Transform, color: Color) {
	drawRect(
		matrixStack,
		transform.position.x,
		transform.position.y,
		transform.width,
		transform.height,
		color,
		transform.position.z
	)
}

/**
 * 绘制边框线条
 * @receiver Drawable
 * @param matrixStack MatrixStack
 * @param x Number
 * @param y Number
 * @param width Number
 * @param height Number
 * @param color IColor
 * @param borderWidth Number
 * @param zOffset Number
 */
fun drawOutline(
	matrixStack: MatrixStack,
	x: Number,
	y: Number,
	width: Number,
	height: Number,
	color: Color,
	borderWidth: Number = 1,
	zOffset: Number = 0
) {
	drawRect(matrixStack, x, y, borderWidth, height, color, zOffset)
	drawRect(
		matrixStack,
		x.toDouble() + width.toDouble() - borderWidth.toDouble(),
		y,
		borderWidth,
		height,
		color,
		zOffset
	)
	drawRect(
		matrixStack,
		x.toDouble() + borderWidth.toDouble(),
		y,
		width.toDouble() - 2 * borderWidth.toDouble(),
		borderWidth,
		color,
		zOffset
	)
	drawRect(
		matrixStack,
		x.toDouble() + borderWidth.toDouble(),
		y.toDouble() + height.toDouble() - borderWidth.toDouble(),
		width.toDouble() - 2 * borderWidth.toDouble(),
		borderWidth,
		color, zOffset
	)
}

/**
 * @see drawOutline
 * @param matrixStack MatrixStack
 * @param transform Transform
 * @param color Color
 * @param borderWidth Number
 */
fun drawOutline(matrixStack: MatrixStack, transform: Transform, color: Color, borderWidth: Number = 1) {
	drawOutline(
		matrixStack,
		transform.position.x,
		transform.position.y,
		transform.width,
		transform.height,
		color,
		borderWidth,
		transform.position.z
	)
}

/**
 *
 * 绘制带边框线条的矩形
 *
 * @receiver Drawable
 * @param matrixStack MatrixStack
 * @param x Number
 * @param y Number
 * @param width Number
 * @param height Number
 * @param color IColor
 * @param outlineColor IColor
 * @param borderWidth Number
 * @param innerOutline Boolean
 */
fun drawOutlinedBox(
	matrixStack: MatrixStack,
	x: Number,
	y: Number,
	width: Number,
	height: Number,
	color: Color,
	outlineColor: Color,
	borderWidth: Number = 1,
	innerOutline: Boolean = true,
	zOffset: Number = 0
) {
	if (innerOutline) {
		drawRect(matrixStack, x, y, width, height, color, zOffset)
		drawOutline(
			matrixStack,
			x.toDouble() - borderWidth.toDouble(),
			y.toDouble() - borderWidth.toDouble(),
			width.toDouble() + borderWidth.toDouble() * 2,
			height.toDouble() + borderWidth.toDouble() * 2,
			outlineColor, borderWidth, zOffset
		)
	} else {
		drawRect(
			matrixStack,
			x.toDouble() + borderWidth.toDouble(),
			y.toDouble() + borderWidth.toDouble(),
			width.toDouble() - 2 * borderWidth.toDouble(),
			height.toDouble() - 2 * borderWidth.toDouble(),
			color, zOffset
		)
		drawOutline(matrixStack, x, y, width, height, outlineColor, borderWidth, zOffset)
	}
}

/**
 * @see drawOutlinedBox
 * @param matrixStack MatrixStack
 * @param transform Transform
 * @param color Color
 * @param outlineColor Color
 * @param borderWidth Number
 * @param innerOutline Boolean
 */
fun drawOutlinedBox(
	matrixStack: MatrixStack,
	transform: Transform,
	color: Color,
	outlineColor: Color,
	borderWidth: Number = 1,
	innerOutline: Boolean = true
) {
	drawOutlinedBox(
		matrixStack,
		transform.position.x,
		transform.position.y,
		transform.width,
		transform.height,
		color,
		outlineColor,
		borderWidth,
		innerOutline,
		transform.position.z
	)
}

/**
 * 绘制渐变
 * @param matrixStack MatrixStack
 * @param startX Number
 * @param startY Number
 * @param endX Number
 * @param endY Number
 * @param startColor Color
 * @param endColor Color
 * @param zOffset Number
 */
fun drawGradient(
	matrixStack: MatrixStack,
	startX: Number,
	startY: Number,
	endX: Number,
	endY: Number,
	startColor: Color,
	endColor: Color,
	zOffset: Number = 0
) {
	disableTexture()
	enableBlend()
	setShader { GameRenderer.getPositionColorProgram()!! }
	val tess = Tessellator.getInstance()
	val buffer = tess.buffer
	val matrix4f = matrixStack.peek().positionMatrix
	buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
	buffer.vertex(matrix4f, endX.toFloat(), startY.toFloat(), zOffset.toFloat()).color(startColor.argb).next()
	buffer.vertex(matrix4f, startX.toFloat(), startY.toFloat(), zOffset.toFloat()).color(startColor.argb).next()
	buffer.vertex(matrix4f, startX.toFloat(), endY.toFloat(), zOffset.toFloat()).color(endColor.argb).next()
	buffer.vertex(matrix4f, endX.toFloat(), endY.toFloat(), zOffset.toFloat()).color(endColor.argb).next()
	tess.draw()
	disableBlend()
	enableTexture()
}

/**
 * 绘制纹理
 * @param matrixStack MatrixStack
 * @param x Number
 * @param y Number
 * @param width Number
 * @param height Number
 * @param u Number
 * @param v Number
 * @param regionWidth Int
 * @param regionHeight Int
 * @param textureWidth Int
 * @param textureHeight Int
 * @param zOffset Number
 */
fun drawTexture(
	matrixStack: MatrixStack,
	x: Number,
	y: Number,
	width: Number,
	height: Number,
	u: Number,
	v: Number,
	regionWidth: Int,
	regionHeight: Int,
	textureWidth: Int = 256,
	textureHeight: Int = 256,
	zOffset: Number = 0
) {
	val matrix4f = matrixStack.peek().positionMatrix
	val bufferBuilder = Tessellator.getInstance().buffer
	setShader { GameRenderer.getPositionTexProgram()!! }
	bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE)
	bufferBuilder.vertex(matrix4f, x.toFloat(), y.toFloat() + height.toFloat(), zOffset.toFloat())
		.texture(u.toFloat() / textureWidth, (v.toFloat() + regionHeight) / textureHeight).next()
	bufferBuilder.vertex(matrix4f, x.toFloat() + width.toFloat(), y.toFloat() + height.toFloat(), zOffset.toFloat())
		.texture((u.toFloat() + regionWidth) / textureWidth, (v.toFloat() + regionHeight) / textureHeight).next()
	bufferBuilder.vertex(matrix4f, x.toFloat() + width.toFloat(), y.toFloat(), zOffset.toFloat())
		.texture((u.toFloat() + regionWidth) / textureWidth, v.toFloat() / textureHeight).next()
	bufferBuilder.vertex(matrix4f, x.toFloat(), y.toFloat(), zOffset.toFloat())
		.texture(u.toFloat() / textureWidth, v.toFloat() / textureHeight).next()
	BufferRenderer.drawWithGlobalProgram(bufferBuilder.end())
}

/**
 * @see drawTexture
 * @param matrixStack MatrixStack
 * @param transform Transform
 * @param u Number
 * @param v Number
 * @param regionWidth Int
 * @param regionHeight Int
 * @param textureWidth Int
 * @param textureHeight Int
 */
fun drawTexture(
	matrixStack: MatrixStack,
	transform: Transform,
	u: Number,
	v: Number,
	regionWidth: Int,
	regionHeight: Int,
	textureWidth: Int = 256,
	textureHeight: Int = 256,
) {
	drawTexture(
		matrixStack, transform.position.x, transform.position.y, transform.width, transform.height,
		u, v, regionWidth, regionHeight, textureWidth, textureHeight
	)
}

/**
 * 渲染.9 格式的材质
 * @receiver Drawable
 */
fun draw9Texture(
	matrixStack: MatrixStack,
	x: Number,
	y: Number,
	cornerLeftWidth: Int,
	cornerRightWidth: Int,
	cornerTopHeight: Int,
	cornerBottomHeight: Int,
	width: Number,
	height: Number,
	u: Number,
	v: Number,
	regionWidth: Int,
	regionHeight: Int,
	textureWidth: Int = 256,
	textureHeight: Int = 256,
	zOffset: Number = 0
) {
	if (cornerLeftWidth == 0 &&
		cornerRightWidth == 0 &&
		cornerTopHeight == 0 &&
		cornerBottomHeight == 0
	) {
		drawTexture(
			matrixStack, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight,
			zOffset
		)
	}

	/**
	 * centerWidth
	 */
	val cw = width.toDouble() - (cornerLeftWidth + cornerRightWidth)

	/**
	 * centerHeight
	 */
	val ch = height.toDouble() - (cornerTopHeight + cornerBottomHeight)

	/**
	 * centerRegionWidth
	 */
	val crw = regionWidth - (cornerLeftWidth + cornerRightWidth)

	/**
	 *  centerRegionHeight
	 */
	val crh = regionHeight - (cornerTopHeight + cornerBottomHeight)

	val centerU = u.toDouble() + cornerLeftWidth
	val rightU = u.toDouble() + (regionWidth - cornerRightWidth)
	val centerV = v.toDouble() + cornerTopHeight
	val bottomV = v.toDouble() + (regionHeight - cornerBottomHeight)
	val centerX = x.toDouble() + cornerLeftWidth
	val rightX = x.toDouble() + (width.toDouble() - cornerRightWidth)
	val centerY = y.toDouble() + cornerTopHeight
	val bottomY = y.toDouble() + (height.toDouble() - cornerBottomHeight)
	//top left
	drawTexture(
		matrixStack, x, y, cornerLeftWidth, cornerTopHeight, u, v,
		cornerLeftWidth, cornerTopHeight, textureWidth, textureHeight,
		zOffset
	)
	//top center
	drawTexture(
		matrixStack, centerX, y, cw, cornerTopHeight, centerU, v,
		crw, cornerTopHeight, textureWidth, textureHeight,
		zOffset
	)
	//top right
	drawTexture(
		matrixStack, rightX, y, cornerRightWidth, cornerTopHeight, rightU, v,
		cornerRightWidth, cornerTopHeight, textureWidth, textureHeight,
		zOffset
	)
	//center left
	drawTexture(
		matrixStack, x, centerY, cornerLeftWidth, ch, u, centerV, cornerLeftWidth, crh,
		textureWidth, textureHeight,
		zOffset
	)
	//center
	drawTexture(matrixStack, centerX, centerY, cw, ch, centerU, centerV, crw, crh, textureWidth, textureHeight, zOffset)
	//center right
	drawTexture(
		matrixStack, rightX, centerY, cornerRightWidth, ch, rightU, centerV,
		cornerRightWidth, crh, textureWidth, textureHeight,
		zOffset
	)
	//bottom left
	drawTexture(
		matrixStack, x, bottomY, cornerLeftWidth, cornerBottomHeight, u, bottomV,
		cornerLeftWidth, cornerBottomHeight, textureWidth, textureHeight,
		zOffset
	)
	//bottom center
	drawTexture(
		matrixStack, centerX, bottomY, cw, cornerBottomHeight, centerU, bottomV,
		crw, cornerBottomHeight, textureWidth, textureHeight,
		zOffset
	)
	//bottom right
	drawTexture(
		matrixStack, rightX, bottomY, cornerRightWidth, cornerBottomHeight, rightU, bottomV,
		cornerRightWidth, cornerBottomHeight, textureWidth, textureHeight,
		zOffset
	)
}

/**
 * @see draw9Texture
 * @param matrixStack MatrixStack
 * @param transform Transform
 * @param cornerLeftWidth Int
 * @param cornerRightWidth Int
 * @param cornerTopHeight Int
 * @param cornerBottomHeight Int
 * @param u Number
 * @param v Number
 * @param regionWidth Int
 * @param regionHeight Int
 * @param textureWidth Int
 * @param textureHeight Int
 */
fun draw9Texture(
	matrixStack: MatrixStack,
	transform: Transform,
	cornerLeftWidth: Int,
	cornerRightWidth: Int,
	cornerTopHeight: Int,
	cornerBottomHeight: Int,
	u: Number,
	v: Number,
	regionWidth: Int,
	regionHeight: Int,
	textureWidth: Int = 256,
	textureHeight: Int = 256,
) {
	draw9Texture(
		matrixStack,
		transform.position.x,
		transform.position.y,
		cornerLeftWidth,
		cornerRightWidth,
		cornerTopHeight,
		cornerBottomHeight,
		transform.width,
		transform.height,
		u,
		v,
		regionWidth,
		regionHeight,
		textureWidth,
		textureHeight,
		transform.position.z
	)
}

/**
 * 渲染.9 格式的材质
 * 只适用于边角为相同大小的正方形的材质
 */
fun draw9Texture(
	matrixStack: MatrixStack,
	x: Number,
	y: Number,
	/**
	 * 边角大小
	 */
	cornerSize: Int,
	width: Number,
	height: Number,
	u: Number,
	v: Number,
	regionWidth: Int,
	regionHeight: Int,
	textureWidth: Int = 256,
	textureHeight: Int = 256,
	zOffset: Number = 0
) {
	draw9Texture(
		matrixStack, x, y, cornerSize, cornerSize, cornerSize, cornerSize,
		width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight,
		zOffset
	)
}

/**
 * @see draw9Texture
 * @param matrixStack MatrixStack
 * @param transform Transform
 * @param cornerSize Int
 * @param u Number
 * @param v Number
 * @param regionWidth Int
 * @param regionHeight Int
 * @param textureWidth Int
 * @param textureHeight Int
 */
fun draw9Texture(
	matrixStack: MatrixStack,
	transform: Transform,
	/**
	 * 边角大小
	 */
	cornerSize: Int,
	u: Number,
	v: Number,
	regionWidth: Int,
	regionHeight: Int,
	textureWidth: Int = 256,
	textureHeight: Int = 256,
) {
	draw9Texture(
		matrixStack,
		transform.position.x,
		transform.position.y,
		cornerSize,
		transform.width,
		transform.height,
		u,
		v,
		regionWidth,
		regionHeight,
		textureWidth,
		textureHeight,
		transform.position.z
	)
}

/**
 * 绘制纹理
 * @param matrixStack MatrixStack
 * @param x Number
 * @param y Number
 * @param width Number
 * @param height Number
 * @param texture GuiTexture
 * @param shaderColor Color
 * @param zOffset Number
 */
fun drawTexture(
	matrixStack: MatrixStack,
	x: Number,
	y: Number,
	width: Number,
	height: Number,
	texture: GuiTexture,
	shaderColor: Color = Colors.WHITE,
	zOffset: Number = 0
) {
	setShaderTexture(texture.texture)
	enableBlend()
	defaultBlendFunc()
	enableDepthTest()
	setShaderColor(shaderColor)
	draw9Texture(
		matrixStack, x, y, texture.corner.left, texture.corner.right, texture.corner.top, texture.corner.bottom,
		width, height, texture.u, texture.v,
		texture.regionWidth, texture.regionHeight, texture.textureWidth, texture.textureHeight, zOffset
	)
	disableBlend()
	setShaderColor(Colors.WHITE)
}

/**
 * @see drawTexture
 * @param matrixStack MatrixStack
 * @param transform Transform
 * @param texture GuiTexture
 * @param shaderColor Color
 */
fun drawTexture(
	matrixStack: MatrixStack,
	transform: Transform,
	texture: GuiTexture,
	shaderColor: Color = Colors.WHITE,
) {
	drawTexture(
		matrixStack,
		transform.position.x, transform.position.y, transform.width, transform.height,
		texture, shaderColor, transform.position.z
	)
}

/**
 * 绘制居中文本
 * @param matrixStack MatrixStack
 * @param text Text
 * @param x Number
 * @param y Number
 * @param width Number
 * @param height Number
 * @param shadow Boolean
 * @param rightToLeft Boolean
 * @param color Color
 * @param backgroundColor Color
 */
fun drawCenteredText(
	matrixStack: MatrixStack,
	text: Text,
	x: Number,
	y: Number,
	width: Number,
	height: Number,
	shadow: Boolean = true,
	rightToLeft: Boolean = false,
	color: Color = Colors.WHITE,
	backgroundColor: Color = Colors.WHITE.alpha(0.5f),
) {
	setShaderColor(Colors.WHITE)
	val centerX = x.toFloat() + width.toFloat() / 2
	val centerY = y.toFloat() + height.toFloat() / 2
	val textWidth = textRenderer.getWidth(text)
	val immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().buffer)
	textRenderer.draw(
		text.string,
		centerX - textWidth / 2,
		centerY - textRenderer.fontHeight / 2,
		color.argb, shadow, matrixStack.peek().positionMatrix, immediate, false,
		backgroundColor.argb, MAX_LIGHT_COORDINATE, rightToLeft
	)
	immediate.draw()
}

/**
 * @see drawCenteredText
 * @param matrixStack MatrixStack
 * @param text Text
 * @param transform Transform
 * @param shadow Boolean
 * @param rightToLeft Boolean
 * @param color Color
 * @param backgroundColor Color
 */
fun drawCenteredText(
	matrixStack: MatrixStack,
	text: Text,
	transform: Transform,
	shadow: Boolean = true,
	rightToLeft: Boolean = false,
	color: Color = Colors.WHITE,
	backgroundColor: Color = Colors.WHITE.alpha(0.5f),
) {
	drawCenteredText(
		matrixStack, text, transform.position.x, transform.position.y, transform.width, transform.height,
		shadow, rightToLeft, color, backgroundColor
	)
}

fun drawText(
	matrixStack: MatrixStack,
	text: String,
	x: Number,
	y: Number,
	shadow: Boolean = true,
	color: Color = Colors.WHITE,
) {
	textRenderer.draw(
		text, x.toFloat(), y.toFloat(), color.argb, shadow, matrixStack.peek().positionMatrix,
		VertexConsumerProvider.immediate(Tessellator.getInstance().buffer), false,
		Colors.WHITE.argb, MAX_LIGHT_COORDINATE, false
	)
}

/**
 * 绘制多行字符串文本
 * @param matrixStack MatrixStack
 * @param lines List<String>
 * @param x Number
 * @param y Number
 * @param lineSpacing Number
 * @param color Color
 * @param shadow Boolean
 * @param align HorizontalAlign
 * @param rightToLeft Boolean
 */
fun drawStringLines(
	matrixStack: MatrixStack,
	lines: List<String>,
	x: Number,
	y: Number,
	lineSpacing: Number = 1,
	color: Color = Colors.BLACK,
	shadow: Boolean = false,
	align: HorizontalAlign = Left,
	rightToLeft: Boolean = false,
) {
	val drawText: (text: String, posX: Float, posY: Float) -> Unit = { text, posX, posY ->
		val immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().buffer)
		textRenderer.draw(
			text, posX, posY,
			color.argb, shadow, matrixStack.peek().positionMatrix, immediate, false, 0,
			MAX_LIGHT_COORDINATE, rightToLeft
		)
		immediate.draw()
	}
	var textY = y.toFloat()
	when (align) {
		Left -> {
			val textX = x.toFloat()
			for (text in lines) {
				drawText(text, textX, textY)
				textY += textRenderer.fontHeight + lineSpacing.toFloat()
			}
		}

		Center -> {
			for (text in lines) {
				val textX = x.toFloat() - (textRenderer.getWidth(text) / 2)
				drawText(text, textX, textY)
				textY += textRenderer.fontHeight + lineSpacing.toFloat()
			}
		}

		Right -> {
			for (text in lines) {
				val textX = x.toFloat() - textRenderer.getWidth(text)
				drawText(text, textX, textY)
				textY += textRenderer.fontHeight + lineSpacing.toFloat()
			}
		}
	}

}

fun drawTextLines(
	matrixStack: MatrixStack,
	lines: List<Text>,
	x: Number,
	y: Number,
	lineSpacing: Number = 1,
	color: Color = Colors.BLACK,
	shadow: Boolean = false,
	align: HorizontalAlign = Left,
	rightToLeft: Boolean = false,
) {
	drawStringLines(
		matrixStack,
		ArrayList<String>().apply { lines.forEach { add(it.string) } },
		x,
		y,
		lineSpacing,
		color,
		shadow,
		align,
		rightToLeft
	)
}