package com.forpleuvoir.ibukigourd.util

import com.forpleuvoir.ibukigourd.IbukiGourd
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

fun Any.logger(modName: String): ModLogger {
	return ModLogger(this::class, modName)
}

internal fun Any.logger(): ModLogger {
	return ModLogger(this::class, IbukiGourd.MOD_NAME)
}

val loader: FabricLoader by lazy { FabricLoader.getInstance() }

val isDevEnv: Boolean by lazy { loader.isDevelopmentEnvironment }

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
			if (predicate(clazz)) list.add(clazz)
		} catch (_: Exception) {
		}
	}
	return list
}