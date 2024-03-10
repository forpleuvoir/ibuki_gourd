package moe.forpleuvoir.ibukigourd.gui.render.context.extension

import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.render.context.RenderContext
import moe.forpleuvoir.ibukigourd.gui.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.render.texture.Corner
import moe.forpleuvoir.ibukigourd.gui.render.texture.TextureInfo
import moe.forpleuvoir.ibukigourd.gui.render.texture.TextureUVMapping
import moe.forpleuvoir.ibukigourd.gui.render.texture.UVMapping
import moe.forpleuvoir.ibukigourd.gui.render.vertex.UVVertex
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTexture
import moe.forpleuvoir.ibukigourd.render.*
import moe.forpleuvoir.ibukigourd.render.helper.draw9Texture
import moe.forpleuvoir.ibukigourd.render.helper.drawTexture
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.gl.ShaderProgram
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats

fun RenderContext.batchRenderTexture(
    shaderSupplier: () -> ShaderProgram? = GameRenderer::getPositionTexColorProgram,
    block: TextureBatchRenderScope.() -> Unit
) {
    setShader(shaderSupplier)
    bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR)
    block(TextureBatchRenderScope)
    bufferBuilder.draw()
}

@Suppress("MemberVisibilityCanBePrivate", "DuplicatedCode")
open class TextureBatchRenderScope private constructor() {

    internal companion object : TextureBatchRenderScope()

    /**
     * 绘制纹理
     * @receiver RenderContext
     * @param x Float
     * @param y Float
     * @param width Float
     * @param height Float
     * @param u Int
     * @param v Int
     * @param uSize Int
     * @param vSize Int
     * @param color ARGBColor
     * @param textureWidth Int
     * @param textureHeight Int
     */
    fun RenderContext.drawTexture(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        u: Int,
        v: Int,
        uSize: Int,
        vSize: Int,
        color: ARGBColor = Colors.WHITE,
        textureWidth: Int = 256,
        textureHeight: Int = 256,
    ) {
        val matrix4f = positionMatrix
        bufferBuilder.apply {
            vertex(matrix4f, x, y + height, 0f)
                    .texture(u.toFloat() / textureWidth, (v.toFloat() + vSize) / textureHeight)
                    .color(color)
                    .next()
            vertex(matrix4f, x + width, y + height, 0f)
                    .texture((u.toFloat() + uSize) / textureWidth, (v.toFloat() + vSize) / textureHeight)
                    .color(color)
                    .next()
            vertex(matrix4f, x + width, y, 0f)
                    .texture((u.toFloat() + uSize) / textureWidth, v.toFloat() / textureHeight)
                    .color(color)
                    .next()
            vertex(matrix4f, x, y, 0f)
                    .texture(u.toFloat() / textureWidth, v.toFloat() / textureHeight)
                    .color(color)
                    .next()
        }
    }

    /**
     * 绘制纹理
     * @param vertex1 UVVertex
     * @param vertex2 UVVertex
     * @param vertex3 UVVertex
     * @param vertex4 UVVertex
     */
    fun RenderContext.drawTexture(vertex1: UVVertex, vertex2: UVVertex, vertex3: UVVertex, vertex4: UVVertex, color: ARGBColor = Colors.WHITE) {
        val matrix4f = positionMatrix
        bufferBuilder.vertex(matrix4f, vertex1).texture(vertex1).color(color).next()
        bufferBuilder.vertex(matrix4f, vertex2).texture(vertex2).color(color).next()
        bufferBuilder.vertex(matrix4f, vertex3).texture(vertex3).color(color).next()
        bufferBuilder.vertex(matrix4f, vertex4).texture(vertex4).color(color).next()
    }

    /**
     * 绘制纹理
     * @receiver RenderContext
     * @param rect Box
     * @param uvMapping UVMapping
     * @param color ARGBColor
     * @param textureWidth Int
     * @param textureHeight Int
     */
    fun RenderContext.drawTexture(rect: Box, uvMapping: UVMapping, color: ARGBColor = Colors.WHITE, textureWidth: Int = 256, textureHeight: Int = 256) {
        val matrix4f = positionMatrix
        bufferBuilder.vertex(matrix4f, rect.vertexes[0]).texture(uvMapping.uStart.toFloat() / textureWidth, uvMapping.vStart.toFloat() / textureHeight).color(color).next()
        bufferBuilder.vertex(matrix4f, rect.vertexes[2]).texture(uvMapping.uEnd.toFloat() / textureWidth, uvMapping.vStart.toFloat() / textureHeight).color(color).next()
        bufferBuilder.vertex(matrix4f, rect.vertexes[3]).texture(uvMapping.uStart.toFloat() / textureHeight, uvMapping.vEnd.toFloat() / textureHeight).color(color).next()
        bufferBuilder.vertex(matrix4f, rect.vertexes[4]).texture(uvMapping.uEnd.toFloat() / textureWidth, uvMapping.vEnd.toFloat() / textureHeight).color(color).next()
    }

    /**
     * @see drawTexture
     * @receiver RenderContext
     * @param transform Transform
     * @param uvMapping UVMapping
     * @param color ARGBColor
     * @param textureWidth Int
     * @param textureHeight Int
     */
    fun RenderContext.drawTexture(transform: Transform, uvMapping: UVMapping, color: ARGBColor = Colors.WHITE, textureWidth: Int = 256, textureHeight: Int = 256) =
        drawTexture(transform.asWorldBox, uvMapping, color, textureWidth, textureHeight)

    /**
     * 渲染.9 格式的纹理
     * @receiver RenderContext
     * @param x Float
     * @param y Float
     * @param width Float
     * @param height Float
     * @param corner Corner
     * @param u Int
     * @param v Int
     * @param uSize Int
     * @param vSize Int
     * @param color ARGBColor
     * @param textureWidth Int
     * @param textureHeight Int
     */
    fun RenderContext.draw9Texture(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        corner: Corner,
        u: Int,
        v: Int,
        uSize: Int,
        vSize: Int,
        color: ARGBColor = Colors.WHITE,
        textureWidth: Int = 256,
        textureHeight: Int = 256,
    ) {

        if (corner == Corner.EMPTY) {
            drawTexture(x, y, width, height, u, v, uSize, vSize, color, textureWidth, textureHeight)
            return
        }

        /**
         * centerWidth
         */
        val cw = width - (corner.left + corner.right)

        /**
         * centerHeight
         */
        val ch = height - (corner.top + corner.bottom)

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
        val centerX = x + corner.left
        val rightX = x + (width - corner.right)
        val centerY = y + corner.top
        val bottomY = y + (height - corner.bottom)

        //top left
        drawTexture(x, y, corner.left.toFloat(), corner.top.toFloat(), u, v, corner.left, corner.top, color, textureWidth, textureHeight)
        //top center
        drawTexture(centerX, y, cw, corner.top.toFloat(), centerU, v, crw, corner.top, color, textureWidth, textureHeight)
        //top right
        drawTexture(rightX, y, corner.right.toFloat(), corner.top.toFloat(), rightU, v, corner.right, corner.top, color, textureWidth, textureHeight)
        //center left
        drawTexture(x, centerY, corner.left.toFloat(), ch, u, centerV, corner.left, crh, color, textureWidth, textureHeight)
        //center
        drawTexture(centerX, centerY, cw, ch, centerU, centerV, crw, crh, color, textureWidth, textureHeight)
        //center right
        drawTexture(rightX, centerY, corner.right.toFloat(), ch, rightU, centerV, corner.right, crh, color, textureWidth, textureHeight)
        //bottom left
        drawTexture(x, bottomY, corner.left.toFloat(), corner.bottom.toFloat(), u, bottomV, corner.left, corner.bottom, color, textureWidth, textureHeight)
        //bottom center
        drawTexture(centerX, bottomY, cw, corner.bottom.toFloat(), centerU, bottomV, crw, corner.bottom, color, textureWidth, textureHeight)
        //bottom right
        drawTexture(rightX, bottomY, corner.right.toFloat(), corner.bottom.toFloat(), rightU, bottomV, corner.right, corner.bottom, color, textureWidth, textureHeight)
    }

    /**
     * @see [draw9Texture]
     * @receiver RenderContext
     * @param box Box
     * @param textureUV TextureUVMapping
     * @param color ARGBColor
     * @param textureWidth Int
     * @param textureHeight Int
     */
    fun RenderContext.draw9Texture(box: Box, textureUV: TextureUVMapping, color: ARGBColor = Colors.WHITE, textureWidth: Int = 256, textureHeight: Int = 256) {
        draw9Texture(
            box.position.x(),
            box.position.y(),
            box.width,
            box.height,
            textureUV.corner,
            textureUV.uStart,
            textureUV.vStart,
            textureUV.uSize,
            textureUV.vSize,
            color,
            textureWidth,
            textureHeight,
        )
    }

    /**
     * @see [draw9Texture]
     * @receiver RenderContext
     * @param transform Transform
     * @param textureUV TextureUVMapping
     * @param color ARGBColor
     * @param textureWidth Int
     * @param textureHeight Int
     */
    fun RenderContext.draw9Texture(
        transform: Transform,
        textureUV: TextureUVMapping,
        color: ARGBColor = Colors.WHITE,
        textureWidth: Int = 256,
        textureHeight: Int = 256
    ) = draw9Texture(transform.asWorldBox, textureUV, color, textureWidth, textureHeight)

    /**
     * 渲染.9 格式的纹理
     * 只适用于边角为相同大小的正方形的纹理
     * @param box Rectangle
     * @param cornerSize Int
     * @param uvMapping UV
     * @param color Color
     * @param textureWidth Int
     * @param textureHeight Int
     */
    fun RenderContext.draw9Texture(
        box: Box,
        cornerSize: Int,
        uvMapping: UVMapping,
        color: ARGBColor = Colors.WHITE,
        textureWidth: Int = 256,
        textureHeight: Int = 256
    ) = draw9Texture(box, TextureUVMapping(Corner(cornerSize), uvMapping), color, textureWidth, textureHeight)


    /**
     * 渲染.9 格式的纹理
     * 只适用于边角为相同大小的正方形的纹理
     * @param transform Transform
     * @param cornerSize Int
     * @param uvMapping UV
     * @param color Color
     * @param textureWidth Int
     * @param textureHeight Int
     */
    fun RenderContext.draw9Texture(
        transform: Transform,
        cornerSize: Int,
        uvMapping: UVMapping,
        color: ARGBColor = Colors.WHITE,
        textureWidth: Int = 256,
        textureHeight: Int = 256
    ) = draw9Texture(transform.asWorldBox, cornerSize, uvMapping, color, textureWidth, textureHeight)

    /**
     * 渲染.9格式纹理
     * @receiver RenderContext
     * @param rect Box
     * @param textureUV TextureUVMapping
     * @param textureInfo TextureInfo
     */
    fun RenderContext.ninePatchTexture(
        rect: Box,
        textureUV: TextureUVMapping,
        textureInfo: TextureInfo,
    ) {
        setShaderTexture(textureInfo.texture)
        draw9Texture(matrixStack, rect, textureUV)
    }

    /**
     * @see [ninePatchTexture]
     * @receiver RenderContext
     * @param transform Transform
     * @param textureUV TextureUVMapping
     * @param textureInfo TextureInfo
     */
    fun RenderContext.ninePatchTexture(
        transform: Transform,
        textureUV: TextureUVMapping,
        textureInfo: TextureInfo,
    ) = ninePatchTexture(transform.asWorldBox, textureUV, textureInfo)

    /**
     * @see [ninePatchTexture]
     * @receiver RenderContext
     * @param transform Transform
     * @param widgetTexture WidgetTexture
     */
    fun RenderContext.ninePatchTexture(transform: Transform, widgetTexture: WidgetTexture) =
        ninePatchTexture(transform.asWorldBox, widgetTexture, widgetTexture.textureInfo)

}


