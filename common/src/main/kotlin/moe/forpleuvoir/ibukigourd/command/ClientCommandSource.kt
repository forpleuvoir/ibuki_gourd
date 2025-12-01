package moe.forpleuvoir.ibukigourd.command

import moe.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.player.LocalPlayer
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

interface ClientCommandSource : SharedSuggestionProvider {

    val client: Minecraft

    val sender: LocalPlayer

    fun sendFeedback(message: Component)

    fun sendError(message: Component)

    val entity: Entity get() = sender

    val position: Vec3 get() = sender.position()

    val rotation: Vec2 get() = sender.rotationVector

    val level: ClientLevel

}

val SharedSuggestionProvider.clientSource: ClientCommandSource get() = ClientCommandSourceImpl(this, mc)
