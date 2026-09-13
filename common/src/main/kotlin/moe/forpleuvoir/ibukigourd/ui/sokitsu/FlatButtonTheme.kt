package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeToken
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.util.codec.dpSize
import moe.forpleuvoir.ibukigourd.util.codec.padding
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.codec.Codec
import moe.forpleuvoir.nebula.serialization.codec.list

/**
 * [FlatButton] 的主题接入声明（同 [ButtonTheme] 的三件套模式：Token + Meta + 扩展属性）。
 */
object FlatButtonTokens {

    /** 背景精灵的染色色板（`flat_button` 素材 `base` 层 `level=tone`）。 */
    val Container = ColorSchemeToken.Primary

    /** 内容色（文字/图标）的基准色；各状态的实际内容色见 [FlatButtonMeta.contentAlpha]。 */
    val Content = ColorSchemeToken.Primary
}

/**
 * 扁平按钮的 meta：**尺寸单位均为 dp**（逻辑像素，消费端转 `.dp`）。
 *
 * ```jsonc
 * ui_meta: {
 *   flat_button: {
 *     min_width: 40, min_height: 40,
 *     padding_horizontal: 12, padding_vertical: 8,
 *     content_blend: [0, 0.4, 0.25, 0],  // normal / pressed / focused / disabled
 *     disabled_blend: 0.5
 *   }
 * }
 * ```
 *
 * [sprite] 为四态纹理标识；素材只画了 `pressed` / `focused` 两张，
 * `normal` / `disabled` **无对应资源** → 图集查询落空 → 该状态不渲染背景（保持透明）。
 * 若日后补上这两个文件，无需改代码即自动生效。
 *
 * [contentBlend] 是**逐状态的内容色修正系数** `t ∈ [0, 1]`（索引顺序必须与 [UiState] 的声明顺序一致：
 * Normal / Pressed / Focused / Disabled，长度不足时缺失状态按 `0f` 处理）。
 *
 * 语义 = **内容色向纯黑/纯白混合的比例**（线性混色，**不涉及 alpha**），用来拉大内容与背景的明暗差：
 * - `t = 0`：完全保持基准内容色
 * - `t = 1`：完全变成黑或白
 * - 方向按当前主题：亮色主题 → 向黑（压暗，背景偏亮）；暗色主题 → 向白（提亮）
 *
 * 用途：背景是"底色（tone）调淡后叠在父容器上"，浓度越高、与同色系内容越容易糊；
 * 而**背景的实际浓度藏在素材像素里（代码读不到）**，所以由这个系数人工表达 ——
 * 背景越实，系数取值越大。**不要朝"底色的配对内容色"混**（实测亮色主题下会把内容洗白：
 * 配对色 itself 是亮色，而背景实际是偏亮的淡化块）。
 */
data class FlatButtonMeta(
    /** 按钮最小尺寸。 */
    val minSize: DpSize,
    /** 内容内边距。 */
    val padding: PaddingValues,
    /** 按钮的四态纹理。 */
    val sprite: UiStateIdentifier,
    /** 逐状态的内容色修正系数（顺序同 [UiState]：normal / pressed / focused / disabled）。 */
    val contentBlend: List<Float>,
    /**
     * **禁用态**专用的内容色弱化比例：内容色向背景色（`ColorScheme.surface`）混合该比例。
     * `0` = 不弱化（与常态一致），`1` = 完全融入背景。
     *
     * 与 [contentBlend] 方向相反（那个是拉大与背景的明暗差，这里是缩小），
     * 单调的正系数无法同时表达两个方向，故单独成字段。
     */
    val disabledBlend: Float,
) {

    /** 取 [state] 的内容色修正系数；越界回落 `0f`（不修改内容色）。 */
    fun contentBlendOf(state: UiState): Float = contentBlend.getOrElse(state.ordinal) { 0f }

    companion object : Codec<FlatButtonMeta> {

        /**
         * 各状态内容色修正系数的默认值（顺序 normal / pressed / focused / disabled）：
         * 有背景的两个状态按素材浓度取值（pressed α200 > focused α127），
         * 无背景的 normal / disabled 不修正。改素材透明度时应同步这些值。
         */
        val defaultContentBlend = listOf(0f, 0.4f, 0.25f, 0f)

        val default = FlatButtonMeta(
            minSize = DpSize(40.dp, 40.dp),
            padding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            sprite = UiStateIdentifier(
                normal = identifier("ui/flat_button/normal"),
                pressed = identifier("ui/flat_button/pressed"),
                focused = identifier("ui/flat_button/focused"),
                disabled = identifier("ui/flat_button/disabled"),
            ),
            contentBlend = defaultContentBlend,
            disabledBlend = 0.5f,
        )

        private val codec = Codec.create<FlatButtonMeta>()
            .field(FlatButtonMeta::minSize).default(default.minSize).codec(Codec.dpSize(1.dp..512.dp, 1.dp..512.dp))
            .field(FlatButtonMeta::padding).default(default.padding).codec(Codec.padding(0.dp..512.dp))
            .field(FlatButtonMeta::sprite).default(default.sprite).codec(UiStateIdentifier)
            .field(FlatButtonMeta::contentBlend).default(default.contentBlend)
            .codec(Codec.list(Codec.float(0f..1f)))
            .field(FlatButtonMeta::disabledBlend).default(default.disabledBlend).codec(Codec.float(0f..1f))
            .build(::FlatButtonMeta)

        override fun serialization(target: FlatButtonMeta): SerializeElement = codec.serialization(target)

        override fun deserialization(data: SerializeElement): Result<FlatButtonMeta> = codec.deserialization(data)
    }
}

/**
 * 主题 meta 的扁平按钮段：缺失/解码失败回落 [FlatButtonMeta] 内置默认。
 */
val SokitsuThemeMeta.flatButton: FlatButtonMeta
    get() = decodeComponent("flat_button", FlatButtonMeta, FlatButtonMeta.default)
