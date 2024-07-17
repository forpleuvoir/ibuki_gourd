package moe.forpleuvoir.ibukigourd.gui.base.scope

import moe.forpleuvoir.ibukigourd.gui.base.GuiDslMark

@GuiDslMark
fun interface GuiScope<T : Any> {
    fun owner(): T


    companion object {

        @JvmName("createScope")
        fun <T : Any> create(screen: T) = GuiScope { screen }

        @Suppress("NOTHING_TO_INLINE")
        inline fun <T : Any> T.create(): GuiScope<T> = create(this)

    }

}