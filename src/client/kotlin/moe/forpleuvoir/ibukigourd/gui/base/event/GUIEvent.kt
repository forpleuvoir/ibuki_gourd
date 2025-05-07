package moe.forpleuvoir.ibukigourd.gui.base.event

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

open class GUIEvent {

    var used: Boolean = false
        protected set

    val canUse get() = !used

    val cantUse get() = used

    @OptIn(ExperimentalContracts::class)
    inline fun canUse(block: () -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }
        if (canUse) block()
    }

    @OptIn(ExperimentalContracts::class)
    inline fun cantUse(block: () -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }
        if (cantUse) block()
    }

    fun use() {
        canUse {
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

    /**
     * 尝试使用当前的 GUIEvent 实例与给定的代码块。
     * 如果 GUIEvent 可以使用并且代码块返回 true，则在该元素上执行 'use' 函数，并返回 true。
     * 如果 GUIEvent 不能使用或代码块返回 false，则返回 false。
     * ```kotlin
     * event.tryUse {
     *     // 如果使用成功，返回 true
     *     true
     * }.onSuccess {
     *     // 执行一些操作
     * }
     * ```
     * @receiver GUIEvent 当前的 GUIEvent 实例。
     * @param condition 要执行的代码块。
     * @return Result<Boolean> 成功（true）如果事件已使用，失败（false）否则。
     */
    inline fun tryUse(condition: () -> Boolean): Result<Unit> {
        if (canUse && condition()) {
            this.use()
            return Result.success(Unit)
        }
        return Result.failure(Exception("Event cannot be used."))
    }

    fun tryUse(condition: Boolean = true): Result<Unit> {
        if (canUse && condition) {
            this.use()
            return Result.success(Unit)
        }
        return Result.failure(Exception("Event cannot be used."))
    }

}