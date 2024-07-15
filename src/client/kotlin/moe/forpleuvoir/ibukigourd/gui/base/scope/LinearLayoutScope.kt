package moe.forpleuvoir.ibukigourd.gui.base.scope

import moe.forpleuvoir.ibukigourd.gui.base.layout.LinearLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.WrappedLinearLayoutData
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.WidgetModifier

interface LinearLayoutScope : GuiScope<LinearLayout> {

    companion object {

        @JvmName("createScope")
        fun <T : LinearLayout> create(screen: T): LinearLayoutScope = object : LinearLayoutScope {
            override val owner: T get() = screen
        }

        @Suppress("NOTHING_TO_INLINE")
        inline fun <T : LinearLayout> T.create(): LinearLayoutScope = create(this)

    }

    fun Modifier.weight(weight: Int) = this then WidgetModifier {
        it.parentData = WrappedLinearLayoutData(weight)
    }


}