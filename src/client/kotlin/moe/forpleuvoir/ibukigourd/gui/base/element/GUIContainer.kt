package moe.forpleuvoir.ibukigourd.gui.base.element

interface GUIContainer<T> {
    fun children(): List<T>

    fun addChild(child: T): T

}