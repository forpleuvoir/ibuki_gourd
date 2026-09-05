package moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas

import kotlin.math.max

/**
 * 缝合布局结果。
 *
 * @param width/height atlas 内容总尺寸（已含两端 padding）
 * @param placed 各 sprite 的放置结果
 */
data class StitchLayout(
    val width: Int,
    val height: Int,
    val placed: List<PlacedSprite>
)

/**
 * 单个 sprite 的缝合位置。
 * [x]/[y] 为内容区坐标（已含 padding 偏移），[width]/[height] 为内容尺寸。
 */
data class PlacedSprite(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)

/**
 * Sokitsu atlas 缝合器：通用 shelf + 自动宽度搜索，目标尽量接近正方形贴图
 * （多纹理共用时更紧凑）。放置占位 = 内容尺寸 + 2×padding；最终 atlas 尺寸取实际占用
 * （不强制 2 的幂），宽度受 [maxSize] 硬上限约束。任一 sprite 放不下则整体缝合失败。
 */
class SokitsuStitcher(
    private val maxSize: Int,
    private val padding: Int,
) {
    private data class Item(val index: Int, val width: Int, val height: Int)

    private val items = mutableListOf<Item>()

    /**
     * 登记一个待缝合的图层（内容尺寸，padding 由缝合器统一加）。
     * 登记顺序会被保留：stitch() 返回的 placed 列表按登记顺序排列。
     */
    fun add(width: Int, height: Int) {
        items += Item(items.size, width, height)
    }

    fun stitch(): Result<StitchLayout> = runCatching {
        val padded = items.map { it to (it.width + padding * 2 to it.height + padding * 2) }
        for (pair in padded) {
            val pw = pair.second.first
            val ph = pair.second.second
            if (pw > maxSize || ph > maxSize) {
                throw IllegalStateException("Sokitsu atlas stitching failed: ${pair.first.width}x${pair.first.height} exceeds maxSize=$maxSize")
            }
        }

        // 面积降序放置（紧凑），placed 仍按登记顺序输出
        val sorted = padded.sortedByDescending { it.second.first.toLong() * it.second.second }
        fun placeIn(w: Int): Pair<Int, List<Pair<Int, PlacedSprite>>> {
            var rowY = 0
            var cursorX = 0
            var rowH = 0
            var maxY = 0
            val out = ArrayList<Pair<Int, PlacedSprite>>(items.size)
            for (pair in sorted) {
                val item = pair.first
                val pw = pair.second.first
                val ph = pair.second.second
                if (cursorX > 0 && cursorX + pw > w) {
                    cursorX = 0
                    rowY += rowH
                    rowH = 0
                }
                out += item.index to PlacedSprite(cursorX + padding, rowY + padding, item.width, item.height)
                cursorX += pw
                rowH = maxOf(rowH, ph)
                maxY = maxOf(maxY, rowY + rowH)
            }
            return maxY to out
        }

        val totalArea = padded.sumOf { it.second.first.toLong() * it.second.second }
        val maxSliceWidth = padded.maxOf { it.second.first }
        val minW = minOf(maxOf(maxSliceWidth, kotlin.math.ceil(kotlin.math.sqrt(totalArea.toDouble())).toInt()), maxSize)
        var lo = minW
        var hi = minW
        while (hi < maxSize && placeIn(hi).first > hi) hi = minOf(hi * 2, maxSize)

        // 采样找面积最小的近方形可行解（结果不超过 maxSize）
        var bestW = hi
        var (bestH, bestPlace) = placeIn(hi)
        val samples = 12
        for (i in 0..samples) {
            val w = lo + ((hi - lo) * i) / samples
            val (h, place) = placeIn(w)
            if (h <= w && (w.toLong() * h < bestW.toLong() * bestH)) {
                bestW = w
                bestH = h
                bestPlace = place
            }
        }
        val placedByIndex = HashMap<Int, PlacedSprite>(bestPlace.size)
        for ((index, sprite) in bestPlace) placedByIndex[index] = sprite
        StitchLayout(bestW, bestH, items.map { placedByIndex.getValue(it.index) })
    }
}
