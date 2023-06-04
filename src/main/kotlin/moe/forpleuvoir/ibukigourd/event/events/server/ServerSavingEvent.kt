package moe.forpleuvoir.ibukigourd.event.events.server

import moe.forpleuvoir.nebula.event.Event
import net.minecraft.server.MinecraftServer

class ServerSavingEvent(@JvmField val server: MinecraftServer) : Event