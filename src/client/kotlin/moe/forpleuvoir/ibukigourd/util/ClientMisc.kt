package moe.forpleuvoir.ibukigourd.util

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.McText
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.sound.SoundManager
import net.minecraft.client.texture.TextureManager
import net.minecraft.client.util.Window
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.resource.ReloadableResourceManagerImpl
import net.minecraft.util.Identifier
import kotlin.reflect.KClass

internal fun Any.logger(): ModLogger {
    return ModLogger(this::class, IbukiGourd.MOD_NAME)
}

internal fun logger(kClass: KClass<*>): ModLogger {
    return ModLogger(kClass::class, IbukiGourd.MOD_NAME)
}

internal fun logger(name: String): ModLogger {
    return ModLogger(name, IbukiGourd.MOD_NAME)
}

val mc: MinecraftClient by lazy { MinecraftClient.getInstance() }

val Window.size get() = Size(this.width, this.height)

val Window.scaledSize get() = Size(this.scaledWidth, this.scaledHeight)

val textRenderer: TextRenderer by lazy { mc.textRenderer }

val soundManager: SoundManager by lazy { mc.soundManager }

val textureManager: TextureManager by lazy { mc.textureManager }

val resourceManager: ReloadableResourceManagerImpl by lazy { mc.resourceManager as ReloadableResourceManagerImpl }

@Suppress("NOTHING_TO_INLINE")
inline fun <S : Screen?> openScreen(screen: S): S {
    mc.setScreen(screen)
    return screen
}

internal fun identifier(path: String): Identifier = identifier(IbukiGourd.MOD_ID, path)

fun MinecraftClient.sendMessage(message: String) {
    this.player?.networkHandler?.let {
        if (message.startsWith("/"))
            it.sendChatCommand(message.substring(1))
        else
            it.sendChatMessage(message)
    }
}

fun MinecraftClient.chatMessage(message: McText) {
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