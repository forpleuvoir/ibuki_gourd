package moe.forpleuvoir.ibukigourd.platform

import com.google.common.collect.ImmutableMap
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.platform.services.PlatformHelper
import moe.forpleuvoir.ibukigourd.util.ModLogger
import net.neoforged.fml.ModList
import net.neoforged.fml.loading.FMLLoader
import net.neoforged.fml.loading.FMLPaths
import java.io.File
import kotlin.reflect.KClass
import kotlin.text.contains

class NeoforgePlatformHelper : PlatformHelper {

    companion object {

        private val logger = ModLogger("NeoforgePlatformHelper", IbukiGourd.MOD_NAME)

        @Suppress("UNCHECKED_CAST")
        private val modPacks by lazy {
            ImmutableMap.builder<String, Set<KClass<*>>>().also { packsMapping ->
                ModList.get().mods.forEach { modInfo ->
                    runCatching {
                        (modInfo.modProperties["package"] as? String)?.let { value ->
                            packsMapping.put(modInfo.modId, scanModPackage(modInfo.modId) { it.startsWith(value) })
                            logger.info("Mod: ${modInfo.modId} register Package: $value")
                        }
                    }
                }
            }.build()
        }

        private fun scanModPackage(modId: String, filter: (String) -> Boolean): Set<KClass<*>> {
            return buildSet {
                ModList.get().getModFileById(modId).file.scanResult.classes
                    .map { it.clazz.className }
                    .filter {
                        filter(it) && !it.contains(".mixin")
                    }
                    .forEach { classInfo ->
                        runCatching { add(Class.forName(classInfo).kotlin) }.onFailure {
                            if (isDevEnv) {
                                logger.warn("Failed to load class: $classInfo")
                                logger.warn(it)
                            }
                        }
                    }
            }
        }
    }

    override fun getPlatformName(): String = "Neoforge"

    override fun isModLoaded(modId: String): Boolean = ModList.get().isLoaded(modId)

    override fun isDevEnvironment(): Boolean = !FMLLoader.getCurrent().isProduction

    override fun getIGModClasses(): Map<String, Set<KClass<*>>> = modPacks

    override fun getConfigDir(): File = FMLPaths.CONFIGDIR.get().toFile()

}