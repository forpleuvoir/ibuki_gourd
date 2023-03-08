package com.forpleuvoir.ibukigourd.util

import com.forpleuvoir.ibukigourd.IbukiGourd
import com.forpleuvoir.ibukigourd.util.text.literal
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import com.google.common.reflect.ClassPath
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.ModMetadata
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.sound.SoundManager
import net.minecraft.client.texture.TextureManager
import net.minecraft.resource.ReloadableResourceManagerImpl
import net.minecraft.util.Identifier
import kotlin.reflect.KClass


val mc: MinecraftClient by lazy { MinecraftClient.getInstance() }

val textRenderer: TextRenderer by lazy { mc.textRenderer }

val soundManager: SoundManager by lazy { mc.soundManager }

val textureManager: TextureManager by lazy { mc.textureManager }

val resourceManager: ReloadableResourceManagerImpl by lazy { mc.resourceManager as ReloadableResourceManagerImpl }

fun resources(nameSpace: String, path: String): Identifier = Identifier(nameSpace, path)

internal fun resources(path: String): Identifier = resources(IbukiGourd.MOD_ID, path)

fun chatMessage(message: net.minecraft.text.Text) {
	mc.inGameHud.chatHud.addMessage(message)
}

fun chatMessage(message: String) {
	chatMessage(literal(message))
}

fun overlayMessage(message: net.minecraft.text.Text, tinted: Boolean = false) {
	mc.inGameHud.setOverlayMessage(message, tinted)
}

fun overlayMessage(message: String, tinted: Boolean = false) {
	mc.inGameHud.setOverlayMessage(literal(message), tinted)
}

fun Any.logger(modName: String): ModLogger {
	return ModLogger(this::class, modName)
}

internal fun Any.logger(): ModLogger {
	return ModLogger(this::class, IbukiGourd.MOD_NAME)
}

val loader: FabricLoader by lazy { FabricLoader.getInstance() }

val isDevEnv: Boolean by lazy { loader.isDevelopmentEnvironment }

fun isDevEnv(action: () -> Unit) {
	if (isDevEnv) action()
}

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
			val list = ImmutableSet.builder<String>()
			it.metadata.customValues["ibukigourd"]?.apply {
				asObject.get("package")?.asArray?.onEach { value ->
					list.add(value.asString)
				}
				packs.put(it.metadata, list.build())
			}
		}
	}.build()
}

fun scanPackage(pack: String, predicate: (KClass<*>) -> Boolean = { true }): List<KClass<*>> {
	val list = ArrayList<KClass<*>>()
	ClassPath.from(IbukiGourd::class.java.classLoader).getTopLevelClassesRecursive(pack).forEach {
		try {
			val clazz = Class.forName(it.name).kotlin
			clazz.java.declaredClasses.forEach { innerClass ->
				if (predicate(innerClass.kotlin)) list.add(innerClass.kotlin)
			}
			if (predicate(clazz)) list.add(clazz)
		} catch (_: Exception) {
		}
	}
	return list
}

/**
 * 判断两个list是否完全匹配
 *
 * list.size == list2.size && list.0 == list2.0
 *
 * @receiver List<*>
 * @param list List<*>
 * @return Boolean
 */
infix fun List<*>.exactMatch(list: List<*>): Boolean {
	return if (this.size == list.size) {
		var isEquals = true
		this.forEachIndexed loop@{ index, obj ->
			if (list[index]!! != obj) {
				isEquals = false
				return@loop
			}
		}
		isEquals
	} else false
}