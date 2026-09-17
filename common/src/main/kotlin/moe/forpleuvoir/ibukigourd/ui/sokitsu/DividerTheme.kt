package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeToken
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.util.codec.dp
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.codec.Codec

/**
 * 分割线的主题接入声明：token 映射（"什么颜色"）+ meta（"多厚"）。
 *
 * 与 [ButtonTokens] / [SurfaceTokens] 同构：本文件只声明该组件映射到主题的哪些语义槽位，
 * 主题核心（[SokitsuThemeMeta]）不认识具体组件。
 */
object DividerTokens {

    /** 线条颜色：取全局描边色，与容器边框同族（同屏出现时不会互相抢眼）。 */
    val Line = ColorSchemeToken.Outline
}

/**
 * 分割线的 meta：**单位均为 dp**（逻辑像素，消费端转 `.dp`）。
 *
 * ```jsonc
 * ui_meta: { divider: { thickness: 1 } }
 * ```
 *
 * 分割线是纯色矩形，**不走纹理管线**：`pixel_scale` 只影响精灵素材的放大倍率，
 * 不影响本组件——[thickness] 即最终屏幕像素高度（场景密度为 1 时 1dp == 1px）。
 */
data class DividerMeta(
    /** 线条厚度。 */
    val thickness: Dp,
) {

    companion object : Codec<DividerMeta> {

        val default = DividerMeta(thickness = 1.dp)

        private val codec = Codec.create<DividerMeta>()
            .field(DividerMeta::thickness).default(default.thickness).codec(Codec.dp(1.dp..16.dp))
            .build(::DividerMeta)

        override fun serialization(target: DividerMeta): SerializeElement = codec.serialization(target)

        override fun deserialization(data: SerializeElement): Result<DividerMeta> = codec.deserialization(data)
    }
}

/**
 * 分割线的主题桥接：组合内不直接读取 [SokitsuThemeMeta.divider]。
 *
 * @see moe.forpleuvoir.ibukigourd.ui.sokitsu.HorizontalDivider
 * @see moe.forpleuvoir.ibukigourd.ui.sokitsu.VerticalDivider
 */
object DividerDefaults {

    /** 当前主题的 divider meta。 */
    inline val meta get() = SokitsuThemeMeta.divider

    /** 线条厚度：内联转发 [DividerMeta.thickness]。 */
    inline val thickness: Dp get() = meta.thickness
}

/** 主题 meta 的分割线段：缺失 / 解码失败回落 [DividerMeta] 内置默认。 */
val SokitsuThemeMeta.divider: DividerMeta
    get() = decodeComponent("divider", DividerMeta, DividerMeta.default)
