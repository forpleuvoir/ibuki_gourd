package moe.forpleuvoir.ibukigourd.util

import com.mojang.blaze3d.platform.Window
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.McText
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.client.sounds.SoundManager
import net.minecraft.server.packs.resources.ReloadableResourceManager

val mc: Minecraft by lazy { Minecraft.getInstance() }

val Window.size get() = Size(this.width, this.height)

val Window.scaledSize get() = Size(this.guiScaledWidth, this.guiScaledHeight)

val textRenderer: Font by lazy { mc.font }

val soundManager: SoundManager by lazy { mc.soundManager }

val textureManager: TextureManager by lazy { mc.textureManager }

val resourceManager: ReloadableResourceManager by lazy { mc.resourceManager as ReloadableResourceManager }

@Suppress("NOTHING_TO_INLINE")
inline fun <S : Screen?> openScreen(screen: S): S {
    mc.setScreen(screen)
    return screen
}

fun Minecraft.sendMessage(message: String) {
    this.player?.connection?.let {
        if (message.startsWith("/"))
            it.sendCommand(message.substring(1))
        else
            it.sendChat(message)
    }
}

fun Minecraft.chatMessage(message: McText) {
    gui.chat.addMessage(message)
    narrator.saySystemChatQueued(message)
}

fun Minecraft.chatMessage(message: String) {
    chatMessage(Literal(message))
}

fun Minecraft.overlayMessage(message: McText, tinted: Boolean = false) {
    this.gui.setOverlayMessage(message, tinted)
}

fun Minecraft.overlayMessage(message: String, tinted: Boolean = false) {
    this.gui.setOverlayMessage(Literal(message), tinted)
}
