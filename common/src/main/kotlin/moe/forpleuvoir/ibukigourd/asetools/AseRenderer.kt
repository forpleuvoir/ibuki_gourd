package moe.forpleuvoir.ibukigourd.asetools

/**
 * 帧解码：把 cel 原始像素转 RGBA、解析 linked cel、并按 z-order 合成图层。
 *
 * 纯字节运算、零依赖（PNG 写出由消费方负责），供运行时与测试共用。
 */
object AseRenderer {

    /**
     * 渲染一帧，返回 (合成画布 RGBA, {layerIndex: 该层独立渲染 RGBA})，
     * 均与 sprite 同尺寸。
     */
    fun renderFrame(sprite: AseSprite, frameIndex: Int): Pair<ByteArray, Map<Int, ByteArray>> {
        val w = sprite.header.width
        val h = sprite.header.height
        val canvas = ByteArray(w * h * 4)
        val layerCanvases = HashMap<Int, ByteArray>()

        val pairs = mutableListOf<Pair<Cel, Layer>>()
        for (cel in sprite.frames[frameIndex].cels) {
            if (cel.celType == CEL_TYPE_COMPRESSED_TILEMAP) continue
            val layer = sprite.layerByIndex(cel.layerIndex) ?: continue
            if (!layer.isVisible) continue
            pairs.add(cel to layer)
        }
        pairs.sortWith(
            compareBy({ it.second.index + it.first.zIndex }, { it.first.zIndex })
        )

        for ((cel, layer) in pairs) {
            val resolved = resolveCel(sprite, frameIndex, cel) ?: continue
            if (resolved.w <= 0 || resolved.h <= 0) continue
            val rgba = celToRgba(sprite, resolved)
            if (rgba.isEmpty()) continue

            if (layer.blendMode != 0) {
                println(
                    "Note: layer '${layer.name}' blend mode " +
                        "${BLEND_MODE_NAMES[layer.blendMode] ?: layer.blendMode} " +
                        "not implemented, using Normal"
                )
            }

            var alpha = resolved.opacity
            if (sprite.header.hasLayerOpacity) alpha = alpha * layer.opacity / 255

            val lc = layerCanvases.getOrPut(layer.index) { ByteArray(w * h * 4) }
            for (j in 0 until resolved.h) {
                val dstY = resolved.y + j
                if (dstY < 0 || dstY >= h) continue
                for (i in 0 until resolved.w) {
                    val dstX = resolved.x + i
                    if (dstX < 0 || dstX >= w) continue
                    val dstOff = (dstY * w + dstX) * 4
                    val srcOff = (j * resolved.w + i) * 4
                    blend(canvas, dstOff, rgba, srcOff, alpha, layer.isBackground)
                    blend(lc, dstOff, rgba, srcOff, alpha, layer.isBackground)
                }
            }
        }

        return canvas to layerCanvases.mapValues { it.value }
    }

    /** 把 cel 的原始像素转成 RGBA 字节流（不应用透明度）。 */
    fun celToRgba(sprite: AseSprite, cel: Cel): ByteArray {
        val raw = cel.raw ?: return ByteArray(0)
        val depth = sprite.header.colorDepth
        val count = cel.w * cel.h
        val out = ByteArray(count * 4)
        when (depth) {
            DEPTH_RGBA -> raw.copyInto(out)
            DEPTH_GRAY -> for (i in 0 until count) {
                val v = raw[i * 2].toInt() and 0xFF
                val a = raw[i * 2 + 1].toInt() and 0xFF
                out[i * 4] = v.toByte()
                out[i * 4 + 1] = v.toByte()
                out[i * 4 + 2] = v.toByte()
                out[i * 4 + 3] = a.toByte()
            }
            DEPTH_INDEXED -> {
                val palette = sprite.palette
                val transparent = sprite.header.transparentIndex
                for (i in 0 until count) {
                    val idx = raw[i].toInt() and 0xFF
                    val o = i * 4
                    if (idx == transparent) {
                        out[o] = 0; out[o + 1] = 0; out[o + 2] = 0; out[o + 3] = 0
                    } else {
                        val p = palette.getOrNull(idx)
                            ?: throw IllegalArgumentException(
                                "Palette index $idx out of range (size=${palette.size})"
                            )
                        out[o] = p.r.toByte(); out[o + 1] = p.g.toByte()
                        out[o + 2] = p.b.toByte(); out[o + 3] = p.a.toByte()
                    }
                }
            }
        }
        return out
    }

    /** 解析 linked cel（type 1），返回携带实际像素数据的拷贝；无法解析返回 null。 */
    fun resolveCel(
        sprite: AseSprite,
        frameIndex: Int,
        cel: Cel,
        visited: MutableSet<String> = mutableSetOf(),
    ): Cel? {
        if (cel.celType != CEL_TYPE_LINKED) return cel
        val key = "$frameIndex@${System.identityHashCode(cel)}"
        if (!visited.add(key)) return null // 循环链接
        val target = cel.frameLink ?: return null
        if (target < 0 || target >= sprite.frames.size) return null
        for (cand in sprite.frames[target].cels) {
            if (cand.layerIndex == cel.layerIndex && cand.celType != CEL_TYPE_LINKED) {
                val resolved = resolveCel(sprite, target, cand, visited) ?: continue
                // 保持当前 cel 的坐标/透明度/z-order，像素取自目标
                return resolved.copy(
                    x = cel.x,
                    y = cel.y,
                    opacity = cel.opacity,
                    zIndex = cel.zIndex,
                    frameIndex = frameIndex,
                )
            }
        }
        return null
    }

    /** 把一个像素以 src-over 混合进画布（整数运算）。 */
    private fun blend(
        dst: ByteArray,
        off: Int,
        src: ByteArray,
        sOff: Int,
        alpha: Int,
        forceOpaque: Boolean,
    ) {
        if (alpha <= 0) return
        val sr = src[sOff].toInt() and 0xFF
        val sg = src[sOff + 1].toInt() and 0xFF
        val sb = src[sOff + 2].toInt() and 0xFF
        var sa = if (forceOpaque) 255 else src[sOff + 3].toInt() and 0xFF
        sa = sa * alpha / 255

        val dr = dst[off].toInt() and 0xFF
        val dg = dst[off + 1].toInt() and 0xFF
        val db = dst[off + 2].toInt() and 0xFF
        val da = dst[off + 3].toInt() and 0xFF

        val oa = sa + da * (255 - sa) / 255
        if (oa == 0) {
            dst[off] = 0; dst[off + 1] = 0; dst[off + 2] = 0; dst[off + 3] = 0
            return
        }
        val daf = da * (255 - sa) / 255.0 // 目标对输出的贡献权重
        dst[off] = ((sr * sa + dr * daf) / oa).toInt().toByte()
        dst[off + 1] = ((sg * sa + dg * daf) / oa).toInt().toByte()
        dst[off + 2] = ((sb * sa + db * daf) / oa).toInt().toByte()
        dst[off + 3] = oa.toByte()
    }
}
