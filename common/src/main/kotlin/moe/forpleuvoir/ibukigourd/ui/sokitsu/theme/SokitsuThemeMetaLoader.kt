package moe.forpleuvoir.ibukigourd.ui.sokitsu.theme

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.api.ClientResourceReloaderListener
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.nebula.serialization.json.JsonDialect
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.server.packs.resources.ResourceManager
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

/**
 * 主题 meta 的资源重载器：与图集同时机重载，
 * 读取**本模组命名空间**的 `assets/ibukigourd/sokitsu_meta.json`，
 * 解析后整体刷新全局单例 [SokitsuThemeMeta]。
 *
 * 只读本模组命名空间：`getResource` 自动取**资源包优先级最高**的一份
 * （用户在资源包界面启用的主题包会覆盖库内默认文件），不去枚举其他命名空间。
 *
 * 文件缺失 → 保持当前 meta（内置默认值）不变；解析失败 → 记警告并保持不变。
 */
object SokitsuThemeMetaLoader : ClientResourceReloaderListener {

    private val logger = logger()

    override val identifier: Identifier = identifier("sokitsu_theme_meta")

    private val metaFileId: Identifier = identifier("sokitsu_meta.json")

    override fun reload(
        sharedState: PreparableReloadListener.SharedState,
        executor: Executor,
        barrier: PreparableReloadListener.PreparationBarrier,
        applyExecutor: Executor
    ): CompletableFuture<Void> {
        val prepare = CompletableFuture.supplyAsync({ load(sharedState.resourceManager()) }, executor)
        val compose = prepare.thenCompose { result -> barrier.wait(result).thenApply { result } }
        return compose.thenAcceptAsync({ loaded -> SokitsuThemeMeta.reload(loaded) }, applyExecutor)
    }

    private fun load(resourceManager: ResourceManager): SokitsuThemeMetaFile {
        val resource = resourceManager.getResource(metaFileId).orElse(null)
            ?: return SokitsuThemeMetaFile.default  // 未提供 meta 文件：保持内置默认
        return runCatching {
            val json = resource.openAsReader().use { it.readText() }
            val element = JsonDialect.decode(json).getOrThrow()
            SokitsuThemeMetaFile.deserialization(element).getOrThrow()
        }.onFailure { logger.warn("Failed to load sokitsu meta '$metaFileId': ${it.message}") }
            .getOrDefault(SokitsuThemeMetaFile.default)
    }
}
