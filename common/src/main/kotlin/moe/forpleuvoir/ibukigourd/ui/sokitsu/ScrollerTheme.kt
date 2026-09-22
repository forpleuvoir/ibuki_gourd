package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeToken
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolve
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.uiSprite
import moe.forpleuvoir.ibukigourd.util.codec.dpSize
import moe.forpleuvoir.ibukigourd.util.codec.ibukigourdIdentifier
import moe.forpleuvoir.ibukigourd.util.contrasting
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.codec.Codec
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvents
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * 滚动条的主题接入声明：token 映射（"什么颜色"）+ meta（"多大、用哪张图"）。
 *
 * 竖直与水平两个组件（含各自的 Overlay 样式）**共用同一份 meta** ——
 * 细轴分量即滚动条厚度，两个方向各自取用。
 */
object ScrollerTokens {

    /** 轨道底色：弱化容器色，比面板深一档但不抢主体。 */
    val Track = ColorSchemeToken.SurfaceVariant

    /** 叠加式轨道底色：主色容器，浮于内容之上时与常规轨道区分。 */
    val OverlayTrack = ColorSchemeToken.PrimaryContainer

    /** 滑块底色：主色，滚动位置是最该被一眼看到的信息。 */
    val Thumb = ColorSchemeToken.Primary

    /**
     * 禁用态轨道 / 滑块的色源。
     *
     * 两者都取**不透明**的语义槽位色，不叠加 alpha —— 素材的 `base` 层走 Multiply
     * 合成（顶点色 × 纹理），压 alpha 会让整层在深色底上消失，只剩 outline 层可见。
     */
    val DisabledTrack = ColorSchemeToken.SurfaceVariant
    val DisabledThumb = ColorSchemeToken.Surface
}

/**
 * 滚动条的 meta：**尺寸单位均为 dp**（逻辑像素），精灵为图集 id。
 *
 * ```jsonc
 * ui_meta: { scroller: {
 *   track_min_size: [27, 27],
 *   track_sprite: "ui/scroller/track",
 *   thumb_min_size: [27, 27],
 *   thumb_sprite: {
 *     normal: "ui/scroller/thumb/normal",
 *     pressed: "ui/scroller/thumb/pressed",
 *     focused: "ui/scroller/thumb/focused",
 *     disabled: "ui/scroller/thumb/disabled"
 *   },
 *   overlay_track_min_size: [15, 15],
 *   overlay_track_sprite: "ui/scroller_overlay/track",
 *   overlay_thumb_min_size: [15, 15],
 *   overlay_thumb_sprite: {
 *     normal: "ui/scroller_overlay/thumb/normal",
 *     pressed: "ui/scroller_overlay/thumb/pressed",
 *     focused: "ui/scroller_overlay/thumb/focused",
 *     disabled: "ui/scroller_overlay/thumb/disabled"
 *   }
 * } }
 * ```
 *
 * 两组尺寸都是**尺寸下限**，每组内**两个方向共用一份**（meta 不区分横竖）：
 * - `trackMinSize` 的细轴分量 = 滚动条厚度，是**恒定值**（竖直条取宽、水平条取高），
 *   不吃父级约束；主轴分量只在主轴无界时兜底。
 * - `thumbMinSize` 的主轴分量 = 滑块最短长度（内容极多时滑块仍可见）；交叉轴分量即厚度。
 * - `overlay*` 一组用于 [VerticalFlatScroller] / [HorizontalFlatScroller] 的**细条（flat）**滚动条，
 *   精灵与最小尺寸均独立于常规组；细条既占布局也浮在内容上都行，由调用方决定。
 */
data class ScrollerMeta(
    /** 轨道最小尺寸：细轴分量恒为滚动条厚度。 */
    val trackMinSize: DpSize,
    /** 轨道精灵图集 id（单张，无状态。交互状态差异由染色承担，不加描边）。 */
    val trackSprite: Identifier,
    /** 滑块最小尺寸：主轴分量 = 滑块最短长度。 */
    val thumbMinSize: DpSize,
    /** 滑块四态精灵图集 id。 */
    val thumbSprite: UiStateIdentifier,
    /** 叠加式轨道最小尺寸：细轴分量恒为叠加滚动条厚度。 */
    val overlayTrackMinSize: DpSize,
    /** 叠加式轨道精灵图集 id。 */
    val overlayTrackSprite: Identifier,
    /** 叠加式滑块最小尺寸：主轴分量 = 滑块最短长度。 */
    val overlayThumbMinSize: DpSize,
    /** 叠加式滑块四态精灵图集 id。 */
    val overlayThumbSprite: UiStateIdentifier,
) {

    companion object : Codec<ScrollerMeta> {

        val default = ScrollerMeta(
            trackMinSize = DpSize(27.dp, 27.dp),
            trackSprite = identifier("ui/scroller/track"),
            thumbMinSize = DpSize(27.dp, 27.dp),
            thumbSprite = UiStateIdentifier(
                normal = identifier("ui/scroller/thumb/normal"),
                pressed = identifier("ui/scroller/thumb/pressed"),
                focused = identifier("ui/scroller/thumb/focused"),
                disabled = identifier("ui/scroller/thumb/disabled"),
            ),
            overlayTrackMinSize = DpSize(15.dp, 15.dp),
            overlayTrackSprite = identifier("ui/scroller_overlay/track"),
            overlayThumbMinSize = DpSize(15.dp, 15.dp),
            overlayThumbSprite = UiStateIdentifier(
                normal = identifier("ui/scroller_overlay/thumb/normal"),
                pressed = identifier("ui/scroller_overlay/thumb/pressed"),
                focused = identifier("ui/scroller_overlay/thumb/focused"),
                disabled = identifier("ui/scroller_overlay/thumb/disabled"),
            ),
        )

        private val codec = Codec.create<ScrollerMeta>()
            .field(ScrollerMeta::trackMinSize).default(default.trackMinSize)
            .codec(Codec.dpSize(1.dp..1024.dp, 1.dp..1024.dp))
            .field(ScrollerMeta::trackSprite).default(default.trackSprite)
            .codec(Codec.ibukigourdIdentifier)
            .field(ScrollerMeta::thumbMinSize).default(default.thumbMinSize)
            .codec(Codec.dpSize(1.dp..1024.dp, 1.dp..4096.dp))
            .field(ScrollerMeta::thumbSprite).default(default.thumbSprite)
            .codec(UiStateIdentifier)
            .field(ScrollerMeta::overlayTrackMinSize).default(default.overlayTrackMinSize)
            .codec(Codec.dpSize(1.dp..1024.dp, 1.dp..1024.dp))
            .field(ScrollerMeta::overlayTrackSprite).default(default.overlayTrackSprite)
            .codec(Codec.ibukigourdIdentifier)
            .field(ScrollerMeta::overlayThumbMinSize).default(default.overlayThumbMinSize)
            .codec(Codec.dpSize(1.dp..1024.dp, 1.dp..4096.dp))
            .field(ScrollerMeta::overlayThumbSprite).default(default.overlayThumbSprite)
            .codec(UiStateIdentifier)
            .build(::ScrollerMeta)

        override fun serialization(target: ScrollerMeta): SerializeElement = codec.serialization(target)

        override fun deserialization(data: SerializeElement): Result<ScrollerMeta> =
            codec.deserialization(data)
    }
}

/**
 * 滚动条的主题桥接：组合内不直接读取 [SokitsuThemeMeta.scroller]。
 */
object ScrollerDefaults {

    /** 当前主题的 scroller meta。 */
    inline val meta get() = SokitsuThemeMeta.scroller

    /** 轨道最小尺寸：内联转发 [ScrollerMeta.trackMinSize]。细轴分量即厚度。 */
    inline val trackMinSize: DpSize get() = meta.trackMinSize

    /** 滑块最小尺寸：内联转发 [ScrollerMeta.thumbMinSize]。主轴分量即最短长度。 */
    inline val thumbMinSize: DpSize get() = meta.thumbMinSize

    /** 叠加式轨道最小尺寸：内联转发 [ScrollerMeta.overlayTrackMinSize]。细轴分量即厚度。 */
    inline val overlayTrackMinSize: DpSize get() = meta.overlayTrackMinSize

    /** 叠加式滑块最小尺寸：内联转发 [ScrollerMeta.overlayThumbMinSize]。主轴分量即最短长度。 */
    inline val overlayThumbMinSize: DpSize get() = meta.overlayThumbMinSize

    /** 轨道精灵：经 UI 图集解析；[overlay] 时取叠加式套装。 */
    fun trackSprite(overlay: Boolean = false): SokitsuSprite =
        SokitsuThemeMeta.uiSprite(if (overlay) meta.overlayTrackSprite else meta.trackSprite)

    /** 滑块四态精灵：经 UI 图集解析；[overlay] 时取叠加式套装。 */
    fun thumbSprite(overlay: Boolean = false): UiStateSprite =
        (if (overlay) meta.overlayThumbSprite else meta.thumbSprite).toSprite()

    /** 按住拖动时的鼠标指针形状。 */
    val LocalHoverIcon = compositionLocalOf { PointerIcon.Hand }

    /** 禁用时的鼠标指针形状。 */
    val LocalDisableIcon = compositionLocalOf { PointerIcon.NotAllowed }

    /**
     * 按下滚动条（拖滑块 / 点轨道空白）时播放的音效；null = 静音。
     *
     * 仅常规样式（[VerticalScroller] / [HorizontalScroller]）发声；
     * 细条样式（[VerticalFlatScroller] / [HorizontalFlatScroller]）按下不播放。
     */
    val LocalPressSound = compositionLocalOf<SoundInstance?> {
        SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f)
    }

    /** 按住轨道空白时，第一次与第二次滚动之间的停顿。 */
    val TrackPressDelayBeforeRepeat: Duration = 300.milliseconds

    /** 按住轨道空白时，后续两次滚动之间的间隔。 */
    val TrackPressRepeatInterval: Duration = 100.milliseconds

    /** 悬停 / 拖动高亮的渐变时长（毫秒）。 */
    const val HighlightDurationMillis = 300

    /** 自动降低可见度：退出活跃状态（悬浮 / 拖动 / 滚动中）后，开始降透明度前的等待时长。 */
    val AutoFadeDelay: Duration = 800.milliseconds

    /** 自动降低可见度的目标不透明度（相对原始可见度）。 */
    const val AutoFadeAlpha: Float = 0.3f

    /** 自动降低可见度 / 恢复不透明度的渐变时长（毫秒）。 */
    const val FadeDurationMillis = 250

    /**
     * 默认滚动条配色：轨道 → 弱化容器色，叠加轨道 → 主色容器，滑块 → 主色；
     * 两个禁用色取不透明语义槽位色。
     *
     * 参数默认 [Color.Unspecified] 语义是"按 [ScrollerTokens] 映射表结合当前主题解析"；
     * 回退顺序：`调用点传参` > [moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuColor]
     * 作用域 > [ScrollerTokens] > [moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorScheme]。
     *
     * @param trackColor 轨道底色 → [ScrollerTokens.Track]
     * @param overlayTrackColor 叠加式轨道底色 → [ScrollerTokens.OverlayTrack]
     * @param thumbColor 滑块底色 → [ScrollerTokens.Thumb]
     * @param disabledTrackColor 禁用轨道底色 → [ScrollerTokens.DisabledTrack]（不透明）
     * @param disabledThumbColor 禁用滑块底色 → [ScrollerTokens.DisabledThumb]（不透明）
     * @param highlightThumbColor 悬停 / 拖动中的滑块底色，默认与 [thumbColor] 同源
     * @param selectedOutlineColor 悬停 / 拖动时滑块描边色，默认取 [thumbColor] 底色的对比色
     */
    @Composable
    fun colors(
        trackColor: Color = Color.Unspecified,
        overlayTrackColor: Color = Color.Unspecified,
        thumbColor: Color = Color.Unspecified,
        disabledTrackColor: Color = Color.Unspecified,
        disabledThumbColor: Color = Color.Unspecified,
        highlightThumbColor: Color = Color.Unspecified,
        selectedOutlineColor: Color = Color.Unspecified,
    ): ScrollerColors {
        val resolvedThumb = thumbColor.resolve(ScrollerTokens.Thumb)
        return ScrollerColors(
            trackColor = trackColor.resolve(ScrollerTokens.Track),
            overlayTrackColor = overlayTrackColor.resolve(ScrollerTokens.OverlayTrack),
            thumbColor = resolvedThumb,
            disabledTrackColor = disabledTrackColor.resolve(ScrollerTokens.DisabledTrack),
            disabledThumbColor = disabledThumbColor.resolve(ScrollerTokens.DisabledThumb),
            highlightThumbColor = highlightThumbColor.takeIf { it != Color.Unspecified } ?: resolvedThumb,
            selectedOutlineColor = selectedOutlineColor.takeOrElse { resolvedThumb.contrasting() },
        )
    }
}

/**
 * 滚动条配色集：六个底色槽位（常规 / 叠加轨道、滑块的启用、禁用、高亮）+ 选中描边色。
 *
 * 字段**全部已解析**，因此是普通 data class，`copy(...)` 即精准覆盖；
 * "槽位映射到主题哪里"由 [ScrollerDefaults.colors] 承担。
 * "状态 → 取值"的映射由 [trackColor] / [thumbColor] 两个方法承担，
 * 调用点不自行按启用状态做 if 判断。
 *
 * @param overlayTrackColor 叠加式轨道底色
 * @param highlightThumbColor 悬停 / 拖动中的滑块底色
 * @param selectedOutlineColor 悬停 / 拖动时滑块 outline 层的覆盖色（选中描边）
 */
@Immutable
data class ScrollerColors(
    val trackColor: Color,
    val overlayTrackColor: Color,
    val thumbColor: Color,
    val disabledTrackColor: Color,
    val disabledThumbColor: Color,
    val highlightThumbColor: Color,
    val selectedOutlineColor: Color,
) {

    /** 轨道底色：禁用 > 叠加样式（主色容器）> 常规（弱化容器色）。 */
    internal fun trackColor(overlay: Boolean, enabled: Boolean): Color = when {
        !enabled -> disabledTrackColor
        overlay  -> overlayTrackColor
        else     -> trackColor
    }

    /** 滑块底色（禁用 > 高亮 > 常态）。 */
    internal fun thumbColor(enabled: Boolean, highlighted: Boolean): Color = when {
        !enabled    -> disabledThumbColor
        highlighted -> highlightThumbColor
        else        -> thumbColor
    }
}

/** 主题 meta 的滚动条段：缺失 / 解码失败回落 [ScrollerMeta] 内置默认。 */
val SokitsuThemeMeta.scroller: ScrollerMeta
    get() = decodeComponent("scroller", ScrollerMeta, ScrollerMeta.default)
