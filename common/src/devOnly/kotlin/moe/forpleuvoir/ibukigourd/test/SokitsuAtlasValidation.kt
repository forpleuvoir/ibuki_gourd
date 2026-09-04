package moe.forpleuvoir.ibukigourd.test

import com.mojang.blaze3d.platform.NativeImage
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.TextureRegion
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.TextureLayer
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.TextureFill
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorLevel
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.LayerExtractor
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuAtlasDefinition
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuLayerSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuStitcher
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.serialization.json.JsonDialect
import net.minecraft.resources.Identifier
import kotlin.math.abs

/**
 * Sokitsu atlas 纯逻辑验证（devOnly，随测试环境初始化运行）。
 * 覆盖：atlas 定义 JSON 解析（density 缺省）、keys 提取、缝合布局（不重叠/顺序）、SokitsuSprite UV。
 * 不触碰 GPU / RenderSystem（上传与资源加载部分依赖游戏环境，不做运行断言）。
 */
object SokitsuAtlasValidation {

    private val logger = logger(IbukiGourd.MOD_NAME)
    private var passed = 0
    private var failed = 0

    fun run() {
        validateAtlasDefinition()
        validateLayerExtractor()
        validateLayerRegionParse()
        validateNinePatchDisableSlice()
        validateStitcher()
        validateSprite()
        logger.info("SokitsuAtlasValidation done: passed $passed / failed $failed / total ${passed + failed}")
    }

    // —— 1. atlas 定义 JSON（density 缺省 = 1）——

    private fun validateAtlasDefinition() {
        val json = """{ "textures": ["ibukigourd:panel", "ibukigourd:button"] }"""
        val def = SokitsuAtlasDefinition.deserialization(JsonDialect.decode(json).getOrThrow()).getOrThrow()
        check("atlas definition: density defaults to 1") { def.density == 1 }
        check("atlas definition: max_size defaults to 0") { def.maxSize == 0 }
        check("atlas definition: padding defaults to 1") { def.padding == 1 }
        check("atlas definition: mip_level defaults to 0") { def.mipLevel == 0 }
        check("atlas definition: textures parsed") {
            def.textures == listOf(
                Identifier.fromNamespaceAndPath("ibukigourd", "panel"),
                Identifier.fromNamespaceAndPath("ibukigourd", "button")
            )
        }

        val jsonFull = """{"max_size":1024,"padding":2,"mip_level":1,"density":3,"textures":["a:b"]}"""
        val defFull = SokitsuAtlasDefinition.deserialization(JsonDialect.decode(jsonFull).getOrThrow()).getOrThrow()
        check("atlas definition: full fields parsed") {
            defFull.maxSize == 1024 && defFull.padding == 2 && defFull.mipLevel == 1 && defFull.density == 3 &&
                defFull.textures == listOf(Identifier.fromNamespaceAndPath("a", "b"))
        }

        // 无 textures 字段的定义（如 ui/icon 描述文件，纹理按 atlas 自身目录自动扫描）应可解析
        val jsonNoTextures = """{"max_size":0,"padding":1,"mip_level":0,"density":1}"""
        val defNoTextures = SokitsuAtlasDefinition.deserialization(JsonDialect.decode(jsonNoTextures).getOrThrow()).getOrThrow()
        check("atlas definition: textures optional (defaults to empty)") { defNoTextures.textures.isEmpty() }
    }

    // —— 2. keys 提取 ——

    private fun validateLayerExtractor() {
        val image = NativeImage(4, 4, true)
        val keyRed = Color.fromARGB(255, 0, 0)
        val green = Color.fromARGB(0, 255, 0)
        image.setPixel(0, 0, keyRed.argb)
        image.setPixel(1, 1, green.argb)
        image.setPixel(2, 2, keyRed.argb)

        val extracted = LayerExtractor.extract(image, listOf(keyRed))
        check("keys extraction: matching pixel kept with original color") { extracted.getPixel(0, 0) == keyRed.argb }
        check("keys extraction: matching pixel kept (second)") { extracted.getPixel(2, 2) == keyRed.argb }
        check("keys extraction: non-matching pixel set transparent") { extracted.getPixel(1, 1) == 0 }
        check("keys extraction: transparent pixel stays transparent") { extracted.getPixel(3, 3) == 0 }
        extracted.close()

        val whole = LayerExtractor.extract(image, emptyList())
        check("keys extraction: empty keys keeps whole image") {
            (0 until 4).all { x -> (0 until 4).all { y -> whole.getPixel(x, y) == image.getPixel(x, y) } }
        }
        whole.close()
        image.close()

        // —— 区域裁剪 ——
        val regionImage = NativeImage(6, 4, true)
        regionImage.setPixel(1, 1, keyRed.argb)   // 区域内命中
        regionImage.setPixel(2, 1, green.argb)    // 区域内未命中
        regionImage.setPixel(5, 3, keyRed.argb)   // 区域外（不在裁剪区域内）

        val cropped = LayerExtractor.extract(regionImage, listOf(keyRed), TextureRegion(0, 0, 3, 3))
        check("layer region: output size equals region size") { cropped.width == 3 && cropped.height == 3 }
        check("layer region: in-region matching pixel kept") { cropped.getPixel(1, 1) == keyRed.argb }
        check("layer region: in-region non-matching pixel transparent") { cropped.getPixel(2, 1) == 0 }
        check("layer region: out-of-region pixel excluded") { cropped.getPixel(2, 2) == 0 }
        check("layer region: offset region reads correct source") {
            val offset = LayerExtractor.extract(regionImage, listOf(keyRed), TextureRegion(1, 1, 4, 4))
            offset.getPixel(0, 0) == keyRed.argb && offset.getPixel(1, 0) == 0 && offset.getPixel(2, 2) == 0
        }
        cropped.close()
        regionImage.close()
    }

    private fun validateLayerRegionParse() {
        // TextureLayer 缺省 region = null（整图）
        val jsonLayer = """{"id":"base","keys":[],"color_level":null,"tint_mode":"Luminance","fill":"stretch"}"""
        val layer = TextureLayer.deserialization(JsonDialect.decode(jsonLayer).getOrThrow()).getOrThrow()
        check("layer region: default null (whole image)") { layer.region == null }

        // TextureLayer 带 region 解析（{u,v,width,height} 形式 → u1/v1 换算）
        val jsonLayerWithRegion =
            """{"id":"icon","keys":[],"color_level":null,"tint_mode":"Flat","fill":"stretch","region":{"u":1,"v":2,"width":3,"height":4}}"""
        val layerWithRegion = TextureLayer.deserialization(JsonDialect.decode(jsonLayerWithRegion).getOrThrow()).getOrThrow()
        check("layer region: parsed from {u,v,width,height}") {
            layerWithRegion.region == TextureRegion(1, 2, 4, 6)
        }

        // TextureLayer 带 region 解析（{u,v,u1,v1} 形式）
        val jsonLayerWithUvRegion =
            """{"id":"icon","keys":[],"color_level":null,"tint_mode":"Flat","fill":"stretch","region":{"u":1,"v":2,"u1":4,"v1":6}}"""
        val layerWithUvRegion = TextureLayer.deserialization(JsonDialect.decode(jsonLayerWithUvRegion).getOrThrow()).getOrThrow()
        check("layer region: parsed from {u,v,u1,v1}") {
            layerWithUvRegion.region == TextureRegion(1, 2, 4, 6)
        }
        check("layer region: width/height computed from u1/v1") {
            val region = TextureRegion(1, 2, 4, 6)
            region.width == 3 && region.height == 4
        }

        // colorLevel 缺省 null / 枚举值解析（原 ColorRef 已移除，改用 ColorLevel）
        val jsonLevel = """{"id":"icon","keys":[],"color_level":"Dark","tint_mode":"Luminance","fill":"stretch"}"""
        val layerLevel = TextureLayer.deserialization(JsonDialect.decode(jsonLevel).getOrThrow()).getOrThrow()
        check("layer color_level: null by default") { layer.colorLevel == null }
        check("layer color_level: enum parsed") { layerLevel.colorLevel == ColorLevel.Dark }
    }

    private fun validateNinePatchDisableSlice() {
        // NinePatch 缺省 disable_slice = 空（全开）
        val jsonDefault = """{"id":"base","keys":[],"color_level":null,"tint_mode":"Luminance","fill":{"mode":"ninepatch","border":{"left":4,"right":4,"top":4,"bottom":4}}}"""
        val layerDefault = TextureLayer.deserialization(JsonDialect.decode(jsonDefault).getOrThrow()).getOrThrow()
        check("ninepatch disable_slice: default empty (all enabled)") {
            val nine = layerDefault.fill as? TextureFill.NinePatch
            nine != null && nine.disableSlice.isEmpty()
        }

        // NinePatch 带 disable_slice 解析
        val jsonSlice = """{"id":"base","keys":[],"color_level":null,"tint_mode":"Luminance","fill":{"mode":"ninepatch","border":{"left":4,"right":4,"top":4,"bottom":4},"disable_slice":[0,4,5]}}"""
        val layerSlice = TextureLayer.deserialization(JsonDialect.decode(jsonSlice).getOrThrow()).getOrThrow()
        check("ninepatch disable_slice: parsed from json") {
            val nine = layerSlice.fill as? TextureFill.NinePatch
            nine != null && nine.disableSlice == listOf(0, 4, 5)
        }

        // SokitsuLayerSprite 携带 disabledSlices（由 fill 派生）
        val layerSprite = SokitsuLayerSprite(
            Identifier.fromNamespaceAndPath("ibukigourd", "sokitsu"),
            Identifier.fromNamespaceAndPath("ibukigourd", "ui/panel"),
            "base",
            0, 0, 64, 64, 512, 512, 1,
            fill = TextureFill.NinePatch(
                border = TextureFill.NinePatch.Border(4, 4, 4, 4),
                disableSlice = listOf(0, 4, 5)
            )
        )
        check("layer sprite: disabledSlices derived from fill") { layerSprite.disabledSlices == setOf(0, 4, 5) }
    }

    // —— 3. 缝合布局 ——

    private fun validateStitcher() {
        val stitcher = SokitsuStitcher(256, 1)
        val sizes = listOf(100 to 50, 60 to 60, 30 to 90)
        sizes.forEach { (w, h) -> stitcher.add(w, h) }
        val layout = stitcher.stitch().getOrThrow()

        check("stitcher: placed count == registered count") { layout.placed.size == 3 }
        check("stitcher: registration order preserved") {
            layout.placed.map { it.width to it.height } == sizes
        }
        check("stitcher: atlas size includes padding bounds") {
            layout.width >= 100 + 2 && layout.height >= 90 + 2 &&
                layout.width <= 256 && layout.height <= 256
        }
        check("stitcher: regions do not overlap") {
            layout.placed.indices.all { i ->
                (i + 1 until layout.placed.size).all { j ->
                    val a = layout.placed[i]
                    val b = layout.placed[j]
                    a.x + a.width <= b.x || b.x + b.width <= a.x || a.y + a.height <= b.y || b.y + b.height <= a.y
                }
            }
        }

        val overflow = SokitsuStitcher(16, 1)
        overflow.add(20, 20)
        check("stitcher: overflow fails") { overflow.stitch().isFailure }
    }

    // —— 4. SokitsuLayerSprite UV 与 SokitsuSprite 容器 ——

    private fun validateSprite() {
        val atlasId = Identifier.fromNamespaceAndPath("ibukigourd", "sokitsu")
        val textureId = Identifier.fromNamespaceAndPath("ibukigourd", "panel")
        val layer = SokitsuLayerSprite(atlasId, textureId, "base", 10, 20, 100, 50, 512, 512, 1)

        check("layer sprite: u0 = (x+padding)/w") { eq(layer.u0, 11f / 512f) }
        check("layer sprite: u1 = (x+padding+width)/w") { eq(layer.u1, (10 + 1 + 100).toFloat() / 512f) }
        check("layer sprite: v0 = (y+padding)/h") { eq(layer.v0, 21f / 512f) }
        check("layer sprite: v1 = (y+padding+height)/h") { eq(layer.v1, (20 + 1 + 50).toFloat() / 512f) }
        check("layer sprite: getU(0.5) midpoint interpolation") { eq(layer.getU(0.5f), (layer.u0 + layer.u1) / 2f) }
        check("layer sprite: getV(0.5) midpoint interpolation") { eq(layer.getV(0.5f), (layer.v0 + layer.v1) / 2f) }
        check("layer sprite: uvMapping content region") {
            val uv = layer.uvMapping()
            uv.uStart == 10 && uv.vStart == 20 && uv.uEnd == 110 && uv.vEnd == 70
        }
        check("layer sprite: missing fallback (all-zero UV)") {
            val missing = SokitsuLayerSprite(atlasId, atlasId, "<missing>", 0, 0, 0, 0, 1, 1, 0)
            missing.u0 == 0f && missing.u1 == 0f && missing.v0 == 0f && missing.v1 == 0f
        }

        // SokitsuSprite 容器：对应整个 SokitsuTexture，含全部图层，可按 layerId 查单层
        val layer2 = SokitsuLayerSprite(atlasId, textureId, "accent", 120, 20, 30, 30, 512, 512, 1)
        val sprite = SokitsuSprite(atlasId, textureId, listOf(layer, layer2))
        check("sprite container: holds all layers in order") {
            sprite.layers.map { it.layerId } == listOf("base", "accent")
        }
        check("sprite container: getLayer by id") {
            sprite.getLayer("accent") == layer2 && sprite.getLayer("nope") == null
        }
        check("sprite container: isEmpty on empty container") {
            SokitsuSprite(atlasId, textureId, emptyList()).isEmpty
        }
    }

    private fun eq(a: Float, b: Float): Boolean = abs(a - b) < 1e-6f

    private fun check(name: String, condition: () -> Boolean) {
        if (condition()) {
            passed++
            logger.info("✓ $name")
        } else {
            failed++
            logger.error("✗ $name")
        }
    }
}