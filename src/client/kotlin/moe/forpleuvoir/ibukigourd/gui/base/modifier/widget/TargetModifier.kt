package moe.forpleuvoir.ibukigourd.gui.base.modifier.widget

import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier

fun interface TargetModifier<T : Any> : Modifier.Element {

    override fun tryApplyModify(target: Any) {
        @Suppress("UNCHECKED_CAST")
        (target as? T)?.let { applyModify(it) }
    }

    fun applyModify(target: T)

}