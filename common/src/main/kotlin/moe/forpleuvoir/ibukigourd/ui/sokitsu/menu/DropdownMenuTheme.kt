package moe.forpleuvoir.ibukigourd.ui.sokitsu.menu

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.UiStateIdentifier
import moe.forpleuvoir.ibukigourd.ui.sokitsu.UiStateSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeToken
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.uiSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.toSprite
import moe.forpleuvoir.ibukigourd.util.codec.dp
import moe.forpleuvoir.ibukigourd.util.codec.dpSize
import moe.forpleuvoir.ibukigourd.util.codec.ibukigourdIdentifier
import moe.forpleuvoir.ibukigourd.util.codec.padding
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.codec.Codec
import net.minecraft.resources.Identifier

/**
 * 弹出菜单的主题接入声明：token 映射（"什么颜色"）+ meta（"面板多大、用哪张图"）。
 *
 * 只描述**弹层面板本身**；菜单内容的外观由内容自己决定，不在本组件主题范围内。
 */
object DropdownMenuTokens {

    /** 面板精灵的染色色板。 */
    val Panel = ColorSchemeToken.SurfaceVariant

    /** 面板内内容色：与 [Panel] 配对的内容色。 */
    val Content = ColorSchemeToken.OnSurfaceVariant
}

/**
 * 弹出菜单的 meta：**单位均为 dp**（逻辑像素，消费端转 `.dp`）。
 *
 * ```jsonc
 * ui_meta: {
 *   dropdown_menu: {
 *     panel_sprite: "ui/dropdown_menu/surface",
 *     item_sprite: {
 *       normal: "ui/dropdown_menu/item/normal",
 *       pressed: "ui/dropdown_menu/item/pressed",
 *       focused: "ui/dropdown_menu/item/focused",
 *       disabled: "ui/dropdown_menu/item/disabled"
 *     },
 *     min_size: [96, 0],
 *     padding: 6,
 *     max_height: 480,
 *     spacing: 4
 *   }
 * }
 * ```
 *
 * [itemSprite] 为条目的四态纹理标识；素材只画了 `pressed` / `focused` 两张，
 * `normal` / `disabled` 无对应资源 → 图集查询落空 → 该状态不渲染背景（保持透明）。
 *
 * 仅含弹层面板自身的尺寸：面板最小尺寸、内容内边距、面板内容区高度上限、面板与触发组件的
 * 间距。面板宽高由内容撑开，各项宽度即各自内容宽度，不做统一。
 */
data class DropdownMenuMeta(
    /** 面板九宫格精灵。 */
    val panelSprite: Identifier,
    /** 条目的四态纹理。 */
    val itemSprite: UiStateIdentifier,
    /** 面板最小尺寸，兜底内容过小的情况。 */
    val minSize: DpSize,
    /** 面板内边距：内容与面板边缘之间的留白。 */
    val padding: PaddingValues,
    /** 面板内容区最大高度：超出后内容区滚动。 */
    val maxHeight: Dp,
    /** 面板与触发组件之间的间距，同时充当面板与窗口其余边缘的安全间距。 */
    val spacing: Dp,
) {

    companion object : Codec<DropdownMenuMeta> {

        val default = DropdownMenuMeta(
            panelSprite = identifier("ui/dropdown_menu/surface"),
            itemSprite = UiStateIdentifier(
                normal = identifier("ui/dropdown_menu/item/normal"),
                pressed = identifier("ui/dropdown_menu/item/pressed"),
                focused = identifier("ui/dropdown_menu/item/focused"),
                disabled = identifier("ui/dropdown_menu/item/disabled"),
            ),
            minSize = DpSize(96.dp, 0.dp),
            padding = PaddingValues(6.dp),
            maxHeight = 480.dp,
            spacing = 4.dp,
        )

        private val codec = Codec.create<DropdownMenuMeta>()
            .field(DropdownMenuMeta::panelSprite).default(default.panelSprite).codec(Codec.ibukigourdIdentifier)
            .field(DropdownMenuMeta::itemSprite).default(default.itemSprite).codec(UiStateIdentifier)
            .field(DropdownMenuMeta::minSize).default(default.minSize)
            .codec(Codec.dpSize(0.dp..1024.dp, 0.dp..1024.dp))
            .field(DropdownMenuMeta::padding).default(default.padding).codec(Codec.padding(0.dp..64.dp))
            .field(DropdownMenuMeta::maxHeight).default(default.maxHeight).codec(Codec.dp(0.dp..4096.dp))
            .field(DropdownMenuMeta::spacing).default(default.spacing).codec(Codec.dp(0.dp..64.dp))
            .build(::DropdownMenuMeta)

        override fun serialization(target: DropdownMenuMeta): SerializeElement = codec.serialization(target)

        override fun deserialization(data: SerializeElement): Result<DropdownMenuMeta> = codec.deserialization(data)
    }
}

/**
 * 弹出菜单的主题桥接：组合内不直接读取 [SokitsuThemeMeta.dropdownMenu]。
 */
object DropdownMenuDefaults {

    /** 当前主题的 dropdown_menu meta。 */
    inline val meta get() = SokitsuThemeMeta.dropdownMenu

    /** 面板精灵：按 [DropdownMenuMeta.panelSprite] 经 UI 图集解析。 */
    fun panelSprite() = SokitsuThemeMeta.uiSprite(meta.panelSprite)

    /** 条目四态精灵：按 [DropdownMenuMeta.itemSprite] 经 UI 图集解析。 */
    fun itemSprite(): UiStateSprite = meta.itemSprite.toSprite()

    /** 面板最小尺寸：内联转发 [DropdownMenuMeta.minSize]。 */
    inline val minSize: DpSize get() = meta.minSize

    /** 面板内边距：内联转发 [DropdownMenuMeta.padding]。 */
    inline val padding: PaddingValues get() = meta.padding

    /** 面板内容区最大高度：内联转发 [DropdownMenuMeta.maxHeight]。 */
    inline val maxHeight: Dp get() = meta.maxHeight

    /** 面板与触发组件的间距：内联转发 [DropdownMenuMeta.spacing]。 */
    inline val spacing: Dp get() = meta.spacing

    // ── 默认条目（DropdownMenuItem）的外形规格 ──────────────────────────
    //
    // 这些是组件自身的默认值，不是主题项：条目属于菜单内容，其尺寸由内容与调用方决定，
    // 不随资源包变化。调用方经参数或 Modifier 覆盖。

    /** 默认条目的最小高度。内容更高时条目随之撑开。 */
    val itemMinHeight: Dp = 20.dp

    /** 默认条目的内容内边距。 */
    val itemPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 6.dp)

    /** 默认条目内图标槽位与内容之间的间距。 */
    val iconSpacing: Dp = 4.dp

    /** 默认条目内图标槽位的像素放大倍率。 */
    val iconScale: Int = 2
}

/** 主题 meta 的弹出菜单段：缺失 / 解码失败回落 [DropdownMenuMeta] 内置默认。 */
val SokitsuThemeMeta.dropdownMenu: DropdownMenuMeta
    get() = decodeComponent("dropdown_menu", DropdownMenuMeta, DropdownMenuMeta.default)
