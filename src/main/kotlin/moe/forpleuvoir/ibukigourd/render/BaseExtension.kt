@file:Suppress("NOTHING_TO_INLINE", "UNUSED")

package moe.forpleuvoir.ibukigourd.render

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import moe.forpleuvoir.ibukigourd.gui.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.render.vertex.UVVertex
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.ShaderProgram
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import org.joml.Matrix4f
import org.joml.Vector2fc
import org.joml.Vector3f
import org.joml.Vector3fc
import java.util.function.Supplier
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

val tessellator: Tessellator get() = Tessellator.getInstance()

val bufferBuilder: BufferBuilder get() = tessellator.buffer

inline fun MatrixStack.translate(vector3: Vector3fc) {
    this.translate(vector3.x(), vector3.y(), vector3.z())
}

inline fun Matrix4f.getPosition(): Vector3f {
    return Vector3f(this.get(3, 0), this.get(3, 1), this.get(3, 2))
}

inline fun BufferBuilder.draw() {
    BufferRenderer.drawWithGlobalProgram(this.end())
}

inline fun setShader(shaderSupplier: Supplier<ShaderProgram?>) = RenderSystem.setShader(shaderSupplier)

inline fun setShader(noinline shaderSupplier: (() -> ShaderProgram?)) = RenderSystem.setShader(shaderSupplier)

inline fun setShaderTexture(texture: Identifier) = RenderSystem.setShaderTexture(0, texture)

inline fun lineWidth(width: Number) = RenderSystem.lineWidth(width.toFloat())

inline fun setShaderColor(color: ARGBColor) = RenderSystem.setShaderColor(color.redF, color.greenF, color.blueF, color.alphaF)

inline fun getShaderColor(): Color = RenderSystem.getShaderColor().run { Color(this[0], this[1], this[2], this[3]) }

@OptIn(ExperimentalContracts::class)
inline fun shaderColor(color: Color, block: () -> Unit) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    val oldColor = getShaderColor()
    setShaderColor(color)
    block()
    setShaderColor(oldColor)
}

@OptIn(ExperimentalContracts::class)
inline fun shaderColorPlus(color: Color, block: () -> Unit) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    val oldColor = getShaderColor()
    setShaderColor(oldColor + color)
    block()
    setShaderColor(oldColor)
}

@OptIn(ExperimentalContracts::class)
inline fun shaderColorMinus(color: Color, block: () -> Unit) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    val oldColor = getShaderColor()
    setShaderColor(oldColor - color)
    block()
    setShaderColor(oldColor)
}

@OptIn(ExperimentalContracts::class)
inline fun shaderColorTimes(color: Color, block: () -> Unit) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    val oldColor = getShaderColor()
    setShaderColor(oldColor * color)
    block()
    setShaderColor(oldColor)
}

@OptIn(ExperimentalContracts::class)
inline fun shaderColorDiv(color: Color, block: () -> Unit) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    val oldColor = getShaderColor()
    setShaderColor(oldColor / color)
    block()
    setShaderColor(oldColor)
}

inline fun polygonMode(face: Int, mode: Int) = RenderSystem.polygonMode(face, mode)

inline fun enablePolygonOffset() = RenderSystem.enablePolygonOffset()

inline fun polygonOffset(factor: Number, units: Number) = RenderSystem.polygonOffset(factor.toFloat(), units.toFloat())

inline fun disablePolygonOffset() = RenderSystem.disablePolygonOffset()

@OptIn(ExperimentalContracts::class)
inline fun usePolygonOffset(factor: Number, units: Number, block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    enablePolygonOffset()
    polygonOffset(factor, units)
    block()
    disablePolygonOffset()
}

inline fun enableBlend() = RenderSystem.enableBlend()

inline fun blendFunc(srcFactor: GlStateManager.SrcFactor, dstFactor: GlStateManager.DstFactor) = RenderSystem.blendFunc(srcFactor, dstFactor)

inline fun blendFunc(srcFactor: Int, dstFactor: Int) = RenderSystem.blendFunc(srcFactor, dstFactor)

inline fun blendFuncSeparate(
    srcFactor: GlStateManager.SrcFactor,
    dstFactor: GlStateManager.DstFactor,
    srcAlpha: GlStateManager.SrcFactor,
    dstAlpha: GlStateManager.DstFactor
) = RenderSystem.blendFuncSeparate(srcFactor, dstFactor, srcAlpha, dstAlpha)

inline fun blendEquation(mode: GLFuncMode) = RenderSystem.blendEquation(mode.value)

/**
 * ADD: （默认）源颜色和目标颜色将被相加。（Cf = (Cs * Sf) + (Cd * Df)）
 *
 * SUBTRACT: 源颜色将从目标颜色中减去。（Cf = (Cd * Df) - (Cs * Sf)）
 *
 * REVERSE_SUBTRACT: 目标颜色将从源颜色中减去。（Cf = (Cs * Sf) - (Cd * Df)）
 *
 * MIN：计算出源颜色和目标颜色每个通道颜色中的较小值。这将导致较暗的结果。
 *
 * MAX:计算出源颜色和目标颜色每个通道颜色中的较大值。这将导致较亮的结果。
 *
 * 其中，“Cf”是最终颜色，“Cs”代表源颜色，“Cd”代表目标颜色，而“Sf”和“Df”则是通过glBlendFunc或glBlendFuncSeparate设置的所谓混合因子。原始的混合方程使用这些颜色和因子来决定每个颜色通道的新值。
 *
 * @param value Int
 */
enum class GLFuncMode(val value: Int) {
    ADD(GlConst.GL_FUNC_ADD),
    SUBTRACT(GlConst.GL_FUNC_SUBTRACT),
    REVERSE_SUBTRACT(GlConst.GL_FUNC_REVERSE_SUBTRACT),
    MIN(GlConst.GL_MIN),
    MAX(GlConst.GL_MAX)
}

inline fun defaultBlendFunc() = RenderSystem.defaultBlendFunc()

inline fun disableBlend() = RenderSystem.disableBlend()

@OptIn(ExperimentalContracts::class)
inline fun useBlend(srcFactor: GlStateManager.SrcFactor, dstFactor: GlStateManager.DstFactor, mode: GLFuncMode = GLFuncMode.ADD, block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    enableBlend()
    blendEquation(mode)
    blendFunc(srcFactor, dstFactor)
    block()
    blendEquation(GLFuncMode.ADD)
    disableBlend()
    defaultBlendFunc()
}

@OptIn(ExperimentalContracts::class)
inline fun useBlendSeparate(
    srcFactor: GlStateManager.SrcFactor,
    dstFactor: GlStateManager.DstFactor,
    srcAlpha: GlStateManager.SrcFactor,
    dstAlpha: GlStateManager.DstFactor,
    mode: GLFuncMode = GLFuncMode.ADD,
    block: () -> Unit
) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    enableBlend()
    blendEquation(mode)
    blendFuncSeparate(srcFactor, dstFactor, srcAlpha, dstAlpha)
    block()
    disableBlend()
    blendEquation(GLFuncMode.ADD)
    defaultBlendFunc()
}

inline fun enableColorLogicOp() = RenderSystem.enableColorLogicOp()

inline fun disableColorLogicOp() = RenderSystem.disableColorLogicOp()

inline fun logicOp(op: GlStateManager.LogicOp) = RenderSystem.logicOp(op)

@OptIn(ExperimentalContracts::class)
inline fun useColorLogicOp(op: GlStateManager.LogicOp, block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    enableColorLogicOp()
    logicOp(op)
    block()
    disableColorLogicOp()
}

inline fun enableDepthTest() = RenderSystem.enableDepthTest()

inline fun disableDepthTest() = RenderSystem.disableDepthTest()

@OptIn(ExperimentalContracts::class)
inline fun useDepthTest(block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    enableDepthTest()
    block()
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

fun setScissor(rect: Box?) {
    if (rect == null) return disableScissor()
    if (!rect.exist) return setScissor(0, 0, 0, 0)
    setScissor(rect.x, rect.y, rect.width, rect.height)
}

inline fun disableScissor() = RenderSystem.disableScissor()

inline fun VertexConsumer.vertex(matrix4f: Matrix4f, vertex: Vector3fc): VertexConsumer =
    vertex(matrix4f, vertex.x(), vertex.y(), vertex.z())

inline fun VertexConsumer.vertex(matrix4f: Matrix4f, vector2fc: Vector2fc): VertexConsumer =
    vertex(matrix4f, vector2fc.x(), vector2fc.y(), 0f)

inline fun VertexConsumer.vertex(matrix4f: Matrix4f, x: Number, y: Number, z: Number): VertexConsumer =
    this.vertex(matrix4f, x.toFloat(), y.toFloat(), z.toFloat())

inline fun VertexConsumer.vertex(matrixStack: MatrixStack, x: Number, y: Number, z: Number): VertexConsumer =
    this.vertex(matrixStack.peek().positionMatrix, x.toFloat(), y.toFloat(), z.toFloat())

inline fun VertexConsumer.vertex(matrixStack: MatrixStack, vector3: Vector3fc): VertexConsumer =
    this.vertex(matrixStack.peek().positionMatrix, vector3)

inline fun VertexConsumer.vertex(matrixStack: MatrixStack, vector2fc: Vector2fc): VertexConsumer =
    this.vertex(matrixStack.peek().positionMatrix, vector2fc)

inline fun VertexConsumer.texture(uv: UVVertex): VertexConsumer =
    this.texture(uv.u, uv.v)

inline fun VertexConsumer.color(color: ARGBColor): VertexConsumer = this.color(color.red, color.green, color.blue, color.alpha)

inline fun VertexConsumer.normal(normal: Vector3fc): VertexConsumer = this.normal(normal.x(), normal.y(), normal.z())