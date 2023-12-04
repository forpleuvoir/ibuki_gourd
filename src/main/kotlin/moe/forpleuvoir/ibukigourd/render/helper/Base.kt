@file:Suppress("NOTHING_TO_INLINE", "UNUSED")

package moe.forpleuvoir.ibukigourd.render.helper

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import moe.forpleuvoir.ibukigourd.render.base.math.ImmutableVector3f
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.rectangle.Rectangle
import moe.forpleuvoir.nebula.common.color.ARGBColor
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.ShaderProgram
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import org.joml.Matrix4f
import java.util.function.Supplier

val tessellator: Tessellator get() = Tessellator.getInstance()

val bufferBuilder: BufferBuilder get() = tessellator.buffer

inline fun MatrixStack.translate(vector3: Vector3<out Number>) {
    this.translate(vector3.x.toFloat(), vector3.y.toFloat(), vector3.z.toFloat())
}

inline fun Matrix4f.translate(vector3: Vector3<out Number>) {
    this.translate(vector3.x.toFloat(), vector3.y.toFloat(), vector3.z.toFloat())
}

inline fun Matrix4f.getPosition(): Vector3<Float> {
    return ImmutableVector3f(this.get(3, 0), this.get(3, 1), this.get(3, 2))
}

inline fun BufferBuilder.draw() {
    BufferRenderer.drawWithGlobalProgram(this.end())
}

inline fun setShader(shaderSupplier: Supplier<ShaderProgram?>) = RenderSystem.setShader(shaderSupplier)

inline fun setShader(noinline shaderSupplier: (() -> ShaderProgram?)) = RenderSystem.setShader(shaderSupplier)

inline fun setShaderTexture(texture: Identifier) = RenderSystem.setShaderTexture(0, texture)

inline fun lineWidth(width: Number) = RenderSystem.lineWidth(width.toFloat())

inline fun setShaderColor(color: ARGBColor) = RenderSystem.setShaderColor(color.redF, color.greenF, color.blueF, color.alphaF)

inline fun enablePolygonOffset() = RenderSystem.enablePolygonOffset()

inline fun polygonOffset(factor: Number, units: Number) = RenderSystem.polygonOffset(factor.toFloat(), units.toFloat())

inline fun disablePolygonOffset() = RenderSystem.disablePolygonOffset()

inline fun enableBlend() = RenderSystem.enableBlend()

inline fun blendFunc(srcFactor: GlStateManager.SrcFactor, dstFactor: GlStateManager.DstFactor) = RenderSystem.blendFunc(srcFactor, dstFactor)

inline fun blendFunc(srcFactor: Int, dstFactor: Int) = RenderSystem.blendFunc(srcFactor, dstFactor)

inline fun defaultBlendFunc() = RenderSystem.defaultBlendFunc()

inline fun disableBlend() = RenderSystem.disableBlend()

inline fun blend(srcFactor: GlStateManager.SrcFactor, dstFactor: GlStateManager.DstFactor, action: () -> Unit) {
    enableBlend()
    blendFunc(srcFactor, dstFactor)
    action()
    disableBlend()
    defaultBlendFunc()
}

inline fun enableDepthTest() = RenderSystem.enableDepthTest()

inline fun disableDepthTest() = RenderSystem.disableDepthTest()

inline fun depthTest(action: () -> Unit){
    enableDepthTest()
    action()
    disableDepthTest()
}

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

inline fun disableScissor() = RenderSystem.disableScissor()

inline fun VertexConsumer.vertex(matrix4f: Matrix4f, vertex: Vector3<Float>): VertexConsumer =
    vertex(matrix4f, vertex.x, vertex.y, vertex.z)

inline fun VertexConsumer.vertex(matrix4f: Matrix4f, x: Number, y: Number, z: Number): VertexConsumer =
    this.vertex(matrix4f, x.toFloat(), y.toFloat(), z.toFloat())

inline fun VertexConsumer.vertex(matrixStack: MatrixStack, x: Number, y: Number, z: Number): VertexConsumer =
    this.vertex(matrixStack.peek().positionMatrix, x.toFloat(), y.toFloat(), z.toFloat())

inline fun VertexConsumer.vertex(matrixStack: MatrixStack, vector3: Vector3<Float>): VertexConsumer =
    this.vertex(matrixStack.peek().positionMatrix, vector3)

inline fun VertexConsumer.color(color: ARGBColor): VertexConsumer = this.color(color.argb)

inline fun VertexConsumer.normal(normal: Vector3<Float>): VertexConsumer = this.normal(normal.x, normal.y, normal.z)