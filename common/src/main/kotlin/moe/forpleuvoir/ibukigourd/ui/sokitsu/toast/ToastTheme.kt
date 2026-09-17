package moe.forpleuvoir.ibukigourd.ui.sokitsu.toast

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeToken
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.uiSprite
import moe.forpleuvoir.ibukigourd.util.codec.dp
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
 * 提示（Toast）的主题接入声明：token 映射（"什么颜色"）。
 *
 * 面板复用气泡提示的素材与配色（见 [ToastMeta.sprite]），故两个 token 与
 * [moe.forpleuvoir.ibukigourd.ui.sokitsu.tooltip.TooltipTokens] 取值一致，
 * 使同一屏内的气泡与提示视觉同族。
 */
object ToastTokens {

    /** 面板精灵的染色色板：中性容器色。 */
    val Body = ColorSchemeToken.SurfaceVariant

    /** 提示内文字与图标的颜色：与 [Body] 配对的内容色（调用点显式传 `contentColor` 可覆盖）。 */
    val Content = ColorSchemeToken.OnSurfaceVariant
}

/**
 * 提示的 meta：**单位均为 dp**（逻辑像素，消费端转 `.dp`）。
 *
 * ```jsonc
 * ui_meta: {
 *   toast: {
 *     padding_horizontal: 18, padding_vertical: 12,
 *     min_width: 64,
 *     progress_height: 2, progress_inset: 0,
 *     enter_duration: "300ms", exit_duration: "300ms",
 *     enter_slide_fraction: 1.0, exit_slide_fraction: 0.25,
 *     sprite: "ui/tooltip/bubble"
 *   }
 * }
 * ```
 *
 * 进出场动画不是数据类字段（[androidx.compose.animation.EnterTransition] 无法序列化）：
 * 本 meta 只存时长与位移比例，由 [ToastDefaults.animation] 组装成 [ToastAnimation]。
 *
 * `enter_slide_fraction` / `exit_slide_fraction` 是**相对自身高度**的位移比例：
 * 进场从下方 `enter_slide_fraction × 高度` 处滑入，出场向该方向滑出 `exit_slide_fraction × 高度`。
 */
data class ToastMeta(
    /** 面板内容内边距。 */
    val padding: PaddingValues,
    /** 面板最小宽度：内容很窄时兜底，避免气泡被压成药丸。 */
    val minWidth: Dp,
    /** 倒计时条高度（贴面板底边内侧，宽度随展示进度收缩）。 */
    val progressHeight: Dp,
    /**
     * 倒计时条在**自动内缩**之上的额外上移量。
     *
     * 自动内缩 = 面板精灵底边的九宫格边框厚度（素材 border × 像素放大倍率 ÷ 素材密度），
     * 用来让进度条躲开面板描边 —— 不躲的话条与描边同色叠在一起，等于看不见。
     * 本字段只用于在此之上再抬高一点，缺省 0（贴描边内侧）。
     */
    val progressInset: Dp,
    /** 进场动画时长。 */
    val enterDuration: Duration,
    /** 出场动画时长；同时是 [ToastHandler] 的退场宽限期来源，退场期间条目仍留在活动列表。 */
    val exitDuration: Duration,
    /** 进场纵向位移比例（相对自身高度，正数 = 从下方滑入）。 */
    val enterSlideFraction: Float,
    /** 出场纵向位移比例（相对自身高度，正数 = 向上滑出）。 */
    val exitSlideFraction: Float,
    /** 面板九宫格精灵：默认复用气泡体，圆角与描边须全部落在 border 内。 */
    val sprite: Identifier,
) {

    companion object : Codec<ToastMeta> {

        val default = ToastMeta(
            padding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            minWidth = 64.dp,
            progressHeight = 2.dp,
            progressInset = 0.dp,
            enterDuration = 300.milliseconds,
            exitDuration = 300.milliseconds,
            enterSlideFraction = 1f,
            exitSlideFraction = 0.25f,
            sprite = identifier("ui/tooltip/bubble"),
        )

        private val codec = Codec.create<ToastMeta>()
            .field(ToastMeta::padding).default(default.padding).codec(Codec.padding(0.dp..64.dp))
            .field(ToastMeta::minWidth).default(default.minWidth).codec(Codec.dp(0.dp..1024.dp))
            .field(ToastMeta::progressHeight).default(default.progressHeight).codec(Codec.dp(0.dp..32.dp))
            .field(ToastMeta::progressInset).default(default.progressInset).codec(Codec.dp(0.dp..64.dp))
            .field(ToastMeta::enterDuration).default(default.enterDuration).codec(Codec.duration)
            .field(ToastMeta::exitDuration).default(default.exitDuration).codec(Codec.duration)
            .field(ToastMeta::enterSlideFraction).default(default.enterSlideFraction).codec(Codec.float(0f..4f))
            .field(ToastMeta::exitSlideFraction).default(default.exitSlideFraction).codec(Codec.float(0f..4f))
            .field(ToastMeta::sprite).default(default.sprite).codec(Codec.ibukigourdIdentifier)
            .build(::ToastMeta)

        override fun serialization(target: ToastMeta): SerializeElement = codec.serialization(target)

        override fun deserialization(data: SerializeElement): Result<ToastMeta> = codec.deserialization(data)
    }
}

/**
 * 提示组件的主题桥接：组合内**不直接**读取 [SokitsuThemeMeta.toast]，
 * 尺寸 / 纹理 id 均经本对象转发，精灵解析也集中在这里。
 *
 * 资源重载或主题切换后自动刷新（[meta] 为内联属性，每次读取即取当前主题值）。
 */
object ToastDefaults {

    /** 当前主题的 toast meta；内联转发 [SokitsuThemeMeta.toast]。 */
    inline val meta get() = SokitsuThemeMeta.toast

    /** 面板精灵：按 [ToastMeta.sprite] 经 UI 图集解析。 */
    fun sprite(): SokitsuSprite = SokitsuThemeMeta.uiSprite(meta.sprite)

    /** 面板内容内边距：内联转发 [ToastMeta.padding]。 */
    inline val padding: PaddingValues get() = meta.padding

    /** 面板最小宽度：内联转发 [ToastMeta.minWidth]。 */
    inline val minWidth: Dp get() = meta.minWidth

    /** 倒计时条高度：内联转发 [ToastMeta.progressHeight]。 */
    inline val progressHeight: Dp get() = meta.progressHeight

    /** 倒计时条额外上移量（在自动躲开描边的内缩之上）：内联转发 [ToastMeta.progressInset]。 */
    inline val progressInset: Dp get() = meta.progressInset

    /** 退场动画时长：内联转发 [ToastMeta.exitDuration]。 */
    inline val exitDuration: Duration get() = meta.exitDuration

    /**
     * 由 meta 组装的默认进出场动画。
     *
     * 每次读取都新建 [ToastAnimation]（内部含 `EnterTransition` 实例），组合内应
     * `remember(ToastDefaults.meta) { ... }` 缓存，避免每帧向 `AnimatedVisibility` 提交新实例。
     */
    val animation: ToastAnimation
        get() {
            val meta = meta
            val enterMs = meta.enterDuration.inWholeMilliseconds.toInt().coerceAtLeast(0)
            val exitMs = meta.exitDuration.inWholeMilliseconds.toInt().coerceAtLeast(0)
            val enterFraction = meta.enterSlideFraction
            val exitFraction = meta.exitSlideFraction
            return ToastAnimation(
                enter = fadeIn(tween(enterMs)) +
                        slideInVertically(tween(enterMs)) { (it * enterFraction).toInt() },
                exit = fadeOut(tween(exitMs)) +
                        slideOutVertically(tween(exitMs)) { -(it * exitFraction).toInt() },
            )
        }
}

/** 主题 meta 的提示段：缺失 / 解码失败回落 [ToastMeta] 内置默认。 */
val SokitsuThemeMeta.toast: ToastMeta
    get() = decodeComponent("toast", ToastMeta, ToastMeta.default)
