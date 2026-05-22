package moe.forpleuvoir.ibukigourd.event.events.client

import moe.forpleuvoir.nebula.event.EventFactory
import net.minecraft.client.Minecraft

/**
 * 客户端生命周期事件
 */
object ClientLifecycleEvent {

    /**
     * 客户端启动中事件
     */
    @JvmField
    val Starting = EventFactory.create<(Minecraft) -> Unit>({}) { listener ->
        { client -> listener.forEach { it(client) } }
    }

    /**
     * 客户端关闭事件
     */
    @JvmField
    val Stopping = EventFactory.create<(Minecraft) -> Unit>({}) { listener ->
        { client -> listener.forEach { it(client) } }
    }

    /**
     * 打开游戏菜单事件
     */
    @JvmField
    val OpenGameMenu = EventFactory.create<(Minecraft) -> Unit>({}) { listener ->
        { client -> listener.forEach { it(client) } }
    }

}