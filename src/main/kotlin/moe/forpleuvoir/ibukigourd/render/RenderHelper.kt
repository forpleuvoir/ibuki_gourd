@file:Suppress("UNUSED", "DuplicatedCode")

package moe.forpleuvoir.ibukigourd.render

import com.mojang.blaze3d.systems.RenderSystem
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTexture
import moe.forpleuvoir.ibukigourd.render.base.Alignment
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.base.PlanarAlignment
import moe.forpleuvoir.ibukigourd.render.base.Size
import moe.forpleuvoir.ibukigourd.render.base.math.ImmutableVector3f
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3f
import moe.forpleuvoir.ibukigourd.render.base.rectangle.Rectangle
import moe.forpleuvoir.ibukigourd.render.base.rectangle.colorRect
import moe.forpleuvoir.ibukigourd.render.base.rectangle.rect
import moe.forpleuvoir.ibukigourd.render.base.texture.Corner
import moe.forpleuvoir.ibukigourd.render.base.texture.TextureInfo
import moe.forpleuvoir.ibukigourd.render.base.texture.TextureUVMapping
import moe.forpleuvoir.ibukigourd.render.base.texture.UVMapping
import moe.forpleuvoir.ibukigourd.render.base.vertex.ColorVertex
import moe.forpleuvoir.ibukigourd.render.base.vertex.ColorVertexImpl
import moe.forpleuvoir.ibukigourd.render.base.vertex.UVVertex
import moe.forpleuvoir.ibukigourd.render.base.vertex.colorVertex
import moe.forpleuvoir.ibukigourd.util.text.Text
import moe.forpleuvoir.ibukigourd.util.text.wrapToLines
import moe.forpleuvoir.ibukigourd.util.text.wrapToTextLines
import moe.forpleuvoir.nebula.common.color.*
import moe.forpleuvoir.nebula.common.util.clamp
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.font.TextRenderer.TextLayerType
import net.minecraft.client.gl.ShaderProgram
import net.minecraft.client.render.*
import net.minecraft.client.render.LightmapTextureManager.MAX_LIGHT_COORDINATE
import net.minecraft.client.resource.language.ReorderingUtil
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.OrderedText
import net.minecraft.util.Identifier
import org.joml.Matrix4f
import java.util.function.Supplier

val tessellator: Tessellator get() = Tessellator.getInstance()

val bufferBuilder: BufferBuilder get() = tessellator.buffer

fun MatrixStack.translate(vector3: Vector3<out Number>) {
	this.translate(vector3.x.toFloat(), vector3.y.toFloat(), vector3.z.toFloat())
}

fun Matrix4f.translate(vector3: Vector3<out Number>) {
	this.translate(vector3.x.toFloat(), vector3.y.toFloat(), vector3.z.toFloat())
}

fun Matrix4f.getPosition(): Vector3<Float> {
	return ImmutableVector3f(this.get(3, 0), this.get(3, 1), this.get(3, 2))
}

fun BufferBuilder.draw() {
	BufferRenderer.drawWithGlobalProgram(this.end())
}

fun setShader(shaderSupplier: Supplier<ShaderProgram?>) = RenderSystem.setShader(shaderSupplier)

fun setShader(shaderSupplier: (() -> ShaderProgram?)) = RenderSystem.setShader(shaderSupplier)

fun setShaderTexture(texture: Identifier) = RenderSystem.setShaderTexture(0, texture)

fun lineWidth(width: Number) = RenderSystem.lineWidth(width.toFloat())

fun setShaderColor(color: ARGBColor) = RenderSystem.setShaderColor(color.redF, color.greenF, color.blueF, color.alphaF)

fun enablePolygonOffset() = RenderSystem.enablePolygonOffset()

fun polygonOffset(factor: Number, units: Number) = RenderSystem.polygonOffset(factor.toFloat(), units.toFloat())

fun disablePolygonOffset() = RenderSystem.disablePolygonOffset()

fun enableBlend() = RenderSystem.enableBlend()

fun defaultBlendFunc() = RenderSystem.defaultBlendFunc()

fun disableBlend() = RenderSystem.disableBlend()

fun enableDepthTest() = RenderSystem.enableDepthTest()

fun disableDepthTest() = RenderSystem.disableDepthTest()

fun setScissor(x: Number, y: Number, width: Number, height: Number) {
	val window = MinecraftClient.getInstance().window
	val framebufferHeight = window.framebufferHeight
	val scale = window.scaleFactor
	val x1 = x.toDouble() * scale
	val y1 = framebufferHeight.toDouble() - (y.toDouble() + height.toDouble()) * scale
	val width1 = width.toDouble() * scale
	val height1 = height.toDouble() * scale
	RenderSystem.enableScissor(x1.toInt(), y1.toInt(), 0.coerceAtLeast(width1.toInt()), 0.coerceAtLeast(height1.toInt()))
}

fun setScissor(rect: Rectangle<Vector3<Float>>?) {
	if (rect == null) return disableScissor()
	if (!rect.exist) return setScissor(0, 0, 0, 0)
	setScissor(rect.x, rect.y, rect.width, rect.height)
}

fun disableScissor() = RenderSystem.disableScissor()

fun VertexConsumer.vertex(matrix4f: Matrix4f, vertex: Vector3<Float>): VertexConsumer =
	vertex(matrix4f, vertex.x, vertex.y, vertex.z)

fun VertexConsumer.vertex(matrix4f: Matrix4f, x: Number, y: Number, z: Number): VertexConsumer =
	this.vertex(matrix4f, x.toFloat(), y.toFloat(), z.toFloat())

fun VertexConsumer.vertex(matrixStack: MatrixStack, x: Number, y: Number, z: Number): VertexConsumer =
	this.vertex(matrixStack.peek().positionMatrix, x.toFloat(), y.toFloat(), z.toFloat())

fun VertexConsumer.vertex(matrixStack: MatrixStack, vector3: Vector3<Float>): VertexConsumer =
	this.vertex(matrixStack.peek().positionMatrix, vector3)

fun VertexConsumer.color(color: ARGBColor): VertexConsumer = this.color(color.argb)

fun VertexConsumer.normal(normal: Vector3<Float>): VertexConsumer = this.normal(normal.x, normal.y, normal.z)

/**
 * 渲染线段
 * @param matrixStack MatrixStack
 * @param lineWidth Number
 * @param vertex1 ColorVertex
 * @param vertex2 ColorVertex
 */
fun renderLine(matrixStack: MatrixStack, lineWidth: Number, vertex1: ColorVertex, vertex2: ColorVertex, normal: Vector3<Float>) {
	setShader(GameRenderer::getRenderTypeLinesProgram)
	enableBlend()
	defaultBlendFunc()
	lineWidth(lineWidth)
	bufferBuilder.begin(VertexFormat.DrawMode.LINE_STRIP, VertexFormats.LINES)
	bufferBuilder.vertex(matrixStack, vertex1).color(vertex1.color).normal(normal).next()
	bufferBuilder.vertex(matrixStack, vertex2).color(vertex2.color).normal(normal).next()
	bufferBuilder.draw()
	lineWidth(1f)
	disableBlend()
}

/**
 * 渲染线段
 * @param matrixStack MatrixStack
 * @param lineWidth Number
 * @param vertexes [List]<[ColorVertex]>
 */
fun renderLine(matrixStack: MatrixStack, lineWidth: Number, vararg vertexes: ColorVertex) {
	setShader(GameRenderer::getPositionColorProgram)
	enableBlend()
	defaultBlendFunc()
	lineWidth(lineWidth)
	bufferBuilder.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR)
	for (vertex in vertexes) {
		bufferBuilder.vertex(matrixStack, vertex).color(vertex.color).next()
	}
	bufferBuilder.draw()
	lineWidth(1f)
	disableBlend()
}

fun renderGradientRect(
	matrixStack: MatrixStack,
	rect: Rectangle<Vector3<Float>>,
	arrangement: Arrangement = Arrangement.Horizontal,
	startColor: ARGBColor,
	endColor: ARGBColor
) {
	arrangement.switch({
		renderRect(matrixStack, colorRect(rect, startColor, endColor, endColor, startColor))
	}, {
		renderRect(matrixStack, colorRect(rect, startColor, startColor, endColor, endColor))
	})
}

fun renderHueGradientRect(
	matrixStack: MatrixStack,
	rect: Rectangle<Vector3<Float>>,
	precision: Int,
	arrangement: Arrangement = Arrangement.Horizontal,
	reverse: Boolean = false,
	hueRange: ClosedFloatingPointRange<Float> = 0f..360f,
	saturation: Float = 1f,
	value: Float = 1f,
	alpha: Float = 1f
) {
	val hueSlice = hueRange.endInclusive / precision
	var hue = if (reverse) hueRange.endInclusive else hueRange.start
	arrangement.switch({
		val lengthSlice = rect.height / precision
		var y = rect.y
		for (i in 0 until precision) {
			val colorStart = HSVColor(hue, saturation, value, alpha, false)
			hue = if (reverse) (hue - hueSlice).clamp(hueRange) else (hue + hueSlice).clamp(hueRange)
			val colorEnd = HSVColor(hue, saturation, value, alpha, false)
			renderRect(matrixStack, colorRect(rect.x, y, rect.z, Size.create(rect.width, lengthSlice), colorStart, colorEnd, colorEnd, colorStart))
			y += lengthSlice
		}
	}, {
		val lengthSlice = rect.width / precision
		var x = rect.x
		for (i in 0 until precision) {
			val colorStart = HSVColor(hue, saturation, value, alpha, false)
			hue = if (reverse) (hue - hueSlice).clamp(hueRange) else (hue + hueSlice).clamp(hueRange)
			val colorEnd = HSVColor(hue, saturation, value, alpha, false)
			renderRect(matrixStack, colorRect(x, rect.y, rect.z, Size.create(lengthSlice, rect.height), colorStart, colorStart, colorEnd, colorEnd))
			x += lengthSlice
		}
	})
}

fun renderSaturationGradientRect(
	matrixStack: MatrixStack,
	rect: Rectangle<Vector3<Float>>,
	arrangement: Arrangement = Arrangement.Horizontal,
	reverse: Boolean = false,
	saturationRange: ClosedFloatingPointRange<Float> = 0f..1f,
	hue: Float = 360f,
	value: Float = 1f,
	alpha: Float = 1f
) {
	val colorStart = HSVColor(hue, (if (reverse) saturationRange.endInclusive else saturationRange.start).clamp(alphaFRange), value, alpha)
	val colorEnd = HSVColor(hue, (if (!reverse) saturationRange.endInclusive else saturationRange.start).clamp(alphaFRange), value, alpha)
	renderGradientRect(matrixStack, rect, arrangement, colorStart, colorEnd)
}

fun renderValueGradientRect(
	matrixStack: MatrixStack,
	rect: Rectangle<Vector3<Float>>,
	arrangement: Arrangement = Arrangement.Horizontal,
	reverse: Boolean = false,
	valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
	hue: Float = 360f,
	saturation: Float = 1f,
	alpha: Float = 1f
) {
	val colorStart = HSVColor(hue, saturation, (if (reverse) valueRange.endInclusive else valueRange.start).clamp(alphaFRange), alpha)
	val colorEnd = HSVColor(hue, saturation, (if (!reverse) valueRange.endInclusive else valueRange.start).clamp(alphaFRange), alpha)
	renderGradientRect(matrixStack, rect, arrangement, colorStart, colorEnd)
}

fun renderAlphaGradientRect(
	matrixStack: MatrixStack,
	rect: Rectangle<Vector3<Float>>,
	arrangement: Arrangement = Arrangement.Horizontal,
	reverse: Boolean = false,
	alphaRange: ClosedFloatingPointRange<Float> = 0f..1f,
	color: ARGBColor
) {
	val colorStart = Color(color.argb).alpha((if (reverse) alphaRange.endInclusive else alphaRange.start).clamp(alphaFRange))
	val colorEnd = Color(color.argb).alpha((if (!reverse) alphaRange.endInclusive else alphaRange.start).clamp(alphaFRange))
	renderGradientRect(matrixStack, rect, arrangement, colorStart, colorEnd)
}

fun renderSVGradientRect(
	matrixStack: MatrixStack,
	rect: Rectangle<Vector3<Float>>,
	arrangement: Arrangement = Arrangement.Horizontal,
	reverse: Boolean = false,
	saturationRange: ClosedFloatingPointRange<Float> = 0f..1f,
	valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
	hue: Float = 360f
) {
	val saturationStart = saturationRange.start
	val saturationEnd = saturationRange.endInclusive
	val valueStart = valueRange.start
	val valueEnd = valueRange.endInclusive
	val saturationStartColor = HSVColor(hue, if (reverse) saturationEnd else saturationStart, valueEnd)
	val saturationEndColor = HSVColor(hue, if (!reverse) saturationEnd else saturationStart, valueEnd)
	val alphaStartColor = Colors.BLACK.alpha((if (reverse) valueEnd else valueStart).clamp(valueRange))
	val alphaEndColor = Colors.BLACK.alpha((if (!reverse) valueEnd else valueStart).clamp(valueRange))
	arrangement.switch({
		renderGradientRect(matrixStack, rect, arrangement, saturationStartColor, saturationEndColor)
		renderGradientRect(matrixStack, rect, Arrangement.Horizontal, alphaStartColor, alphaEndColor)
	}, {
		renderGradientRect(matrixStack, rect, arrangement, saturationStartColor, saturationEndColor)
		renderGradientRect(matrixStack, rect, Arrangement.Vertical, alphaStartColor, alphaEndColor)
	})
}

/**
 * 渲染矩形
 * @param matrixStack MatrixStack
 * @param rect Rectangle
 */
fun renderRect(matrixStack: MatrixStack, rect: Rectangle<ColorVertex>) {
	enableBlend()
	setShader(GameRenderer::getPositionColorProgram)
	bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
	for (vertex in rect.vertexes) {
		bufferBuilder.vertex(matrixStack, vertex).color(vertex.color).next()
	}
	bufferBuilder.draw()
	disableBlend()
}

/**
 * 渲染矩形
 * @param matrixStack MatrixStack
 * @param rect Rectangle<Vector3<Float>>
 * @param color ARGBColor
 */
fun renderRect(matrixStack: MatrixStack, rect: Rectangle<Vector3<Float>>, color: ARGBColor) {
	enableBlend()
	setShader(GameRenderer::getPositionColorProgram)
	bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
	for (vertex in rect.vertexes) {
		bufferBuilder.vertex(matrixStack, vertex).color(color).next()
	}
	bufferBuilder.draw()
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
	renderRect(matrixStack, colorRect(colorVertex, width.toFloat(), height.toFloat()))
}

/**
 * @see renderRect
 * @param matrixStack MatrixStack
 * @param transform Transform
 * @param color Color
 */
fun renderRect(matrixStack: MatrixStack, transform: Transform, color: ARGBColor) =
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
	renderRect(matrixStack, colorVertex.x(colorVertex.x + width.toFloat() - borderWidth.toFloat()), borderWidth, height)
	renderRect(matrixStack, colorVertex.x(colorVertex.x + borderWidth.toFloat()), width.toFloat() - 2 * borderWidth.toFloat(), borderWidth)
	renderRect(
		matrixStack, colorVertex.xyz(
			colorVertex.x + borderWidth.toFloat(),
			colorVertex.y + height.toFloat() - borderWidth.toFloat(),
		),
		width.toFloat() - 2 * borderWidth.toFloat(), borderWidth
	)
}

/**
 * @see renderOutline
 * @param matrixStack MatrixStack
 * @param transform Transform
 * @param color Color
 * @param borderWidth Number
 */
fun renderOutline(matrixStack: MatrixStack, transform: Transform, color: ARGBColor, borderWidth: Number = 1) =
	renderOutline(matrixStack, ColorVertexImpl(transform.worldPosition, color), transform.width, transform.height, borderWidth)

/**
 *  @see renderOutline
 * @param matrixStack MatrixStack
 * @param rect Rectangle<Vector3<Float>>
 * @param color Color
 * @param borderWidth Number
 */
fun renderOutline(matrixStack: MatrixStack, rect: Rectangle<Vector3<Float>>, color: ARGBColor, borderWidth: Number = 1) =
	renderOutline(matrixStack, ColorVertexImpl(rect.position, color), rect.width, rect.height, borderWidth)


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
	outlineColor: ARGBColor,
	borderWidth: Number = 1,
	innerOutline: Boolean = true,
) {
	if (innerOutline) {
		renderRect(matrixStack, colorVertex, width, height)
		renderOutline(
			matrixStack,
			colorVertex.xyz(
				colorVertex.x - borderWidth.toFloat(),
				colorVertex.y - borderWidth.toFloat()
			).color(outlineColor),
			width.toFloat() + borderWidth.toFloat() * 2, height.toFloat() + borderWidth.toFloat() * 2,
			borderWidth
		)
	} else {
		renderRect(
			matrixStack,
			colorVertex.xyz(
				colorVertex.x - borderWidth.toFloat(),
				colorVertex.y - borderWidth.toFloat()
			),
			width.toFloat() - 2 * borderWidth.toFloat(), height.toFloat() - 2 * borderWidth.toFloat(),
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
	color: ARGBColor,
	outlineColor: ARGBColor,
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
	bufferBuilder.draw()
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
	bufferBuilder.vertex(matrix4f, vertex1).texture(vertex1.u, vertex1.v).next()
	bufferBuilder.vertex(matrix4f, vertex2).texture(vertex2.u, vertex2.v).next()
	bufferBuilder.vertex(matrix4f, vertex3).texture(vertex3.u, vertex3.v).next()
	bufferBuilder.vertex(matrix4f, vertex4).texture(vertex4.u, vertex4.v).next()
	bufferBuilder.draw()
}


/**
 * 绘制纹理
 * @param matrixStack MatrixStack
 * @param rect Rectangle<Vector3<Float>>
 * @param uvMapping UVMapping
 * @param textureWidth Int
 * @param textureHeight Int
 */
fun drawTexture(matrixStack: MatrixStack, rect: Rectangle<Vector3<Float>>, uvMapping: UVMapping, textureWidth: Int = 256, textureHeight: Int = 256) {
	val matrix4f = matrixStack.peek().positionMatrix
	setShader(GameRenderer::getPositionTexProgram)
	bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE)
	bufferBuilder.vertex(matrix4f, rect.vertexes[0]).texture(uvMapping.u1.toFloat() / textureWidth, uvMapping.v1.toFloat() / textureHeight).next()
	bufferBuilder.vertex(matrix4f, rect.vertexes[2]).texture(uvMapping.u2.toFloat() / textureWidth, uvMapping.v1.toFloat() / textureHeight).next()
	bufferBuilder.vertex(matrix4f, rect.vertexes[3]).texture(uvMapping.u1.toFloat() / textureHeight, uvMapping.v2.toFloat() / textureHeight).next()
	bufferBuilder.vertex(matrix4f, rect.vertexes[4]).texture(uvMapping.u2.toFloat() / textureWidth, uvMapping.v2.toFloat() / textureHeight).next()
	bufferBuilder.draw()
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

fun draw9Texture(matrixStack: MatrixStack, rect: Rectangle<Vector3<Float>>, textureUV: TextureUVMapping, textureWidth: Int = 256, textureHeight: Int = 256) {
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
fun draw9Texture(
	matrixStack: MatrixStack,
	rect: Rectangle<Vector3<Float>>,
	cornerSize: Int,
	uvMapping: UVMapping,
	textureWidth: Int = 256,
	textureHeight: Int = 256
) =
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
fun renderTexture(
	matrixStack: MatrixStack,
	rect: Rectangle<Vector3<Float>>,
	textureUV: TextureUVMapping,
	textureInfo: TextureInfo,
	shaderColor: ARGBColor = Colors.WHITE
) {
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
fun renderTexture(matrixStack: MatrixStack, rect: Rectangle<Vector3<Float>>, widgetTexture: WidgetTexture, shaderColor: ARGBColor = Colors.WHITE) {
	renderTexture(matrixStack, rect, widgetTexture, widgetTexture.textureInfo, shaderColor)
}

/**
 * @see renderTexture
 * @param matrixStack MatrixStack
 * @param transform Transform
 * @param textureUV GuiTexture
 * @param shaderColor Color
 */
fun renderTexture(
	matrixStack: MatrixStack,
	transform: Transform,
	textureUV: TextureUVMapping,
	textureInfo: TextureInfo,
	shaderColor: ARGBColor = Colors.WHITE
) =
	renderTexture(matrixStack, transform.asWorldRect, textureUV, textureInfo, shaderColor)

/**
 * 渲染材质
 * @param matrixStack MatrixStack
 * @param transform Transform
 * @param widgetTexture WidgetTexture
 * @param shaderColor Color
 */
fun renderTexture(matrixStack: MatrixStack, transform: Transform, widgetTexture: WidgetTexture, shaderColor: ARGBColor = Colors.WHITE) =
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
 * @param color ARGBColor
 * @param backgroundColor Color
 */
fun TextRenderer.renderText(
	matrixStack: MatrixStack,
	text: Text,
	x: Number,
	y: Number,
	shadow: Boolean = false,
	layerType: TextLayerType = TextLayerType.NORMAL,
	rightToLeft: Boolean = this.isRightToLeft,
	color: ARGBColor = Color(text.style.color?.rgb ?: 0x000000),
	backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) {
	this.draw(
		ReorderingUtil.reorder(text, rightToLeft),
		x.toFloat(),
		y.toFloat(),
		color.argb,
		shadow,
		matrixStack.peek().positionMatrix,
		VertexConsumerProvider.immediate(bufferBuilder),
		layerType,
		backgroundColor.argb,
		MAX_LIGHT_COORDINATE
	)
	bufferBuilder.draw()
}

fun TextRenderer.renderText(
	matrixStack: MatrixStack,
	text: OrderedText,
	x: Number,
	y: Number,
	shadow: Boolean = false,
	layerType: TextLayerType = TextLayerType.NORMAL,
	color: ARGBColor = Color(0x000000),
	backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) {
	this.draw(
		text,
		x.toFloat(),
		y.toFloat(),
		color.argb,
		shadow,
		matrixStack.peek().positionMatrix,
		VertexConsumerProvider.immediate(bufferBuilder),
		layerType,
		backgroundColor.argb,
		MAX_LIGHT_COORDINATE,
	)
	bufferBuilder.draw()
}

fun TextRenderer.renderText(
	matrixStack: MatrixStack,
	text: String,
	x: Number,
	y: Number,
	shadow: Boolean = false,
	layerType: TextLayerType = TextLayerType.NORMAL,
	rightToLeft: Boolean = this.isRightToLeft,
	color: ARGBColor = Color(0x000000),
	backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) {
	this.draw(
		text,
		x.toFloat(),
		y.toFloat(),
		color.argb,
		shadow,
		matrixStack.peek().positionMatrix,
		VertexConsumerProvider.immediate(bufferBuilder),
		layerType,
		backgroundColor.argb,
		MAX_LIGHT_COORDINATE,
		rightToLeft
	)
	bufferBuilder.draw()
}

/**
 * 渲染对齐文本
 * @receiver TextRenderer
 * @param matrixStack MatrixStack
 * @param text String
 * @param rect Rectangle<Vector3<Float>>
 * @param align (Arrangement) -> Alignment
 * @param shadow Boolean
 * @param layerType TextRenderer.TextLayerType
 * @param rightToLeft Boolean
 * @param color ARGBColor
 * @param backgroundColor ARGBColor
 */
inline fun TextRenderer.renderAlignmentText(
	matrixStack: MatrixStack,
	text: String,
	rect: Rectangle<Vector3<Float>>,
	align: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
	shadow: Boolean = false,
	layerType: TextLayerType = TextLayerType.NORMAL,
	rightToLeft: Boolean = this.isRightToLeft,
	color: ARGBColor = Color(0x000000),
	backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) {
	val textWidth = getWidth(text)
	val position = align(Arrangement.Vertical).align(rect, rect(Vector3f(), textWidth, fontHeight))
	renderText(matrixStack, text, position.x, position.y, shadow, layerType, rightToLeft, color, backgroundColor)
}

/**
 * 渲染对齐文本
 * @receiver TextRenderer
 * @param matrixStack MatrixStack
 * @param text Text
 * @param rect Rectangle<Vector3<Float>>
 * @param align (Arrangement) -> Alignment
 * @param shadow Boolean
 * @param layerType TextRenderer.TextLayerType
 * @param color ARGBColor
 * @param backgroundColor ARGBColor
 */
inline fun TextRenderer.renderAlignmentText(
	matrixStack: MatrixStack,
	text: Text,
	rect: Rectangle<Vector3<Float>>,
	align: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
	shadow: Boolean = false,
	layerType: TextLayerType = TextLayerType.NORMAL,
	rightToLeft: Boolean = this.isRightToLeft,
	color: ARGBColor = Color(text.style.color?.rgb ?: 0x000000),
	backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) {
	val textWidth = getWidth(text)
	val position = align(Arrangement.Vertical).align(rect, rect(Vector3f(), textWidth, fontHeight))
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
 * @param color ARGBColor
 * @param backgroundColor Color
 */
inline fun TextRenderer.renderAlignmentText(
	matrixStack: MatrixStack,
	text: Text,
	transform: Transform,
	align: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
	shadow: Boolean = false,
	layerType: TextLayerType = TextLayerType.NORMAL,
	rightToLeft: Boolean = this.isRightToLeft,
	color: ARGBColor = Color(text.style.color?.rgb ?: 0x000000),
	backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) = renderAlignmentText(matrixStack, text, transform.asWorldRect, align, shadow, layerType, rightToLeft, color, backgroundColor)


/**
 * 绘制多行字符串文本
 * @receiver TextRenderer
 * @param matrixStack MatrixStack
 * @param string String
 * @param rect Rectangle<Vector3<Float>>
 * @param lineSpacing Number
 * @param align (Arrangement) -> Alignment
 * @param shadow Boolean
 * @param layerType TextRenderer.TextLayerType
 * @param rightToLeft Boolean
 * @param color ARGBColor
 * @param backgroundColor ARGBColor
 */
inline fun TextRenderer.renderStringLines(
	matrixStack: MatrixStack,
	string: String,
	rect: Rectangle<Vector3<Float>>,
	lineSpacing: Number = 1,
	align: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
	shadow: Boolean = false,
	layerType: TextLayerType = TextLayerType.NORMAL,
	rightToLeft: Boolean = this.isRightToLeft,
	color: ARGBColor = Colors.BLACK,
	backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) {
	var top: Float = rect.top
	for (text in string.wrapToLines(this, rect.width.toInt())) {
		renderAlignmentText(
			matrixStack, text,
			rect(Vector3f(0f, top, 0f), rect.width, this.fontHeight), align,
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
 * @param color ARGBColor
 * @param backgroundColor Color
 */
inline fun TextRenderer.renderStringLines(
	matrixStack: MatrixStack,
	lines: List<String>,
	rect: Rectangle<Vector3<Float>>,
	lineSpacing: Number = 1,
	align: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
	shadow: Boolean = false,
	layerType: TextLayerType = TextLayerType.NORMAL,
	rightToLeft: Boolean = this.isRightToLeft,
	color: ARGBColor = Colors.BLACK,
	backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) {
	var top: Float = rect.top
	for (text in lines.wrapToLines(this, rect.width.toInt())) {
		renderAlignmentText(
			matrixStack, text,
			rect(Vector3f(0f, top, 0f), rect.width, this.fontHeight), align,
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
 * @param color ARGBColor
 * @param backgroundColor Color
 */
inline fun TextRenderer.renderTextLines(
	matrixStack: MatrixStack,
	text: Text,
	rect: Rectangle<Vector3<Float>>,
	lineSpacing: Number = 1,
	align: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
	shadow: Boolean = false,
	layerType: TextLayerType = TextLayerType.NORMAL,
	rightToLeft: Boolean = this.isRightToLeft,
	color: ARGBColor = Color(text.style.color?.rgb ?: 0x000000),
	backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) {
	var top: Float = rect.top
	for (t in text.wrapToTextLines(this, rect.width.toInt())) {
		renderAlignmentText(
			matrixStack, text,
			rect(Vector3f(0f, top, 0f), rect.width, this.fontHeight), align,
			shadow, layerType, rightToLeft, color, backgroundColor
		)
		top += this.fontHeight + lineSpacing.toFloat()
	}
}


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
 * @param color ARGBColor
 * @param backgroundColor Color
 */
inline fun TextRenderer.renderTextLines(
	matrixStack: MatrixStack,
	lines: List<Text>,
	rect: Rectangle<Vector3<Float>>,
	lineSpacing: Number = 1,
	align: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
	shadow: Boolean = false,
	layerType: TextLayerType = TextLayerType.NORMAL,
	rightToLeft: Boolean = this.isRightToLeft,
	color: ARGBColor = Color(lines[0].style.color?.rgb ?: 0x000000),
	backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) {
	renderStringLines(
		matrixStack,
		ArrayList<String>().apply { lines.forEach { add(it.string) } },
		rect, lineSpacing,
		align, shadow, layerType, rightToLeft, color, backgroundColor,
	)
}

