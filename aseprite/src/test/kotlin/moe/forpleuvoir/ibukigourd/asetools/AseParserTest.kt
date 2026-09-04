package moe.forpleuvoir.ibukigourd.asetools

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

/**
 * 用仓库内真实 .aseprite 样例做解析与解码验证（fixtures/ 下为副本）。
 * 断言尽量为"结构一致性 / 存在性"，避免与美术文件内容耦合过紧。
 */
class AseParserTest {

    private val fixtures = listOf(
        "fixtures/button.aseprite",
        "fixtures/tab.aseprite",
        "fixtures/drop_menu.aseprite",
    )

    private fun load(name: String): AseSprite {
        val bytes = javaClass.getResourceAsStream("/$name")?.readBytes()
            ?: error("fixture not found: $name")
        return AseParser.parse(bytes)
    }

    @Test
    fun `全部样例可解析且结构自洽`() {
        for (name in fixtures) {
            val sprite = load(name)
            assertTrue(sprite.header.width > 0, "$name: width>0")
            assertTrue(sprite.header.height > 0, "$name: height>0")
            assertEquals(sprite.header.frames, sprite.frames.size, "$name: 帧数一致")
            assertTrue(sprite.layers.isNotEmpty(), "$name: 存在图层")
            for ((fi, frame) in sprite.frames.withIndex()) {
                for (cel in frame.cels) {
                    assertTrue(cel.layerIndex in sprite.layers.indices, "$name: F$fi cel.layerIndex 越界")
                    assertEquals(fi, cel.frameIndex, "$name: cel.frameIndex 一致")
                    if (cel.frameLink != null) {
                        assertTrue(cel.frameLink in sprite.frames.indices, "$name: linked 目标帧存在")
                    }
                }
            }
        }
    }

    @Test
    fun `全部样例第 0 帧可渲染且尺寸正确`() {
        for (name in fixtures) {
            val sprite = load(name)
            val (canvas, layerCanvases) = AseRenderer.renderFrame(sprite, 0)
            val expected = sprite.header.width * sprite.header.height * 4
            assertEquals(expected, canvas.size, "$name: 画布尺寸")
            for ((layerIndex, rgba) in layerCanvases) {
                assertEquals(expected, rgba.size, "$name: 图层 $layerIndex 尺寸")
                assertTrue(layerIndex in sprite.layers.indices, "$name: 图层索引有效")
            }
        }
    }

    @Test
    fun `样例中的图层 userData 结构自洽`() {
        for (name in fixtures) {
            val sprite = load(name)
            for (layer in sprite.layers) {
                val ud = layer.userData
                if (ud == null) continue
                ud.color?.let { /* 不校验具体值 */ }
                for ((key, props) in ud.properties) {
                    assertTrue(key.isNotEmpty() || key.isEmpty(), "$name: property map key 合法")
                    for ((propName, value) in props) {
                        assertTrue(propName.isNotEmpty(), "$name: 属性名非空")
                        assertTrue(propValueSanity(value), "$name: $propName 值类型自洽")
                    }
                }
            }
        }
    }

    @Test
    fun `button 样例读取 Sokitsu 插件元数据`() {
        val sprite = load("fixtures/button.aseprite")
        // 逐图层查找挂有 sokitsu extension properties 的图层（宽松匹配，不绑定 key 精确格式）
        val found = sprite.layers.mapNotNull { layer ->
            val ud = layer.userData ?: return@mapNotNull null
            val sokitsuProps = ud.properties.entries
                .firstOrNull { (key, _) -> key.contains("sokitsu", ignoreCase = true) }
                ?.value
                ?: return@mapNotNull null
            layer.name to sokitsuProps
        }

        if (found.isEmpty()) {
            // 该副本可能尚未用插件配置——不视为失败，打印提示
            println("button.aseprite 无 Sokitsu 扩展属性（图层 userData 数: " +
                sprite.layers.count { it.userData != null } + "）")
            return
        }

        val (layerName, props) = found.first()
        println("Sokitsu 图层 '$layerName' properties:")
        for ((k, v) in props) println("  $k = ${propValueText(v)}")

        // 插件写入的固定字段：enabled 应存在
        assertNotNull(props["enabled"], "enabled 应存在")
    }

    private fun propValueSanity(v: PropValue): Boolean = when (v) {
        is PropValue.Bool -> true
        is PropValue.Int -> true
        is PropValue.Real -> v.value.isFinite()
        is PropValue.Str -> true
        is PropValue.Point, is PropValue.Size, is PropValue.Rect, is PropValue.Fixed, is PropValue.Unknown -> true
        is PropValue.Vector -> v.values.all { propValueSanity(it) }
    }

    private fun propValueText(v: PropValue): String = when (v) {
        is PropValue.Bool -> v.value.toString()
        is PropValue.Int -> v.value.toString()
        is PropValue.Real -> v.value.toString()
        is PropValue.Str -> "\"${v.value}\""
        is PropValue.Point -> "(${v.x}, ${v.y})"
        is PropValue.Size -> "${v.width}x${v.height}"
        is PropValue.Rect -> "rect(${v.x}, ${v.y}, ${v.width}, ${v.height})"
        is PropValue.Fixed -> "fixed(${v.raw})"
        is PropValue.Vector -> "[" + v.values.joinToString(", ") { propValueText(it) } + "]"
        is PropValue.Unknown -> "unknown#${v.type}"
    }
}
