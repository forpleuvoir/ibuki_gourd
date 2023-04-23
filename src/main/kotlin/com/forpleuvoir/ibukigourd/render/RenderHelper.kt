@file:Suppress("UNUSED", "DuplicatedCode")

package com.forpleuvoir.ibukigourd.render

import com.forpleuvoir.ibukigourd.gui.base.Transform
import com.forpleuvoir.ibukigourd.gui.texture.WidgetTexture
import com.forpleuvoir.ibukigourd.render.base.*
import com.forpleuvoir.ibukigourd.render.base.HorizontalAlignment.Left
import com.forpleuvoir.ibukigourd.render.base.math.Vector3
import com.forpleuvoir.ibukigourd.render.base.math.Vector3f
import com.forpleuvoir.ibukigourd.render.base.texture.Corner
import com.forpleuvoir.ibukigourd.render.base.texture.TextureInfo
import com.forpleuvoir.ibukigourd.render.base.texture.TextureUVMapping
import com.forpleuvoir.ibukigourd.render.base.texture.UVMapping
import com.forpleuvoir.ibukigourd.render.base.vertex.*
import com.forpleuvoir.ibukigourd.util.text.literal
import com.forpleuvoir.ibukigourd.util.text.wrapToLines
import com.forpleuvoir.nebula.common.color.Color
import com.forpleuvoir.nebula.common.color.Colors
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.font.TextRenderer.TextLayerType
import net.minecraft.client.gl.ShaderProgram
import net.minecraft.client.render.*
import net.minecraft.client.render.LightmapTextureManager.MAX_LIGHT_COORDINATE
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.joml.Matrix4f
import java.util.function.Supplier

val tessellator: Tessellator by lazy { Tessellator.getInstance() }

val bufferBuilder: BufferBuilder by lazy { tessellator.buffer }

fun setShader(shaderSupplier: Supplier<ShaderProgram?>) = RenderSystem.setShader(shaderSupplier)

fun setShader(shaderSupplier: (() -> ShaderProgram?)) = RenderSystem.setShader(shaderSupplier)

fun setShaderTexture(texture: Identifier) = RenderSystem.setShaderTexture(0, texture)

fun lineWidth(width: Number) = RenderSystem.lineWidth(width.toFloat())

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
	RenderSystem.enableScissor(x1.toInt(), y1.toInt(), 0.coerceAtLeast(width1.toInt()), 0.coerceAtLeast(height1.toInt()))
}

fun enableScissor(transform: Transform) =
	enableScissor(transform.worldX, transform.worldY, transform.width, transform.height)


fun disableScissor() = RenderSystem.disableScissor()

fun VertexConsumer.vertex(matrix4f: Matrix4f, vertex: Vertex): VertexConsumer =
	vertex.let {
		vertex(matrix4f, vertex.vector3f())
		if (it is ColorVertex) color(it.color)
		if (it is UVVertex) texture(it.u, it.v)
		this
	}

fun VertexConsumer.vertex(matrixStack: MatrixStack, vertex: Vertex): VertexConsumer =
	vertex(matrixStack.peek().positionMatrix, vertex)

fun VertexConsumer.vertex(matrix4f: Matrix4f, x: Number, y: Number, z: Number): VertexConsumer =
	this.vertex(matrix4f, x.toFloat(), y.toFloat(), z.toFloat())

fun VertexConsumer.vertex(matrixStack: MatrixStack, x: Number, y: Number, z: Number): VertexConsumer =
	this.vertex(matrixStack.peek().positionMatrix, x.toFloat(), y.toFloat(), z.toFloat())

fun VertexConsumer.vertex(matrix4f: Matrix4f, vector3: Vector3<out Number>): VertexConsumer =
	this.vertex(matrix4f, vector3.x.toFloat(), vector3.y.toFloat(), vector3.z.toFloat())

fun VertexConsumer.vertex(matrixStack: MatrixStack, vector3: Vector3<out Number>): VertexConsumer =
	this.vertex(matrixStack.peek().positionMatrix, vector3.x.toFloat(), vector3.y.toFloat(), vector3.z.toFloat())

fun VertexConsumer.quadsVertex(matrix4f: Matrix4f, quads: Quadrilateral): VertexConsumer {
	vertex(matrix4f, quads.vertex1).next()
	vertex(matrix4f, quads.vertex2).next()
	vertex(matrix4f, quads.vertex3).next()
	vertex(matrix4f, quads.vertex4).next()
	return this
}

fun VertexConsumer.quadsVertex(matrixStack: MatrixStack, quads: Quadrilateral): VertexConsumer =
	this.quadsVertex(matrixStack.peek().positionMatrix, quads)

fun VertexConsumer.color(color: Color): VertexConsumer = this.color(color.argb)

/**
 * 渲染多顶点的线
 * @param matrixStack MatrixStack
 * @param lineWidth Number
 * @param colorVertices Array<[ColorVertex]>
 */
fun renderLine(matrixStack: MatrixStack, lineWidth: Number, vararg colorVertices: ColorVertex) {
	setShader(GameRenderer::getRenderTypeLinesProgram)
	enableBlend()
	defaultBlendFunc()
	lineWidth(lineWidth)
	bufferBuilder.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES)
	for (vertex in colorVertices) {
		bufferBuilder.vertex(matrixStack, vertex).next()
	}
	tessellator.draw()
	lineWidth(1f)
	disableBlend()
}

/**
 * 渲染四边形
 * @param matrixStack MatrixStack
 * @param quads Quadrilateral
 */
fun renderQuads(matrixStack: MatrixStack, quads: Quadrilateral) {
	enableBlend()
	setShader(GameRenderer::getPositionColorProgram)
	bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
	bufferBuilder.quadsVertex(matrixStack, quads)
	tessellator.draw()
	disableBlend()
}

/**
 * 渲染矩形
 * @param matrixStack MatrixStack
 * @param colorVertex Vertex
 * @param width Number
 * @param height Number
 */
fun renderRect(matrixStack: MatrixStack, colorVertex: ColorVertex, width: Number, height: Number) {
	setShader(GameRenderer::getPositionColorProgram)
	enableBlend()
	defaultBlendFunc()
	val matrix4f = matrixStack.peek().positionMatrix
	bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
	bufferBuilder.quadsVertex(matrix4f, Rectangle(colorVertex, width, height))
	tessellator.draw()
	disableBlend()
}

/**
 * @see renderRect
 * @param matrixStack MatrixStack
 * @param transform Transform
 * @param color Color
 */
fun renderRect(matrixStack: MatrixStack, transform: Transform, color: Color) =
	renderRect(matrixStack, colorVertex(transform.worldPosition, color), transform.width, transform.height)


/**
 * 渲染边框线条
 * @param matrixStack MatrixStack
 * @param colorVertex Vertex
 * @param width Number
 * @param height Number
 * @param borderWidth Number
 */
fun renderOutline(matrixStack: MatrixStack, colorVertex: ColorVertex, width: Number, height: Number, borderWidth: Number = 1) {
	renderRect(matrixStack, colorVertex, borderWidth, height)
	renderRect(matrixStack, colorVertex.x(colorVertex.x.toDouble() + width.toFloat() - borderWidth.toFloat()), borderWidth, height)
	renderRect(matrixStack, colorVertex.x(colorVertex.x.toDouble() + borderWidth.toDouble()), width.toDouble() - 2 * borderWidth.toDouble(), borderWidth)
	renderRect(
		matrixStack, colorVertex.xyz(
			colorVertex.x.toDouble() + borderWidth.toDouble(),
			colorVertex.y.toDouble() + height.toDouble() - borderWidth.toDouble(),
		),
		width.toDouble() - 2 * borderWidth.toDouble(), borderWidth
	)
}

/**
 * @see renderOutline
 * @param matrixStack MatrixStack
 * @param transform Transform
 * @param color Color
 * @param borderWidth Number
 */
fun renderOutline(matrixStack: MatrixStack, transform: Transform, color: Color, borderWidth: Number = 1) =
	renderOutline(matrixStack, ColorVertexImpl(transform.worldPosition, color), transform.width, transform.height, borderWidth)


/**
 * 渲染带边框线条的矩形
 * @param matrixStack MatrixStack
 * @param colorVertex Vertex
 * @param width Number
 * @param height Number
 * @param outlineColor Color
 * @param borderWidth Number
 * @param innerOutline Boolean
 */
fun renderOutlinedBox(
	matrixStack: MatrixStack,
	colorVertex: ColorVertex,
	width: Number,
	height: Number,
	outlineColor: Color,
	borderWidth: Number = 1,
	innerOutline: Boolean = true,
) {
	if (innerOutline) {
		renderRect(matrixStack, colorVertex, width, height)
		renderOutline(
			matrixStack,
			colorVertex.xyz(
				colorVertex.x.toDouble() - borderWidth.toDouble(),
				colorVertex.y.toDouble() - borderWidth.toDouble()
			).color(outlineColor),
			width.toDouble() + borderWidth.toDouble() * 2, height.toDouble() + borderWidth.toDouble() * 2,
			borderWidth
		)
	} else {
		renderRect(
			matrixStack,
			colorVertex.xyz(
				colorVertex.x.toDouble() - borderWidth.toDouble(),
				colorVertex.y.toDouble() - borderWidth.toDouble()
			),
			width.toDouble() - 2 * borderWidth.toDouble(), height.toDouble() - 2 * borderWidth.toDouble(),
		)
		renderOutline(matrixStack, colorVertex.color(outlineColor), width, height, borderWidth)
	}
}

/**
 * @see renderOutlinedBox
 * @param matrixStack MatrixStack
 * @param transform Transform
 * @param color Color
 * @param outlineColor Color
 * @param borderWidth Number
 * @param innerOutline Boolean
 */
fun renderOutlinedBox(
	matrixStack: MatrixStack,
	transform: Transform,
	color: Color,
	outlineColor: Color,
	borderWidth: Number = 1,
	innerOutline: Boolean = true
) {
	renderOutlinedBox(
		matrixStack, ColorVertexImpl(transform.worldPosition, color), transform.width, transform.height,
		outlineColor, borderWidth, innerOutline,
	)
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
 * @param uSize Int
 * @param vSize Int
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
	u: Int,
	v: Int,
	uSize: Int,
	vSize: Int,
	textureWidth: Int = 256,
	textureHeight: Int = 256,
	zOffset: Number = 0
) {
	val matrix4f = matrixStack.peek().positionMatrix
	setShader(GameRenderer::getPositionTexProgram)
	bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE)
	bufferBuilder.vertex(matrix4f, x.toFloat(), y.toFloat() + height.toFloat(), zOffset.toFloat())
		.texture(u.toFloat() / textureWidth, (v.toFloat() + vSize) / textureHeight).next()
	bufferBuilder.vertex(matrix4f, x.toFloat() + width.toFloat(), y.toFloat() + height.toFloat(), zOffset.toFloat())
		.texture((u.toFloat() + uSize) / textureWidth, (v.toFloat() + vSize) / textureHeight).next()
	bufferBuilder.vertex(matrix4f, x.toFloat() + width.toFloat(), y.toFloat(), zOffset.toFloat())
		.texture((u.toFloat() + uSize) / textureWidth, v.toFloat() / textureHeight).next()
	bufferBuilder.vertex(matrix4f, x.toFloat(), y.toFloat(), zOffset.toFloat())
		.texture(u.toFloat() / textureWidth, v.toFloat() / textureHeight).next()
	tessellator.draw()
}

/**
 * 绘制纹理
 * @param matrixStack MatrixStack
 * @param vertex1 UVVertex
 * @param vertex2 UVVertex
 * @param vertex3 UVVertex
 * @param vertex4 UVVertex
 */
fun drawTexture(matrixStack: MatrixStack, vertex1: UVVertex, vertex2: UVVertex, vertex3: UVVertex, vertex4: UVVertex) {
	val matrix4f = matrixStack.peek().positionMatrix
	setShader(GameRenderer::getPositionTexProgram)
	bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE)
	bufferBuilder.vertex(matrix4f, vertex1.vector3f()).texture(vertex1.u, vertex1.v).next()
	bufferBuilder.vertex(matrix4f, vertex2.vector3f()).texture(vertex2.u, vertex2.v).next()
	bufferBuilder.vertex(matrix4f, vertex3.vector3f()).texture(vertex3.u, vertex3.v).next()
	bufferBuilder.vertex(matrix4f, vertex4.vector3f()).texture(vertex4.u, vertex4.v).next()
	tessellator.draw()
}


/**
 * 绘制纹理
 * @param matrixStack MatrixStack
 * @param quads Quadrilateral
 * @param uvMapping UV
 * @param textureWidth Int
 * @param textureHeight Int
 */
fun drawTexture(matrixStack: MatrixStack, quads: Quadrilateral, uvMapping: UVMapping, textureWidth: Int = 256, textureHeight: Int = 256) {
	val matrix4f = matrixStack.peek().positionMatrix
	setShader(GameRenderer::getPositionTexProgram)
	bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE)
	bufferBuilder.vertex(matrix4f, quads.vertex1).texture(uvMapping.u1.toFloat() / textureWidth, uvMapping.v1.toFloat() / textureHeight).next()
	bufferBuilder.vertex(matrix4f, quads.vertex2).texture(uvMapping.u2.toFloat() / textureWidth, uvMapping.v1.toFloat() / textureHeight).next()
	bufferBuilder.vertex(matrix4f, quads.vertex3).texture(uvMapping.u1.toFloat() / textureHeight, uvMapping.v2.toFloat() / textureHeight).next()
	bufferBuilder.vertex(matrix4f, quads.vertex4).texture(uvMapping.u2.toFloat() / textureWidth, uvMapping.v2.toFloat() / textureHeight).next()
	tessellator.draw()
}

/**
 * @see drawTexture
 * @param matrixStack MatrixStack
 * @param transform Transform
 * @param uvMapping UV
 * @param textureWidth Int
 * @param textureHeight Int
 */
fun drawTexture(matrixStack: MatrixStack, transform: Transform, uvMapping: UVMapping, textureWidth: Int = 256, textureHeight: Int = 256) =
	drawTexture(matrixStack, transform.asWorldRect, uvMapping, textureWidth, textureHeight)

/**
 *  渲染.9 格式的材质
 * @param matrixStack MatrixStack
 * @param x Number
 * @param y Number
 * @param corner: Corner
 * @param width Number
 * @param height Number
 * @param u Number
 * @param v Number
 * @param uSize Int
 * @param vSize Int
 * @param textureWidth Int
 * @param textureHeight Int
 * @param zOffset Number
 */
fun draw9Texture(
	matrixStack: MatrixStack,
	x: Number,
	y: Number,
	width: Number,
	height: Number,
	corner: Corner,
	u: Int,
	v: Int,
	uSize: Int,
	vSize: Int,
	textureWidth: Int = 256,
	textureHeight: Int = 256,
	zOffset: Number = 0
) {
	if (corner.left == 0 && corner.right == 0 && corner.top == 0 && corner.bottom == 0)
		drawTexture(matrixStack, x, y, width, height, u, v, uSize, vSize, textureWidth, textureHeight, zOffset)

	/**
	 * centerWidth
	 */
	val cw = width.toDouble() - (corner.left + corner.right)

	/**
	 * centerHeight
	 */
	val ch = height.toDouble() - (corner.top + corner.bottom)

	/**
	 * centerRegionWidth
	 */
	val crw = uSize - (corner.left + corner.right)

	/**
	 *  centerRegionHeight
	 */
	val crh = vSize - (corner.top + corner.bottom)

	val centerU = u + corner.left
	val rightU = u + (uSize - corner.right)
	val centerV = v + corner.top
	val bottomV = v + (vSize - corner.bottom)
	val centerX = x.toDouble() + corner.left
	val rightX = x.toDouble() + (width.toDouble() - corner.right)
	val centerY = y.toDouble() + corner.top
	val bottomY = y.toDouble() + (height.toDouble() - corner.bottom)
	//top left
	drawTexture(matrixStack, x, y, corner.left, corner.top, u, v, corner.left, corner.top, textureWidth, textureHeight, zOffset)
	//top center
	drawTexture(matrixStack, centerX, y, cw, corner.top, centerU, v, crw, corner.top, textureWidth, textureHeight, zOffset)
	//top right
	drawTexture(matrixStack, rightX, y, corner.right, corner.top, rightU, v, corner.right, corner.top, textureWidth, textureHeight, zOffset)
	//center left
	drawTexture(matrixStack, x, centerY, corner.left, ch, u, centerV, corner.left, crh, textureWidth, textureHeight, zOffset)
	//center
	drawTexture(matrixStack, centerX, centerY, cw, ch, centerU, centerV, crw, crh, textureWidth, textureHeight, zOffset)
	//center right
	drawTexture(matrixStack, rightX, centerY, corner.right, ch, rightU, centerV, corner.right, crh, textureWidth, textureHeight, zOffset)
	//bottom left
	drawTexture(matrixStack, x, bottomY, corner.left, corner.bottom, u, bottomV, corner.left, corner.bottom, textureWidth, textureHeight, zOffset)
	//bottom center
	drawTexture(matrixStack, centerX, bottomY, cw, corner.bottom, centerU, bottomV, crw, corner.bottom, textureWidth, textureHeight, zOffset)
	//bottom right
	drawTexture(matrixStack, rightX, bottomY, corner.right, corner.bottom, rightU, bottomV, corner.right, corner.bottom, textureWidth, textureHeight, zOffset)
}

fun draw9Texture(matrixStack: MatrixStack, rect: Rectangle, textureUV: TextureUVMapping, textureWidth: Int = 256, textureHeight: Int = 256) {
	draw9Texture(
		matrixStack,
		rect.position.x,
		rect.position.y,
		rect.width,
		rect.height,
		textureUV.corner,
		textureUV.u1,
		textureUV.v1,
		textureUV.uSize,
		textureUV.vSize,
		textureWidth,
		textureHeight,
		rect.position.z
	)
}

/**
 * @see draw9Texture
 * @param matrixStack MatrixStack
 * @param transform Transform
 * @param textureUV TextureUV
 * @param textureWidth Int
 * @param textureHeight Int
 */
fun draw9Texture(matrixStack: MatrixStack, transform: Transform, textureUV: TextureUVMapping, textureWidth: Int = 256, textureHeight: Int = 256) =
	draw9Texture(matrixStack, transform.asWorldRect, textureUV, textureWidth, textureHeight)

/**
 * 渲染.9 格式的材质
 * 只适用于边角为相同大小的正方形的材质
 * @param matrixStack MatrixStack
 * @param rect Rectangle
 * @param cornerSize Int
 * @param uvMapping UV
 * @param textureWidth Int
 * @param textureHeight Int
 */
fun draw9Texture(matrixStack: MatrixStack, rect: Rectangle, cornerSize: Int, uvMapping: UVMapping, textureWidth: Int = 256, textureHeight: Int = 256) =
	draw9Texture(matrixStack, rect, TextureUVMapping(Corner(cornerSize), uvMapping), textureWidth, textureHeight)


/**
 * @see draw9Texture
 * @param matrixStack MatrixStack
 * @param transform Transform
 * @param cornerSize Int
 * @param uvMapping UV
 * @param textureWidth Int
 * @param textureHeight Int
 */
fun draw9Texture(matrixStack: MatrixStack, transform: Transform, cornerSize: Int, uvMapping: UVMapping, textureWidth: Int = 256, textureHeight: Int = 256) =
	draw9Texture(matrixStack, transform.asWorldRect, cornerSize, uvMapping, textureWidth, textureHeight)


/**
 * 渲染纹理
 * @param matrixStack MatrixStack
 * @param rect Rectangle
 * @param textureUV TextureUV
 * @param textureInfo TextureInfo
 * @param shaderColor Color
 */
fun renderTexture(matrixStack: MatrixStack, rect: Rectangle, textureUV: TextureUVMapping, textureInfo: TextureInfo, shaderColor: Color = Colors.WHITE) {
	setShaderTexture(textureInfo.texture)
	enableBlend()
	defaultBlendFunc()
	enableDepthTest()
	setShaderColor(shaderColor)
	draw9Texture(matrixStack, rect, textureUV)
	disableBlend()
	setShaderColor(Colors.WHITE)
}

/**
 * @see renderTexture
 * @param matrixStack MatrixStack
 * @param rect Rectangle
 * @param widgetTexture WidgetTexture
 * @param shaderColor Color
 */
fun renderTexture(matrixStack: MatrixStack, rect: Rectangle, widgetTexture: WidgetTexture, shaderColor: Color = Colors.WHITE) {
	renderTexture(matrixStack, rect, widgetTexture, widgetTexture.textureInfo, shaderColor)
}

/**
 * @see renderTexture
 * @param matrixStack MatrixStack
 * @param transform Transform
 * @param textureUV GuiTexture
 * @param shaderColor Color
 */
fun renderTexture(matrixStack: MatrixStack, transform: Transform, textureUV: TextureUVMapping, textureInfo: TextureInfo, shaderColor: Color = Colors.WHITE) =
	renderTexture(matrixStack, transform.asWorldRect, textureUV, textureInfo, shaderColor)

/**
 * 渲染材质
 * @param matrixStack MatrixStack
 * @param transform Transform
 * @param widgetTexture WidgetTexture
 * @param shaderColor Color
 */
fun renderTexture(matrixStack: MatrixStack, transform: Transform, widgetTexture: WidgetTexture, shaderColor: Color = Colors.WHITE) =
	renderTexture(matrixStack, transform.asWorldRect, widgetTexture, widgetTexture.textureInfo, shaderColor)

/**
 * 渲染文本
 * @receiver TextRenderer
 * @param matrixStack MatrixStack
 * @param text Text
 * @param x Number
 * @param y Number
 * @param shadow Boolean
 * @param layerType [TextLayerType]
 * @param rightToLeft Boolean
 * @param color Color
 * @param backgroundColor Color
 */
fun TextRenderer.renderText(
	matrixStack: MatrixStack,
	text: Text,
	x: Number,
	y: Number,
	shadow: Boolean = true,
	layerType: TextLayerType = TextLayerType.NORMAL,
	rightToLeft: Boolean = false,
	color: Color = Color(text.style.color?.rgb ?: 0xFFFFFF),
	backgroundColor: Color = Colors.WHITE.alpha(0),
) {
	val immediate = VertexConsumerProvider.immediate(bufferBuilder)
	draw(
		text.string, x.toFloat(), y.toFloat(), color.argb, shadow, matrixStack.peek().positionMatrix,
		immediate, layerType, backgroundColor.argb, MAX_LIGHT_COORDINATE, rightToLeft
	)
	immediate.draw()
}

/**
 * 渲染对齐文本
 * @param matrixStack MatrixStack
 * @param text Text
 * @param rect Rectangle
 * @param align Alignment
 * @param shadow Boolean
 * @param rightToLeft Boolean
 * @param color Color
 * @param backgroundColor Color
 */
fun TextRenderer.renderAlignmentText(
	matrixStack: MatrixStack,
	text: Text,
	rect: Rectangle,
	align: Alignment = PlanarAlignment.Center,
	shadow: Boolean = true,
	layerType: TextLayerType = TextLayerType.NORMAL,
	rightToLeft: Boolean = false,
	color: Color = Color(text.style.color?.rgb ?: 0xFFFFFF),
	backgroundColor: Color = Colors.WHITE.alpha(0),
) {
	val textWidth = getWidth(text)
	val position = align.align(rect, Rectangle(vertex(Vector3f()), textWidth, fontHeight))
	renderText(matrixStack, text, position.x, position.y, shadow, layerType, rightToLeft, color, backgroundColor)
}

/**
 * @see renderAlignmentText
 * @param matrixStack MatrixStack
 * @param text Text
 * @param transform Transform
 * @param align Alignment
 * @param shadow Boolean
 * @param layerType [TextLayerType]
 * @param rightToLeft Boolean
 * @param color Color
 * @param backgroundColor Color
 */
fun TextRenderer.renderAlignmentText(
	matrixStack: MatrixStack,
	text: Text,
	transform: Transform,
	align: Alignment = PlanarAlignment.Center,
	shadow: Boolean = true,
	layerType: TextLayerType = TextLayerType.NORMAL,
	rightToLeft: Boolean = false,
	color: Color = Color(text.style.color?.rgb ?: 0xFFFFFF),
	backgroundColor: Color = Colors.WHITE.alpha(0),
) = renderAlignmentText(matrixStack, text, transform.asWorldRect, align, shadow, layerType, rightToLeft, color, backgroundColor)


/**
 * 绘制多行字符串文本
 * @receiver TextRenderer
 * @param matrixStack MatrixStack
 * @param string String
 * @param rect Rectangle
 * @param lineSpacing Number
 * @param align HorizontalAlignment
 * @param shadow Boolean
 * @param layerType [TextLayerType]
 * @param rightToLeft Boolean
 * @param color Color
 * @param backgroundColor Color
 */
fun TextRenderer.renderStringLines(
	matrixStack: MatrixStack,
	string: String,
	rect: Rectangle,
	lineSpacing: Number = 1,
	align: HorizontalAlignment = Left,
	shadow: Boolean = true,
	layerType: TextLayerType = TextLayerType.NORMAL,
	rightToLeft: Boolean = false,
	color: Color = Colors.WHITE,
	backgroundColor: Color = Colors.WHITE.alpha(0),
) {
	var top: Float = rect.top
	for (text in string.wrapToLines(this, rect.width.toInt())) {
		renderAlignmentText(
			matrixStack, literal(text),
			Rectangle(vertex(Vector3f(0, top, 0)), rect.width, this.fontHeight), align,
			shadow, layerType, rightToLeft, color, backgroundColor
		)
		top += this.fontHeight + lineSpacing.toFloat()
	}
}

/**
 * 绘制多行字符串文本
 * @receiver TextRenderer
 * @param matrixStack MatrixStack
 * @param lines List<String>
 * @param rect Rectangle
 * @param lineSpacing Number
 * @param align HorizontalAlignment
 * @param shadow Boolean
 * @param layerType [TextLayerType]
 * @param rightToLeft Boolean
 * @param color Color
 * @param backgroundColor Color
 */
fun TextRenderer.renderStringLines(
	matrixStack: MatrixStack,
	lines: List<String>,
	rect: Rectangle,
	lineSpacing: Number = 1,
	align: HorizontalAlignment = Left,
	shadow: Boolean = true,
	layerType: TextLayerType = TextLayerType.NORMAL,
	rightToLeft: Boolean = false,
	color: Color = Colors.WHITE,
	backgroundColor: Color = Colors.WHITE.alpha(0),
) {
	var top: Float = rect.top
	for (text in lines.wrapToLines(this, rect.width.toInt())) {
		renderAlignmentText(
			matrixStack, literal(text),
			Rectangle(vertex(Vector3f(0, top, 0)), rect.width, this.fontHeight), align,
			shadow, layerType, rightToLeft, color, backgroundColor
		)
		top += this.fontHeight + lineSpacing.toFloat()
	}
}

/**
 * 绘制多行文本
 * @receiver TextRenderer
 * @param matrixStack MatrixStack
 * @param text [Text]
 * @param rect Rectangle
 * @param lineSpacing Number
 * @param align HorizontalAlignment
 * @param shadow Boolean
 * @param layerType [TextLayerType]
 * @param rightToLeft Boolean
 * @param color Color
 * @param backgroundColor Color
 */
fun TextRenderer.renderTextLines(
	matrixStack: MatrixStack,
	text: Text,
	rect: Rectangle,
	lineSpacing: Number = 1,
	align: HorizontalAlignment = Left,
	shadow: Boolean = true,
	layerType: TextLayerType = TextLayerType.NORMAL,
	rightToLeft: Boolean = false,
	color: Color = Color(text.style.color?.rgb ?: 0xFFFFFF),
	backgroundColor: Color = Colors.WHITE.alpha(0),
) = renderStringLines(matrixStack, text.string, rect, lineSpacing, align, shadow, layerType, rightToLeft, color, backgroundColor)


/**
 * 绘制多行文本
 * @receiver TextRenderer
 * @param matrixStack MatrixStack
 * @param lines List<[Text]>
 * @param rect Rectangle
 * @param lineSpacing Number
 * @param align HorizontalAlignment
 * @param shadow Boolean
 * @param layerType [TextLayerType]
 * @param rightToLeft Boolean
 * @param color Color
 * @param backgroundColor Color
 */
fun TextRenderer.renderTextLines(
	matrixStack: MatrixStack,
	lines: List<Text>,
	rect: Rectangle,
	lineSpacing: Number = 1,
	align: HorizontalAlignment = Left,
	shadow: Boolean = true,
	layerType: TextLayerType = TextLayerType.NORMAL,
	rightToLeft: Boolean = false,
	color: Color = Color(lines[0].style.color?.rgb ?: 0xFFFFFF),
	backgroundColor: Color = Colors.WHITE.alpha(0),
) {
	renderStringLines(
		matrixStack,
		ArrayList<String>().apply { lines.forEach { add(it.string) } },
		rect, lineSpacing,
		align, shadow, layerType, rightToLeft, color, backgroundColor,
	)
}

