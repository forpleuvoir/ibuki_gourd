package moe.forpleuvoir.ibukigourd.ui.util.render

/**
 * 图集矩形分配器。
 *
 * 管理固定尺寸图集中的空闲矩形，使用可释放的 guillotine/free-rect 分配算法：
 * 每次从“能容纳请求且剩余面积最小”的空闲矩形左上角分配，并把剩余空间拆成
 * 互不重叠的右侧区域与底部区域。释放时把矩形放回空闲列表，并合并相邻空闲区域。
 *
 * 本类不依赖 Minecraft、Compose 或 Skia，可独立进行单元测试。
 *
 * @property width  图集宽度（像素）
 * @property height 图集高度（像素）
 */
internal class AtlasRectAllocator(
    val width: Int,
    val height: Int,
) {

    /** 当前空闲矩形列表，任意两个空闲矩形互不重叠 */
    private val freeRects = mutableListOf(AtlasRect(0, 0, width, height))

    /**
     * 分配一块 [w] × [h] 的区域。
     *
     * @return 分配到的矩形；图集已满或请求尺寸非法/超过图集尺寸时返回 null
     */
    fun allocate(w: Int, h: Int): AtlasRect? {
        if (w <= 0 || h <= 0) return null
        if (w > width || h > height) return null

        var best: AtlasRect? = null
        var bestRemaining = Long.MAX_VALUE
        for (free in freeRects) {
            if (free.width >= w && free.height >= h) {
                val remaining = free.width.toLong() * free.height - w.toLong() * h
                if (remaining < bestRemaining) {
                    bestRemaining = remaining
                    best = free
                }
            }
        }

        val chosen = best ?: return null
        freeRects.remove(chosen)

        // 从左上角分配，剩余空间拆成右侧区域和底部区域（互不重叠）
        val right = AtlasRect(chosen.x + w, chosen.y, chosen.width - w, h)
        val bottom = AtlasRect(chosen.x, chosen.y + h, chosen.width, chosen.height - h)
        addFreeRect(right)
        addFreeRect(bottom)

        return AtlasRect(chosen.x, chosen.y, w, h)
    }

    /**
     * 释放一块之前由 [allocate] 分配的区域，使其重新可被使用。
     */
    fun release(rect: AtlasRect) {
        addFreeRect(rect)
    }

    /**
     * 当前空闲像素总数。
     */
    val freePixels: Long
        get() = freeRects.sumOf { it.area }

    /**
     * 当前最大的连续空闲区域像素数。
     */
    val largestFreeRectPixels: Long
        get() = freeRects.maxOfOrNull { it.area } ?: 0L

    /**
     * 将 [rect] 加入空闲列表，去除被其他空闲矩形包含的冗余区域，并尝试合并相邻空闲矩形。
     */
    private fun addFreeRect(rect: AtlasRect) {
        if (rect.width <= 0 || rect.height <= 0) return
        if (freeRects.any { it.contains(rect) }) return

        freeRects.removeAll { rect.contains(it) }
        freeRects.add(rect)
        mergeAdjacent()
    }

    /**
     * 反复合并完整共边且宽度或高度一致的相邻空闲矩形，直到无法继续合并。
     */
    private fun mergeAdjacent() {
        var changed = true
        while (changed) {
            changed = false
            outer@ for (i in freeRects.indices) {
                for (j in i + 1 until freeRects.size) {
                    val a = freeRects[i]
                    val b = freeRects[j]
                    val merged = mergeIfAdjacent(a, b)
                    if (merged != null) {
                        freeRects.removeAt(j)
                        freeRects[i] = merged
                        changed = true
                        break@outer
                    }
                }
            }
        }
    }

    private fun mergeIfAdjacent(a: AtlasRect, b: AtlasRect): AtlasRect? {
        // 左右相邻：y 相同、高度相同、水平边重合
        if (a.y == b.y && a.height == b.height) {
            if (a.x + a.width == b.x) {
                return AtlasRect(a.x, a.y, a.width + b.width, a.height)
            }
            if (b.x + b.width == a.x) {
                return AtlasRect(b.x, b.y, a.width + b.width, a.height)
            }
        }
        // 上下相邻：x 相同、宽度相同、垂直边重合
        if (a.x == b.x && a.width == b.width) {
            if (a.y + a.height == b.y) {
                return AtlasRect(a.x, a.y, a.width, a.height + b.height)
            }
            if (b.y + b.height == a.y) {
                return AtlasRect(b.x, b.y, a.width, a.height + b.height)
            }
        }
        return null
    }
}

/**
 * 图集中的一个矩形区域。
 */
internal data class AtlasRect(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
) {
    val right: Int get() = x + width
    val bottom: Int get() = y + height
    val area: Long get() = width.toLong() * height.toLong()

    fun contains(other: AtlasRect): Boolean =
        other.x >= x && other.y >= y &&
            other.right <= right && other.bottom <= bottom
}
