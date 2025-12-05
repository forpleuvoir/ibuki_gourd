package moe.forpleuvoir.ibukigourd.gui.base.extensions.guigraphics

import com.mojang.blaze3d.vertex.VertexConsumer
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.*
import moe.forpleuvoir.ibukigourd.gui.base.render.vertex.UVVertex
import moe.forpleuvoir.ibukigourd.render.color
import moe.forpleuvoir.ibukigourd.render.setShaderTexture
import moe.forpleuvoir.ibukigourd.render.uv
import moe.forpleuvoir.ibukigourd.render.vertex
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors
import org.joml.Matrix4f
import kotlin.math.absoluteValue

//fun IGGuiGraphics.batchRenderTextureColored(
//    block: TextureBatchRenderScope.(GuiGraphics) -> Unit
//) = batchRenderTextureColored(matrix4f) scope@{
//    this@scope.block(this@batchRenderTextureColored)
//}

fun batchRenderTextureColored(
    vertexConsumer: VertexConsumer,
    matrix4f: Matrix4f,
    block: TextureBatchRenderScope.() -> Unit
) {
    block.invoke(TextureBatchRenderScope(vertexConsumer, matrix4f))
}

@Suppress("MemberVisibilityCanBePrivate", "DuplicatedCode")
open class TextureBatchRenderScope internal constructor(
    val vertexConsumer: VertexConsumer,
    val matrix4f: Matrix4f
) {

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
        val textureU = u.toFloat() / textureWidth.toFloat()
        val textureV = v.toFloat() / textureHeight.toFloat()
        val textureUEnd = (u + uSize).toFloat() / textureWidth.toFloat()
        val textureVEnd = (v + vSize).toFloat() / textureHeight.toFloat()
        vertexConsumer.apply {
            //top left
            vertex(matrix4f, x, y, 0f).uv(textureU, textureV).color(color)
            //bottom left
            vertex(matrix4f, x, y + height, 0f).uv(textureU, textureVEnd).color(color)
            //bottom right
            vertex(matrix4f, x + width, y + height, 0f).uv(textureUEnd, textureVEnd).color(color)
            //top right
            vertex(matrix4f, x + width, y, 0f).uv(textureUEnd, textureV).color(color)
        }
    }

    fun pushTexture(vertex1: UVVertex, vertex2: UVVertex, vertex3: UVVertex, vertex4: UVVertex, color: ARGBColor = Colors.WHITE) {
        vertexConsumer.vertex(matrix4f, vertex1).uv(vertex1).color(color)
        vertexConsumer.vertex(matrix4f, vertex2).uv(vertex2).color(color)
        vertexConsumer.vertex(matrix4f, vertex3).uv(vertex3).color(color)
        vertexConsumer.vertex(matrix4f, vertex4).uv(vertex4).color(color)
    }

    fun pushTexture(box: Box, uvMapping: UVMapping, color: ARGBColor = Colors.WHITE, textureWidth: Int = 256, textureHeight: Int = 256) {
        vertexConsumer.vertex(matrix4f, box.vertexes[0]).uv(uvMapping.uStart.toFloat() / textureWidth, uvMapping.vStart.toFloat() / textureHeight)
            .color(color)
        vertexConsumer.vertex(matrix4f, box.vertexes[2]).uv(uvMapping.uEnd.toFloat() / textureWidth, uvMapping.vStart.toFloat() / textureHeight)
            .color(color)
        vertexConsumer.vertex(matrix4f, box.vertexes[3]).uv(uvMapping.uStart.toFloat() / textureHeight, uvMapping.vEnd.toFloat() / textureHeight)
            .color(color)
        vertexConsumer.vertex(matrix4f, box.vertexes[4]).uv(uvMapping.uEnd.toFloat() / textureWidth, uvMapping.vEnd.toFloat() / textureHeight).color(color)

    }

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


    fun pushNinePatchTexture(
        box: Box,
        cornerSize: Int,
        uvMapping: UVMapping,
        color: ARGBColor = Colors.WHITE,
        textureWidth: Int = 256,
        textureHeight: Int = 256
    ) = pushNinePatchTexture(box, TextureUVMapping(Corner(cornerSize), uvMapping), color, textureWidth, textureHeight)

    fun pushNinePatchTexture(
        box: Box,
        textureUV: TextureUVMapping,
        textureInfo: TextureInfo,
        color: ARGBColor = Colors.WHITE
    ) {
        setShaderTexture(textureInfo.texture)
        pushNinePatchTexture(box, textureUV, color, textureInfo.width, textureInfo.height)
    }

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


