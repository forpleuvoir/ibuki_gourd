package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.TextureFill
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeToken
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuPixelScale
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolve
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.uiSprite
import moe.forpleuvoir.ibukigourd.util.codec.dp
import moe.forpleuvoir.ibukigourd.util.codec.ibukigourdIdentifier
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.codec.Codec
import net.minecraft.resources.Identifier

/**
 * 页签条的主题接入声明：token 映射（"什么颜色"）+ meta（"用哪张图"）。
 *
 * 配色只剩"染色色"这一维 —— 高光、描边、投影的形状全部由素材图层承担
 * （`tone` / `outline` / `shadow` 三个 colorSlot，见 [moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.TextureLayer.colorSlot]）。
 */
object TabStripTokens {

    /** 面板 tone 层底色。 */
    val Panel = ColorSchemeToken.SurfaceVariant

    /** 未选中页签 tone 层底色。 */
    val Tab = ColorSchemeToken.Surface

    /** outline 层描边色（悬停 / 按下时可被调用点覆盖）。 */
    val Outline = ColorSchemeToken.Outline

    /** 未选中页签内容色。 */
    val Content = ColorSchemeToken.OnSurfaceVariant

    /** 选中页签内容色。 */
    val ContentSelected = ColorSchemeToken.OnSurface

    /** 溢出箭头 tone 层底色。 */
    val Arrow = ColorSchemeToken.Surface
}

/**
 * 页签条的 meta：四个状态的页签精灵（上/下两个朝向 × 选中/未选中）+ 面板与箭头精灵，
 * 资源包经 `ui_meta.tab_strip` 段覆盖。
 *
 * ```jsonc
 * ui_meta: { tab_strip: {
 *   panel_sprite: "ui/tab_strip/panel",
 *   top_tab_sprite: "ui/tab_strip/tab/top_inactive",
 *   top_selected_tab_sprite: "ui/tab_strip/tab/top_active",
 *   bottom_tab_sprite: "ui/tab_strip/tab/bottom_inactive",
 *   bottom_selected_tab_sprite: "ui/tab_strip/tab/bottom_active",
 *   arrow_left_sprite: "ui/tab_strip/arrow_left",
 *   arrow_right_sprite: "ui/tab_strip/arrow_right",
 *   tab_gap: 12.0,
 *   row_padding: 36.0,
 *   tab_padding_horizontal: 22.0,
 *   tab_padding_vertical: 8.0,
 *   selected_extra_height: 6.0
 * } }
 * ```
 *
 * 数值单位 **dp**（与 `ScrollerMeta` 等其它组件一致，就是屏幕上的实际尺寸），
 * 消费端直接当 dp 用、不再乘任何倍率。
 *
 * 这里只登记**素材表达不了的东西**：页签间隙、行的左右内边距、页签内部的内边距，
 * 以及选中页签比未选中高出的量（两个页签精灵画布同尺寸，素材本身表达不了这个差）。
 * 页签盒高、熔接带、箭头尺寸仍在素材里（画布尺寸 + border），组件按精灵自身尺寸算。
 *
 * 素材形态约定（美术）：
 * - 页签九宫格贴**面板那一侧**开口（`top_*` 底边开口、`bottom_*` 顶边开口），该侧 border 取负值；
 * - **选中 / 未选中的差别也在这条带上**：选中页签该带填满（熔进面板、盖住面板边框），
 *   未选中该带透明 —— 两者画布尺寸相同、布局盒高也相同，面板边框会在未选中页签下方照常露出；
 * - 面板是闭合九宫格（四边 border 均为正）；
 * - 箭头为 stretch 填充，按素材自身尺寸方形绘制。
 */
data class TabStripMeta(
    /** 面板精灵图集 id。 */
    val panelSprite: Identifier,
    /**
     * 全屏用的面板精灵图,只提供顶部边框,适用于全屏的标签页
     */
    val screenPanelSprite: Identifier,
    /** 未选中页签精灵图集 id（页签行在面板上方）。 */
    val topTabSprite: Identifier,
    /** 选中页签精灵图集 id（页签行在面板上方）。 */
    val topSelectedTabSprite: Identifier,
    /** 未选中页签精灵图集 id（页签行在面板下方）。 */
    val bottomTabSprite: Identifier,
    /** 选中页签精灵图集 id（页签行在面板下方）。 */
    val bottomSelectedTabSprite: Identifier,
    /** 左溢出箭头精灵图集 id。 */
    val arrowLeftSprite: Identifier,
    /** 右溢出箭头精灵图集 id。 */
    val arrowRightSprite: Identifier,
    /** 相邻页签之间的间隙。 */
    val tabGap: Dp,
    /**
     * 页签行的左右内边距（**只作用于水平方向**）。
     *
     * 面板铺满整块宽、页签行再左右各缩进这些像素，页签因此不会压到面板边框上
     * （压上去时页签的硬角与面板的圆角会挤在一起）。两侧箭头也在缩进后的区域内。
     */
    val rowPadding: Dp,
    /**
     * 页签**内部**的左右内边距：内容与页签边框之间的距离。
     *
     * 它同时决定页签的横向布局（宽度 = 内容宽 + 两侧内边距）；贴面板那侧的空间由素材边框承担。
     */
    val tabPaddingHorizontal: Dp,
    /**
     * 页签**内部**的上下内边距。
     *
     * 页签高度 = 内容高 + 上下内边距（**没有另外的高度尺寸**），所以页签的高矮由它决定；
     * 选中页签多出的高度是它自己的**底部内边距**，见 [selectedExtraHeight]。
     */
    val tabPaddingVertical: Dp,
    /**
     * 选中页签比未选中**高出**的量：加在它的**底部内边距**上。
     *
     * 贴面板那条边被"贴面板侧对齐"钉住，于是这点底部内边距自然把页签朝远离面板的一侧撑出去，
     * 上下两种朝向都成立、不需要方向判断。
     */
    val selectedExtraHeight: Dp,
) {

    /**
     * 按朝向与选中态取页签精灵 id。
     */
    fun tabSprite(placement: TabStripPlacement, selected: Boolean): Identifier = when (placement) {
        TabStripPlacement.Top    -> if (selected) topSelectedTabSprite else topTabSprite
        TabStripPlacement.Bottom -> if (selected) bottomSelectedTabSprite else bottomTabSprite
    }

    companion object : Codec<TabStripMeta> {

        val default = TabStripMeta(
            panelSprite = identifier("ui/tab_strip/panel"),
            screenPanelSprite = identifier("ui/tab_strip/screen_panel"),
            topTabSprite = identifier("ui/tab_strip/tab/top_inactive"),
            topSelectedTabSprite = identifier("ui/tab_strip/tab/top_active"),
            bottomTabSprite = identifier("ui/tab_strip/tab/bottom_inactive"),
            bottomSelectedTabSprite = identifier("ui/tab_strip/tab/bottom_active"),
            arrowLeftSprite = identifier("ui/tab_strip/arrow_left"),
            arrowRightSprite = identifier("ui/tab_strip/arrow_right"),
            tabGap = 12.dp,
            // 页签行左右缩进：36dp 时第一个页签离面板左缘太远，改成"只让开面板圆角"的量
            rowPadding = 8.dp,
            tabPaddingHorizontal = 22.dp,
            tabPaddingVertical = 8.dp,
            selectedExtraHeight = 6.dp,
        )

        private val codec = Codec.create<TabStripMeta>()
            .field(TabStripMeta::panelSprite).default(default.panelSprite).codec(Codec.ibukigourdIdentifier)
            .field(TabStripMeta::screenPanelSprite).default(default.screenPanelSprite).codec(Codec.ibukigourdIdentifier)
            .field(TabStripMeta::topTabSprite).default(default.topTabSprite).codec(Codec.ibukigourdIdentifier)
            .field(TabStripMeta::topSelectedTabSprite).default(default.topSelectedTabSprite).codec(Codec.ibukigourdIdentifier)
            .field(TabStripMeta::bottomTabSprite).default(default.bottomTabSprite).codec(Codec.ibukigourdIdentifier)
            .field(TabStripMeta::bottomSelectedTabSprite).default(default.bottomSelectedTabSprite).codec(Codec.ibukigourdIdentifier)
            .field(TabStripMeta::arrowLeftSprite).default(default.arrowLeftSprite).codec(Codec.ibukigourdIdentifier)
            .field(TabStripMeta::arrowRightSprite).default(default.arrowRightSprite).codec(Codec.ibukigourdIdentifier)
            .field(TabStripMeta::tabGap).default(default.tabGap).codec(Codec.dp(0.dp..256.dp))
            .field(TabStripMeta::rowPadding).default(default.rowPadding).codec(Codec.dp(0.dp..256.dp))
            .field(TabStripMeta::tabPaddingHorizontal).default(default.tabPaddingHorizontal).codec(Codec.dp(0.dp..256.dp))
            .field(TabStripMeta::tabPaddingVertical).default(default.tabPaddingVertical).codec(Codec.dp(0.dp..256.dp))
            .field(TabStripMeta::selectedExtraHeight).default(default.selectedExtraHeight).codec(Codec.dp(0.dp..256.dp))
            .build(::TabStripMeta)

        override fun serialization(target: TabStripMeta): SerializeElement = codec.serialization(target)

        override fun deserialization(data: SerializeElement): Result<TabStripMeta> =
            codec.deserialization(data)
    }
}

/** 主题 meta 的 tab_strip 段：缺失 / 解码失败回落 [TabStripMeta] 内置默认。 */
val SokitsuThemeMeta.tabStrip: TabStripMeta
    get() = decodeComponent("tab_strip", TabStripMeta, TabStripMeta.default)

/**
 * 页签条用到的全部精灵（已按朝向解析）。
 *
 * 每个精灵都带 `shadow` 图层；**阴影与本体分开画**（见 [TabStrip]），
 * 调用方经 [TabStripDefaults.shadowOnly] / [TabStripDefaults.bodyOnly] 取其中一趟。
 */
@Immutable
data class TabStripSprites(
    /** 面板精灵（闭合九宫格）。 */
    val panel: SokitsuSprite,
    /** 选中页签精灵。 */
    val tabSelected: SokitsuSprite,
    /** 未选中页签精灵。 */
    val tab: SokitsuSprite,
    /** 左溢出箭头精灵。 */
    val arrowLeft: SokitsuSprite,
    /** 右溢出箭头精灵。 */
    val arrowRight: SokitsuSprite,
) {

    /** 按选中态取页签精灵。 */
    fun tabFor(selected: Boolean): SokitsuSprite = if (selected) tabSelected else tab

    /**
     * 页签**素材自身**的盒高（素材画布像素）= 逻辑高 − 贴面板一侧负 border 的外扩量。
     *
     * 取选中 / 未选中里更高的那个。页签的实际高度是"内容 + 内边距"，不由它决定；
     * 它在这里的作用是箭头精灵缺失时 [arrowBoxSize] 的兜底尺寸。
     */
    val tabBoxHeight: Dp get() = maxOf(tabSelected.boxHeight(), tab.boxHeight())

    /**
     * 溢出箭头的边长（设计像素）：stretch 填充按素材自身尺寸画，取左右两个里更大的那个；
     * 素材缺失（空精灵）时回落页签盒高，免得箭头槽位塌成 0、溢出时无从翻页。
     */
    val arrowBoxSize: Dp
        get() = maxOf(arrowLeft.squareSide(), arrowRight.squareSide()).takeIf { it > 0.dp } ?: tabBoxHeight
}

/**
 * 精灵的布局盒高（设计像素）= 逻辑高 − **贴面板一侧**负 border 的外扩量。
 *
 * 负 border 是素材自带的"熔接带"：它画在布局盒之外，所以盒高比画布高矮这一截。
 * 返回值是**素材画布像素**；页签高度不由它决定（页签靠内容 + 内边距撑开），
 * 它只作为箭头精灵缺失时 [TabStripSprites.arrowBoxSize] 的兜底尺寸。
 */
private fun SokitsuSprite.boxHeight(): Dp {
    val outset = layers.maxOfOrNull { layer ->
        val border = (layer.fill as? TextureFill.NinePatch)?.border ?: return@maxOfOrNull 0
        maxOf(-border.top, -border.bottom, 0)
    } ?: 0
    return (logicalHeight - outset).dp
}

/** 精灵的方形边长（设计像素，取宽高更大者）：stretch 填充按素材自身尺寸绘制时用。 */
private fun SokitsuSprite.squareSide(): Dp = maxOf(logicalWidth, logicalHeight).dp

/**
 * 页签条的主题桥接：组合内不直接读取 [SokitsuThemeMeta.tabStrip]。
 *
 * 素材表达不了的尺寸（间隙、行内边距、页签内边距、选中页签多出的高度）登记在 [TabStripMeta] 里，
 * 这里内联转发；素材自己就有的尺寸（页签盒高、箭头边长）从精灵自身算（[TabStripSprites]），
 * 不登记也不硬编码。两者单位都是 dp，调用点直接吃。
 * 本对象另放**行为参数**（动画时长、混色比例），它们与素材无关、也不随缩放变化。
 */
object TabStripDefaults {

    /** 当前主题的 tab_strip meta。 */
    inline val meta get() = SokitsuThemeMeta.tabStrip

    /** 相邻页签间隙：内联转发 [TabStripMeta.tabGap]。 */
    inline val tabGap: Dp get() = meta.tabGap

    /** 溢出箭头边长（dp）：取自精灵自身，见 [TabStripSprites.arrowBoxSize]。 */
    @Composable @ReadOnlyComposable
    fun arrowSize(sprites: TabStripSprites): Dp = sprites.arrowBoxSize * LocalSokitsuPixelScale.current

    /** 页签行左右内边距：内联转发 [TabStripMeta.rowPadding]。 */
    inline val rowPadding: Dp get() = meta.rowPadding

    /** 页签内部左右内边距：内联转发 [TabStripMeta.tabPaddingHorizontal]。 */
    inline val tabPaddingHorizontal: Dp get() = meta.tabPaddingHorizontal

    /** 页签内部上下内边距：内联转发 [TabStripMeta.tabPaddingVertical]。 */
    inline val tabPaddingVertical: Dp get() = meta.tabPaddingVertical

    /** 选中页签多出的高度：内联转发 [TabStripMeta.selectedExtraHeight]。 */
    inline val selectedExtraHeight: Dp get() = meta.selectedExtraHeight


    /**
     * 按朝向解析全部精灵。
     *
     * @param placement 页签行相对面板的位置
     */
    @Composable
    fun sprites(placement: TabStripPlacement): TabStripSprites = TabStripSprites(
        panel = SokitsuThemeMeta.uiSprite(meta.panelSprite),
        tabSelected = SokitsuThemeMeta.uiSprite(meta.tabSprite(placement, selected = true)),
        tab = SokitsuThemeMeta.uiSprite(meta.tabSprite(placement, selected = false)),
        arrowLeft = SokitsuThemeMeta.uiSprite(meta.arrowLeftSprite),
        arrowRight = SokitsuThemeMeta.uiSprite(meta.arrowRightSprite),
    )

    /**
     * 按朝向解析全部精灵。
     *
     * @param placement 页签行相对面板的位置
     */
    @Composable
    fun screenPanelSprites(placement: TabStripPlacement): TabStripSprites = TabStripSprites(
        panel = SokitsuThemeMeta.uiSprite(meta.screenPanelSprite),
        tabSelected = SokitsuThemeMeta.uiSprite(meta.tabSprite(placement, selected = true)),
        tab = SokitsuThemeMeta.uiSprite(meta.tabSprite(placement, selected = false)),
        arrowLeft = SokitsuThemeMeta.uiSprite(meta.arrowLeftSprite),
        arrowRight = SokitsuThemeMeta.uiSprite(meta.arrowRightSprite),
    )

    /**
     * 只保留阴影图层：单独一趟画在**最底层**（面板与页签本体都压在它上面）。
     *
     * 阴影层是整块轮廓，与本体同面积：随本体一起画会遮住本体的先画部分、画在本体之后又会把它压暗，
     * 故必须与本体拆开；而它又必须在面板之前提交，页面层级才是
     * 「投影 → 面板 → 页签本体」（见 [TabStrip]）。
     */
    fun shadowOnly(sprite: SokitsuSprite): SokitsuSprite = sprite.filterLayer { it.isShadow }

    /** 只保留非阴影图层（页签 / 箭头的可见部分）。 */
    fun bodyOnly(sprite: SokitsuSprite): SokitsuSprite = sprite.filterLayer { !it.isShadow }

    /** 翻页滑动动画时长（毫秒）。 */
    const val SlideDurationMillis = 160

    /** 未选中页签悬停时向 [TabStripColors.tabHighlight] 靠拢的比例。 */
    const val HoverBlend = 0.6f

    /** 未选中页签按下时向描边色靠拢的比例。 */
    const val PressedBlend = 0.18f

    /**
     * 默认配色：色板语义与旧占位实现一致 ——
     * 面板 → 弱化容器色，未选中页签 → 容器色，选中页签 → **与面板同色**（连体），
     * 箭头 → 容器色。
     *
     * 参数默认 [Color.Unspecified] 语义是"按 [TabStripTokens] 映射表结合当前主题解析"；
     * 回退顺序：`调用点传参` > [moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuColor]
     * 作用域 > [TabStripTokens] > [moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorScheme]。
     *
     * @param panelColor 面板 tone 层底色 → [TabStripTokens.Panel]
     * @param tabColor 未选中页签 tone 层底色 → [TabStripTokens.Tab]
     * @param selectedTabColor 选中页签 tone 层底色，默认与 [panelColor] 同源（连体）
     * @param outlineColor outline 层描边色 → [TabStripTokens.Outline]
     * @param contentColor 未选中页签内容色 → [TabStripTokens.Content]
     * @param selectedContentColor 选中页签内容色 → [TabStripTokens.ContentSelected]
     * @param arrowColor 箭头 tone 层底色 → [TabStripTokens.Arrow]
     * @param tabHighlightColor 未选中页签悬停时的 tone 提亮目标色，默认取 [tabColor] 提亮结果
     */
    @Composable
    fun colors(
        panelColor: Color = Color.Unspecified,
        tabColor: Color = Color.Unspecified,
        selectedTabColor: Color = Color.Unspecified,
        outlineColor: Color = Color.Unspecified,
        contentColor: Color = Color.Unspecified,
        selectedContentColor: Color = Color.Unspecified,
        arrowColor: Color = Color.Unspecified,
        tabHighlightColor: Color = Color.Unspecified,
    ): TabStripColors {
        val panel = panelColor.resolve(TabStripTokens.Panel)
        val tab = tabColor.resolve(TabStripTokens.Tab)
        return TabStripColors(
            panel = panel,
            tab = tab,
            // 选中页签默认与面板同色：两者由同一张图的 tone 层染色，同色才连成一体
            tabSelected = selectedTabColor.takeOrElse { panel },
            outline = outlineColor.resolve(TabStripTokens.Outline),
            tabHighlight = tabHighlightColor.takeIf { it != Color.Unspecified }
                ?: lerp(tab, Color.White, HighlightBlend),
            content = contentColor.resolve(TabStripTokens.Content),
            contentSelected = selectedContentColor.resolve(TabStripTokens.ContentSelected),
            arrow = arrowColor.resolve(TabStripTokens.Arrow),
        )
    }

    /** 悬停提亮目标的默认提亮比例（[TabStripColors.tabHighlight] 的推导用）。 */
    const val HighlightBlend = 0.35f
}

/**
 * 页签条配色集：**只有一个染色色维度** —— 每个参与绘制的精灵各取一个 tone 层底色。
 *
 * 字段全部已解析，因此是普通 data class，`copy(...)` 即精准覆盖；
 * "槽位映射到主题哪里"由 [TabStripDefaults.colors] 承担。
 */
@Immutable
data class TabStripColors(
    /** 面板 tone 层底色（选中页签默认取同色以形成连体）。 */
    val panel: Color,
    /** 未选中页签 tone 层底色。 */
    val tab: Color,
    /** 选中页签 tone 层底色。 */
    val tabSelected: Color,
    /** outline 层描边色（悬停 / 聚焦时可由调用点覆盖）。 */
    val outline: Color,
    /** 未选中页签悬停时的 tone 提亮目标色。 */
    val tabHighlight: Color,
    /** 未选中页签内容色。 */
    val content: Color,
    /** 选中页签内容色。 */
    val contentSelected: Color,
    /** 溢出箭头 tone 层底色。 */
    val arrow: Color,
) {

    /** 按选中态取页签 tone 层底色。 */
    fun toneFor(selected: Boolean): Color = if (selected) tabSelected else tab
}
