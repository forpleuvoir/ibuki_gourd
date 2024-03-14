package moe.forpleuvoir.ibukigourd.gui.base.event

interface GUIEventBus {

    fun broadcast(event: GUIEvent)

    fun subscribe(event: GUIEvent, listener: (GUIEvent) -> Unit)

}