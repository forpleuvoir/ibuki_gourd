package moe.forpleuvoir.ibukigourd.ui.sokitsu.draw

import androidx.compose.ui.graphics.Color
import moe.forpleuvoir.ibukigourd.render.extension.AnchorPosition
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.TextureFill
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Sokitsu 精灵绘制的纯几何 / 着色计算。无 Compose 状态、无副作用，便于单测。
 *
 * 本文件所有尺寸约定均在**屏幕像素空间**；换算链路：
 * - 素材物理像素 → 逻辑像素：除以素材密度 [moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuLayerSprite.density]
 * - 逻辑像素 → 屏幕像素：乘以像素放大倍率 [moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuPixelScale]
 * - 最终倍率 = `pixelScale / sprite.density`
 */

/**
 * 将屏幕像素尺寸吸附到像素放大倍率的整数逻辑像素（四舍五入到最接近的 pixelScale 倍数），
 * 保证像素风素材放大时边缘对齐设备像素栅格，避免亚像素采样导致的模糊/接缝。
 *
 * @param size 屏幕像素尺寸
 * @param pixelScale 像素放大倍率（1 逻辑像素 = N×N 屏幕像素块；≤0 视为 1:1 原样返回）
 */
fun snapToDensity(size: Float, pixelScale: Int): Float {
    if (pixelScale <= 0) return size
    return (size / pixelScale).roundToInt() * pixelScale.toFloat()
}

/**
 * 九宫格分片的单个格子：目标矩形（屏幕像素，绝对坐标）+ 行优先索引（0..8，从左到右、从上到下）。
 */
data class NineSliceCell(
    val index: Int,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
)

/**
 * 九宫格一维切分的 4 个边界坐标（升序，允许越界）。
 *
 * 语义对齐 compose-minecraft 的 `pushNineSliced`：
 * - 正值边框向内收缩，中心只减正值；
 * - 负值边框向区域外扩（绝对值），中心不减；
 * - 结果边界可能 < 0 或 > [size]（负边框外扩所致），调用方据此采样纹理外区域。
 *
 * @param near 起边（左 / 上）边框值，已换算到与 [size] 相同的像素空间
 * @param far 终边（右 / 下）边框值，已换算到与 [size] 相同的像素空间
 * @param size 该维度总尺寸
 * @return `[first, center, third, third + |far|]` 四个边界
 */
fun ninePatchBoundaries(near: Float, far: Float, size: Float): FloatArray {
    val n = abs(near)
    val f = abs(far)
    val first = if (near >= 0f) 0f else -n
    val center = if (near >= 0f) n else 0f
    val third = if (far >= 0f) size - f else size
    return floatArrayOf(first, center, third, third + f)
}

/**
 * 计算九宫格分片的屏幕矩形。
 *
 * 边框（[left]/[top]/[right]/[bottom]）已换算到屏幕像素（= 素材物理边框 × pixelScale / atlasDensity）。
 * 语义见 [ninePatchBoundaries]：正值向内收缩、负值向区域外扩、中心只减正值。
 * 当某维度正边框之和超过目标尺寸时，中心格坍缩为负尺寸（被跳过），角/边可能重叠——
 * 这是九宫格的退化形态，与 `pushNineSliced` 一致，不做 half-width clamp。
 *
 * @return 未禁用的分片列表（跳过 [disabledSlices] 中的索引与零/负面积格子），顺序 = 行优先索引升序。
 */
fun ninePatchSlices(
    width: Float,
    height: Float,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    disabledSlices: Set<Int> = emptySet(),
): List<NineSliceCell> {
    if (width <= 0f || height <= 0f) return emptyList()

    val xs = ninePatchBoundaries(left, right, width)
    val ys = ninePatchBoundaries(top, bottom, height)

    val cells = ArrayList<NineSliceCell>(9)
    for (row in 0..2) {
        for (col in 0..2) {
            val index = row * 3 + col
            if (index in disabledSlices) continue
            val cw = xs[col + 1] - xs[col]
            val ch = ys[row + 1] - ys[row]
            if (cw <= 0f || ch <= 0f) continue
            cells += NineSliceCell(index, xs[col], ys[row], cw, ch)
        }
    }
    return cells
}

/**
 * 九宫格纹理绘制在布局盒子**外侧**的边框量（屏幕像素），按 `[左, 上, 右, 下]` 排列。
 *
 * border 负值表示该像素带绘制在盒子外侧（正值留在盒子内侧），故单边外扩量 = `max(-border, 0)`；
 * 各图层取最大值（图层间 border 可不同），非 NinePatch 图层贡献 0。
 *
 * 于是「盒子 + 外侧带」= 纹理的完整绘制区域，盒子尺寸 = 纹理尺寸 − 外扩量
 * （见 [boxWidthPx] / [boxHeightPx]）。
 */
fun SokitsuSprite.ninePatchOutsetPx(pixelScale: Int): FloatArray {
    var left = 0f
    var top = 0f
    var right = 0f
    var bottom = 0f
    for (layer in layers) {
        val border = (layer.fill as? TextureFill.NinePatch)?.border ?: continue
        val scale = pixelScale.toFloat() / layer.density.coerceAtLeast(1)
        left = maxOf(left, (-border.left).coerceAtLeast(0) * scale)
        top = maxOf(top, (-border.top).coerceAtLeast(0) * scale)
        right = maxOf(right, (-border.right).coerceAtLeast(0) * scale)
        bottom = maxOf(bottom, (-border.bottom).coerceAtLeast(0) * scale)
    }
    return floatArrayOf(left, top, right, bottom)
}

/**
 * 九宫格精灵的布局盒子宽度（屏幕像素）= 纹理宽度 − 负 border 的外扩量。
 *
 * 负 border 的像素带绘制在盒子外侧，故盒子比纹理小；按盒子尺寸布局时，各分片源/目标同宽，
 * 纹理 1:1 绘制、不触发拉伸。非 NinePatch 填充外扩量为 0，盒子即纹理尺寸。
 *
 * @param pixelScale 像素放大倍率（1 逻辑像素 = N×N 屏幕像素块）
 */
fun SokitsuSprite.boxWidthPx(pixelScale: Int): Float =
    logicalWidth * pixelScale - ninePatchOutsetPx(pixelScale).let { it[0] + it[2] }

/** 见 [boxWidthPx]。 */
fun SokitsuSprite.boxHeightPx(pixelScale: Int): Float =
    logicalHeight * pixelScale - ninePatchOutsetPx(pixelScale).let { it[1] + it[3] }

/**
 * 平铺（[moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.TextureFill.Tile]）单个 tile 的屏幕尺寸。
 *
 * @param logicalSize 图层逻辑尺寸（= 物理尺寸 / 素材密度）
 * @param scale Tile.scale（≥1，相对源图的平铺倍率）
 * @param pixelScale 像素放大倍率（1 逻辑像素 = N×N 屏幕像素块）
 */
fun tileSizePx(logicalSize: Float, scale: Float, pixelScale: Int): Float =
    logicalSize * scale * pixelScale
