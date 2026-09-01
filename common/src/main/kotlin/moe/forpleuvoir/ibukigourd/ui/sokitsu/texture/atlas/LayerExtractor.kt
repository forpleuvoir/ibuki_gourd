package moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas

import com.mojang.blaze3d.platform.NativeImage
import moe.forpleuvoir.nebula.common.color.Color

/**
 * 按 layer.keys 从 SokitsuTexture 基础图中提取该图层的内容：
 * - [keys] 非空：保留颜色（RGB 相等，忽略 alpha）命中 keys 的像素，原色不变；未命中的不透明像素置为全透明
 * - [keys] 为空：整张原图都属于该图层
 * - 原本透明的像素始终保留（不属于任何 keys 内容，保持透明）
 *
 * 输出为新 NativeImage（尺寸与原图一致），由调用方负责关闭。
 * 注意：本步只做"颜色提取"，不做 tintMode 着色（着色策略待定，属后续范围）。
 */
object LayerExtractor {

    fun extract(source: NativeImage, keys: List<Color>): NativeImage {
        if (keys.isEmpty()) return source.mappedCopy { it }

        val keyRgb = keys.mapTo(HashSet()) { it.argb and 0x00FFFFFF }
        return source.mappedCopy { pixel ->
            val alpha = pixel ushr 24 and 0xFF
            if (alpha == 0) {
                pixel
            } else if ((pixel and 0x00FFFFFF) in keyRgb) {
                pixel
            } else {
                0
            }
        }
    }
}