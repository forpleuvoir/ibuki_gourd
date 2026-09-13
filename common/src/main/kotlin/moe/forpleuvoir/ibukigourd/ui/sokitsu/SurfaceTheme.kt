package moe.forpleuvoir.ibukigourd.ui.sokitsu

import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeToken
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.util.codec.ibukigourdIdentifier
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.codec.Codec
import net.minecraft.resources.Identifier

/**
 * 表面容器（[Surface]）的 token 映射。
 *
 * 对应 Material3 `Surface` 的默认取色：容器 = `surface`、内容 = `onSurface`。
 * 内容色**不登记 token**，而是由解析后的容器色板配对推导
 * （[moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.contentColorFor]）：
 * 这样作用域把 tone 切成 secondary / error 时，内容色会自动跟着变成
 * `onSecondary` / `onError`；固定取 token 反而会丢掉这个联动
 * （与 [ButtonTokens.Content] 的处理一致）。
 *
 * 本文件与 [Surface] 平级放在同一包；后续若引入 Surface 的数值型 meta
 * （如面板最小内边距），也应并入本文件，与 [ButtonTheme.kt] / [SwitchTheme.kt] 同规矩。
 */
object SurfaceTokens {

    /** 容器背景精灵的染色色板。 */
    val Container = ColorSchemeToken.Surface
}

/**
 * Surface 的 meta：三张面板九宫格精灵的 id，资源包经 `ui_meta.surface` 段覆盖。
 *
 * ```jsonc
 * ui_meta: {
 *   surface: {
 *     panel_sprite: "ui/surface/panel",
 *     floating_panel_sprite: "ui/surface/float_panel",
 *     embedded_panel_sprite: "ui/surface/embed_panel"
 *   }
 * }
 * ```
 *
 * 三张素材的形态约定（美术，图层与 border 已核）：
 * - `panel`：平面（base + outline，border 2）—— 默认容器；
 * - `float_panel`：凸起带阴影（shadow + base + highlight + outline，border 3）—— Dialog 等浮层；
 * - `embed_panel`：凹槽（base + highlight + dark + outline，border 3）—— 内嵌容器。
 */
data class SurfaceMeta(
    /** 普通面板精灵：默认 [Surface] 的背景。 */
    val panelSprite: Identifier,
    /** 浮动面板精灵：Dialog 等浮层容器。 */
    val floatingPanelSprite: Identifier,
    /** 嵌入面板精灵：凹槽 / 内嵌容器。 */
    val embeddedPanelSprite: Identifier,
) {

    companion object : Codec<SurfaceMeta> {

        val default = SurfaceMeta(
            panelSprite = identifier("ui/surface/panel"),
            floatingPanelSprite = identifier("ui/surface/float_panel"),
            embeddedPanelSprite = identifier("ui/surface/embed_panel"),
        )

        private val codec = Codec.create<SurfaceMeta>()
            .field(SurfaceMeta::panelSprite).default(default.panelSprite).codec(Codec.ibukigourdIdentifier)
            .field(SurfaceMeta::floatingPanelSprite).default(default.floatingPanelSprite).codec(Codec.ibukigourdIdentifier)
            .field(SurfaceMeta::embeddedPanelSprite).default(default.embeddedPanelSprite).codec(Codec.ibukigourdIdentifier)
            .build(::SurfaceMeta)

        override fun serialization(target: SurfaceMeta): SerializeElement = codec.serialization(target)

        override fun deserialization(data: SerializeElement): Result<SurfaceMeta> = codec.deserialization(data)
    }
}

/**
 * 主题 meta 的 surface 段：缺失 / 解码失败回落 [SurfaceMeta] 内置默认。
 */
val SokitsuThemeMeta.surface: SurfaceMeta
    get() = decodeComponent("surface", SurfaceMeta, SurfaceMeta.default)
