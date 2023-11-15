package moe.forpleuvoir.ibukigourd.gui.tip

import moe.forpleuvoir.ibukigourd.gui.base.element.Element

class DropTip(
    parent: Element,
    tipHandler: () -> TipHandler = { parent.screen() }
) : Tip({ parent }, tipHandler) {


}