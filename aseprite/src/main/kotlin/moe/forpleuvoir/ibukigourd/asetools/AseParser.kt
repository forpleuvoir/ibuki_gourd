package moe.forpleuvoir.ibukigourd.asetools

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * ASE 文件解析：128 字节 header + 每帧 16 字节帧头 + chunk 流。
 */
object AseParser {

    fun parse(data: ByteArray): AseSprite {
        if (data.size < 128) throw IllegalArgumentException("File too short, not a valid ASE file")
        val r = Reader(data)
        val fileSize = r.u32()
        if (r.u16() != MAGIC_HEADER) throw IllegalArgumentException("Bad magic number, not an ASE file")
        val frames = r.u16()
        val width = r.u16()
        val height = r.u16()
        val colorDepth = r.u16()
        val flags = r.u32()
        val speed = r.u16()
        r.u32() // set 0
        r.u32() // set 0
        val transparentIndex = r.u8()
        r.read(3) // ignore
        val numColors = r.u16()
        val pixelWidth = r.u8()
        val pixelHeight = r.u8()
        val gridX = r.s16()
        val gridY = r.s16()
        val gridWidth = r.u16()
        val gridHeight = r.u16()
        r.read(84) // future

        val header = AseHeader(
            fileSize = fileSize,
            frames = frames,
            width = width,
            height = height,
            colorDepth = colorDepth,
            flags = flags,
            speed = speed,
            transparentIndex = transparentIndex,
            numColors = if (numColors == 0) 256 else numColors,
            pixelWidth = pixelWidth,
            pixelHeight = pixelHeight,
            gridX = gridX,
            gridY = gridY,
            gridWidth = gridWidth,
            gridHeight = gridHeight,
        )
        val sprite = AseSprite(header)
        var hasNewPalette = false
        // user data（0x2020）归属状态机：
        // lastObject 记录最近读取的"可挂载 user data 的对象"；tags 之后的 userData
        // 属于 tag（我们不关心），按 tag 计数连续丢弃，避免错挂到 layer/cel。
        var lastObject = OBJ_OTHER
        var pendingUserDataToSkip = 0L

        for (fi in 0 until frames) {
            val frameBytes = r.u32()
            if (r.u16() != MAGIC_FRAME) throw IllegalArgumentException("Frame $fi: bad frame magic number")
            val oldChunks = r.u16()
            val duration = r.u16()
            r.read(2) // future
            val newChunks = r.u32()
            val numChunks = if (newChunks != 0L) newChunks else oldChunks.toLong()

            val frame = Frame(bytesInFrame = frameBytes, duration = duration)
            sprite.frames.add(frame)

            repeat(numChunks.toInt()) {
                val chunkSize = r.u32()
                val chunkType = r.u16()
                val chunkData = r.read(chunkSize.toInt() - 6)
                val cr = Reader(chunkData)
                try {
                    when (chunkType) {
                        CHUNK_OLD_PALETTE_4 -> if (!hasNewPalette) parseOldPalette(sprite, cr, 255)
                        CHUNK_OLD_PALETTE_11 -> if (!hasNewPalette) parseOldPalette(sprite, cr, 63)
                        CHUNK_LAYER -> {
                            parseLayer(sprite, cr)
                            lastObject = OBJ_LAYER
                        }
                        CHUNK_CEL -> {
                            parseCel(sprite, cr, fi)
                            lastObject = OBJ_CEL
                        }
                        CHUNK_PALETTE -> {
                            parsePalette(sprite, cr)
                            hasNewPalette = true
                            // 1.3：sprite 自身 user data 紧随首帧 palette，丢弃（不在 layer/cel 上挂载）
                            lastObject = OBJ_OTHER
                        }
                        CHUNK_EXTERNAL_FILES -> parseExternalFiles(sprite, cr)
                        CHUNK_TAGS -> {
                            // 只取 tag 数量：其后跟等量的 user data（属于各 tag），需跳过
                            pendingUserDataToSkip = cr.u16().toLong()
                        }
                        CHUNK_USER_DATA -> {
                            if (pendingUserDataToSkip > 0) {
                                pendingUserDataToSkip--
                            } else {
                                when (lastObject) {
                                    OBJ_LAYER -> sprite.layers.last().userData = parseUserData(sprite, cr)
                                    OBJ_CEL -> sprite.frames[fi].cels.last().userData = parseUserData(sprite, cr)
                                    else -> Unit
                                }
                            }
                        }
                        CHUNK_SLICE, CHUNK_TILESET -> lastObject = OBJ_OTHER
                        // 其余 chunk（cel extra / color profile / mask / path 等）不影响解析目标，直接跳过
                    }
                } catch (e: Exception) {
                    throw IllegalArgumentException(
                        "Frame $fi: failed to parse chunk 0x${chunkType.toString(16).padStart(4, '0')} " +
                            "(${CHUNK_NAMES[chunkType] ?: "?"}): ${e.message}", e
                    )
                }
            }
        }
        return sprite
    }

    private const val OBJ_OTHER = 0
    private const val OBJ_LAYER = 1
    private const val OBJ_CEL = 2

    // property 值类型（0x2020 properties 的 WORD Type）
    private const val TYPE_BOOL = 0x0001
    private const val TYPE_INT8 = 0x0002
    private const val TYPE_UINT8 = 0x0003
    private const val TYPE_INT16 = 0x0004
    private const val TYPE_UINT16 = 0x0005
    private const val TYPE_INT32 = 0x0006
    private const val TYPE_UINT32 = 0x0007
    private const val TYPE_INT64 = 0x0008
    private const val TYPE_UINT64 = 0x0009
    private const val TYPE_FIXED = 0x000A
    private const val TYPE_FLOAT = 0x000B
    private const val TYPE_DOUBLE = 0x000C
    private const val TYPE_STRING = 0x000D
    private const val TYPE_POINT = 0x000E
    private const val TYPE_SIZE = 0x000F
    private const val TYPE_RECT = 0x0010
    private const val TYPE_VECTOR = 0x0011

    private fun parseOldPalette(sprite: AseSprite, r: Reader, maxRgb: Int) {
        val packets = r.u16()
        val entries = mutableListOf<PaletteEntry>()
        var skip = 0
        repeat(packets) {
            skip += r.u8() // 相对上一个包跳过 N 个条目
            while (entries.size < skip) entries.add(PaletteEntry(0, 0, 0, 0))
            var count = r.u8()
            if (count == 0) count = 256
            repeat(count) {
                val rr = r.u8() * (255 / maxRgb)
                val gg = r.u8() * (255 / maxRgb)
                val bb = r.u8() * (255 / maxRgb)
                entries.add(PaletteEntry(rr, gg, bb, 255))
            }
            skip += count
        }
        sprite.palette.clear()
        sprite.palette.addAll(entries)
    }

    private fun parseLayer(sprite: AseSprite, r: Reader) {
        val flags = r.u16()
        val layerType = r.u16()
        val childLevel = r.u16()
        r.u16() // default width（忽略）
        r.u16() // default height（忽略）
        val blendMode = r.u16()
        val opacity = r.u8()
        r.read(3) // future
        val name = r.string()
        var tilesetIndex: Int? = null
        var uuid: String? = null
        if (layerType == LAYER_TYPE_TILEMAP) tilesetIndex = r.u32().toInt()
        if (sprite.header.hasLayersUuid) uuid = r.read(16).joinToString("") { "%02x".format(it) }
        sprite.layers.add(
            Layer(
                index = sprite.layers.size,
                flags = flags,
                layerType = layerType,
                childLevel = childLevel,
                blendMode = blendMode,
                opacity = opacity,
                name = name,
                tilesetIndex = tilesetIndex,
                uuid = uuid,
            )
        )
    }

    private fun parseCel(sprite: AseSprite, r: Reader, frameIndex: Int) {
        val layerIndex = r.u16()
        val x = r.s16()
        val y = r.s16()
        val opacity = r.u8()
        val celType = r.u16()
        val zIndex = r.s16()
        r.read(5) // future

        val w: Int
        val h: Int
        var raw: ByteArray? = null
        var frameLink: Int? = null
        var tilemapInfo: String? = null

        when (celType) {
            CEL_TYPE_RAW -> {
                w = r.u16()
                h = r.u16()
                raw = r.read(w * h * sprite.header.depthBpp)
            }
            CEL_TYPE_LINKED -> {
                w = 0; h = 0
                frameLink = r.u16()
            }
            CEL_TYPE_COMPRESSED_IMAGE -> {
                w = r.u16()
                h = r.u16()
                val compressed = r.readRest()
                raw = zlibDecompress(compressed)
                val expected = w * h * sprite.header.depthBpp
                if (raw.size != expected) {
                    throw IllegalArgumentException(
                        "Cel decompressed size mismatch: expected $expected bytes, got ${raw.size} " +
                            "(layer=$layerIndex, frame=$frameIndex)"
                    )
                }
            }
            CEL_TYPE_COMPRESSED_TILEMAP -> {
                w = r.u16()
                h = r.u16()
                val bitsPerTile = r.u16()
                val idMask = r.u32()
                val xFlip = r.u32()
                val yFlip = r.u32()
                val dFlip = r.u32()
                r.read(10)
                tilemapInfo =
                    "${w}x$h tiles, ${bitsPerTile}bit, id=0x${idMask.toString(16)}, " +
                        "xflip=0x${xFlip.toString(16)}, yflip=0x${yFlip.toString(16)}, " +
                        "dflip=0x${dFlip.toString(16)}"
            }
            else -> throw IllegalArgumentException(
                "Unknown cel type $celType (layer=$layerIndex, frame=$frameIndex)"
            )
        }

        sprite.frames[frameIndex].cels.add(
            Cel(
                layerIndex = layerIndex,
                x = x,
                y = y,
                opacity = opacity,
                celType = celType,
                zIndex = zIndex,
                frameIndex = frameIndex,
                w = w,
                h = h,
                raw = raw,
                frameLink = frameLink,
                tilemapInfo = tilemapInfo,
            )
        )
    }

    private fun parsePalette(sprite: AseSprite, r: Reader) {
        val newSize = r.u32().toInt()
        val first = r.u32().toInt()
        val last = r.u32().toInt()
        r.read(8) // future

        while (sprite.palette.size < newSize) {
            sprite.palette.add(PaletteEntry(0, 0, 0, 0))
        }
        if (sprite.palette.size > newSize) {
            sprite.palette.subList(newSize, sprite.palette.size).clear()
        }

        for (i in first..last) {
            val entryFlags = r.u16()
            val rr = r.u8()
            val gg = r.u8()
            val bb = r.u8()
            val aa = r.u8()
            var name = ""
            if (entryFlags and 1 != 0) name = r.string()
            if (i < sprite.palette.size) {
                sprite.palette[i] = PaletteEntry(rr, gg, bb, aa, name)
            }
        }
    }

    // ------------------------------------------------------------------
    // 0x2008 External Files
    // ------------------------------------------------------------------

    private fun parseExternalFiles(sprite: AseSprite, r: Reader) {
        val count = r.u32()
        r.read(8)   // chunk 级 reserved（规范：numEntries 后跟 BYTE[8] 保留）
        repeat(count.toInt()) {
            val id = r.u32()
            r.u8()      // type（0=external palette / 1=tileset / 2=extension）
            r.read(7)   // entry 级 reserved
            val name = r.string()
            sprite.externalFiles[id] = name
        }
    }

    // ------------------------------------------------------------------
    // 0x2020 User Data（text / color / properties）
    // ------------------------------------------------------------------

    private fun parseUserData(sprite: AseSprite, r: Reader): AseUserData {
        val flags = r.u32()
        var text: String? = null
        var color: Int? = null

        if (flags and 1L != 0L) text = r.string()
        if (flags and 2L != 0L) {
            val rr = r.u8(); val gg = r.u8(); val bb = r.u8(); val aa = r.u8()
            color = (aa shl 24) or (rr shl 16) or (gg shl 8) or bb
        }

        if (flags and 4L == 0L) return AseUserData(text = text, color = color)

        // properties：size(DWORD) 含本字段与 map 数；此处只关心数据布局
        r.u32() // size in bytes
        val mapCount = r.u32()
        val maps = mutableMapOf<String, Map<String, PropValue>>()
        repeat(mapCount.toInt()) {
            val keyId = r.u32()
            val propCount = r.u32()
            val props = mutableMapOf<String, PropValue>()
            repeat(propCount.toInt()) {
                val name = r.string()
                val type = r.u16()
                props[name] = parsePropValue(type, r)
            }
            val keyName = if (keyId == 0L) "" else (sprite.externalFiles[keyId] ?: "ext#$keyId")
            maps[keyName] = props
        }
        return AseUserData(text = text, color = color, properties = maps)
    }

    private fun parsePropValue(type: Int, r: Reader): PropValue {
        return when (type) {
            TYPE_BOOL -> PropValue.Bool(r.u8() != 0)
            TYPE_INT8 -> PropValue.Int(r.u8().toByte().toLong())
            TYPE_UINT8 -> PropValue.Int(r.u8().toLong())
            TYPE_INT16 -> PropValue.Int(r.s16().toLong())
            TYPE_UINT16 -> PropValue.Int(r.u16().toLong())
            TYPE_INT32 -> PropValue.Int(r.s32().toLong())
            TYPE_UINT32 -> PropValue.Int(r.u32())
            TYPE_INT64 -> PropValue.Int(r.u64())
            TYPE_UINT64 -> PropValue.Int(r.u64())
            TYPE_FIXED -> PropValue.Fixed(r.s32())
            TYPE_FLOAT -> PropValue.Real(readF32(r))
            TYPE_DOUBLE -> PropValue.Real(readF64(r))
            TYPE_STRING -> PropValue.Str(r.string())
            TYPE_POINT -> PropValue.Point(r.s32(), r.s32())
            TYPE_SIZE -> PropValue.Size(r.s32(), r.s32())
            TYPE_RECT -> PropValue.Rect(r.s32(), r.s32(), r.s32(), r.s32())
            TYPE_VECTOR -> {
                val count = r.u32().toInt()
                val elemType = r.u16()
                val values = ArrayList<PropValue>(count)
                repeat(count) {
                    val t = if (elemType == 0) r.u16() else elemType
                    values.add(parsePropValue(t, r))
                }
                PropValue.Vector(elementType = elemType, values = values)
            }
            else -> throw IllegalArgumentException("Unknown property value type 0x${type.toString(16)}")
        }
    }

    private fun readF32(r: Reader): Double = ByteBuffer.wrap(r.read(4)).order(ByteOrder.LITTLE_ENDIAN).float.toDouble()

    private fun readF64(r: Reader): Double = ByteBuffer.wrap(r.read(8)).order(ByteOrder.LITTLE_ENDIAN).double
}