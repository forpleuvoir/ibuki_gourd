package moe.forpleuvoir.ibukigourd.gui.base.scope

import moe.forpleuvoir.ibukigourd.gui.base.layout.WrappedBoxLayoutData
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.WidgetModifier

interface BoxLayoutScope {

    fun Modifier.align(alignment: Alignment) = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is WrappedBoxLayoutData -> it.parentData = parentData.copy(alignment = alignment)
            null                    -> it.parentData = WrappedBoxLayoutData(alignment = alignment)
        }
    }

    /**
     *
     * 填充至Box大小
     *
     * @receiver Modifier
     */
    fun Modifier.fillWidth() = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is WrappedBoxLayoutData -> it.parentData = parentData.copy(fillWidth = true)
            null                    -> it.parentData = WrappedBoxLayoutData(fillWidth = true)
        }
    }

    /**
     *
     * 填充至Box大小
     *
     * @receiver Modifier
     */
    fun Modifier.fillHeight() = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is WrappedBoxLayoutData -> it.parentData = parentData.copy(fillHeight = true)
            null                    -> it.parentData = WrappedBoxLayoutData(fillHeight = true)
        }
    }


    fun Modifier.fill() = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is WrappedBoxLayoutData -> it.parentData = parentData.copy(fillHeight = true, fillWidth = true)
            null                    -> it.parentData = WrappedBoxLayoutData(fillHeight = true, fillWidth = true)
        }
    }

}