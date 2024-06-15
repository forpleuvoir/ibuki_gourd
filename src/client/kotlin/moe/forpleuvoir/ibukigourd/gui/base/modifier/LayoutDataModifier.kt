package moe.forpleuvoir.ibukigourd.gui.base.modifier

import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.base.layout.LayoutData

class LayoutDataModifier(val layoutData: LayoutData) : ModifierImpl() {

    override fun modify(element: Element) {
        (element.layoutData as MutableMap).put(layoutData::class, layoutData)
    }

}

fun Modifier.layoutData(layoutData: LayoutData): Modifier = this.then(LayoutDataModifier(layoutData))

