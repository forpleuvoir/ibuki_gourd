package moe.forpleuvoir.ibukigourd.asetools

import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Sokitsu 导出探针：
 * - 遍历 fixtures 下**所有** .aseprite，按 Sokitsu 规则导出图层（跳过禁用/组/空隐藏层），
 *   每层输出整画布尺寸（不裁剪），先做单文件横向排列 JSON/PNG；
 * - 另模拟一次**图集合成**：把全部文件的导出层切片按 shelf 算法打包成一张 atlas 图 +
 *   布局 JSON（每切片记录 file/layer/坐标/尺寸），用于后续对接运行时图集加载前的正确性验证。
 * 输出目录：aseprite/build/test-output/
 */
class LayoutProbeTest {

    private val fixturesDir = "/fixtures"

    /** 单个导出图层：整画布 RGBA + 归属信息 */
    private data class Slice(
        val rgba: ByteArray,
        val w: Int,
        val h: Int,
        val source: String,      // 源文件（不含扩展名）
        val layerIndex: Int,
        val layerName: String,
    )

    /** 解析结果：源文件尺寸 + 导出层切片 + 跳过层 */
    private data class ExportResult(
        val source: String,
        val width: Int,
        val height: Int,
        val slices: List<Slice>,
        val skipped: List<String>,
    )

    private val outDir: Path = Paths.get("build", "test-output").toAbsolutePath()

    /** 1) 遍历 fixtures 全部 .aseprite，逐个做横向导出 */
    @Test
    fun `导出全部 fixtures 的横向布局`() {
        Files.createDirectories(outDir)
        val fixtureFiles = fixtureFiles()
        check(fixtureFiles.isNotEmpty()) { "fixtures 目录为空: $fixturesDir" }
        for (path in fixtureFiles) {
            val name = path.fileName.toString().removeSuffix(".aseprite")
            val result = exportLayers(name)
            exportHorizontalStrip(result)
        }
        println("已导出 ${fixtureFiles.size} 个 fixture 的横向布局")
    }

    /** 2) 模拟图集合成：全部导出层切片通用 shelf 打包，目标近正方形贴图 */
    @Test
    fun `模拟合成 atlas 图集`() {
        Files.createDirectories(outDir)
        val slices = fixtureFiles()
            .flatMap { exportLayers(it.fileName.toString().removeSuffix(".aseprite")).slices }
        val (atlasW, atlasH, placed) = packNearSquare(slices)

        val image = BufferedImage(atlasW, atlasH, BufferedImage.TYPE_INT_ARGB)
        val json = buildString {
            appendLine("{")
            appendLine("  \"atlas\": { \"width\": $atlasW, \"height\": $atlasH },")
            appendLine("  \"packer\": \"通用 shelf（按高度降序）；宽度自动搜索使贴图尽量接近正方形\",")
            appendLine("  \"slices\": [")
            placed.forEachIndexed { i, (slice, pos) ->
                val comma = if (i == placed.lastIndex) "" else ","
                drawRgba(image, slice.rgba, slice.w, slice.h, pos.first, pos.second)
                appendLine(
                    "    { \"file\": \"${slice.source}\", \"layerIndex\": ${slice.layerIndex}, " +
                        "\"layer\": \"${slice.layerName}\", \"x\": ${pos.first}, \"y\": ${pos.second}, " +
                        "\"w\": ${slice.w}, \"h\": ${slice.h} }$comma"
                )
            }
            appendLine("  ]")
            append("}")
        }
        val jsonPath = outDir.resolve("fixtures-atlas.json")
        val pngPath = outDir.resolve("fixtures-atlas.png")
        Files.writeString(jsonPath, json)
        ImageIO.write(image, "png", pngPath.toFile())
        println("atlas json -> $jsonPath (${placed.size} slices, ${atlasW}x$atlasH)")
        println("atlas png  -> $pngPath")
    }

    /**
     * 通用 shelf 打包：切片按高度降序逐行放置（行放不下即换行，无"源文件边界"概念），
     * 宽度从下界（最宽切片 / sqrt(总面积)）向上倍增找到可行解，再在小范围内搜索
     * 面积最小的近方形排布。将来真实图集打包可直接复用此算法。
     */
    private fun packNearSquare(slices: List<Slice>): Triple<Int, Int, List<Pair<Slice, Pair<Int, Int>>>> {
        if (slices.isEmpty()) return Triple(0, 0, emptyList())
        val totalArea = slices.sumOf { it.w.toLong() * it.h.toLong() }
        val sorted = slices.sortedByDescending { it.h }

        fun placeIn(w: Int): Pair<Int, List<Pair<Slice, Pair<Int, Int>>>> {
            var rowY = 0
            var cursorX = 0
            var rowH = 0
            var maxY = 0
            val out = mutableListOf<Pair<Slice, Pair<Int, Int>>>()
            for (slice in sorted) {
                if (cursorX > 0 && cursorX + slice.w > w) {
                    cursorX = 0
                    rowY += rowH
                    rowH = 0
                }
                out += slice to (cursorX to rowY)
                cursorX += slice.w
                rowH = maxOf(rowH, slice.h)
                maxY = maxOf(maxY, rowY + rowH)
            }
            return maxY to out
        }

        val minW = maxOf(sorted.maxOf { it.w }, kotlin.math.ceil(kotlin.math.sqrt(totalArea.toDouble())).toInt())
        var lo = minW
        var hi = minW
        while (placeIn(hi).first > hi) hi *= 2 // 倍增到第一组可行解

        // 在 [hi/2, hi] 内线性采样，取面积最小且近方的可行解
        var bestW = hi
        var bestH = placeIn(hi).first
        var bestPlace = placeIn(hi).second
        val samples = 12
        for (i in 0..samples) {
            val w = lo + ((hi - lo) * i) / samples
            val (h, place) = placeIn(w)
            if (h <= w && (w.toLong() * h < bestW.toLong() * bestH)) {
                bestW = w
                bestH = h
                bestPlace = place
            }
        }
        return Triple(bestW, bestH, bestPlace)
    }

    // ------------------------------------------------------------------
    // 内部：解析 / 导出 / 绘制
    // ------------------------------------------------------------------

    private fun fixtureFiles(): List<Path> {
        val uri = checkNotNull(javaClass.getResource(fixturesDir)) { "fixtures dir not found: $fixturesDir" }.toURI()
        return Files.list(Paths.get(uri)).use { stream ->
            stream.filter { it.fileName.toString().endsWith(".aseprite") }.sorted().toList()
        }
    }

    /** 解析单个 .ase，按 Sokitsu 规则取出导出层切片（整画布）。 */
    private fun exportLayers(source: String): ExportResult {
        val bytes = checkNotNull(javaClass.getResourceAsStream("$fixturesDir/$source.aseprite")) {
            "fixture not found: $source"
        }.readBytes()
        val sprite = AseParser.parse(bytes)
        val cw = sprite.header.width
        val ch = sprite.header.height
        val layerCanvases = AseRenderer.renderFrame(sprite, 0).second

        val exportLayers = sprite.layers.filter { layer ->
            !isSokitsuExplicitlyDisabled(layer) &&
                layer.layerType != 1 && // 组
                !(!layer.isVisible && celsInAnyFrame(sprite, layer.index) == 0) // 空隐藏辅助层
        }

        val slices = mutableListOf<Slice>()
        for (layer in exportLayers) {
            val rgba = layerCanvases[layer.index] ?: continue
            if (rgba.isEmpty()) continue
            slices += Slice(rgba, cw, ch, source, layer.index, layer.name)
        }
        val skipped = sprite.layers.filter { l -> exportLayers.none { it.index == l.index } }.map { it.name }
        return ExportResult(source, cw, ch, slices, skipped)
    }

    /** 单文件横向排列导出（JSON + PNG），便于目检每层内容。 */
    private fun exportHorizontalStrip(result: ExportResult) {
        val canvasW = result.width * result.slices.size
        val canvasH = result.height
        val json = buildString {
            appendLine("{")
            appendLine("  \"file\": \"$fixturesDir/${result.source}.aseprite\",")
            appendLine("  \"canvas\": { \"width\": $canvasW, \"height\": $canvasH },")
            appendLine("  \"tiles\": [")
            result.slices.forEachIndexed { i, s ->
                val comma = if (i == result.slices.lastIndex) "" else ","
                appendLine(
                    "    { \"layerIndex\": ${s.layerIndex}, \"layer\": \"${s.layerName}\", " +
                        "\"x\": ${i * result.width}, \"y\": 0, \"w\": ${s.w}, \"h\": ${s.h} }$comma"
                )
            }
            appendLine("  ],")
            appendLine("  \"skipped\": [" + result.skipped.joinToString { "\"$it\"" } + "]")
            append("}")
        }
        val image = BufferedImage(canvasW, canvasH, BufferedImage.TYPE_INT_ARGB)
        result.slices.forEachIndexed { i, s ->
            drawRgba(image, s.rgba, s.w, s.h, i * result.width, 0)
        }
        Files.writeString(outDir.resolve("${result.source}-layout.json"), json)
        ImageIO.write(image, "png", outDir.resolve("${result.source}-hstrip.png").toFile())
    }

    /** 把整画布 RGBA 拷贝到目标图 (dx, dy) 处。 */
    private fun drawRgba(target: BufferedImage, rgba: ByteArray, w: Int, h: Int, dx: Int, dy: Int) {
        for (y in 0 until h) {
            for (x in 0 until w) {
                val off = (y * w + x) * 4
                val r = rgba[off].toInt() and 0xFF
                val g = rgba[off + 1].toInt() and 0xFF
                val b = rgba[off + 2].toInt() and 0xFF
                val a = rgba[off + 3].toInt() and 0xFF
                target.setRGB(dx + x, dy + y, (a shl 24) or (r shl 16) or (g shl 8) or b)
            }
        }
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
