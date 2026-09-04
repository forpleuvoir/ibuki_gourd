package moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas

import com.mojang.blaze3d.platform.NativeImage
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.TextureRegion
import moe.forpleuvoir.nebula.common.color.Color

/**
 * 按 layer.keys 从 SokitsuTexture 基础图中提取该图层的内容：
 * - [keys] 非空：保留颜色（RGB 相等，忽略 alpha）命中 keys 的像素，原色不变；未命中的不透明像素置为全透明
 * - [keys] 为空：整张图都属于该图层
 * - 原本透明的像素始终保留（不属于任何 keys 内容，保持透明）
 *
 * [region] 指定渲染区域（可选，缺省 = 整张源图）：
 * - 非 null 时输出新 NativeImage 的尺寸 = 区域尺寸，只对区域内像素做提取，区域外不参与（即裁剪）
 * - 区域必须落在源图范围内
 *
 * 输出为新 NativeImage，由调用方负责关闭。
 * 注意：本步只做"颜色提取"，不做 tintMode 着色（着色策略待定，属后续范围）。
 */
object LayerExtractor {

    fun extract(source: NativeImage, keys: List<Color>, region: TextureRegion? = null): NativeImage {
        val regionWidth = region?.width ?: source.width
        val regionHeight = region?.height ?: source.height
        val uOffset = region?.u ?: 0
        val vOffset = region?.v ?: 0

        if (region != null) {
            require(uOffset + regionWidth <= source.width && vOffset + regionHeight <= source.height) {
                "TextureLayer region ${region}u/${region.v}+${region.width}x${region.height} exceeds source ${source.width}x${source.height}"
            }
        }

        val keyRgb = if (keys.isEmpty()) {
            null
        } else {
            keys.mapTo(HashSet()) { it.argb and 0x00FFFFFF }
        }

        val result = NativeImage(regionWidth, regionHeight, true)
        for (y in 0 until regionHeight) {
            for (x in 0 until regionWidth) {
                val pixel = source.getPixel(uOffset + x, vOffset + y)
                val alpha = pixel ushr 24 and 0xFF
                result.setPixel(
                    x, y,
                    when {
                        keyRgb == null -> pixel
                        alpha == 0     -> pixel
                        (pixel and 0x00FFFFFF) in keyRgb -> pixel
                        else           -> 0
                    }
                )
            }
        }
        return result
    }
}