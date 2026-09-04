package moe.forpleuvoir.ibukigourd.asetools

import java.io.ByteArrayInputStream
import java.util.zip.InflaterInputStream

// ---------------------------------------------------------------------------
// 常量（ASE 文件规范，Intel 小端）
// ---------------------------------------------------------------------------

const val MAGIC_HEADER = 0xA5E0
const val MAGIC_FRAME = 0xF1FA

const val CHUNK_OLD_PALETTE_4 = 0x0004
const val CHUNK_OLD_PALETTE_11 = 0x0011
const val CHUNK_LAYER = 0x2004
const val CHUNK_CEL = 0x2005
const val CHUNK_CEL_EXTRA = 0x2006
const val CHUNK_COLOR_PROFILE = 0x2007
const val CHUNK_EXTERNAL_FILES = 0x2008
const val CHUNK_MASK = 0x2016
const val CHUNK_PATH = 0x2017
const val CHUNK_TAGS = 0x2018
const val CHUNK_PALETTE = 0x2019
const val CHUNK_USER_DATA = 0x2020
const val CHUNK_SLICE = 0x2022
const val CHUNK_TILESET = 0x2023

// Layer flags（0x2004）
const val LAYER_FLAG_VISIBLE = 1
const val LAYER_FLAG_BACKGROUND = 8

// Layer type（0x2004）
const val LAYER_TYPE_IMAGE = 0
const val LAYER_TYPE_GROUP = 1
const val LAYER_TYPE_TILEMAP = 2

// Cel type（0x2005）
const val CEL_TYPE_RAW = 0
const val CEL_TYPE_LINKED = 1
const val CEL_TYPE_COMPRESSED_IMAGE = 2
const val CEL_TYPE_COMPRESSED_TILEMAP = 3

// Header flags
const val HEADER_FLAG_LAYER_OPACITY = 1
const val HEADER_FLAG_LAYERS_UUID = 4

const val DEPTH_RGBA = 32
const val DEPTH_GRAY = 16
const val DEPTH_INDEXED = 8

val BLEND_MODE_NAMES = mapOf(
    0 to "Normal", 1 to "Multiply", 2 to "Screen", 3 to "Overlay",
    4 to "Darken", 5 to "Lighten", 6 to "Color Dodge", 7 to "Color Burn",
    8 to "Hard Light", 9 to "Soft Light", 10 to "Difference", 11 to "Exclusion",
    12 to "Hue", 13 to "Saturation", 14 to "Color", 15 to "Luminosity",
    16 to "Addition", 17 to "Subtract", 18 to "Divide",
)

val CHUNK_NAMES = mapOf(
    CHUNK_OLD_PALETTE_4 to "old palette(4)",
    CHUNK_OLD_PALETTE_11 to "old palette(11)",
    CHUNK_LAYER to "layer",
    CHUNK_CEL to "cel",
    CHUNK_CEL_EXTRA to "cel extra",
    CHUNK_COLOR_PROFILE to "color profile",
    CHUNK_EXTERNAL_FILES to "external files",
    CHUNK_MASK to "mask (deprecated)",
    CHUNK_PATH to "path (unused)",
    CHUNK_TAGS to "tags",
    CHUNK_PALETTE to "palette",
    CHUNK_USER_DATA to "user data",
    CHUNK_SLICE to "slice",
    CHUNK_TILESET to "tileset",
)

// ---------------------------------------------------------------------------
// 二进制读取（小端）
// ---------------------------------------------------------------------------

class Reader(private val data: ByteArray) {
    var pos = 0
        private set

    fun read(n: Int): ByteArray {
        if (pos + n > data.size) {
            throw IllegalArgumentException(
                "Read out of bounds: need $n bytes, ${data.size - pos} remaining (offset 0x${pos.toString(16)})"
            )
        }
        val out = data.copyOfRange(pos, pos + n)
        pos += n
        return out
    }

    fun readRest(): ByteArray {
        val out = data.copyOfRange(pos, data.size)
        pos = data.size
        return out
    }

    fun u8(): Int = read(1)[0].toInt() and 0xFF

    fun u16(): Int {
        val b = read(2)
        return (b[0].toInt() and 0xFF) or ((b[1].toInt() and 0xFF) shl 8)
    }

    fun s16(): Int {
        val u = u16()
        return if (u >= 0x8000) u - 0x10000 else u
    }

    /** 32 位无符号（0..0xFFFFFFFF） */
    fun u32(): Long {
        val b = read(4)
        return (
            (b[0].toLong() and 0xFF)
                or ((b[1].toLong() and 0xFF) shl 8)
                or ((b[2].toLong() and 0xFF) shl 16)
                or ((b[3].toLong() and 0xFF) shl 24)
        )
    }

    fun s32(): Int = u32().toInt()

    fun u64(): Long {
        val b = read(8)
        var v = 0L
        for (i in 0..7) v = v or ((b[i].toLong() and 0xFF) shl (8 * i))
        return v
    }

    fun string(): String {
        val length = u16()
        return read(length).toString(Charsets.UTF_8)
    }
}

fun zlibDecompress(data: ByteArray): ByteArray =
    InflaterInputStream(ByteArrayInputStream(data)).use { it.readBytes() }

// ---------------------------------------------------------------------------
// 数据结构
// ---------------------------------------------------------------------------

data class AseHeader(
    val fileSize: Long,
    val frames: Int,
    val width: Int,
    val height: Int,
    val colorDepth: Int,
    val flags: Long,
    val speed: Int,
    val transparentIndex: Int,
    val numColors: Int,
    val pixelWidth: Int,
    val pixelHeight: Int,
    val gridX: Int,
    val gridY: Int,
    val gridWidth: Int,
    val gridHeight: Int,
) {
    val depthBpp: Int
        get() = when (colorDepth) {
            DEPTH_RGBA -> 4
            DEPTH_GRAY -> 2
            DEPTH_INDEXED -> 1
            else -> throw IllegalArgumentException("Unsupported color depth $colorDepth bpp (expected 8/16/32)")
        }

    val hasLayerOpacity: Boolean get() = flags and HEADER_FLAG_LAYER_OPACITY.toLong() != 0L
    val hasLayersUuid: Boolean get() = flags and HEADER_FLAG_LAYERS_UUID.toLong() != 0L
}

data class Layer(
    val index: Int,          // 出现顺序（规范 NOTE.2）
    val flags: Int,
    val layerType: Int,      // 0 image / 1 group / 2 tilemap
    val childLevel: Int,
    val blendMode: Int,
    val opacity: Int,
    val name: String,
    val tilesetIndex: Int? = null,
    val uuid: String? = null,
    /** 紧随该 layer chunk 的 user data（0x2020，含 Sokitsu 扩展属性） */
    var userData: AseUserData? = null,
) {
    val isBackground: Boolean get() = flags and LAYER_FLAG_BACKGROUND != 0
    val isVisible: Boolean get() = flags and LAYER_FLAG_VISIBLE != 0
}

data class Cel(
    val layerIndex: Int,
    val x: Int,
    val y: Int,
    val opacity: Int,
    val celType: Int,
    val zIndex: Int,
    val frameIndex: Int,
    val w: Int = 0,
    val h: Int = 0,
    val raw: ByteArray? = null,        // 已解压的原始像素
    val frameLink: Int? = null,        // type 1
    val tilemapInfo: String? = null,   // type 3（仅记录，不渲染）
    /** 紧随该 cel chunk 的 user data（0x2020） */
    var userData: AseUserData? = null,
)

data class PaletteEntry(
    val r: Int,
    val g: Int,
    val b: Int,
    val a: Int,
    val name: String = "",
)

data class Frame(
    val bytesInFrame: Long,
    val duration: Int,
    val cels: MutableList<Cel> = mutableListOf(),
)

data class AseSprite(
    val header: AseHeader,
    val frames: MutableList<Frame> = mutableListOf(),
    val layers: MutableList<Layer> = mutableListOf(),
    val palette: MutableList<PaletteEntry> = mutableListOf(),
    /** 0x2008 external files：Entry ID -> 名称（properties map 的 key 经它解析为 "extension:xxx"） */
    val externalFiles: MutableMap<Long, String> = mutableMapOf(),
) {
    fun layerByIndex(index: Int): Layer? = layers.getOrNull(index)
}

// ---------------------------------------------------------------------------
// User Data（0x2020）：text / color / extension properties
// ---------------------------------------------------------------------------

/**
 * 挂在某个对象（layer / cel / sprite...）上的 user data。
 *
 * [properties] 的键：properties map 的 key(DWORD) 经 external files 解析——
 * key==0 为 ""（用户属性），key!=0 为 external name（扩展属性，如
 * "extension:forpleuvoir/sokitsu"）。值为强类型二进制解析结果。
 */
data class AseUserData(
    val text: String? = null,
    /** ARGB（0xAARRGGBB），仅当 flags 带 color 位 */
    val color: Int? = null,
    val properties: Map<String, Map<String, PropValue>> = emptyMap(),
)

/** 0x2020 properties 的强类型值（对应 spec 的 WORD Type 0x0001~0x0011） */
sealed interface PropValue {

    data class Bool(val value: Boolean) : PropValue

    /** 全部整数类型统一为 Long 存储，调用方按需取 Int */
    data class Int(val value: Long) : PropValue

    /** FLOAT / DOUBLE 统一为 Double */
    data class Real(val value: Double) : PropValue

    data class Str(val value: String) : PropValue

    data class Point(val x: kotlin.Int, val y: kotlin.Int) : PropValue

    data class Size(val width: kotlin.Int, val height: kotlin.Int) : PropValue

    data class Rect(
        val x: kotlin.Int,
        val y: kotlin.Int,
        val width: kotlin.Int,
        val height: kotlin.Int,
    ) : PropValue

    /** 16.16 定点数，raw 为原始 int32 */
    data class Fixed(val raw: kotlin.Int) : PropValue

    /**
     * [elementType] 为元素统类型（0x0001~0x0011），
     * 0 表示每个元素前各带自己的类型（此时 [values] 已解出各自类型）
     */
    data class Vector(val elementType: kotlin.Int, val values: List<PropValue>) : PropValue

    /** 未知/未实现类型（宽容处理，不中断解析） */
    data class Unknown(val type: kotlin.Int) : PropValue
}