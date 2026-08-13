package moe.forpleuvoir.ibukigourd.util

import androidx.compose.ui.unit.IntSize
import com.mojang.blaze3d.platform.Window
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.Text
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.client.sounds.SoundManager
import net.minecraft.server.packs.resources.ReloadableResourceManager

val mc: Minecraft by lazy { Minecraft.getInstance() }

val Window.size get() = IntSize(this.width, this.height)

val Window.scaledSize get() = IntSize(this.guiScaledWidth, this.guiScaledHeight)

val textRenderer: Font by lazy { mc.font }

val soundManager: SoundManager by lazy { mc.soundManager }

val textureManager: TextureManager by lazy { mc.textureManager }

val resourceManager: ReloadableResourceManager by lazy { mc.resourceManager as ReloadableResourceManager }

@Suppress("NOTHING_TO_INLINE")
inline fun <S : Screen?> openScreen(screen: S): S {
    mc.gui.setScreen(screen)
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

fun Minecraft.chatMessage(message: Text) {
    gui.hud.getChat().addClientSystemMessage(message)
    narrator.saySystemChatQueued(message)
}

fun Minecraft.chatMessage(message: String) {
    chatMessage(Literal(message))
}

fun Minecraft.overlayMessage(message: Text, tinted: Boolean = false) {
    this.gui.hud.setOverlayMessage(message, tinted)
}

fun Minecraft.overlayMessage(message: String, tinted: Boolean = false) {
    this.gui.hud.setOverlayMessage(Literal(message), tinted)
}
