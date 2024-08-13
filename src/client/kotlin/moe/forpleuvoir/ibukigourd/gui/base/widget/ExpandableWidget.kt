package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.nebula.common.util.primitive.pick

interface ExpandableWidget : IGWidget {

    var expandState: ExpandState

    fun onExpand(action: () -> Unit)

    fun onClose(action: () -> Unit)

}

@JvmInline
value class ExpandState private constructor(private val state: Boolean) {

    companion object {

        val Expended: ExpandState = ExpandState(true)

        val Closed: ExpandState = ExpandState(false)

    }

    fun <T> pick(v1: T, v2: T) = state.pick(v1, v2)

    val isExpanded: Boolean
        get() = state

    inline fun isExpanded(block: () -> Unit) = if (isExpanded) block() else Unit

    val isClosed: Boolean
        get() = !state

    inline fun isClosed(block: () -> Unit) = if (isClosed) block() else Unit

    operator fun not(): ExpandState = ExpandState(!state)

    override fun toString(): String {
        return if (isExpanded) "Expended" else "Closed"
    }


}