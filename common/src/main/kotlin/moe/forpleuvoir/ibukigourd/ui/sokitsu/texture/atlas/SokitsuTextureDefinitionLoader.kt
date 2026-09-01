package moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas

import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.SokitsuTexture
import moe.forpleuvoir.nebula.serialization.json.JsonDialect
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.ResourceManager
import java.io.IOException

/**
 * 纹理定义文件路径：assets/<ns>/texture/sokitsu/<textureId.path>.json
 * （textureId 为 `ibukigourd:ui/panel` 这类相对 sokitsu 目录的 id，前缀目录与扩展名在此拼接）
 */
internal fun Identifier.toSokitsuDefinitionFile(): Identifier = withPrefix("texture/sokitsu/").withSuffix(".json")

/**
 * 从 ResourceManager 加载 SokitsuTexture 定义（经现有 SokitsuTexture Codec 解析）。
 */
class SokitsuTextureDefinitionLoader(private val resourceManager: ResourceManager) {

    fun load(textureId: Identifier): Result<SokitsuTexture> = runCatching {
        val resource = resourceManager.getResource(textureId.toSokitsuDefinitionFile())
            .orElseThrow { IOException("Missing sokitsu texture definition: ${textureId.toSokitsuDefinitionFile()}") }
        val json = resource.openAsReader().use { it.readText() }
        val element = JsonDialect.decode(json).getOrThrow()
        SokitsuTexture.deserialization(element).getOrThrow()
    }
}