package moe.forpleuvoir.ibukigourd.ui.sokitsu

import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeToken
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.util.codec.ibukigourdIdentifier
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.codec.Codec
import net.minecraft.resources.Identifier

/**
 * 开关的主题接入声明：token 映射（"什么颜色"）+ 尺寸 meta（"多大/多密"）合一。
 *
 * 一个组件与主题的全部耦合集中在这个文件里，分两部分：
 * 1. [SwitchTokens] —— 组件各部位映射到主题的哪个语义槽位；
 * 2. [SwitchMeta] —— 组件的数值型默认参数（**单位均为 dp**），可被主题资源包的
 *    `sokitsu_meta.json` 中 `ui_meta.switch` 段覆盖。
 *
 * 本文件与 [Switch] 平级放在同一包；主题核心（[SokitsuThemeMeta]）不认识任何具体组件，
 * 每个组件都以"Token 对象 + Meta 数据类 + 扩展属性"的三件套模式自行接入。
 */
object SwitchTokens {

    /** 开启态把手。 */
    val CheckedThumb = ColorSchemeToken.Primary

    /** 开启态轨道：主色容器色（[ColorSchemeToken.PrimaryContainer]），与开启态把手同源、明显可分。 */
    val CheckedTrack = ColorSchemeToken.PrimaryContainer

    /** 关闭态把手：中性灰（[ColorSchemeToken.OnSurfaceVariant]）。 */
    val UncheckedThumb = ColorSchemeToken.OnSurfaceVariant

    /** 关闭态轨道：退回主题背景色（[ColorSchemeToken.Background]），与页面同色、弱化为"未开启"。 */
    val UncheckedTrack = ColorSchemeToken.Background

    /** 禁用态把手基准色（会被 [DisabledThumbOpacity] 压透明）。 */
    val DisabledThumb = ColorSchemeToken.OnSurface

    /** 禁用态轨道基准色（会被 [DisabledTrackOpacity] 压透明）。 */
    val DisabledTrack = ColorSchemeToken.SurfaceVariant

    /** 禁用态把手不透明度，沿用 Material3 `DisabledLabelTextOpacity`。 */
    const val DisabledThumbOpacity = 0.38f

    /** 禁用态轨道不透明度，沿用 Material3 `DisabledContainerOpacity`。 */
    const val DisabledTrackOpacity = 0.12f
}

/**
 * 开关的 meta：**单位均为 dp**（逻辑像素，消费端转 `.dp`）。
 *
 * ```jsonc
 * ui_meta: {
 *   switch: { track_width: 90, track_height: 48, thumb_size: 48 }
 * }
 * ```
 */
data class SwitchMeta(
    /** 轨道宽度（dp），与 switch.track 纹理比例匹配。 */
    val trackWidth: Int,

    /** 轨道高度（dp），把手贴轨道上下边缘。 */
    val trackHeight: Int,

    /** 把手尺寸（dp）。 */
    val thumbSize: Int,

    /** 把手常态纹理。 */
    val thumbNormalSprite: Identifier,

    /** 把手按下态纹理。 */
    val thumbPressedSprite: Identifier,

    /** 把手聚焦态纹理。 */
    val thumbFocusedSprite: Identifier,

    /** 把手禁用态纹理。 */
    val thumbDisabledSprite: Identifier,

    /** 轨道纹理。 */
    val trackSprite: Identifier,
) {

    companion object : Codec<SwitchMeta> {

        val default = SwitchMeta(
            trackWidth = 90,
            trackHeight = 48,
            thumbSize = 48,
            thumbNormalSprite = identifier("ui/switch.thumb.normal"),
            thumbPressedSprite = identifier("ui/switch.thumb.pressed"),
            thumbFocusedSprite = identifier("ui/switch.thumb.focused"),
            thumbDisabledSprite = identifier("ui/switch.thumb.disabled"),
            trackSprite = identifier("ui/switch.track"),
        )

        private val codec = Codec.create<SwitchMeta>()
            .field(SwitchMeta::trackWidth).default(default.trackWidth).codec(Codec.int(8..256))
            .field(SwitchMeta::trackHeight).default(default.trackHeight).codec(Codec.int(8..256))
            .field(SwitchMeta::thumbSize).default(default.thumbSize).codec(Codec.int(8..256))
            .field(SwitchMeta::thumbNormalSprite).default(default.thumbNormalSprite).codec(Codec.ibukigourdIdentifier)
            .field(SwitchMeta::thumbPressedSprite).default(default.thumbPressedSprite).codec(Codec.ibukigourdIdentifier)
            .field(SwitchMeta::thumbFocusedSprite).default(default.thumbFocusedSprite).codec(Codec.ibukigourdIdentifier)
            .field(SwitchMeta::thumbDisabledSprite).default(default.thumbDisabledSprite).codec(Codec.ibukigourdIdentifier)
            .field(SwitchMeta::trackSprite).default(default.trackSprite).codec(Codec.ibukigourdIdentifier)
            .build(::SwitchMeta)

        override fun serialization(target: SwitchMeta): SerializeElement = codec.serialization(target)

        override fun deserialization(data: SerializeElement): Result<SwitchMeta> = codec.deserialization(data)
    }
}

/**
 * 主题 meta 的开关段：缺失/解码失败回落 [SwitchMeta] 内置默认。
 */
val SokitsuThemeMeta.switch: SwitchMeta
    get() = decodeComponent("switch", SwitchMeta, SwitchMeta.default)
