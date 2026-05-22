package moe.forpleuvoir.ibukigourd.event.events.client

import moe.forpleuvoir.nebula.event.EventFactory
import net.minecraft.client.Minecraft

/**
 * 客户端tick事件
 */
object ClientTickEvent {

    /**
     * 客户端tick开始事件
     */
    @JvmField
    val TickStart = EventFactory.create<(Minecraft) -> Unit>({}) { listener ->
        { client -> listener.forEach { it(client) } }
    }

    /**
     * 客户端tick结束事件
     */
    @JvmField
    val TickEnd = EventFactory.create<(Minecraft) -> Unit>({}) { listener ->
        { client -> listener.forEach { it(client) } }
    }

}