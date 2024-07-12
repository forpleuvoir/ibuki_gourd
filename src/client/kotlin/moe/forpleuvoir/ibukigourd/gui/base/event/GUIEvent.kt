package moe.forpleuvoir.ibukigourd.gui.base.event

import moe.forpleuvoir.ibukigourd.gui.base.GuiLayer
import moe.forpleuvoir.ibukigourd.gui.base.element.IGElement
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

open class GUIEvent {

    companion object {
        fun <R : GUIEvent> R.layer(layer: GuiLayer): R = apply {
            this.layer = layer
        }

    }

    lateinit var layer: GuiLayer

    var used: Boolean = false
        protected set

    fun canUse(element: IGElement): Boolean {
        return !used && checkLayer(element)
    }

    fun cantUse(element: IGElement): Boolean {
        return used || !checkLayer(element)
    }

    @OptIn(ExperimentalContracts::class)
    inline fun canUse(element: IGElement, block: () -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }
        if (canUse(element)) block()
    }

    @OptIn(ExperimentalContracts::class)
    inline fun cantUse(element: IGElement, block: () -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }
        if (cantUse(element)) block()
    }

    fun use(element: IGElement) {
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

    fun checkLayer(element: IGElement): Boolean {
        if (::layer.isInitialized) {
            return element.layer == this@GUIEvent.layer
        }
        return false
    }

    @OptIn(ExperimentalContracts::class)
    fun checkLayer(element: IGElement, block: () -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }
        if (checkLayer(element)) block()
    }

}