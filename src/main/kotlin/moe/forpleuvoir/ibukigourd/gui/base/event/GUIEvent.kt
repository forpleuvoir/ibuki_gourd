package moe.forpleuvoir.ibukigourd.gui.base.event

open class GUIEvent {

    var used: Boolean = false
        protected set

    fun use() {
        used = true
    }

    inline fun onUsed(block: () -> Unit) {
        if (used) block()
    }

}