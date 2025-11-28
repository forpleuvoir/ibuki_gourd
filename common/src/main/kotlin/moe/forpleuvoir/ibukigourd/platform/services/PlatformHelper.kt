package moe.forpleuvoir.ibukigourd.platform.services

import java.io.File

interface PlatformHelper {

    fun getPlatformName(): String

    fun isModLoaded(modId: String): Boolean

    fun isDevEnvironment(): Boolean

    fun getEnvironmentName(): String {
        return if (isDevEnvironment()) "development" else "production"
    }

    /**
     * 获取定义了IG信息的模组的包
     *
     * @return: Map<modId, packages>
     */
    fun getIGModPackage(): Map<String, Set<String>>

    fun getConfigDir(): File

}