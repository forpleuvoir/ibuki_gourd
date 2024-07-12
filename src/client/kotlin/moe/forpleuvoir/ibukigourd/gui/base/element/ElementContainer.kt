package moe.forpleuvoir.ibukigourd.gui.base.element

interface ElementContainer {

    fun elementChildren(): List<IGElement>

    fun <T : IGElement> addElementChild(child: T): T

}