package moe.forpleuvoir.ibukigourd.platform.services

import java.io.File
import kotlin.reflect.KClass

interface PlatformHelper {

    fun getPlatformName(): String

    fun isModLoaded(modId: String): Boolean

    fun isDevEnvironment(): Boolean

    fun getEnvironmentName(): String {
        return if (isDevEnvironment()) "development" else "production"
    }

    /**
     * 定义了IG信息的所有类
     *
     * ModId:KClasses
     */
    fun getIGModClasses(): Map<String, Set<KClass<*>>>

    fun getConfigDir(): File

}