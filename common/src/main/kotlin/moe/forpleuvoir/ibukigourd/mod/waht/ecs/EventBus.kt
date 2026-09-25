package moe.forpleuvoir.ibukigourd.mod.waht.ecs

import kotlin.reflect.KClass

/**
 * 事件订阅句柄,调用 [cancel] 取消订阅。
 */
fun interface Subscription {
    fun cancel()
}

/**
 * 基于事件类型([KClass]) + 通道([Channel])路由的事件总线。
 *
 * [emit] 为同步分发,在调用线程内顺序回调所有订阅者。
 * 适合碰撞、得分等彩蛋游戏的即时交互;若需延迟/排队,可由调用方在外部缓冲。
 *
 * 通过 [Channel] 区分相同类型的不同语义事件,避免类型冲突。
 * 例如 `world.emit(0.5f, Channel("score"))` 和 `world.emit(0.5f, Channel("health"))` 不会互相干扰。
 */
class EventBus {

    private data class Key(val type: KClass<*>, val channel: Channel)

    private val subscribers = HashMap<Key, MutableList<(Any) -> Unit>>()

    /**
     * 订阅指定通道和类型的事件,返回可取消的 [Subscription]。
     *
     * @param T 事件类型
     * @param channel 事件通道,用于区分同类型的不同语义;不指定则全局匹配
     * @param handler 收到事件时的回调(类型安全,已做 `is T` 检查)
     */
    inline fun <reified T : Any> subscribe(
        channel: Channel = Channel.DEFAULT,
        noinline handler: (T) -> Unit,
    ): Subscription {
        return subscribeRaw(T::class, channel) { event ->
            @Suppress("UNCHECKED_CAST")
            handler(event as T)
        }
    }

    /**
     * 发布事件到指定通道,同步分发给所有匹配的订阅者。
     *
     * 注意:遍历期间允许订阅者再次 [emit] 或 [cancel](基于快照迭代,不会 ConcurrentModificationException)。
     */
    fun emit(event: Any, channel: Channel = Channel.DEFAULT) {
        val key = Key(event::class, channel)
        // 快照避免遍历中订阅/取消导致 ConcurrentModificationException
        val list = subscribers[key]?.toList() ?: return
        for (handler in list) handler(event)
    }

    /**
     * 清空所有订阅者。
     */
    fun clear() {
        subscribers.clear()
    }

    @PublishedApi
    internal fun subscribeRaw(type: KClass<*>, channel: Channel, handler: (Any) -> Unit): Subscription {
        val key = Key(type, channel)
        val list = subscribers.getOrPut(key) { mutableListOf() }
        list.add(handler)
        return Subscription { list.remove(handler) }
    }
}

/**
 * 事件通道,用于区分同类型事件的不同语义。
 *
 * 使用 [Channel.DEFAULT] 时行为与旧版一致(仅按类型路由)。
 */
data class Channel(val name: String) {
    companion object {
        val DEFAULT = Channel("")
    }
}

/**
 * 在 [World] 的事件总线上订阅事件。
 */
inline fun <reified T : Any> World.onEvent(
    channel: Channel = Channel.DEFAULT,
    noinline handler: (T) -> Unit,
): Subscription = events.subscribe(channel, handler)

/**
 * 在 [World] 的事件总线上发布事件。
 */
fun World.emit(event: Any, channel: Channel = Channel.DEFAULT): Unit = events.emit(event, channel)
