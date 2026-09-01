package moe.forpleuvoir.ibukigourd.test

import com.mojang.blaze3d.platform.NativeImage
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.LayerExtractor
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuAtlasDefinition
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

    // —— 4. SokitsuSprite UV ——

    private fun validateSprite() {
        val atlasId = Identifier.fromNamespaceAndPath("ibukigourd", "sokitsu")
        val textureId = Identifier.fromNamespaceAndPath("ibukigourd", "panel")
        val sprite = SokitsuSprite(atlasId, textureId, "base", 10, 20, 100, 50, 512, 512, 1)

        check("sprite: u0 = (x+padding)/w") { eq(sprite.u0, 11f / 512f) }
        check("sprite: u1 = (x+padding+width)/w") { eq(sprite.u1, (10 + 1 + 100).toFloat() / 512f) }
        check("sprite: v0 = (y+padding)/h") { eq(sprite.v0, 21f / 512f) }
        check("sprite: v1 = (y+padding+height)/h") { eq(sprite.v1, (20 + 1 + 50).toFloat() / 512f) }
        check("sprite: getU(0.5) midpoint interpolation") { eq(sprite.getU(0.5f), (sprite.u0 + sprite.u1) / 2f) }
        check("sprite: getV(0.5) midpoint interpolation") { eq(sprite.getV(0.5f), (sprite.v0 + sprite.v1) / 2f) }
        check("sprite: uvMapping content region") {
            val uv = sprite.uvMapping()
            uv.uStart == 10 && uv.vStart == 20 && uv.uEnd == 110 && uv.vEnd == 70
        }
        check("sprite: missing fallback (all-zero UV)") {
            val missing = SokitsuSprite(atlasId, atlasId, "<missing>", 0, 0, 0, 0, 1, 1, 0)
            missing.u0 == 0f && missing.u1 == 0f && missing.v0 == 0f && missing.v1 == 0f
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