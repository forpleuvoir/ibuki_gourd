package moe.forpleuvoir.ibukigourd.ui.selector

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.VerticalDivider
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeToken
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuPixelScale
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolve
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolveFaded
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.uiSprite
import moe.forpleuvoir.ibukigourd.util.codec.dpSize
import moe.forpleuvoir.ibukigourd.util.codec.ibukigourdIdentifier
import moe.forpleuvoir.ibukigourd.util.codec.padding
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.codec.Codec
import net.minecraft.resources.Identifier

/**
 * 选择器触发件的主题接入声明：token 映射（"什么颜色"）+ meta（"多大、用哪张图"）。
 */
object SelectorTriggerTokens {

    /** 皮肤精灵的染色色板。 */
    val Container = ColorSchemeToken.SurfaceVariant

    /** 内容色：与 [Container] 配对的内容色。 */
    val Content = ColorSchemeToken.OnSurfaceVariant

    /** 悬停 / 聚焦时 outline 层的高亮色。 */
    val SelectedOutline = ColorSchemeToken.Primary

    /** 禁用态内容色的不透明度系数。 */
    val DisabledContentOpacity = 0.5f
}

/**
 * 选择器触发件的 meta：**单位均为 dp**（逻辑像素，消费端转 `.dp`）。
 *
 * ```jsonc
 * ui_meta: {
 *   selector_trigger: {
 *     sprite: "ui/dropdown_menu/selector",
 *     min_size: [96, 20],
 *     padding: [16, 8, 12, 12]
 *   }
 * }
 * ```
 *
 * [sprite] 为皮肤的**单张**纹理：状态差异全部由染色表达，不按状态分素材。
 *
 * 仅含触发件自身的皮肤与尺寸：最小尺寸、内容内边距。
 */
data class SelectorTriggerMeta(
    /** 皮肤的九宫格精灵。 */
    val sprite: Identifier,
    /** 触发件最小尺寸，兜底内容过小的情况。 */
    val minSize: DpSize,
    /** 内容与皮肤边缘之间的留白；末端值同时决定展开指示区的"分割线 → 箭头"间距。 */
    val padding: PaddingValues,
) {

    companion object : Codec<SelectorTriggerMeta> {

        val default = SelectorTriggerMeta(
            sprite = identifier("ui/dropdown_menu/selector"),
            minSize = DpSize(96.dp, 20.dp),
            // 起始侧 16、末端侧 8：末端同时是箭头到皮肤右边界的距离，收窄以让指示区更贴近边界
            padding = PaddingValues(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 12.dp),
        )

        private val codec = Codec.create<SelectorTriggerMeta>()
            .field(SelectorTriggerMeta::sprite).default(default.sprite).codec(Codec.ibukigourdIdentifier)
            .field(SelectorTriggerMeta::minSize).default(default.minSize)
            .codec(Codec.dpSize(0.dp..1024.dp, 0.dp..1024.dp))
            .field(SelectorTriggerMeta::padding).default(default.padding).codec(Codec.padding(0.dp..64.dp))
            .build(::SelectorTriggerMeta)

        override fun serialization(target: SelectorTriggerMeta): SerializeElement = codec.serialization(target)

        override fun deserialization(data: SerializeElement): Result<SelectorTriggerMeta> = codec.deserialization(data)
    }
}

/**
 * 选择器触发件的主题桥接：组合内不直接读取 [SokitsuThemeMeta.selectorTrigger]。
 */
object SelectorTriggerDefaults {

    /**
     * 展开箭头的 [Icon] 尺寸倍率。
     *
     * 与 [Icon] 的默认倍率相同，供 [expandIcon] 在不依赖 `LocalSokitsuPixelScale`
     * 的前提下算出箭头高度（分割线要与箭头等高）。
     */
    private const val IconArrowScale = 2

    /** 当前主题的 selector_trigger meta。 */
    inline val meta get() = SokitsuThemeMeta.selectorTrigger

    /** 皮肤精灵：按 [SelectorTriggerMeta.sprite] 经 UI 图集解析。 */
    fun sprite(): SokitsuSprite = SokitsuThemeMeta.uiSprite(meta.sprite)

    /** 触发件最小尺寸：内联转发 [SelectorTriggerMeta.minSize]。 */
    inline val minSize: DpSize get() = meta.minSize

    /** 内容内边距：内联转发 [SelectorTriggerMeta.padding]。 */
    inline val padding: PaddingValues get() = meta.padding

    /**
     * 默认展开图标：一道竖直分割线 + 一张转动的 `Icons.Down` 折角。
     *
     * 分割线属于本图标自身（两者一起构成"展开指示区"），换掉本槽位即整体替换。
     *
     * 箭头用**一张素材旋转**而不是按 [expanded] 换 `Icons.Down` / `Icons.Up`：`up` 素材正是
     * `down` 的上下翻转，而这张折角左右对称，翻转等价于旋转 180° —— 落点就是 `Icons.Up`，
     * 中间还多出过渡过程（与配置页分组指示同一套做法）。
     *
     * 三个尺寸都跟随当前渲染体系：
     * - 分割线厚度 = 当前 [LocalSokitsuPixelScale]（一个素材像素的宽度）；
     * - 分割线高度 = 箭头 [Icon] 的实际布局高（同精灵、同倍率），两者等高；
     * - 分割线与箭头的间距 = [contentPaddingEnd]，即箭头到皮肤右边界的距离。
     *
     * @param contentPaddingEnd 触发件内容内边距的末端值（由 [SelectorTrigger] 传入）
     */
    @Composable
    fun expandIcon(expanded: Boolean, contentPaddingEnd: Dp) {
        val thickness = LocalSokitsuPixelScale.current.dp
        val rotation by animateFloatAsState(if (expanded) 180f else 0f)
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 定长而非 fillMaxHeight：本 Row 的高度由内容撑开，铺满会解析为 0（不可见）
            VerticalDivider(length = Icons.Down.logicalHeight.dp * IconArrowScale, thickness = thickness)
            Spacer(Modifier.width(contentPaddingEnd))
            Icon(Icons.Down, modifier = Modifier.rotate(rotation))
        }
    }

    /**
     * 默认配色集：三个参数默认均未指定（[androidx.compose.ui.graphics.Color.Unspecified]），
     * 语义是"调用方没意见，按 [SelectorTriggerTokens] 映射表结合当前主题解析"。
     */
    @Composable
    fun colors(
        color: Color = Color.Unspecified,
        contentColor: Color = Color.Unspecified,
        selectedOutlineColor: Color = Color.Unspecified,
    ): SelectorColors = SelectorColors(
        color = color.resolve(SelectorTriggerTokens.Container),
        contentColor = contentColor.resolve(SelectorTriggerTokens.Content),
        selectedOutlineColor = selectedOutlineColor.resolve(SelectorTriggerTokens.SelectedOutline),
    )

    /**
     * 默认禁用态配色集：三处与 [colors] 同槽位解析，内容色经禁用态解析链压一层
     * [SelectorTriggerTokens.DisabledContentOpacity]（调用点显式传值时不再叠加）。
     */
    @Composable
    fun disableColors(
        color: Color = Color.Unspecified,
        contentColor: Color = Color.Unspecified,
        selectedOutlineColor: Color = Color.Unspecified,
    ): SelectorColors = SelectorColors(
        color = color.resolve(SelectorTriggerTokens.Container),
        contentColor = contentColor.resolveFaded(
            SelectorTriggerTokens.Content,
            SelectorTriggerTokens.DisabledContentOpacity,
        ),
        selectedOutlineColor = selectedOutlineColor.resolve(SelectorTriggerTokens.SelectedOutline),
    )
}

/** 主题 meta 的选择器触发件段：缺失 / 解码失败回落 [SelectorTriggerMeta] 内置默认。 */
val SokitsuThemeMeta.selectorTrigger: SelectorTriggerMeta
    get() = decodeComponent("selector_trigger", SelectorTriggerMeta, SelectorTriggerMeta.default)
