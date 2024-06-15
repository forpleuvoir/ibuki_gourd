package moe.forpleuvoir.ibukigourd.util

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.text.Literal
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.sound.SoundManager
import net.minecraft.client.texture.TextureManager
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.resource.ReloadableResourceManagerImpl
import net.minecraft.util.Identifier

internal fun Any.logger(): ModLogger {
    return ModLogger(this::class, IbukiGourd.MOD_NAME)
}

val mc: MinecraftClient by lazy { MinecraftClient.getInstance() }

val textRenderer: TextRenderer by lazy { mc.textRenderer }

val soundManager: SoundManager by lazy { mc.soundManager }

val textureManager: TextureManager by lazy { mc.textureManager }

val resourceManager: ReloadableResourceManagerImpl by lazy { mc.resourceManager as ReloadableResourceManagerImpl }

internal fun resources(path: String): Identifier = resources(IbukiGourd.MOD_ID, path)

fun MinecraftClient.sendMessage(message: String) {
    this.player?.networkHandler?.let {
        if (message.startsWith("/"))
            it.sendChatCommand(message)
        else
            it.sendChatMessage(message)
    }
}

fun MinecraftClient.chatMessage(message: net.minecraft.text.Text) {
    inGameHud.chatHud.addMessage(message)
}

fun MinecraftClient.chatMessage(message: String) {
    chatMessage(Literal(message))
}

fun MinecraftClient.overlayMessage(message: net.minecraft.text.Text, tinted: Boolean = false) {
    this.inGameHud.setOverlayMessage(message, tinted)
}

fun MinecraftClient.overlayMessage(message: String, tinted: Boolean = false) {
    this.inGameHud.setOverlayMessage(Literal(message), tinted)
}

fun MatrixStack.rest() {
    if (!isEmpty) {
        this.pop()
        rest()
    }
}