package moe.forpleuvoir.ibukigourd.asetools

import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * 布局探针：按 Sokitsu 导出规则处理一个 .ase：
 * - 跳过被插件禁用导出的图层（userData 中 sokitsu 元数据 enabled=false）
 * - 每个导出图层输出**整画布尺寸**（不裁剪），保证画布内位置/切片语义不破坏
 * - 图层横向排列（按图层顺序，画布宽度为单位从左到右拼接）
 * 输出：aseprite/build/test-output/{button-layout.json, button-hstrip.png}
 */
class LayoutProbeTest {


    /** 多 fixture 冒烟：button（验证基准）+ tab + drop_menu（多图层/结构更复杂） */
    private val fixtures = listOf("button", "tab", "drop_menu")

    private data class Tile(
        val index: Int,
        val name: String,
        val x: Int,
        val w: Int,
        val h: Int,
    )

    @Test
    fun `导出横向布局 JSON 与 PNG`() {
        for (name in fixtures) exportLayout(name)
    }

    private fun exportLayout(fixtureName: String) {
        val resourcePath = "/fixtures/$fixtureName.aseprite"
        val bytes = checkNotNull(javaClass.getResourceAsStream(resourcePath)) { "fixture not found: $resourcePath" }
            .readBytes()
        val sprite = AseParser.parse(bytes)
        val cw = sprite.header.width
        val ch = sprite.header.height
        val frameIndex = 0

        val layerCanvases = AseRenderer.renderFrame(sprite, frameIndex).second

        // 决定导出哪些层（Sokitsu 语义）
        val exportLayers = sprite.layers.filter { layer ->
            !isSokitsuExplicitlyDisabled(layer) &&
                layer.layerType != 1 && // 跳过组
                // 完全没有像素的隐藏层也跳过（如编辑辅助层）
                !(!layer.isVisible && celsInAnyFrame(sprite, layer.index) == 0)
        }

        val tiles = mutableListOf<Tile>()
        var cursorX = 0
        for (layer in exportLayers) {
            val rgba = layerCanvases[layer.index] ?: continue
            if (rgba.isEmpty()) continue
            tiles += Tile(
                index = layer.index,
                name = layer.name,
                x = cursorX,
                w = cw,
                h = ch,
            )
            cursorX += cw
        }
        val canvasW = cursorX
        val canvasH = ch

        val outDir: Path = Paths.get("build", "test-output").toAbsolutePath()
        Files.createDirectories(outDir)

        // 1) JSON 描述（整画布段：x = n * canvasWidth）
        val json = buildString {
            appendLine("{")
            appendLine("  \"file\": \"$resourcePath\",")
            appendLine("  \"sprite\": {")
            appendLine("    \"width\": $cw,")
            appendLine("    \"height\": $ch,")
            appendLine("    \"frame\": $frameIndex,")
            appendLine("    \"colorDepth\": ${sprite.header.colorDepth}")
            appendLine("  },")
            appendLine("  \"externalFiles\": [" + sprite.externalFiles.map { (k, v) -> "{\"id\":$k,\"name\":\"$v\"}" }.joinToString(", ") + "],")
            appendLine("  \"rule\": \"图层横向排列：仅导出层，每段为整画布尺寸（不裁剪），按图层顺序拼接\",")
            appendLine("  \"canvas\": { \"width\": $canvasW, \"height\": $canvasH },")
            appendLine("  \"tiles\": [")
            tiles.forEachIndexed { i, t ->
                val comma = if (i == tiles.lastIndex) "" else ","
                appendLine(
                    "    { \"index\": ${t.index}, \"name\": \"${t.name}\", \"x\": ${t.x}, \"y\": 0, \"w\": ${t.w}, \"h\": ${t.h} }$comma"
                )
            }
            appendLine("  ]")
            appendLine("  ,\"skipped\": [")
            val skipped = sprite.layers.filter { layer -> exportLayers.none { it.index == layer.index } }
            skipped.forEachIndexed { i, s ->
                val comma = if (i == skipped.lastIndex) "" else ","
                appendLine("    { \"index\": ${s.index}, \"name\": \"${s.name}\" }$comma")
            }
            appendLine("  ]")
            appendLine("  ,\"meta\": [")
            sprite.layers.forEachIndexed { i, l ->
                val comma = if (i == sprite.layers.lastIndex) "" else ","
                val sokitsu = l.userData?.properties?.entries?.firstOrNull { (k, _) -> k.contains("sokitsu", true) }
                val keysJson = (l.userData?.properties?.keys ?: emptySet()).joinToString { "\"$it\"" }
                appendLine(
                    "    { \"name\": \"${l.name}\", \"keys\": [$keysJson], " +
                        "\"sokitsuEnabled\": ${(sokitsu?.value?.get("enabled") as? PropValue.Bool)?.value} }$comma"
                )
            }
            appendLine("  ]")
            append("}")
        }
        Files.writeString(outDir.resolve("$fixtureName-layout.json"), json)

        // 2) 合成 PNG：每段 = 该层独立画布的整画布拷贝
        val image = BufferedImage(canvasW, canvasH, BufferedImage.TYPE_INT_ARGB)
        for (tile in tiles) {
            val rgba = layerCanvases[tile.index] ?: continue
            for (y in 0 until ch) {
                for (x in 0 until cw) {
                    val off = (y * cw + x) * 4
                    val r = rgba[off].toInt() and 0xFF
                    val g = rgba[off + 1].toInt() and 0xFF
                    val b = rgba[off + 2].toInt() and 0xFF
                    val a = rgba[off + 3].toInt() and 0xFF
                    image.setRGB(tile.x + x, y, (a shl 24) or (r shl 16) or (g shl 8) or b)
                }
            }
        }
        val pngPath = outDir.resolve("$fixtureName-hstrip.png")
        ImageIO.write(image, "png", pngPath.toFile())

        println("json -> $outDir/$fixtureName-layout.json")
        println("png  -> $pngPath")
    }

    /** 该层是否被 Sokitsu 插件显式禁用导出（userData 元数据 enabled=false）。 */
    private fun isSokitsuExplicitlyDisabled(layer: Layer): Boolean {
        val props = layer.userData?.properties ?: return false
        val sokitsu = props.entries.firstOrNull { (key, _) -> key.contains("sokitsu", ignoreCase = true) }?.value ?: return false
        val enabled = sokitsu["enabled"] as? PropValue.Bool ?: return false
        return !enabled.value
    }

    private fun celsInAnyFrame(sprite: AseSprite, layerIndex: Int): Int =
        sprite.frames.sumOf { f -> f.cels.count { it.layerIndex == layerIndex } }
}
