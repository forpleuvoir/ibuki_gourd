package moe.forpleuvoir.ibukigourd.ui.util.render

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CalculateAtlasSizeTest {

    @Test
    fun `当前默认值可计算为 16384x8192`() {
        val size = calculateAtlasSize(134_217_728L, 16384)
        assertEquals(16384, size.width)
        assertEquals(8192, size.height)
        assertTrue(size.width.toLong() * size.height >= 134_217_728L)
    }

    @Test
    fun `尺寸覆盖请求面积且宽高不超过纹理限制`() {
        val maxTextureSize = 8192
        val maxArea = maxTextureSize.toLong() * maxTextureSize
        for (area in listOf(1L, 64L, 4096L, 1_000_000L, maxArea / 2, maxArea)) {
            val size = calculateAtlasSize(area, maxTextureSize)
            assertTrue(size.width in 1..maxTextureSize, "width 越界: $size")
            assertTrue(size.height in 1..maxTextureSize, "height 越界: $size")
            assertTrue(
                size.width.toLong() * size.height >= area,
                "面积不足: area=$area, size=$size"
            )
            // 宽度为 2 的幂（不超过 maxTextureSize）
            assertTrue(
                size.width <= maxTextureSize &&
                    size.width > 0 &&
                    (size.width and (size.width - 1)) == 0,
                "宽度不是 2 的幂: $size"
            )
            // 高度向上对齐到 64
            assertEquals(0, size.height % 64)
        }
    }

    @Test
    fun `请求面积超过纹理能力时按整张纹理限制`() {
        val maxTextureSize = 1024
        val size = calculateAtlasSize(1024L * 1024 * 4, maxTextureSize)
        assertEquals(1024, size.width)
        assertEquals(1024, size.height)
    }

    @Test
    fun `非法参数被拒绝`() {
        assertFailsWith<IllegalArgumentException> { calculateAtlasSize(0, 1024) }
        assertFailsWith<IllegalArgumentException> { calculateAtlasSize(-1, 1024) }
        assertFailsWith<IllegalArgumentException> { calculateAtlasSize(1024, 0) }
        assertFailsWith<IllegalArgumentException> { calculateAtlasSize(1024, 1024, 0) }
    }
}
