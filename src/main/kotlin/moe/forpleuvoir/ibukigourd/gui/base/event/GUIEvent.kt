package moe.forpleuvoir.ibukigourd.gui.base.event

open class GUIEvent {

    var used: Boolean = false
        protected set

    fun use() {
        used = true
    }

    /**
     * 如果事件被使用，则执行block
     * @param block () -> Unit
     */
    inline fun used(block: () -> Unit) {
        if (used) block()
    }

    /**
     * 如果事件未被使用，则执行block
     * @param block () -> Unit
     */
    inline fun unUsed(block: () -> Unit) {
        if (used) block()
    }

}