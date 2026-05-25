package moe.forpleuvoir.ibukigourd.ui.skia.internal

/**
 * GPU 帧资源延迟回收器。
 *
 * 旧帧资源不会立即销毁（因为 GPU 可能仍在读取），
 * 而是先放入回收队列，经过若干帧后再释放。
 */
internal class FrameRetirement {

    private val pending = mutableListOf<Entry>()

    private class Entry(
        val frame: GpuFrame,
        /** 剩余存活帧数，归零后释放 */
        var remainingFrames: Int = 4,
    )

    /**
     * 将已退役的帧加入回收队列。
     * 通常会紧随 [GpuFrame] 替换操作后调用。
     */
    fun retire(frame: GpuFrame) {
        pending.add(Entry(frame))
    }

    /**
     * 每帧调用一次，减少退役帧的存活计数，
     * 帧数归零的帧将被彻底释放。
     */
    fun update() {
        val iter = pending.iterator()
        while (iter.hasNext()) {
            val entry = iter.next()
            entry.remainingFrames--
            if (entry.remainingFrames <= 0) {
                entry.frame.close()
                iter.remove()
            }
        }
    }

    /**
     * 立即释放所有待回收的帧资源。
     * 在 [RenderSurface] 销毁时调用。
     */
    fun dispose() {
        pending.forEach { it.frame.close() }
        pending.clear()
    }
}
