package moe.forpleuvoir.ibukigourd.event

import moe.forpleuvoir.nebula.event.Event
import moe.forpleuvoir.nebula.event.EventFactory
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 可取消上下文，表示一个可以被取消执行的操作上下文。
 *
 * 实现此接口的类型需要维护内部的取消状态，并通过 [cancel] 触发取消、
 * [isCancelled] 查询当前取消状态。
 *
 * 通过 [createEvent] 可以创建关联的取消事件，当上下文被取消时通知所有已注册的监听器。
 */
interface CancellableContext {

    companion object {

        /**
         * 创建一个限定于指定 [CancellableContext] 子类型的取消事件。
         *
         * 注册的监听器会在该上下文被取消时被逐一调用；若上下文已被取消则跳过当前监听器。
         *
         * @param T 上下文的具体类型，必须继承 [CancellableContext]
         * @return 类型为 `(T) -> Unit` 的事件，用于注册取消回调
         */
        inline fun <reified T : CancellableContext> createEvent(): Event<(T) -> Unit> =
            EventFactory.create({}) { listener ->
                { context ->
                    listener.forEach { callback ->
                        if (context.isCancelled()) return@forEach
                        callback(context)
                    }
                }
            }
    }

    /**
     * 尝试取消当前上下文。
     *
     * 如果上下文尚未取消则触发取消操作并返回 `true`；如果已被取消则返回 `false`。
     *
     * @return 如果之前尚未取消则返回 `true`，否则返回 `false`
     */
    fun cancel(): Boolean

    /**
     * 检查当前上下文是否已被取消。
     *
     * @return 如果已取消则返回 `true`，否则返回 `false`
     */
    fun isCancelled(): Boolean
}

open class CancellableContextImpl : CancellableContext {

    private val cancelled = AtomicBoolean(false)

    final override fun cancel(): Boolean = cancelled.compareAndSet(false, true)

    final override fun isCancelled() = cancelled.get()
}
