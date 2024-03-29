@file:Suppress("DuplicatedCode", "unused")

package moe.forpleuvoir.ibukigourd.render.helper

import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.render.texture.Corner
import moe.forpleuvoir.ibukigourd.gui.render.texture.TextureInfo
import moe.forpleuvoir.ibukigourd.gui.render.texture.TextureUVMapping
import moe.forpleuvoir.ibukigourd.gui.render.texture.UVMapping
import moe.forpleuvoir.ibukigourd.gui.render.vertex.UVVertex
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTexture
import moe.forpleuvoir.ibukigourd.render.*
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.gl.ShaderProgram
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import org.joml.Matrix4f

fun textureBatchDraw(
    shaderSupplier: () -> ShaderProgram?,
    drawMode: VertexFormat.DrawMode,
    format: VertexFormat,
    bufferBuilder: BufferBuilder = moe.forpleuvoir.ibukigourd.render.bufferBuilder,
    block: BatchDrawScope.() -> Unit
) {
    setShader(shaderSupplier)
    bufferBuilder.begin(drawMode, format)
    BatchDrawScope.bufferBuilder = bufferBuilder
    block(BatchDrawScope)
    BatchDrawScope.bufferBuilder!!.draw()
    BatchDrawScope.bufferBuilder = null
}

object BatchDrawScope {

    var bufferBuilder: BufferBuilder? = null
        internal set

    fun drawTexture(
        matrix4f: Matrix4f,
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
        bufferBuilder!!.vertex(matrix4f, x.toFloat(), y.toFloat() + height.toFloat(), zOffset.toFloat())
                .texture(u.toFloat() / textureWidth, (v.toFloat() + vSize) / textureHeight).next()
        bufferBuilder!!.vertex(matrix4f, x.toFloat() + width.toFloat(), y.toFloat() + height.toFloat(), zOffset.toFloat())
                .texture((u.toFloat() + uSize) / textureWidth, (v.toFloat() + vSize) / textureHeight).next()
        bufferBuilder!!.vertex(matrix4f, x.toFloat() + width.toFloat(), y.toFloat(), zOffset.toFloat())
                .texture((u.toFloat() + uSize) / textureWidth, v.toFloat() / textureHeight).next()
        bufferBuilder!!.vertex(matrix4f, x.toFloat(), y.toFloat(), zOffset.toFloat())
                .texture(u.toFloat() / textureWidth, v.toFloat() / textureHeight).next()
    }

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

        if (corner == Corner.EMPTY) {
            drawTexture(matrixStack, x, y, width, height, u, v, uSize, vSize, textureWidth, textureHeight, zOffset)
            return
        }

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
        val centerX = x.toFloat() + corner.left
        val rightX = x.toFloat() + (width.toFloat() - corner.right)
        val centerY = y.toFloat() + corner.top
        val bottomY = y.toFloat() + (height.toFloat() - corner.bottom)
        val matrix4f = matrixStack.peek().positionMatrix

        //top left
        drawTexture(matrix4f, x, y, corner.left, corner.top, u, v, corner.left, corner.top, textureWidth, textureHeight, zOffset)
        //top center
        drawTexture(matrix4f, centerX, y, cw, corner.top, centerU, v, crw, corner.top, textureWidth, textureHeight, zOffset)
        //top right
        drawTexture(matrix4f, rightX, y, corner.right, corner.top, rightU, v, corner.right, corner.top, textureWidth, textureHeight, zOffset)
        //center left
        drawTexture(matrix4f, x, centerY, corner.left, ch, u, centerV, corner.left, crh, textureWidth, textureHeight, zOffset)
        //center
        drawTexture(matrix4f, centerX, centerY, cw, ch, centerU, centerV, crw, crh, textureWidth, textureHeight, zOffset)
        //center right
        drawTexture(matrix4f, rightX, centerY, corner.right, ch, rightU, centerV, corner.right, crh, textureWidth, textureHeight, zOffset)
        //bottom left
        drawTexture(matrix4f, x, bottomY, corner.left, corner.bottom, u, bottomV, corner.left, corner.bottom, textureWidth, textureHeight, zOffset)
        //bottom center
        drawTexture(matrix4f, centerX, bottomY, cw, corner.bottom, centerU, bottomV, crw, corner.bottom, textureWidth, textureHeight, zOffset)
        //bottom right
        drawTexture(matrix4f, rightX, bottomY, corner.right, corner.bottom, rightU, bottomV, corner.right, corner.bottom, textureWidth, textureHeight, zOffset)
    }

    fun draw9Texture(matrixStack: MatrixStack, box: Box, textureUV: TextureUVMapping, textureWidth: Int = 256, textureHeight: Int = 256) {
        draw9Texture(
            matrixStack,
            box.position.x(),
            box.position.y(),
            box.width,
            box.height,
            textureUV.corner,
            textureUV.uStart,
            textureUV.vStart,
            textureUV.uSize,
            textureUV.vSize,
            textureWidth,
            textureHeight,
            0f
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
        draw9Texture(matrixStack, transform.asWorldBox, textureUV, textureWidth, textureHeight)

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
        rect: Box,
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
        draw9Texture(matrixStack, transform.asWorldBox, cornerSize, uvMapping, textureWidth, textureHeight)


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
        rect: Box,
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
    fun renderTexture(matrixStack: MatrixStack, rect: Box, widgetTexture: WidgetTexture, shaderColor: ARGBColor = Colors.WHITE) {
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
        renderTexture(matrixStack, transform.asWorldBox, textureUV, textureInfo, shaderColor)

    /**
     * 渲染材质
     * @param matrixStack MatrixStack
     * @param transform Transform
     * @param widgetTexture WidgetTexture
     * @param shaderColor Color
     */
    fun renderTexture(matrixStack: MatrixStack, transform: Transform, widgetTexture: WidgetTexture, shaderColor: ARGBColor = Colors.WHITE) =
        renderTexture(matrixStack, transform.asWorldBox, widgetTexture, widgetTexture.textureInfo, shaderColor)
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
    bufferBuilder.vertex(matrix4f, vertex1).texture(vertex1).next()
    bufferBuilder.vertex(matrix4f, vertex2).texture(vertex2).next()
    bufferBuilder.vertex(matrix4f, vertex3).texture(vertex3).next()
    bufferBuilder.vertex(matrix4f, vertex4).texture(vertex4).next()
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
fun drawTexture(matrixStack: MatrixStack, rect: Box, uvMapping: UVMapping, textureWidth: Int = 256, textureHeight: Int = 256) {
    val matrix4f = matrixStack.peek().positionMatrix
    setShader(GameRenderer::getPositionTexProgram)
    bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE)
    bufferBuilder.vertex(matrix4f, rect.vertexes[0]).texture(uvMapping.uStart.toFloat() / textureWidth, uvMapping.vStart.toFloat() / textureHeight).next()
    bufferBuilder.vertex(matrix4f, rect.vertexes[2]).texture(uvMapping.uEnd.toFloat() / textureWidth, uvMapping.vStart.toFloat() / textureHeight).next()
    bufferBuilder.vertex(matrix4f, rect.vertexes[3]).texture(uvMapping.uStart.toFloat() / textureHeight, uvMapping.vEnd.toFloat() / textureHeight).next()
    bufferBuilder.vertex(matrix4f, rect.vertexes[4]).texture(uvMapping.uEnd.toFloat() / textureWidth, uvMapping.vEnd.toFloat() / textureHeight).next()
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
    drawTexture(matrixStack, transform.asWorldBox, uvMapping, textureWidth, textureHeight)

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

    if (corner == Corner.EMPTY) {
        drawTexture(matrixStack, x, y, width, height, u, v, uSize, vSize, textureWidth, textureHeight, zOffset)
        return
    }

    /**
     * centerWidth
     */
    val cw = width.toFloat() - (corner.left + corner.right)

    /**
     * centerHeight
     */
    val ch = height.toFloat() - (corner.top + corner.bottom)

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
    val centerX = x.toFloat() + corner.left
    val rightX = x.toFloat() + (width.toFloat() - corner.right)
    val centerY = y.toFloat() + corner.top
    val bottomY = y.toFloat() + (height.toFloat() - corner.bottom)
    val matrix4f = matrixStack.peek().positionMatrix

    textureBatchDraw(GameRenderer::getPositionTexProgram, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE) {
        //top left
        drawTexture(matrix4f, x, y, corner.left, corner.top, u, v, corner.left, corner.top, textureWidth, textureHeight, zOffset)
        //top center
        drawTexture(matrix4f, centerX, y, cw, corner.top, centerU, v, crw, corner.top, textureWidth, textureHeight, zOffset)
        //top right
        drawTexture(matrix4f, rightX, y, corner.right, corner.top, rightU, v, corner.right, corner.top, textureWidth, textureHeight, zOffset)
        //center left
        drawTexture(matrix4f, x, centerY, corner.left, ch, u, centerV, corner.left, crh, textureWidth, textureHeight, zOffset)
        //center
        drawTexture(matrix4f, centerX, centerY, cw, ch, centerU, centerV, crw, crh, textureWidth, textureHeight, zOffset)
        //center right
        drawTexture(matrix4f, rightX, centerY, corner.right, ch, rightU, centerV, corner.right, crh, textureWidth, textureHeight, zOffset)
        //bottom left
        drawTexture(matrix4f, x, bottomY, corner.left, corner.bottom, u, bottomV, corner.left, corner.bottom, textureWidth, textureHeight, zOffset)
        //bottom center
        drawTexture(matrix4f, centerX, bottomY, cw, corner.bottom, centerU, bottomV, crw, corner.bottom, textureWidth, textureHeight, zOffset)
        //bottom right
        drawTexture(matrix4f, rightX, bottomY, corner.right, corner.bottom, rightU, bottomV, corner.right, corner.bottom, textureWidth, textureHeight, zOffset)
    }
}

fun draw9Texture(matrixStack: MatrixStack, box: Box, textureUV: TextureUVMapping, textureWidth: Int = 256, textureHeight: Int = 256) {
    draw9Texture(
        matrixStack,
        box.position.x(),
        box.position.y(),
        box.width,
        box.height,
        textureUV.corner,
        textureUV.uStart,
        textureUV.vStart,
        textureUV.uSize,
        textureUV.vSize,
        textureWidth,
        textureHeight,
        0f
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
    draw9Texture(matrixStack, transform.asWorldBox, textureUV, textureWidth, textureHeight)

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
    rect: Box,
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
    draw9Texture(matrixStack, transform.asWorldBox, cornerSize, uvMapping, textureWidth, textureHeight)


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
    rect: Box,
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
fun renderTexture(matrixStack: MatrixStack, rect: Box, widgetTexture: WidgetTexture, shaderColor: ARGBColor = Colors.WHITE) {
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
    renderTexture(matrixStack, transform.asWorldBox, textureUV, textureInfo, shaderColor)

/**
 * 渲染材质
 * @param matrixStack MatrixStack
 * @param transform Transform
 * @param widgetTexture WidgetTexture
 * @param shaderColor Color
 */
fun renderTexture(matrixStack: MatrixStack, transform: Transform, widgetTexture: WidgetTexture, shaderColor: ARGBColor = Colors.WHITE) =
    renderTexture(matrixStack, transform.asWorldBox, widgetTexture, widgetTexture.textureInfo, shaderColor)