package moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas

import androidx.compose.ui.unit.IntSize
import com.mojang.blaze3d.platform.NativeImage
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.ResourceManager
import java.io.IOException

/**
 * 基础图文件路径：assets/<ns>/texture/sokitsu/<textureId.path>.png
 */
internal fun Identifier.toSokitsuImageFile(): Identifier = withPrefix("texture/sokitsu/").withSuffix(".png")

/**
 * SokitsuTexture 基础图提供器（可注入，便于测试替换）。
 */
fun interface SokitsuTextureImageProvider {

    /**
     * 加载某 SokitsuTexture 的基础图并校验其尺寸与定义一致。
     *
     * @param textureId  SokitsuTexture id
     * @param expectedSize 定义中声明的 size（PNG 实际尺寸必须一致）
     */
    fun load(textureId: Identifier, expectedSize: IntSize): Result<NativeImage>
}

/**
 * 默认实现：从 ResourceManager 读取资源 PNG，并校验尺寸。
 */
class ResourceManagerSokitsuTextureImageProvider(
    private val resourceManager: ResourceManager
) : SokitsuTextureImageProvider {

    override fun load(textureId: Identifier, expectedSize: IntSize): Result<NativeImage> = runCatching {
        val fileId = textureId.toSokitsuImageFile()
        val resource = resourceManager.getResource(fileId)
            .orElseThrow { IOException("Missing sokitsu texture image: $fileId") }
        resource.open().use { input ->
            NativeImage.read(input)
        }
    }.map { image ->
        if (image.width != expectedSize.width || image.height != expectedSize.height) {
            image.close()
            throw IOException(
                "Sokitsu texture '$textureId' image size ${image.width}x${image.height} does not match definition size ${expectedSize.width}x${expectedSize.height}"
            )
        }
        image
    }
}