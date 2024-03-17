package moe.forpleuvoir.ibukigourd.gui.screen

import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.base.element.Layer
import moe.forpleuvoir.ibukigourd.gui.base.event.*

class ScreenEventBus(private val layers: Set<Layer> = setOf(Layer.pop, Layer.default)) {

    constructor(vararg layers: Layer) : this(layers.toSet())

    private val charTyped: MutableMap<Layer, ArrayList<GUIEventDepthHandler<CharTypedEvent>>> = HashMap()

    private val keyPress: MutableMap<Layer, ArrayList<GUIEventDepthHandler<KeyPressEvent>>> = HashMap()

    private val keyRelease: MutableMap<Layer, ArrayList<GUIEventDepthHandler<KeyReleaseEvent>>> = HashMap()

    private val mousePress: MutableMap<Layer, ArrayList<GUIEventDepthHandler<MousePressEvent>>> = HashMap()

    private val mouseRelease: MutableMap<Layer, ArrayList<GUIEventDepthHandler<MouseReleaseEvent>>> = HashMap()

    private val mouseMove: MutableMap<Layer, ArrayList<GUIEventDepthHandler<MouseMoveEvent>>> = HashMap()

    private val mouseDrag: MutableMap<Layer, ArrayList<GUIEventDepthHandler<MouseDragEvent>>> = HashMap()

    private val mouseScroll: MutableMap<Layer, ArrayList<GUIEventDepthHandler<MouseScrollEvent>>> = HashMap()

    private val mouseEnter: MutableMap<Layer, ArrayList<GUIEventDepthHandler<MouseEnterEvent>>> = HashMap()

    private val mouseLeave: MutableMap<Layer, ArrayList<GUIEventDepthHandler<MouseLeaveEvent>>> = HashMap()

    fun broadcast(event: GUIEvent) {
        when (event) {
            is CharTypedEvent    -> dispatchEventToHandlers(event, charTyped)
            is KeyPressEvent     -> dispatchEventToHandlers(event, keyPress)
            is KeyReleaseEvent   -> dispatchEventToHandlers(event, keyRelease)
            is MousePressEvent   -> dispatchEventToHandlers(event, mousePress)
            is MouseReleaseEvent -> dispatchEventToHandlers(event, mouseRelease)
            is MouseMoveEvent    -> dispatchEventToHandlers(event, mouseMove)
            is MouseDragEvent    -> dispatchEventToHandlers(event, mouseDrag)
            is MouseScrollEvent  -> dispatchEventToHandlers(event, mouseScroll)
            is MouseEnterEvent   -> dispatchEventToHandlers(event, mouseEnter)
            is MouseLeaveEvent   -> dispatchEventToHandlers(event, mouseLeave)
        }
    }

    private fun <T : GUIEvent> dispatchEventToHandlers(event: T, map: Map<Layer, List<GUIEventDepthHandler<T>>>) {
        val unknownLayers = map.keys - layers.toSet()
        layers.forEach {
            map[it]?.forEach { eventHandler ->
                eventHandler.listener(event)
            }
        }
        unknownLayers.forEach {
            map[it]?.forEach { eventHandler ->
                eventHandler.listener(event)
            }
        }
    }

    fun subscribe(event: Element) {
        if (charTyped[event.layer] == null) {
            charTyped[event.layer] = ArrayList()
        }
        charTyped[event.layer]!!.add(0, GUIEventDepthHandler(event.depth, event.charTyped))

        if (keyPress[event.layer] == null) {
            keyPress[event.layer] = ArrayList()
        }
        keyPress[event.layer]!!.add(0, GUIEventDepthHandler(event.depth, event.keyPress))

        if (keyRelease[event.layer] == null) {
            keyRelease[event.layer] = ArrayList()
        }
        keyRelease[event.layer]!!.add(0, GUIEventDepthHandler(event.depth, event.keyRelease))

        if (mousePress[event.layer] == null) {
            mousePress[event.layer] = ArrayList()
        }
        mousePress[event.layer]!!.add(0, GUIEventDepthHandler(event.depth, event.mouseClick))

        if (mouseRelease[event.layer] == null) {
            mouseRelease[event.layer] = ArrayList()
        }
        mouseRelease[event.layer]!!.add(0, GUIEventDepthHandler(event.depth, event.mouseRelease))

        if (mouseMove[event.layer] == null) {
            mouseMove[event.layer] = ArrayList()
        }
        mouseMove[event.layer]!!.add(0, GUIEventDepthHandler(event.depth, event.mouseMove))

        if (mouseDrag[event.layer] == null) {
            mouseDrag[event.layer] = ArrayList()
        }
        mouseDrag[event.layer]!!.add(0, GUIEventDepthHandler(event.depth, event.mouseDragging))

        if (mouseScroll[event.layer] == null) {
            mouseScroll[event.layer] = ArrayList()
        }
        mouseScroll[event.layer]!!.add(0, GUIEventDepthHandler(event.depth, event.mouseScrolling))

        if (mouseEnter[event.layer] == null) {
            mouseEnter[event.layer] = ArrayList()
        }
        mouseEnter[event.layer]!!.add(0, GUIEventDepthHandler(event.depth, event.mouseEnter))

        if (mouseLeave[event.layer] == null) {
            mouseLeave[event.layer] = ArrayList()
        }
        mouseLeave[event.layer]!!.add(0, GUIEventDepthHandler(event.depth, event.mouseLeave))

    }

}

data class GUIEventDepthHandler<T : GUIEvent>(val depth: Int, val listener: (T) -> Unit)