package moe.forpleuvoir.ibukigourd.gui.base.modifier

import moe.forpleuvoir.ibukigourd.gui.base.element.IGDrawable
import moe.forpleuvoir.ibukigourd.gui.base.element.IGElement
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget

interface TargetModifier<T : Any> : Modifier.Element {

    val target: T

    override fun applyModify() {
        applyModify(target)
    }

    fun applyModify(target: T)

}

interface IGElementModifier : TargetModifier<IGElement>

interface IGDrawableModifier : TargetModifier<IGDrawable>

interface IGWidgetModifier : TargetModifier<IGWidget>