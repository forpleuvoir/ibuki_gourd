package moe.forpleuvoir.ibukigourd.ui.util.render

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AtlasRectAllocatorTest {

    private val atlasSize = 512

    private fun assertNoOverlap(allocated: Collection<AtlasRect>) {
        val list = allocated.toList()
        for (i in list.indices) {
            for (j in i + 1 until list.size) {
                val a = list[i]
                val b = list[j]
                assertTrue(
                    !intersects(a, b),
                    "重叠区域: $a 与 $b"
                )
            }
        }
    }

    private fun assertInside(rect: AtlasRect, size: Int = atlasSize) {
        assertTrue(rect.x >= 0 && rect.y >= 0, "越界: $rect")
        assertTrue(rect.right <= size, "越界: $rect")
        assertTrue(rect.bottom <= size, "越界: $rect")
    }

    private fun intersects(a: AtlasRect, b: AtlasRect): Boolean =
        a.x < b.right && b.x < a.right && a.y < b.bottom && b.y < a.bottom

    @Test
    fun `空图集首次分配`() {
        val allocator = AtlasRectAllocator(atlasSize, atlasSize)
        val rect = allocator.allocate(64, 64)
        assertNotNull(rect)
        assertEquals(0, rect.x)
        assertEquals(0, rect.y)
        assertEquals(64, rect.width)
        assertEquals(64, rect.height)
        assertEquals(atlasSize.toLong() * atlasSize - 64L * 64, allocator.freePixels)
    }

    @Test
    fun `连续分配多个相同尺寸区域`() {
        val allocator = AtlasRectAllocator(atlasSize, atlasSize)
        val rects = mutableListOf<AtlasRect>()
        repeat(16) {
            val rect = allocator.allocate(64, 64)
            assertNotNull(rect)
            rects.add(rect)
        }
        assertEquals(16, rects.size)
        assertNoOverlap(rects)
        rects.forEach(::assertInside)
    }

    @Test
    fun `混合尺寸分配`() {
        val allocator = AtlasRectAllocator(atlasSize, atlasSize)
        val rects = mutableListOf<AtlasRect>()
        val sizes = listOf(
            16 to 16,
            32 to 64,
            48 to 48,
            64 to 32,
            96 to 48,
            128 to 64,
        )
        repeat(20) {
            val (w, h) = sizes[it % sizes.size]
            val rect = allocator.allocate(w, h)
            assertNotNull(rect)
            rects.add(rect)
        }
        assertNoOverlap(rects)
        rects.forEach(::assertInside)
    }

    @Test
    fun `释放后重新分配相同尺寸`() {
        val allocator = AtlasRectAllocator(atlasSize, atlasSize)
        val a = allocator.allocate(64, 64)!!
        val b = allocator.allocate(64, 64)!!
        allocator.release(a)
        val c = allocator.allocate(64, 64)
        assertNotNull(c)
        assertTrue(!intersects(c, b))
        assertNoOverlap(listOf(b, c))
    }

    @Test
    fun `padding 后不越界`() {
        val allocator = AtlasRectAllocator(128, 128)
        val alloc = allocator.allocate(32 + 2, 32 + 2)
        assertNotNull(alloc)
        assertInside(alloc)
    }

    @Test
    fun `图集满时返回失败`() {
        val allocator = AtlasRectAllocator(64, 64)
        val a = allocator.allocate(64, 64)
        assertNotNull(a)
        assertNull(allocator.allocate(1, 1))
        assertEquals(0L, allocator.freePixels)
        assertEquals(0L, allocator.largestFreeRectPixels)
    }

    @Test
    fun `相邻空闲矩形合并`() {
        val allocator = AtlasRectAllocator(128, 64)
        // 分配两个 32x32，然后释放，两者应能合并
        val a = allocator.allocate(32, 32)!!
        val b = allocator.allocate(32, 32)!!
        allocator.release(a)
        allocator.release(b)

        val merged = allocator.allocate(64, 32)
        assertNotNull(merged)
        assertEquals(64, merged.width)
        assertEquals(32, merged.height)
    }

    @Test
    fun `反复分配和释放后不出现重叠`() {
        val allocator = AtlasRectAllocator(atlasSize, atlasSize)
        val active = mutableListOf<AtlasRect>()
        val random = Random(42)
        repeat(500) {
            if (active.isEmpty() || random.nextFloat() < 0.5f) {
                val w = listOf(16, 32, 48, 64)[random.nextInt(4)]
                val h = listOf(16, 32, 48, 64)[random.nextInt(4)]
                val rect = allocator.allocate(w, h)
                if (rect != null) active.add(rect)
            } else {
                val idx = random.nextInt(active.size)
                val removed = active.removeAt(idx)
                allocator.release(removed)
            }
            assertNoOverlap(active)
            active.forEach(::assertInside)
            assertTrue(allocator.freePixels >= 0)
        }
    }

    @Test
    fun `完全清空后能够重新获得整张图集空间`() {
        val allocator = AtlasRectAllocator(atlasSize, atlasSize)
        val allocated = mutableListOf<AtlasRect>()
        val random = Random(7)
        repeat(200) {
            val rect = allocator.allocate(32, 32)
            if (rect != null) allocated.add(rect)
        }
        allocated.forEach(allocator::release)

        val full = allocator.allocate(atlasSize, atlasSize)
        assertNotNull(full)
        assertEquals(0, full.x)
        assertEquals(0, full.y)
        assertEquals(atlasSize, full.width)
        assertEquals(atlasSize, full.height)
    }

    @Test
    fun `非法请求被拒绝`() {
        val allocator = AtlasRectAllocator(64, 64)
        assertNull(allocator.allocate(0, 10))
        assertNull(allocator.allocate(10, 0))
        assertNull(allocator.allocate(-1, 10))
        assertNull(allocator.allocate(10, -1))
        assertNull(allocator.allocate(65, 10))
        assertNull(allocator.allocate(10, 65))
    }

    @Test
    fun `随机压力测试不重叠`() {
        val allocator = AtlasRectAllocator(1024, 1024)
        val active = mutableListOf<AtlasRect>()
        val random = Random(20260729)
        val sizes = listOf(16, 32, 48, 64, 96, 128)

        repeat(2000) {
            if (active.isEmpty() || random.nextFloat() < 0.55f) {
                val w = sizes[random.nextInt(sizes.size)]
                val h = sizes[random.nextInt(sizes.size)]
                allocator.allocate(w, h)?.let { active.add(it) }
            } else {
                val idx = random.nextInt(active.size)
                allocator.release(active.removeAt(idx))
            }

            if (it % 50 == 0) {
                assertNoOverlap(active)
                active.forEach { assertInside(it, 1024) }
                assertTrue(allocator.freePixels >= 0)
            }
        }

        assertNoOverlap(active)
        active.forEach { assertInside(it, 1024) }
        assertTrue(allocator.freePixels >= 0)
    }
}
