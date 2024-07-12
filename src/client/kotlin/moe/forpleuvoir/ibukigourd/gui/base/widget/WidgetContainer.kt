package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.measure.Measurable

interface WidgetContainer : Measurable {

    fun widgetChildren(): List<IGWidget>

    fun <W : IGWidget> addWidgetChild(child: W): W

}