@file:Suppress("NOTHING_TO_INLINE", "UNUSED")

package moe.forpleuvoir.ibukigourd.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexConsumer
import moe.forpleuvoir.ibukigourd.gui.base.render.vertex.UVVertex
import moe.forpleuvoir.ibukigourd.util.math.Vector2f
import moe.forpleuvoir.ibukigourd.util.textureManager
import moe.forpleuvoir.nebula.common.color.ARGBColor
import net.minecraft.client.renderer.texture.AbstractTexture
import net.minecraft.resources.Identifier
import org.joml.*

val PoseStack.pose: Matrix4f get() = this.last().pose()

val Matrix3x2f.toMatrix4f: Matrix4f get() = Matrix4f().mul(this)

inline fun PoseStack.translate(vector3: Vector3fc) {
    this.translate(vector3.x(), vector3.y(), vector3.z())
}

inline fun PoseStack.Pose.translate(vector3: Vector3fc) {
    this.translate(vector3.x(), vector3.y(), vector3.z())
}

inline fun PoseStack.translate(vector2: Vector2fc) {
    this.translate(vector2.x(), vector2.y(), 0f)
}

inline fun PoseStack.Pose.translate(vector2: Vector2fc) {
    this.translate(vector2.x(), vector2.y(), 0f)
}

inline fun PoseStack.Pose.translate(x: Number, y: Number) {
    this.translate(x.toFloat(), y.toFloat(), 0f)
}

inline fun PoseStack.scale(vector2: Vector2fc) {
    this.scale(vector2.x(), vector2.y(), 1f)
}

inline fun PoseStack.scale(vector3: Vector3fc) {
    this.scale(vector3.x(), vector3.y(), vector3.z())
}

inline fun Matrix4f.getPosition(): Vector3f {
    return Vector3f(this.get(3, 0), this.get(3, 1), this.get(3, 2))
}

val Identifier.asTexture: AbstractTexture get() = textureManager.getTexture(this)

/**
 * - 默认的Z轴坐标值，用于在渲染时指定顶点的Z轴位置。
 * - 该值通常作为默认参数提供，以便在未明确指定Z轴坐标时使用。
 * - 初始值为0，表示在屏幕空间中位于默认的深度平面。
 */
var defaultZOffset: Float = 0f
    @Deprecated("Don't modify unless you know what you're doing") set

@Suppress("DEPRECATION")
inline fun runWithZOffset(offset: Float, block: () -> Unit) {
    val z = defaultZOffset
    defaultZOffset = offset
    block()
    defaultZOffset = z
}

inline fun VertexConsumer.vertex(matrix4f: Matrix4f, vertex: Vector3fc): VertexConsumer =
    addVertex(matrix4f, vertex.x(), vertex.y(), vertex.z())

inline fun VertexConsumer.vertex(matrix4f: Matrix4f, vector2fc: Vector2fc, z: Float = defaultZOffset): VertexConsumer =
    vertex(matrix4f, vector2fc.x(), vector2fc.y(), z)

inline fun VertexConsumer.vertex(matrix4f: Matrix4f, x: Number, y: Number, z: Number): VertexConsumer =
    this.addVertex(matrix4f, x.toFloat(), y.toFloat(), z.toFloat())

inline fun VertexConsumer.vertex(poseStack: PoseStack, x: Number, y: Number, z: Number): VertexConsumer =
    this.vertex(poseStack.pose, x.toFloat(), y.toFloat(), z.toFloat())

inline fun VertexConsumer.vertex(poseStack: PoseStack, vector3: Vector3fc): VertexConsumer =
    this.vertex(poseStack.pose, vector3)

inline fun VertexConsumer.vertex(poseStack: PoseStack, vector2fc: Vector2fc, z: Float = defaultZOffset): VertexConsumer =
    this.vertex(poseStack.pose, vector2fc, z)

fun VertexConsumer.vertex(pose: Matrix3x2f, x: Float, y: Float): VertexConsumer {
    val vector2f = pose.transformPosition(x, y, Vector2f())
    return addVertex(vector2f.x(), vector2f.y(), defaultZOffset)
}

inline fun VertexConsumer.uv(uv: UVVertex): VertexConsumer =
    this.setUv(uv.u, uv.v)

inline fun VertexConsumer.uv(u: Number, v: Number): VertexConsumer =
    this.setUv(u.toFloat(), v.toFloat())

inline fun VertexConsumer.color(color: ARGBColor): VertexConsumer = this.setColor(color.red, color.green, color.blue, color.alpha)

inline fun VertexConsumer.normal(normal: Vector3fc): VertexConsumer = this.setNormal(normal.x(), normal.y(), normal.z())

