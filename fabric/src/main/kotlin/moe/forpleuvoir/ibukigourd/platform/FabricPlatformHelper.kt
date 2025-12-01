package moe.forpleuvoir.ibukigourd.platform

import com.google.common.collect.ImmutableMap
import com.google.common.reflect.ClassPath
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.platform.services.PlatformHelper
import moe.forpleuvoir.ibukigourd.util.ModLogger
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import kotlin.reflect.KClass

class FabricPlatformHelper : PlatformHelper {

    companion object {

        private val logger = ModLogger("FabricPlatformHelper", IbukiGourd.MOD_NAME)

        private val loader: FabricLoader by lazy { FabricLoader.getInstance() }

        private val modPacks by lazy {
            ImmutableMap.builder<String, Set<KClass<*>>>().also { packsMapping ->
                loader.allMods.forEach { mod ->
                    mod.metadata.customValues[IbukiGourd.MOD_ID]?.apply {
                        asObject.get("package")?.asString?.let { value ->
                            packsMapping.put(mod.metadata.id, scanPackage(value))
                            logger.info("Mod: ${mod.metadata.id} register Package: $value")
                        }
                    }
                }
            }.build()
        }

        private fun scanPackage(pack: String, predicate: (KClass<*>) -> Boolean = { true }): Set<KClass<*>> {
            return buildSet {
                ClassPath.from(IbukiGourd::class.java.classLoader).getTopLevelClassesRecursive(pack).forEach {
                    runCatching {
                        val clazz = Class.forName(it.name).kotlin
                        clazz.java.declaredClasses.forEach { innerClass ->
                            if (predicate(innerClass.kotlin)) add(innerClass.kotlin)
                        }
                        if (predicate(clazz)) add(clazz)
                    }
                }
            }
        }

    }

    override fun getPlatformName(): String = "Fabric"

    override fun isModLoaded(modId: String): Boolean = loader.isModLoaded(modId)

    override fun isDevEnvironment(): Boolean = loader.isDevelopmentEnvironment

    override fun getIGModClasses(): Map<String, Set<KClass<*>>> = modPacks

    override fun getConfigDir(): File = loader.configDir.toFile()

}