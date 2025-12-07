@file:Suppress("DuplicatedCode", "unused")

package moe.forpleuvoir.ibukigourd.gui.base.extensions.guigraphics

import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.util.math.Vector3f
import org.joml.Vector2fc
import org.joml.Vector3fc


val textRenderOffset: Vector3fc by lazy {
    Vector3f(0.0f, 0.4f, 0f)
//    ModernUICompat.textEngineEnabled(
//        Vector3f(0.0f, 0.0f, 0f), Vector3f(0.0f, 0.4f, 0f)
//    )
}

internal data class RoundBox(
    val round: Int,
    val pixelSize: Float,
    val width: Float,
    val height: Float
)

internal const val roundBoxCacheSize = 50

internal val roundBoxCache = LinkedHashMap<RoundBox, Set<Pair<Vector2fc, Size<Float>>>>(roundBoxCacheSize)