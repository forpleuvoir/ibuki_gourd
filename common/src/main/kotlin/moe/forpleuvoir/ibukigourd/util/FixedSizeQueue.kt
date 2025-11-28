package moe.forpleuvoir.ibukigourd.util

class FixedSizeQueue<T>(private val capacity: Int) : Iterable<T> {
    private val queue = ArrayDeque<T>()

    fun add(element: T) {
        if (queue.size >= capacity) {
            queue.removeFirst()
        }
        queue.addLast(element)
    }

    override fun iterator(): Iterator<T> = queue.iterator()

    @Suppress("UNCHECKED_CAST")
    fun asArray(): Array<T> = queue.toArray() as Array<T>
}