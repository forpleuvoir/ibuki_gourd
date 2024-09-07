package moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext

import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.*
import moe.forpleuvoir.ibukigourd.gui.base.render.vertex.UVVertex
import moe.forpleuvoir.ibukigourd.render.*
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.gl.ShaderProgram
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.*

fun DrawContext.batchRenderTextureColored(
    beforeAction: () -> Unit = { enableBlend() },
    shaderSupplier: (() -> ShaderProgram?)? = GameRenderer::getPositionTexColorProgram,
    block: TextureBatchRenderScope.(DrawContext) -> Unit
) {
    setShader(shaderSupplier)
    beforeAction()
    val bufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR)
    block.invoke(TextureBatchRenderScope(bufferBuilder, this), this)
    bufferBuilder.endNullable()?.let {
        BufferRenderer.drawWithGlobalProgram(it)
    }
}

@Suppress("MemberVisibilityCanBePrivate", "DuplicatedCode")
open class TextureBatchRenderScope internal constructor(private val bufferBuilder: BufferBuilder, private val context: DrawContext) {

    /**
     * 绘制纹理
     * @receiver DrawContext
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
    fun pushTexture(
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
        val matrix4f = context.positionMatrix
        bufferBuilder.apply {
            vertex(matrix4f, x, y + height, 0f)
                .texture(u.toFloat() / textureWidth, (v.toFloat() + vSize) / textureHeight)
                .color(color)
            vertex(matrix4f, x + width, y + height, 0f)
                .texture((u.toFloat() + uSize) / textureWidth, (v.toFloat() + vSize) / textureHeight)
                .color(color)
            vertex(matrix4f, x + width, y, 0f)
                .texture((u.toFloat() + uSize) / textureWidth, v.toFloat() / textureHeight)
                .color(color)
            vertex(matrix4f, x, y, 0f)
                .texture(u.toFloat() / textureWidth, v.toFloat() / textureHeight)
                .color(color)
        }
    }

    /**
     * 绘制纹理
     * @param vertex1 UVVertex
     * @param vertex2 UVVertex
     * @param vertex3 UVVertex
     * @param vertex4 UVVertex
     */
    fun pushTexture(vertex1: UVVertex, vertex2: UVVertex, vertex3: UVVertex, vertex4: UVVertex, color: ARGBColor = Colors.WHITE) {
        val matrix4f = context.positionMatrix
        bufferBuilder.vertex(matrix4f, vertex1).texture(vertex1).color(color)
        bufferBuilder.vertex(matrix4f, vertex2).texture(vertex2).color(color)
        bufferBuilder.vertex(matrix4f, vertex3).texture(vertex3).color(color)
        bufferBuilder.vertex(matrix4f, vertex4).texture(vertex4).color(color)
    }

    /**
     * 绘制纹理
     * @receiver DrawContext
     * @param box Box
     * @param uvMapping UVMapping
     * @param color ARGBColor
     * @param textureWidth Int
     * @param textureHeight Int
     */
    fun pushTexture(box: Box, uvMapping: UVMapping, color: ARGBColor = Colors.WHITE, textureWidth: Int = 256, textureHeight: Int = 256) {
        val matrix4f = context.positionMatrix
        bufferBuilder.vertex(matrix4f, box.vertexes[0]).texture(uvMapping.uStart.toFloat() / textureWidth, uvMapping.vStart.toFloat() / textureHeight)
            .color(color)
        bufferBuilder.vertex(matrix4f, box.vertexes[2]).texture(uvMapping.uEnd.toFloat() / textureWidth, uvMapping.vStart.toFloat() / textureHeight)
            .color(color)
        bufferBuilder.vertex(matrix4f, box.vertexes[3]).texture(uvMapping.uStart.toFloat() / textureHeight, uvMapping.vEnd.toFloat() / textureHeight)
            .color(color)
        bufferBuilder.vertex(matrix4f, box.vertexes[4]).texture(uvMapping.uEnd.toFloat() / textureWidth, uvMapping.vEnd.toFloat() / textureHeight).color(color)

    }


    /**
     * 渲染.9 格式的纹理
     * @receiver DrawContext
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
    fun pushNinePatchTexture(
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

        if (corner == Corner.Unspecified) {
            pushTexture(x, y, width, height, u, v, uSize, vSize, color, textureWidth, textureHeight)
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
        pushTexture(x, y, corner.left.toFloat(), corner.top.toFloat(), u, v, corner.left, corner.top, color, textureWidth, textureHeight)
        //top center
        pushTexture(centerX, y, cw, corner.top.toFloat(), centerU, v, crw, corner.top, color, textureWidth, textureHeight)
        //top right
        pushTexture(rightX, y, corner.right.toFloat(), corner.top.toFloat(), rightU, v, corner.right, corner.top, color, textureWidth, textureHeight)
        //center left
        pushTexture(x, centerY, corner.left.toFloat(), ch, u, centerV, corner.left, crh, color, textureWidth, textureHeight)
        //center
        pushTexture(centerX, centerY, cw, ch, centerU, centerV, crw, crh, color, textureWidth, textureHeight)
        //center right
        pushTexture(rightX, centerY, corner.right.toFloat(), ch, rightU, centerV, corner.right, crh, color, textureWidth, textureHeight)
        //bottom left
        pushTexture(x, bottomY, corner.left.toFloat(), corner.bottom.toFloat(), u, bottomV, corner.left, corner.bottom, color, textureWidth, textureHeight)
        //bottom center
        pushTexture(centerX, bottomY, cw, corner.bottom.toFloat(), centerU, bottomV, crw, corner.bottom, color, textureWidth, textureHeight)
        //bottom right
        pushTexture(
            rightX,
            bottomY,
            corner.right.toFloat(),
            corner.bottom.toFloat(),
            rightU,
            bottomV,
            corner.right,
            corner.bottom,
            color,
            textureWidth,
            textureHeight
        )
    }

    /**
     * @see [pushNinePatchTexture]
     * @param box Box
     * @param textureUV TextureUVMapping
     * @param color ARGBColor
     * @param textureWidth Int
     * @param textureHeight Int
     */
    fun pushNinePatchTexture(
        box: Box,
        textureUV: TextureUVMapping,
        color: ARGBColor = Colors.WHITE,
        textureWidth: Int = 256,
        textureHeight: Int = 256
    ) = pushNinePatchTexture(
        box.x,
        box.y,
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
    fun pushNinePatchTexture(
        box: Box,
        cornerSize: Int,
        uvMapping: UVMapping,
        color: ARGBColor = Colors.WHITE,
        textureWidth: Int = 256,
        textureHeight: Int = 256
    ) = pushNinePatchTexture(box, TextureUVMapping(Corner(cornerSize), uvMapping), color, textureWidth, textureHeight)

    /**
     * 渲染.9格式纹理
     * @param box Box
     * @param textureUV TextureUVMapping
     * @param textureInfo TextureInfo
     * @param color Color
     */
    fun pushNinePatchTexture(
        box: Box,
        textureUV: TextureUVMapping,
        textureInfo: TextureInfo,
        color: ARGBColor = Colors.WHITE
    ) {
        setShaderTexture(textureInfo.texture)
        pushNinePatchTexture(box, textureUV, color, textureInfo.width, textureInfo.height)
    }

    /**
     * @see [pushNinePatchTexture]
     * @param box Box
     * @param widgetTexture WidgetTexture
     * @param color Color
     */
    fun pushWidgetTexture(box: Box, widgetTexture: WidgetTexture, color: ARGBColor = Colors.WHITE) =
        pushNinePatchTexture(box, widgetTexture, widgetTexture.textureInfo, color)


    /**
     * @see [pushWidgetTexture]
     * @param transform [Transform]
     * @param widgetTexture WidgetTexture
     * @param color Color
     */
    fun pushWidgetTexture(transform: Transform, widgetTexture: WidgetTexture, color: ARGBColor = Colors.WHITE) =
        pushNinePatchTexture(transform.asWorldBox, widgetTexture, widgetTexture.textureInfo, color)


    fun pushTileTexture(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        u: Int,
        v: Int,
        uSize: Int,
        vSize: Int,
        tileScale: Float = 1f,
        color: ARGBColor = Colors.WHITE,
        textureWidth: Int = 256,
        textureHeight: Int = 256,
    ) {
        if (width > 0f && height > 0f && uSize > 0 && vSize > 0 && tileScale > 0) {
            var currentX = x
            var currentY = y
            val tileWidth = uSize * tileScale
            val tileHeight = vSize * tileScale
            while (currentY < y + height) {
                while (currentX < x + width) {
                    pushTexture(currentX, currentY, tileWidth, tileHeight, u, v, uSize, vSize, color, textureWidth, textureHeight)
                    currentX += tileWidth
                }
                currentY += tileHeight
                currentX = x
            }
        }
    }


    fun pushTileTexture(
        box: Box,
        uvMapping: UVMapping,
        color: ARGBColor = Colors.WHITE,
        tileScale: Float = 1f,
        textureWidth: Int = 256,
        textureHeight: Int = 256
    ) = pushTileTexture(
        box.x,
        box.y,
        box.width,
        box.height,
        uvMapping.uStart,
        uvMapping.vStart,
        uvMapping.uSize,
        uvMapping.vSize,
        tileScale,
        color,
        textureWidth,
        textureHeight
    )

    fun pushTileTexture(
        box: Box,
        widgetTexture: WidgetTexture,
        color: ARGBColor = Colors.WHITE,
        tileScale: Float = 1f
    ) {
        setShaderTexture(widgetTexture.textureInfo.texture)
        pushTileTexture(
            box,
            widgetTexture,
            color,
            tileScale,
            widgetTexture.textureInfo.width,
            widgetTexture.textureInfo.height
        )
    }


    fun pushTileTexture(
        transform: Transform,
        widgetTexture: WidgetTexture,
        color: ARGBColor = Colors.WHITE,
        tileScale: Float = 1f
    ) = pushTileTexture(transform.asWorldBox, widgetTexture, color, tileScale)

}


