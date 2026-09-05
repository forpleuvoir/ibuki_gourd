package moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas

import com.mojang.blaze3d.platform.NativeImage
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.api.ClientResourceReloaderListener
import moe.forpleuvoir.ibukigourd.render.extension.texture.Corner
import moe.forpleuvoir.ibukigourd.render.extension.texture.TextureUVMapping
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.TextureFill
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.TextureTintMode
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorLevel
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.ibukigourd.util.textureManager
import moe.forpleuvoir.nebula.serialization.json.JsonDialect
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.server.packs.resources.ResourceManager
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

/**
 * Sokitsu atlas 管理器（object 门面，多图集注册表）。
 *
 * 每个 atlas 对应一个定义文件 `assets/<ns>/sokitsu_atlas/<atlasId>.json` 与一张 atlas 大图，
 * 按 atlas id 分别注册进 TextureManager。reload 时扫描 `sokitsu_atlas/` 目录下所有定义并逐一构建。
 *
 * 构建流程（reload 两阶段）：
 * - prepare（后台线程）：扫描定义 → 对每个 atlas 读定义 JSON → 逐纹理加载定义 JSON + 基础图 PNG
 *   → LayerExtractor 按 keys 提取图层 → 自建 Stitcher 缝合 → 产出 [SokitsuAtlasPreparations]
 * - apply（渲染线程）：[SokitsuAtlasTexture.upload] 上传 GpuTexture + 注册进 TextureManager
 *
 * 失败粒度：单个纹理加载失败记警告并跳过；单个 atlas 构建失败记错误并保留该 atlas 上一版；
 * 其余 atlas 不受影响。
 */
object SokitsuAtlasManager : ClientResourceReloaderListener {

    private val logger = logger()

    /**
     * 默认 atlas id（定义文件 assets/<ns>/sokitsu_atlas/sokitsu.json，纹理注册 key 同为该 id）。
     */
    val DEFAULT_ATLAS_ID: Identifier = Identifier.fromNamespaceAndPath(IbukiGourd.MOD_ID, "sokitsu")

    /**
     * UI 组件通用图集 id（定义文件 assets/<ns>/sokitsu_atlas/ui.json，
     * 纹理位于 texture/sokitsu/ui/，如 ui/button、ui/button.press）。
     *
     * 按钮、下拉框、面板等 UI 组件的背景精灵统一放在该图集，组件经
     * [sprite][SokitusAtlasManager.sprite](UI_ATLAS_ID, textureId) 取用。
     */
    val UI_ATLAS_ID: Identifier = identifier("ui")

    /** atlas 定义所在资源目录（相对 assets/<ns>/）。 */
    private const val ATLAS_DIRECTORY = "sokitsu_atlas"

    /**
     * max_size 缺省（0 = 平台上限）时的缝合上限回退值。
     * TODO: 平台纹理上限需在渲染线程获取（RenderSystem），后续在 apply 阶段换算。
     */
    private const val FALLBACK_MAX_SIZE = 4096

    private val atlasByDefinitionId = mutableMapOf<Identifier, SokitsuAtlasTexture>()
    private val definitions = mutableMapOf<Identifier, SokitsuAtlasDefinition>()

    override val identifier: Identifier = DEFAULT_ATLAS_ID

    // —— 查询 API（默认目标为 DEFAULT_ATLAS_ID）——

    /**
     * 查询整个 SokitsuTexture 的精灵（容器，含全部图层）；未加载/未命中返回空容器（不会抛异常）。
     */
    fun sprite(atlasId: Identifier = DEFAULT_ATLAS_ID, textureId: Identifier): SokitsuSprite =
        atlasByDefinitionId[atlasId]?.getSprite(textureId) ?: missingSprite(atlasId, textureId)

    /**
     * 查询单个图层精灵；未加载/未命中返回 missing layer（全 0 UV，不会抛异常）。
     */
    fun layer(atlasId: Identifier = DEFAULT_ATLAS_ID, textureId: Identifier, layerId: String): SokitsuLayerSprite =
        atlasByDefinitionId[atlasId]?.getLayer(textureId, layerId) ?: missingLayer(atlasId, textureId, layerId)

    /**
     * 某 SokitsuTexture 的全部图层精灵，保持 layers 声明顺序（渲染叠加顺序）。
     */
    fun layers(atlasId: Identifier = DEFAULT_ATLAS_ID, textureId: Identifier): List<SokitsuLayerSprite> =
        atlasByDefinitionId[atlasId]?.layers(textureId) ?: emptyList()

    /**
     * 查询单个图层的 UV 映射（corner 缺省为不指定，供后续 fill 绘制使用）。
     */
    fun uvMapping(
        atlasId: Identifier = DEFAULT_ATLAS_ID,
        textureId: Identifier,
        layerId: String,
        corner: Corner = Corner.Unspecified
    ): TextureUVMapping = layer(atlasId, textureId, layerId).textureUVMapping(corner)

    /**
     * atlas 级渲染缩放倍率（定义缺省 1；未加载时返回 1）。
     */
    fun density(atlasId: Identifier = DEFAULT_ATLAS_ID): Int = definitions[atlasId]?.density ?: 1

    /**
     * 底层 atlas 纹理（未加载时返回 null）。
     */
    fun atlasTexture(atlasId: Identifier = DEFAULT_ATLAS_ID): SokitsuAtlasTexture? = atlasByDefinitionId[atlasId]

    /**
     * 已加载成功的 atlas id 集合。
     */
    fun loadedAtlasIds(): Set<Identifier> = atlasByDefinitionId.keys.toSet()

    /**
     * 某 atlas 是否已加载成功过（reload 之后为 true）。
     */
    fun isLoaded(atlasId: Identifier = DEFAULT_ATLAS_ID): Boolean = atlasByDefinitionId.containsKey(atlasId)

    /**
     * 手动加载/重建单个 atlas（同步：prepare 构建 + apply 上传，需在渲染线程/客户端环境调用）。
     * 定义缺失或构建失败时返回 Result.failure，并保留该 atlas 上一版。
     */
    fun loadAtlas(atlasId: Identifier): Result<Unit> = runCatching {
        val resourceManager = currentResourceManager
            ?: throw IllegalStateException("ResourceManager is not available")
        val prepared = buildPreparations(resourceManager, atlasId)
        if (prepared != null) {
            applyPreparations(atlasId, prepared)
        }
    }

    // —— reload SPI（prepare 后台线程，apply 渲染线程）——

    override fun reload(
        sharedState: PreparableReloadListener.SharedState,
        executor: Executor,
        barrier: PreparableReloadListener.PreparationBarrier,
        applyExecutor: Executor
    ): CompletableFuture<Void> {
        currentResourceManager = sharedState.resourceManager()
        val prepareStep: CompletableFuture<Map<Identifier, SokitsuAtlasPreparations>> = CompletableFuture.supplyAsync({
            buildAllPreparations(sharedState.resourceManager())
        }, executor)
        val composeStep = prepareStep.thenCompose { result ->
            barrier.wait(result).thenApply { result }
        }
        return composeStep.thenAcceptAsync({ prepared -> applyAll(prepared) }, applyExecutor)
    }

    // —— 构建 ——

    /** 最近一次 reload 的资源管理器（供手动 loadAtlas 使用；无则 null）。 */
    private var currentResourceManager: ResourceManager? = null

    private fun buildAllPreparations(resourceManager: ResourceManager): Map<Identifier, SokitsuAtlasPreparations> {
        val atlasIds = discoverAtlasIds(resourceManager)
        if (atlasIds.isEmpty()) {
            logger.warn("No sokitsu atlas definitions found under '$ATLAS_DIRECTORY/'")
            return emptyMap()
        }

        val result = LinkedHashMap<Identifier, SokitsuAtlasPreparations>()
        for (atlasId in atlasIds) {
            runCatching { buildPreparations(resourceManager, atlasId) }
                .onSuccess { prepared -> if (prepared != null) result[atlasId] = prepared }
                .onFailure { logger.error("Failed to build sokitsu atlas '$atlasId': ${it.message}", it) }
        }
        return result
    }

    /** 扫描资源目录，收集所有 atlas 定义 id（文件 <ns>:sokitsu_atlas/<id>.json → id <ns>:<id>）。 */
    private fun discoverAtlasIds(resourceManager: ResourceManager): List<Identifier> =
        resourceManager.listResources(ATLAS_DIRECTORY) { id -> id.path.endsWith(".json") }
            .keys
            .map { id -> id.withPath { it.removePrefix("$ATLAS_DIRECTORY/").removeSuffix(".json") } }
            .sorted()

    /**
     * 收集该 atlas 的全部纹理 id：显式 [SokitsuAtlasDefinition.textures] + 自动扫描 atlas 自身目录
     * `texture/sokitsu/<atlasId.path>/` 下所有 <textureId>.json 定义（去重、保持声明/扫描顺序）。
     * 纹理 id 取文件 id 相对 texture/sokitsu/ 的路径去 .json 后缀，如 texture/sokitsu/ui/panel.json → ibukigourd:ui/panel。
     */
    private fun collectTextureIds(
        resourceManager: ResourceManager,
        atlasId: Identifier,
        definition: SokitsuAtlasDefinition
    ): List<Identifier> {
        val ids = LinkedHashSet<Identifier>()
        ids += definition.textures
        val scanDirectory = "texture/sokitsu/${atlasId.path}"
        // .ase 时代：目录下放 <textureId>.aseprite（源文件）；旧 json 定义兼容保留
        resourceManager.listResources(scanDirectory) { id -> id.path.endsWith(".aseprite") }
            .keys
            .map { id -> id.withPath { it.removePrefix("texture/sokitsu/").removeSuffix(".aseprite") } }
            .forEach { ids += it }
        resourceManager.listResources(scanDirectory) { id -> id.path.endsWith(".json") }
            .keys
            .map { id -> id.withPath { it.removePrefix("texture/sokitsu/").removeSuffix(".json") } }
            .forEach { ids += it }
        return ids.toList()
    }

    private fun buildPreparations(resourceManager: ResourceManager, atlasId: Identifier): SokitsuAtlasPreparations? {
        val definition = loadDefinition(resourceManager, atlasId)
            ?: throw IllegalStateException("Missing atlas definition file: ${definitionFileId(atlasId)}")
        definitions[atlasId] = definition

        val textureIds = collectTextureIds(resourceManager, atlasId, definition)
        if (textureIds.isEmpty()) {
            logger.warn("Sokitsu atlas '$atlasId' has no textures under 'texture/sokitsu/${atlasId.path}/'")
            return null
        }

        val textureLoader = SokitsuTextureDefinitionLoader(resourceManager)
        val imageProvider = ResourceManagerSokitsuTextureImageProvider(resourceManager)
        val stitcher = SokitsuStitcher(
            maxSize = if (definition.maxSize > 0) definition.maxSize else FALLBACK_MAX_SIZE,
            padding = definition.padding.also {
                require(it >= 0) { "padding must not be negative: $it" }
            }
        )
        val aseLoader = SokitsuAseLoader(resourceManager)

        data class Entry(
            val textureId: Identifier,
            val layerId: String,
            val image: NativeImage,
            val colorLevel: ColorLevel?,
            val tintMode: TextureTintMode,
            val fill: TextureFill,
            val tintAlpha: Boolean = false,
        )
        val entries = mutableListOf<Entry>()

        for (textureId in textureIds) {
            if (resourceManager.getResource(textureId.toSokitsuAseFile()).isPresent) {
                // .ase 直读路径：源文件即纹理，导出层直接缝合
                val aseTexture = aseLoader.load(textureId)
                    .onFailure { logger.warn("Skipping .ase texture $textureId: ${it.message}") }
                    .getOrNull() ?: continue
                for (aseLayer in aseTexture.layers) {
                    stitcher.add(aseTexture.width, aseTexture.height)
                    entries += Entry(
                        textureId,
                        aseLayer.layerId,
                        aseLayer.image,
                        aseLayer.layer.colorLevel,
                        aseLayer.layer.tintMode,
                        aseLayer.layer.fill,
                        aseLayer.layer.tintAlpha,
                    )
                }
                continue
            }

            // 旧 png+json 路径（兼容；逐步迁移到 .ase）
            val texture = textureLoader.load(textureId)
                .onFailure { logger.warn("Skipping texture $textureId: ${it.message}") }
                .getOrNull() ?: continue
            val baseImage = imageProvider.load(textureId, texture.size)
                .onFailure { logger.warn("Skipping texture $textureId: ${it.message}") }
                .getOrNull() ?: continue

            for (layer in texture.layers) {
                val layerImage = LayerExtractor.extract(baseImage, layer.keys, layer.region)
                stitcher.add(layerImage.width, layerImage.height)
                entries += Entry(textureId, layer.id, layerImage, layer.colorLevel, layer.tintMode, layer.fill, layer.tintAlpha)
            }
            baseImage.close()
        }

        if (entries.isEmpty()) {
            logger.warn("Sokitsu atlas '$atlasId' resolved to no usable textures, skipped")
            return null
        }

        val layout = stitcher.stitch().getOrThrow()
        val regions = entries.mapIndexed { index, entry ->
            val placed = layout.placed[index]
            SokitsuAtlasRegion(entry.textureId, entry.layerId, placed.x, placed.y, entry.image, entry.colorLevel, entry.tintMode, entry.fill)
        }
        return SokitsuAtlasPreparations(layout.width, layout.height, definition.padding, regions, definition.density)
    }

    private fun loadDefinition(resourceManager: ResourceManager, atlasId: Identifier): SokitsuAtlasDefinition? = runCatching {
        val fileId = definitionFileId(atlasId)
        val resource = resourceManager.getResource(fileId).orElse(null) ?: return null
        val json = resource.openAsReader().use { it.readText() }
        SokitsuAtlasDefinition.deserialization(JsonDialect.decode(json).getOrThrow()).getOrThrow()
    }.onFailure { logger.error("Failed to parse atlas definition: ${it.message}", it) }.getOrNull()

    private fun definitionFileId(atlasId: Identifier): Identifier =
        atlasId.withPrefix("$ATLAS_DIRECTORY/").withSuffix(".json")

    // —— apply ——

    private fun applyAll(prepared: Map<Identifier, SokitsuAtlasPreparations>) {
        for ((atlasId, preparations) in prepared) {
            runCatching { applyPreparations(atlasId, preparations) }
                .onFailure { logger.error("Failed to upload sokitsu atlas '$atlasId': ${it.message}", it) }
        }
    }

    private fun applyPreparations(atlasId: Identifier, preparations: SokitsuAtlasPreparations) {
        val texture = atlasByDefinitionId.getOrPut(atlasId) { SokitsuAtlasTexture(atlasId) }
        try {
            texture.upload(preparations)
            textureManager.register(atlasId, texture)
        } finally {
            preparations.regions.forEach { it.image.close() }
        }
    }

    private fun missingSprite(atlasId: Identifier, textureId: Identifier): SokitsuSprite =
        SokitsuSprite(atlasId, textureId, emptyList())

    private fun missingLayer(atlasId: Identifier, textureId: Identifier, layerId: String): SokitsuLayerSprite =
        SokitsuLayerSprite(atlasId, textureId, layerId, 0, 0, 0, 0, 1, 1)
}