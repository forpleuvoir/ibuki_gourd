package moe.forpleuvoir.ibukigourd.ui.sokitsu.tooltip

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.render.extension.AnchorPosition
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeToken
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.uiSprite
import moe.forpleuvoir.ibukigourd.util.codec.dp
import moe.forpleuvoir.ibukigourd.util.codec.dpSize
import moe.forpleuvoir.ibukigourd.util.codec.ibukigourdIdentifier
import moe.forpleuvoir.ibukigourd.util.codec.padding
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.codec.Codec
import moe.forpleuvoir.nebula.serialization.codec.duration
import net.minecraft.resources.Identifier
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * 气泡提示的主题接入声明：token 映射（"什么颜色"）+ 尺寸/纹理 meta（"多大、用哪张图"）合一。
 *
 * 与 [moe.forpleuvoir.ibukigourd.ui.sokitsu.ButtonTokens] / [moe.forpleuvoir.ibukigourd.ui.sokitsu.SliderTokens]
 * 同构：本文件只声明该组件映射到主题的哪些语义槽位，主题核心（[SokitsuThemeMeta]）不认识具体组件。
 */
object TooltipTokens {

    /** 气泡体容器精灵的染色色板：中性容器色，与页面主体弱区分。 */
    val Body = ColorSchemeToken.SurfaceVariant

    /**
     * 气泡内文字颜色：[ColorSchemeToken.OnSurfaceVariant] —— 与 [Body]（[ColorSchemeToken.SurfaceVariant]）
     * 配对的内容色，经 [moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.fromToken]
     * 解析为当前主题色板后取 base（调用点显式传 `contentColor` 可覆盖）。
     */
    val Content = ColorSchemeToken.OnSurfaceVariant
}

/**
 * 气泡提示的 meta：**单位均为 dp**（逻辑像素，消费端转 `.dp`；与屏幕像素的换算由主题的 pixel_scale 决定）。
 *
 * ```jsonc
 * ui_meta: {
 *   tooltip: {
 *     min_width: 96, min_height: 32,
 *     padding_horizontal: 10, padding_vertical: 6,
 *     spacing: 9,
 *     delay: "400ms", exit_duration: "150ms",
 *     bubble_sprite: "ui/tooltip/bubble",
 *     arrow_above:  "ui/tooltip/arrow/above",
 *     arrow_below:  "ui/tooltip/arrow/below",
 *     arrow_left:   "ui/tooltip/arrow/left",
 *     arrow_right:  "ui/tooltip/arrow/right"
 *   }
 * }
 * ```
 *
 * 四张箭头精灵为独立文件（绘制只支持轴对齐四边形，无旋转/镜像，不能靠一张图旋转出四向），
 * 位于 `arrow/` 子目录，文件名 `above/below/left/right` 表示"气泡在目标 X 侧"（非"箭头指向 X"），
 * 与气泡位置（[AnchorPosition]）**1:1 对应**，见 [arrow]。
 * 箭头嵌入边的 border 取负值以探入气泡缺口，纹理规格见
 * [sokitsuBubbleSprite][moe.forpleuvoir.ibukigourd.ui.sokitsu.draw.sokitsuBubbleSprite]。
 */
data class TooltipMeta(
    /**
     * 气泡最小尺寸（dp）：兜底内容/箭头尺寸，防止气泡体过小导致缺口计算非法。
     * 宽须容纳箭头横向（约 16×pixelScale） + 两侧 border；高须容纳左右向箭头的纵向（约 16×pixelScale） + 上下 border。
     */
    val minSize: DpSize,
    /** 气泡内文字内边距。 */
    val padding: PaddingValues,
    /** 气泡体与锚组件之间的安全间距（同时充当与屏幕边缘的安全间距）。 */
    val spacing: Dp,
    /** 指针悬停到气泡展示之间的延迟，避免指针划过时频繁弹窗。 */
    val delay: Duration,
    /**
     * 进出场动画时长，同时是展示态转隐藏后到弹层卸载的预留时长：
     * 退出动画播完后弹层才卸载，故它必须不小于退出动画时长。
     */
    val exitDuration: Duration,
    /** 气泡体九宫格精灵（凹槽/凸起同套图层约定，圆角与描边须全部落在 border 内）。 */
    val bubbleSprite: Identifier,
    /** 四个方向的箭头精灵（子目录 `arrow/`，文件名 = 气泡在目标 X 侧）。 */
    val arrowAbove: Identifier,
    val arrowBelow: Identifier,
    val arrowLeft: Identifier,
    val arrowRight: Identifier,
) {

    /**
     * 按气泡位置取对应的箭头精灵 id。
     *
     * [anchor]（[AnchorPosition]）即"气泡在目标的 X 侧"，与 `arrow/` 子目录文件名
     * `above/below/left/right`（气泡位置语义）**1:1**，无需任何反转映射：
     * - [AnchorPosition.Above]（气泡在目标上方）→ `arrow/above`
     * - [AnchorPosition.Below]（气泡在目标下方）→ `arrow/below`
     * - [AnchorPosition.Left]（气泡在目标左侧）→ `arrow/left`
     * - [AnchorPosition.Right]（气泡在目标右侧）→ `arrow/right`
     *
     * 箭头所在边（气泡体哪条边贴箭头）为上述位置的反向。
     */
    fun arrow(anchor: AnchorPosition): Identifier = when (anchor) {
        AnchorPosition.Above -> arrowAbove
        AnchorPosition.Below -> arrowBelow
        AnchorPosition.Left -> arrowLeft
        AnchorPosition.Right -> arrowRight
    }

    companion object : Codec<TooltipMeta> {

        val default = TooltipMeta(
            minSize = DpSize(48.dp, 48.dp),
            padding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            spacing = 9.dp,
            delay = 100.milliseconds,
            exitDuration = 150.milliseconds,
            bubbleSprite = identifier("ui/tooltip/bubble"),
            arrowAbove = identifier("ui/tooltip/arrow/above"),
            arrowBelow = identifier("ui/tooltip/arrow/below"),
            arrowLeft = identifier("ui/tooltip/arrow/left"),
            arrowRight = identifier("ui/tooltip/arrow/right"),
        )

        private val codec = Codec.create<TooltipMeta>()
            .field(TooltipMeta::minSize).default(default.minSize).codec(Codec.dpSize(16.dp..1024.dp, 16.dp..512.dp))
            .field(TooltipMeta::padding).default(default.padding).codec(Codec.padding(0.dp..64.dp))
            .field(TooltipMeta::spacing).default(default.spacing).codec(Codec.dp(0.dp..32.dp))
            .field(TooltipMeta::delay).default(default.delay).codec(Codec.duration)
            .field(TooltipMeta::exitDuration).default(default.exitDuration).codec(Codec.duration)
            .field(TooltipMeta::bubbleSprite).default(default.bubbleSprite).codec(Codec.ibukigourdIdentifier)
            .field(TooltipMeta::arrowAbove).default(default.arrowAbove).codec(Codec.ibukigourdIdentifier)
            .field(TooltipMeta::arrowBelow).default(default.arrowBelow).codec(Codec.ibukigourdIdentifier)
            .field(TooltipMeta::arrowLeft).default(default.arrowLeft).codec(Codec.ibukigourdIdentifier)
            .field(TooltipMeta::arrowRight).default(default.arrowRight).codec(Codec.ibukigourdIdentifier)
            .build(::TooltipMeta)

        override fun serialization(target: TooltipMeta): SerializeElement = codec.serialization(target)

        override fun deserialization(data: SerializeElement): Result<TooltipMeta> = codec.deserialization(data)
    }
}

/**
 * Tooltip 组件的主题桥接：组合内**不直接**读取 [SokitsuThemeMeta.tooltip]，所有尺寸 /
 * 纹理 id / 间距均经本对象转发，精灵解析也集中在这里——与 [moe.forpleuvoir.ibukigourd.ui.sokitsu.ButtonDefaults] /
 * [moe.forpleuvoir.ibukigourd.ui.sokitsu.SliderDefaults] 同构。资源重载或主题切换后自动刷新
 * （[meta] 为内联属性，每次读取即取当前主题值）。
 */
object TooltipDefaults {

    /** 当前主题的 tooltip meta（尺寸/纹理 id/间距）；内联转发 [SokitsuThemeMeta.tooltip]。 */
    inline val meta get() = SokitsuThemeMeta.tooltip

    /** 气泡体精灵：按 [TooltipMeta.bubbleSprite] 经 UI 图集解析。 */
    fun bodySprite(): SokitsuSprite = SokitsuThemeMeta.uiSprite(meta.bubbleSprite)

    /** 气泡最小尺寸：内联转发 [TooltipMeta.minSize]。 */
    inline val minSize: DpSize get() = meta.minSize

    /** 气泡内边距：内联转发 [TooltipMeta.padding]。 */
    inline val padding: PaddingValues get() = meta.padding

    /** 与锚组件（及屏幕边缘）的间距：内联转发 [TooltipMeta.spacing]。 */
    inline val spacing: Dp get() = meta.spacing

    /** 悬停延迟：内联转发 [TooltipMeta.delay]。 */
    inline val delay: Duration get() = meta.delay

    /** 出场动画时长（兼卸载预留时长）：内联转发 [TooltipMeta.exitDuration]。 */
    inline val exitDuration: Duration get() = meta.exitDuration

    /**
     * 箭头精灵：按 [anchor]（气泡位置）经 [TooltipMeta.arrow] 取 id 后解析。
     * [anchor] 与文件名 1:1，无需反转。
     */
    fun arrowSprite(anchor: AnchorPosition): SokitsuSprite = SokitsuThemeMeta.uiSprite(meta.arrow(anchor))
}

/**
 * 主题 meta 的气泡提示段：缺失/解码失败回落 [TooltipMeta] 内置默认。
 */
val SokitsuThemeMeta.tooltip: TooltipMeta
    get() = decodeComponent("tooltip", TooltipMeta, TooltipMeta.default)
