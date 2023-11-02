@file:Suppress("unused")
@file:OptIn(ExperimentalContracts::class)

package moe.forpleuvoir.ibukigourd.util

import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import com.google.common.reflect.ClassPath
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.util.text.literal
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.ModMetadata
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.sound.SoundManager
import net.minecraft.client.texture.TextureManager
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.resource.ReloadableResourceManagerImpl
import net.minecraft.util.Identifier
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KClass

val mc: MinecraftClient by lazy { MinecraftClient.getInstance() }

val textRenderer: TextRenderer by lazy { mc.textRenderer }

val soundManager: SoundManager by lazy { mc.soundManager }

val textureManager: TextureManager by lazy { mc.textureManager }

val resourceManager: ReloadableResourceManagerImpl by lazy { mc.resourceManager as ReloadableResourceManagerImpl }

val mouseX: Double get() = mc.mouse.x * mc.window.scaledWidth.toDouble() / mc.window.width.toDouble()

val mouseY: Double get() = mc.mouse.y * mc.window.scaledHeight.toDouble() / mc.window.height.toDouble()


fun resources(nameSpace: String, path: String): Identifier = Identifier(nameSpace, path)

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
    chatMessage(literal(message))
}

fun MinecraftClient.overlayMessage(message: net.minecraft.text.Text, tinted: Boolean = false) {
    this.inGameHud.setOverlayMessage(message, tinted)
}

fun MinecraftClient.overlayMessage(message: String, tinted: Boolean = false) {
    this.inGameHud.setOverlayMessage(literal(message), tinted)
}

fun Any.logger(modName: String): ModLogger {
    return ModLogger(this::class, modName)
}

internal fun Any.logger(): ModLogger {
    return ModLogger(this::class, IbukiGourd.MOD_NAME)
}

val loader: FabricLoader by lazy { FabricLoader.getInstance() }

val isDevEnv: Boolean by lazy { loader.isDevelopmentEnvironment }


inline fun isDevEnv(action: () -> Unit) {
	contract {
		callsInPlace(action, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
	}
    if (isDevEnv) action()
}

fun MatrixStack.rest() {
    if (!isEmpty) {
        this.pop()
        rest()
    }
}

@JvmName("measureTime")
fun javaMeasureTime(action: Runnable) =
    kotlin.time.measureTime(action::run).let { it.toString() to it.inWholeNanoseconds }


fun scanModPackage(predicate: (KClass<*>) -> Boolean = { true }): Map<ModMetadata, Set<KClass<*>>> {
    val builder = ImmutableMap.builder<ModMetadata, Set<KClass<*>>>()
    modPacks.forEach { (metadata, packs) ->
        val set = ImmutableSet.builder<KClass<*>>()
        packs.forEach {
            set.addAll(scanPackage(it, predicate))
        }
        builder.put(metadata, set.build())
    }
    return builder.build()
}

val modPacks: Map<ModMetadata, Set<String>> by lazy {
    ImmutableMap.builder<ModMetadata, Set<String>>().also { packs ->
        loader.allMods.forEach {
            val set = ImmutableSet.builder<String>()
            it.metadata.customValues[IbukiGourd.MOD_ID]?.apply {
                asObject.get("package")?.asArray?.onEach { value ->
                    set.add(value.asString)
                }
                packs.put(it.metadata, set.build())
            }
        }
    }.build()
}

fun scanPackage(pack: String, predicate: (KClass<*>) -> Boolean = { true }): List<KClass<*>> {
    val list = ArrayList<KClass<*>>()
    ClassPath.from(IbukiGourd::class.java.classLoader).getTopLevelClassesRecursive(pack).forEach {
        runCatching {
            val clazz = Class.forName(it.name).kotlin
            clazz.java.declaredClasses.forEach { innerClass ->
                if (predicate(innerClass.kotlin)) list.add(innerClass.kotlin)
            }
            if (predicate(clazz)) list.add(clazz)
        }
    }
    return list
}

/**
 * 判断两个list是否完全匹配
 *
 * list.size == list2.size && list[0] == list2[0]
 *
 * @receiver List<*>
 * @param list List<*>
 * @return Boolean
 */
fun <T> List<T>.exactMatch(list: List<T>, contrast: (T, T) -> Boolean = { a, b -> a == b }): Boolean {
    return if (this.size == list.size) {
        var isEquals = true
        this.forEachIndexed loop@{ index, obj ->
            if (!contrast(list[index]!!, obj)) {
                isEquals = false
                return@loop
            }
        }
        isEquals
    } else false
}