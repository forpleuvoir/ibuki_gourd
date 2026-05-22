package moe.forpleuvoir.ibukigourd.event.events.server

import moe.forpleuvoir.nebula.event.EventFactory
import net.minecraft.server.MinecraftServer


object ServerLifecycleEvent {

    /**
     * 服务端启动完成事件
     */
    @JvmField
    val Started = EventFactory.create<(MinecraftServer) -> Unit>({}) { listener ->
        { s -> listener.forEach { it(s) } }
    }

    /**
     * 服务端启动中事件
     */
    @JvmField
    val Starting = EventFactory.create<(MinecraftServer) -> Unit>({}) { listener ->
        { s -> listener.forEach { it(s) } }
    }


    /**
     * 服务端关闭事件
     */
    @JvmField
    val Stopped = EventFactory.create<(MinecraftServer) -> Unit>({}) { listener ->
        { s -> listener.forEach { it(s) } }
    }


    /**
     * 服务端关闭中事件
     */
    @JvmField
    val Stopping = EventFactory.create<(MinecraftServer) -> Unit>({}) { listener ->
        { s -> listener.forEach { it(s) } }
    }

    /**
     * 服务端保存中事件
     */
    @JvmField
    val Saving = EventFactory.create<(MinecraftServer) -> Unit>({}) { listener ->
        { s -> listener.forEach { it(s) } }
    }


}