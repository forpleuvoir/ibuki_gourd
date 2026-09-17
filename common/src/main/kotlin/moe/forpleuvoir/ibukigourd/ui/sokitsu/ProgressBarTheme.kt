package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeToken
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.util.codec.dp
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.codec.Codec

/**
 * 进度条的主题接入声明：token 映射（"什么颜色"）+ meta（"多高"）。
 *
 * 与 [ButtonTokens] / [SurfaceTokens] 同构：本文件只声明该组件映射到主题的哪些语义槽位，
 * 主题核心（[SokitsuThemeMeta]）不认识具体组件。
 */
object ProgressTokens {

    /** 已完成部分：取主色，进度进展是全屏最该被一眼看到的信息。 */
    val Indicator = ColorSchemeToken.Primary

    /** 未完成轨道：取弱化容器色，与背景拉开一档但不抢主色。 */
    val Track = ColorSchemeToken.SurfaceVariant
}

/**
 * 进度条的 meta：**单位均为 dp**（逻辑像素，消费端转 `.dp`）。
 *
 * ```jsonc
 * ui_meta: { progress_bar: { height: 4 } }
 * ```
 *
 * 进度条是纯色矩形，**不走纹理管线**：`pixel_scale` 只影响精灵素材的放大倍率，
 * 不影响本组件——[height] 即最终屏幕像素高度（场景密度为 1 时 1dp == 1px）。
 */
data class ProgressMeta(
    /** 轨道 / 填充的高度。 */
    val height: Dp,
) {

    companion object : Codec<ProgressMeta> {

        val default = ProgressMeta(height = 4.dp)

        private val codec = Codec.create<ProgressMeta>()
            .field(ProgressMeta::height).default(default.height).codec(Codec.dp(1.dp..64.dp))
            .build(::ProgressMeta)

        override fun serialization(target: ProgressMeta): SerializeElement = codec.serialization(target)

        override fun deserialization(data: SerializeElement): Result<ProgressMeta> = codec.deserialization(data)
    }
}

/**
 * 进度条的主题桥接：组合内不直接读取 [SokitsuThemeMeta.progressBar]。
 *
 * @see moe.forpleuvoir.ibukigourd.ui.sokitsu.ProgressBar
 */
object ProgressDefaults {

    /** 当前主题的 progress meta。 */
    inline val meta get() = SokitsuThemeMeta.progressBar

    /** 轨道高度：内联转发 [ProgressMeta.height]。 */
    inline val height: Dp get() = meta.height
}

/** 主题 meta 的进度条段：缺失 / 解码失败回落 [ProgressMeta] 内置默认。 */
val SokitsuThemeMeta.progressBar: ProgressMeta
    get() = decodeComponent("progress_bar", ProgressMeta, ProgressMeta.default)
