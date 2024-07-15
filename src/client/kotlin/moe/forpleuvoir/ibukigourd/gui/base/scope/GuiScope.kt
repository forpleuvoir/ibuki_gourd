package moe.forpleuvoir.ibukigourd.gui.base.scope

import moe.forpleuvoir.ibukigourd.gui.base.GuiDslMark

@GuiDslMark
interface GuiScope<T : Any> {
    val owner: T


    companion object {

        @JvmName("createScope")
        fun <T : Any> create(screen: T): GuiScope<T> = object : GuiScope<T> {
            override val owner: T get() = screen
        }

        @Suppress("NOTHING_TO_INLINE")
        inline fun <T : Any> T.create(): GuiScope<T> = create(this)

    }

}