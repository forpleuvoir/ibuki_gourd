package moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext

import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.*
import moe.forpleuvoir.ibukigourd.gui.base.render.vertex.UVVertex
import moe.forpleuvoir.ibukigourd.render.*
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.gl.ShaderProgramKey
import net.minecraft.client.gl.ShaderProgramKeys.POSITION_TEX_COLOR
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import kotlin.math.absoluteValue

fun DrawContext.batchRenderTextureColored(
    beginAction: () -> Unit = { enableBlend() },
    endAction: () -> Unit = { disableBlend() },
    shaderSupplier: ShaderProgramKey = POSITION_TEX_COLOR,
    block: TextureBatchRenderScope.(DrawContext) -> Unit
) = batchRenderTextureColored(matrices, beginAction, endAction, shaderSupplier) scope@{
    this@scope.block(this@batchRenderTextureColored)
}

fun batchRenderTextureColored(
    matrices: MatrixStack,
    beginAction: () -> Unit = { enableBlend() },
    endAction: () -> Unit = { disableBlend() },
    shaderSupplier: ShaderProgramKey = POSITION_TEX_COLOR,
    block: TextureBatchRenderScope.() -> Unit
) {
    setShader(shaderSupplier)
    beginAction()
    val bufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR)
    block.invoke(TextureBatchRenderScope(bufferBuilder, matrices))
    bufferBuilder.endNullable()?.let {
        BufferRenderer.drawWithGlobalProgram(it)
    }
    endAction()
}

@Suppress("MemberVisibilityCanBePrivate", "DuplicatedCode")
open class TextureBatchRenderScope internal constructor(
    private val bufferBuilder: BufferBuilder,
    private val matrices: MatrixStack
) {

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
        val matrix4f = matrices.positionMatrix
        val textureU = u.toFloat() / textureWidth.toFloat()
        val textureV = v.toFloat() / textureHeight.toFloat()
        val textureUEnd = (u + uSize).toFloat() / textureWidth.toFloat()
        val textureVEnd = (v + vSize).toFloat() / textureHeight.toFloat()
        bufferBuilder.apply {
            //top left
            vertex(matrix4f, x, y, 0f).texture(textureU, textureV).color(color)
            //bottom left
            vertex(matrix4f, x, y + height, 0f).texture(textureU, textureVEnd).color(color)
            //bottom right
            vertex(matrix4f, x + width, y + height, 0f).texture(textureUEnd, textureVEnd).color(color)
            //top right
            vertex(matrix4f, x + width, y, 0f).texture(textureUEnd, textureV).color(color)
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
        val matrix4f = matrices.positionMatrix
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
        val matrix4f = matrices.positionMatrix
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

        //corner.left
        val cl = corner.left.absoluteValue.toFloat()
        //corner.right
        val cr = corner.right.absoluteValue.toFloat()
        //corner.top
        val ct = corner.top.absoluteValue.toFloat()
        //corner.bottom
        val cb = corner.bottom.absoluteValue.toFloat()

        /**
         * centerWidth
         */
        val cw = width - (corner.left.coerceAtLeast(0) + corner.right.coerceAtLeast(0))

        /**
         * centerHeight
         */
        val ch = height - (corner.top.coerceAtLeast(0) + corner.bottom.coerceAtLeast(0))

        val leftX = if (corner.left >= 0) x else x - cl
        val centerX = if (corner.left >= 0) x + cl else x
        val rightX = if (corner.right >= 0) x + (width - corner.right) else x + width

        val topY = if (corner.top >= 0) y else y - ct
        val centerY = if (corner.top >= 0) y + ct else y
        val bottomY = if (corner.bottom >= 0) y + (height - corner.bottom) else y + height

        val leftU = if (corner.left >= 0) u else u - cl.toInt()
        val centerU = if (corner.left >= 0) u + cl.toInt() else u
        val rightU = if (corner.right >= 0) u + (uSize - cr.toInt()) else u + uSize

        val topV = if (corner.top >= 0) v else v - ct.toInt()
        val centerV = if (corner.top >= 0) v + ct.toInt() else v
        val bottomV = if (corner.bottom >= 0) v + (vSize - cb.toInt()) else v + vSize

        val leftUS = cl.toInt()
        val centerUS = uSize - (corner.left.coerceAtLeast(0) + corner.right.coerceAtLeast(0))
        val rightUS = cr.toInt()

        val topVS = ct.toInt()
        val centerVS = vSize - (corner.top.coerceAtLeast(0) + corner.bottom.coerceAtLeast(0))
        val bottomVS = cb.toInt()

        //top left
        pushTexture(leftX, topY, cl, ct, leftU, topV, leftUS, topVS, color, textureWidth, textureHeight)
        //top center
        pushTexture(centerX, topY, cw, ct, centerU, topV, centerUS, topVS, color, textureWidth, textureHeight)
        //top right
        pushTexture(rightX, topY, cr, ct, rightU, topV, rightUS, topVS, color, textureWidth, textureHeight)

        //center left
        pushTexture(leftX, centerY, cl, ch, leftU, centerV, leftUS, centerVS, color, textureWidth, textureHeight)
        //center
        pushTexture(centerX, centerY, cw, ch, centerU, centerV, centerUS, centerVS, color, textureWidth, textureHeight)
        //center right
        pushTexture(rightX, centerY, cr, ch, rightU, centerV, rightUS, centerVS, color, textureWidth, textureHeight)

        //bottom left
        pushTexture(leftX, bottomY, cl, cb, leftU, bottomV, leftUS, bottomVS, color, textureWidth, textureHeight)
        //bottom center
        pushTexture(centerX, bottomY, cw, cb, centerU, bottomV, centerUS, bottomVS, color, textureWidth, textureHeight)
        //bottom right
        pushTexture(rightX, bottomY, cr, cb, rightU, bottomV, rightUS, bottomVS, color, textureWidth, textureHeight)
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
        pushNinePatchTexture(transform.asWorldCoordinateBox, widgetTexture, widgetTexture.textureInfo, color)


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
    ) = pushTileTexture(transform.asWorldCoordinateBox, widgetTexture, color, tileScale)

}


