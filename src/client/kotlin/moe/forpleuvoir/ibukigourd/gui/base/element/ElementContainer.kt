package moe.forpleuvoir.ibukigourd.gui.base.element

interface ElementContainer<E : IGElement> {

    fun elementChildren(): List<E>

}