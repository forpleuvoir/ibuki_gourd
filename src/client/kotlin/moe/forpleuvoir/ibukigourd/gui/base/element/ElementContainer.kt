package moe.forpleuvoir.ibukigourd.gui.base.element

interface ElementContainer : IGElement {

    fun children(): List<IGElement>

    fun <T : IGElement> addElement(element: T): T

}