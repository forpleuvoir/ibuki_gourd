package moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas

import com.mojang.blaze3d.platform.NativeImage
import moe.forpleuvoir.ibukigourd.asetools.AseParser
import moe.forpleuvoir.ibukigourd.asetools.AseRenderer
import moe.forpleuvoir.ibukigourd.asetools.AseSprite
import moe.forpleuvoir.ibukigourd.asetools.Layer
import moe.forpleuvoir.ibukigourd.asetools.PropValue
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.CenterFill
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.SLOT_NONE
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.SLOT_TONE
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.TextureFill
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.TextureLayer
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.TextureRegion
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.TextureTintMode
import moe.forpleuvoir.ibukigourd.util.logger
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.ResourceManager
import java.io.IOException

/** .ase 源文件路径：assets/<ns>/texture/sokitsu/<textureId.path>.aseprite */
internal fun Identifier.toSokitsuAseFile(): Identifier = withPrefix("texture/sokitsu/").withSuffix(".aseprite")

/**
 * .ase 直读解析结果：画布尺寸 + 导出层条目（整画布 NativeImage + 对齐后的 TextureLayer 元数据）。
 */
data class SokitsuAseTexture(
    val textureId: Identifier,
    val width: Int,
    val height: Int,
    val layers: List<SokitsuAseTexture.SokitsuAseLayer>,
) {
    /** 一个导出层：image 为整画布 RGBA（未裁剪，保证位置/切片语义）；layer 携带渲染元数据。 */
    data class SokitsuAseLayer(
        val layerId: String,
        val image: NativeImage,
        val layer: TextureLayer,
    )
}

/**
 * 从资源目录读取 `.aseprite`，按 Sokitsu 规则解析出可直接缝合进 atlas 的图层：
 * - 源文件 = 一个纹理（画布即组件范围），图层顺序即渲染叠加顺序
 * - 跳过：sokitsu 元数据 enabled=false、组（layerType=1）、`#` 开头的辅助层、无像素的隐藏辅助层
 * - 元数据（colorSlot / fill）从层 userData 的 `forpleuvoir/sokitsu`
 *   properties 读取，与插件写入的 key 一一对应
 * - 同 colorSlot 的层**各自独立输出**（不合并），渲染端按 colorSlot 染色
 * - 每个导出层输出**整画布** NativeImage（不做裁剪）
 */
class SokitsuAseLoader(private val resourceManager: ResourceManager) {

    fun load(textureId: Identifier): Result<SokitsuAseTexture> = runCatching {
        val fileId = textureId.toSokitsuAseFile()
        val resource = resourceManager.getResource(fileId)
            .orElseThrow { IOException("Missing sokitsu .ase source: $fileId") }
        val bytes = resource.open().use { it.readBytes() }
        val sprite = AseParser.parse(bytes)
        exportLayers(sprite).let { (exported, skipped) ->
            logger().debug("SokitsuAseLoader '{}': exported {} layers, skipped {}", textureId, exported.size, skipped)
            SokitsuAseTexture(textureId, sprite.header.width, sprite.header.height, exported)
        }
    }

    /** 按 Sokitsu 规则导出图层；返回 (导出层列表, 跳过层名列表)。 */
    private fun exportLayers(sprite: AseSprite): Pair<List<SokitsuAseTexture.SokitsuAseLayer>, List<String>> {
        val w = sprite.header.width
        val h = sprite.header.height
        val canvases = AseRenderer.renderFrame(sprite, 0).second
        val exported = mutableListOf<SokitsuAseTexture.SokitsuAseLayer>()
        val skipped = mutableListOf<String>()

        for (layer in sprite.layers) {
            val props = sokitsuProps(layer)
            if (props != null && (props["enabled"] as? PropValue.Bool)?.value == false) {
                skipped += layer.name
                continue
            }
            if (layer.layerType == 1) { // 组
                skipped += layer.name
                continue
            }
            if (layer.name.startsWith("#")) { // 辅助/残留层
                skipped += layer.name
                continue
            }
            val hasPixels = sprite.frames.any { f -> f.cels.any { it.layerIndex == layer.index } }
            if (!layer.isVisible && !hasPixels) {
                skipped += layer.name
                continue
            }
            val rgba = canvases[layer.index]
            if (rgba == null || rgba.isEmpty()) {
                skipped += layer.name
                continue
            }
            val meta = toTextureLayer(layer, props, w, h)
            exported += SokitsuAseTexture.SokitsuAseLayer(
                layerId = layer.name,
                image = rgbaToNativeImage(rgba, w, h),
                layer = meta,
            )
        }
        return exported to skipped
    }

    // —— 元数据映射（与插件 aseprite/ase-plugin/sokitsu.lua 写入的 key 对齐）——

    /** 取该层 userData 中 Sokitsu 外部文件（key 含 "sokitsu"）的属性表；无则 null。 */
    private fun sokitsuProps(layer: Layer): Map<String, PropValue>? =
        layer.userData?.properties?.entries?.firstOrNull { (k, _) -> k.contains("sokitsu", ignoreCase = true) }?.value

    private fun toTextureLayer(layer: Layer, props: Map<String, PropValue>?, canvasW: Int, canvasH: Int): TextureLayer {
        val fill = mapFill(props)
        return TextureLayer(
            id = layer.name,
            keys = emptyList(),
            colorSlot = props?.string("level")?.trim()?.takeIf { it.isNotEmpty() } ?: SLOT_TONE,
            // 合成策略与槽位正交：tint 决定"怎么合"，level 决定"取哪个色"；
            // 未标注（或含历史模式名）由 TextureTintMode.fromName 归一，未知/缺失回落 Mask
            tintMode = props?.string("tint")?.let(TextureTintMode::fromName) ?: TextureTintMode.Mask,
            fill = fill,
            region = mapRegion(props, canvasW, canvasH),
        )
    }

    private fun mapFill(props: Map<String, PropValue>?): TextureFill {
        val fillName = props?.string("fill") ?: return TextureFill.Stretch
        return when (fillName) {
            "stretch" -> TextureFill.Stretch
            "tile" -> TextureFill.Tile(
                scale = props.real("scale")?.toFloat() ?: 1f
            )
            "ninepatch" -> TextureFill.NinePatch(
                border = TextureFill.NinePatch.Border(
                    left = props.int("borderLeft") ?: 0,
                    top = props.int("borderTop") ?: 0,
                    right = props.int("borderRight") ?: 0,
                    bottom = props.int("borderBottom") ?: 0,
                ),
                disableSlice = props.vectorInts("disableSlice") ?: emptyList(),
                centerFill = when (props.string("centerFill")?.lowercase()) {
                    "tile" -> CenterFill.Tile
                    else -> CenterFill.Stretch
                },
                centerScale = (props.real("centerScale")?.toFloat() ?: 1f).coerceAtLeast(1f),
            )
            else -> TextureFill.Stretch
        }
    }

    /**
     * 插件目前不写 region；默认整画布即区域（返回 null 表示整张源图都属于该图层，
     * 与 [TextureRegion] 缺省语义一致）。若将来插件写入 region（多元素 .ase），在此读取。
     */
    private fun mapRegion(props: Map<String, PropValue>?, canvasW: Int, canvasH: Int): TextureRegion? {
        val rect = props?.get("region") as? PropValue.Rect ?: return null
        return TextureRegion(rect.x, rect.y, rect.x + rect.width, rect.y + rect.height)
    }

    // —— 工具 ——

    /** RGBA 字节（R,G,B,A 每像素 4 字节）→ NativeImage（RGBA8 内存布局，可直接 writeToTexture 上传）。 */
    private fun rgbaToNativeImage(rgba: ByteArray, w: Int, h: Int): NativeImage {
        val image = NativeImage(w, h, true)
        for (y in 0 until h) {
            for (x in 0 until w) {
                val off = (y * w + x) * 4
                val r = rgba[off].toInt() and 0xFF
                val g = rgba[off + 1].toInt() and 0xFF
                val b = rgba[off + 2].toInt() and 0xFF
                val a = rgba[off + 3].toInt() and 0xFF
                // NativeImage 内存按 R,G,B,A 字节序（little-endian int = 0xAABBGGRR）
                image.setPixel(x, y, (a shl 24) or (b shl 16) or (g shl 8) or r)
            }
        }
        return image
    }

    private fun Map<String, PropValue>.string(key: String): String? =
        (this[key] as? PropValue.Str)?.value

    private fun Map<String, PropValue>.bool(key: String): Boolean? =
        (this[key] as? PropValue.Bool)?.value

    private fun Map<String, PropValue>.int(key: String): Int? =
        (this[key] as? PropValue.Int)?.value?.toInt()

    private fun Map<String, PropValue>.real(key: String): Double? =
        (this[key] as? PropValue.Real)?.value

    private fun Map<String, PropValue>.vectorInts(key: String): List<Int>? =
        (this[key] as? PropValue.Vector)?.values?.mapNotNull { (it as? PropValue.Int)?.value?.toInt() }
}
