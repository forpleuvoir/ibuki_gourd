package moe.forpleuvoir.ibukigourd.gui.base.event

import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.base.element.Layer
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

open class GUIEvent {

    lateinit var layer: Layer

    var used: Boolean = false
        protected set

    fun canUse(element: Element): Boolean {
        return !used && checkLayer(element)
    }

    fun cantUse(element: Element): Boolean {
        return used || !checkLayer(element)
    }

    @OptIn(ExperimentalContracts::class)
    inline fun canUse(element: Element, block: () -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }
        if (canUse(element)) block()
    }

    @OptIn(ExperimentalContracts::class)
    inline fun cantUse(element: Element, block: () -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }
        if (cantUse(element)) block()
    }

    fun use(element: Element) {
        canUse(element) {
            used = true
        }
    }

    /**
     * 如果事件被使用，则执行block
     * @param block () -> Unit
     */
    inline fun used(block: () -> Unit) {
        if (used) block()
    }

    /**
     * 如果事件未被使用，则执行block
     * @param block () -> Unit
     */
    inline fun unUsed(block: () -> Unit) {
        if (used) block()
    }

    fun checkLayer(element: Element): Boolean {
        if (::layer.isInitialized) {
            return element.layer == this@GUIEvent.layer
        }
        return false
    }

    @OptIn(ExperimentalContracts::class)
    fun checkLayer(element: Element, block: () -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }
        if (checkLayer(element)) block()
    }

}