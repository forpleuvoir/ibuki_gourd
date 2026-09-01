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
 * Sokitsu atlas 缝合器（自建实现，参考原版 Stitcher 的 Region 递归二分 + 面积降序思路，不依赖原版类）。
 *
 * 放置时以"内容尺寸 + 2×padding"为占位尺寸；最终 atlas 尺寸 = 各占位区域右下角的实际占用（不强制 2 的幂）。
 * 任一 sprite 放不下 [maxSize] 则整体缝合失败。
 */
class SokitsuStitcher(
    private val maxSize: Int,
    private val padding: Int
) {
    private data class Item(val index: Int, val width: Int, val height: Int)

    private val items = mutableListOf<Item>()
    private var nextIndex = 0

    /**
     * 登记一个待缝合的图层（尺寸为内容尺寸，padding 由缝合器统一加）。
     * 登记顺序会被保留：stitch() 返回的 placed 列表按登记顺序排列。
     */
    fun add(width: Int, height: Int) {
        items += Item(nextIndex++, width, height)
    }

    fun stitch(): Result<StitchLayout> = runCatching {
        val root = Region(0, 0, maxSize, maxSize)
        // 面积降序，大图优先（与原版一致）
        val sorted = items.sortedByDescending { it.width.toLong() * it.height }
        val placedByIndex = HashMap<Int, PlacedSprite>(sorted.size)
        var atlasWidth = 0
        var atlasHeight = 0

        for ((index, width, height) in sorted) {
            val paddedW = width + padding * 2
            val paddedH = height + padding * 2
            val region = root.place(paddedW, paddedH)
                ?: throw IllegalStateException(
                    "Sokitsu atlas stitching failed: ${width}x$height does not fit maxSize=$maxSize"
                )
            placedByIndex[index] = PlacedSprite(region.x + padding, region.y + padding, width, height)
            atlasWidth = max(atlasWidth, region.x + paddedW)
            atlasHeight = max(atlasHeight, region.y + paddedH)
        }

        // 按登记顺序（= 图层声明顺序的扁平展开）输出
        StitchLayout(atlasWidth, atlasHeight, items.map { placedByIndex.getValue(it.index) })
    }

    /**
     * 递归二分区域：尝试放置 paddedW×paddedH 的占位，成功返回占位区域（原点为其左上角）。
     * 分裂策略参考原版 Region.add：holder 专座 + 两种方向的剩余切分选择。
     */
    private class Region(val x: Int, val y: Int, val width: Int, val height: Int) {
        private var occupied: Boolean = false
        private var children: List<Region>? = null

        fun place(paddedW: Int, paddedH: Int): Region? {
            if (occupied) return null
            if (paddedW > width || paddedH > height) return null

            if (paddedW == width && paddedH == height) {
                occupied = true
                return this
            }

            if (children == null) {
                children = buildList {
                    add(Region(x, y, paddedW, paddedH)) // holder 专座
                    val spareWidth = width - paddedW
                    val spareHeight = height - paddedH
                    when {
                        spareWidth > 0 && spareHeight > 0 -> {
                            val right = max(height, spareWidth)
                            val bottom = max(width, spareHeight)
                            if (right >= bottom) {
                                add(Region(x, y + paddedH, paddedW, spareHeight))
                                add(Region(x + paddedW, y, spareWidth, height))
                            } else {
                                add(Region(x + paddedW, y, spareWidth, paddedH))
                                add(Region(x, y + paddedH, width, spareHeight))
                            }
                        }
                        spareWidth == 0 -> add(Region(x, y + paddedH, paddedW, spareHeight))
                        else            -> add(Region(x + paddedW, y, spareWidth, paddedH))
                    }
                }
            }

            for (child in children!!) {
                child.place(paddedW, paddedH)?.let { return it }
            }
            return null
        }
    }
}