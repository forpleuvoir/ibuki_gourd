package com.forpleuvoir.ibukigourd.event.events.server

import com.forpleuvoir.nebula.event.Event
import net.minecraft.server.MinecraftServer

class ServerSavingEvent(@JvmField val server: MinecraftServer) : Event