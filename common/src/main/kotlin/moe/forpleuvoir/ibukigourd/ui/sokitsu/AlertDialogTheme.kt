package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.animation.core.Easing
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogTransition
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeToken
import moe.forpleuvoir.ibukigourd.util.math.easing.EasingCurve
import moe.forpleuvoir.ibukigourd.util.math.easing.EasingDirection
import moe.forpleuvoir.ibukigourd.util.math.easing.ease
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.util.codec.dp
import moe.forpleuvoir.ibukigourd.util.codec.ibukigourdIdentifier
import moe.forpleuvoir.ibukigourd.util.codec.padding
import moe.forpleuvoir.ibukigourd.util.math.easing.Ease
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.codec.Codec
import net.minecraft.resources.Identifier

/**
 * 提示对话框（[AlertDialog]）的主题接入声明：token 映射（"什么颜色"）+ 尺寸 meta（"多大/多密"）合一。
 *
 * 一个组件与主题的全部耦合集中在这个文件里，分两部分：
 * 1. [AlertDialogTokens] —— 组件各部位映射到主题的哪个语义槽位；
 * 2. [AlertDialogMeta] —— 组件的数值型默认参数（**单位均为 dp**），可被主题资源包的
 *    `sokitsu_meta.json` 中 `ui_meta.alert_dialog` 段覆盖。
 *
 * 与 [ButtonTheme.kt] / [SwitchTheme.kt] 同规矩：[SokitsuThemeMeta] 不认识任何具体组件，
 * 每个组件自行以"Token 对象 + Meta 数据类 + 扩展属性"的三件套接入。
 */
object AlertDialogTokens {

    /** 面板容器精灵的染色色板：与主 surface 拉开一档，弹层才立得起来。 */
    val Container = ColorSchemeToken.SurfaceVariant

    /** 标题内容色：取主表面内容色，比正文更醒目。 */
    val TitleContent = ColorSchemeToken.OnSurface

    /** 正文内容色：取表面变体的配对内容色，弱于标题。 */
    val TextContent = ColorSchemeToken.OnSurfaceVariant

    /** 图标着色：默认与标题同源。 */
    val IconContent = ColorSchemeToken.OnSurface
}

/**
 * 提示对话框单段动画（入场一段、退场一段）的参数：**单位均为 dp / 毫秒**。
 *
 * 位移与透明度共用一条进度：透明度 = 进度值，纵向位移 = `(1 - 进度值) × offset`
 * （正数即向屏幕下方偏移，因此"从下方滑入"由入场段的正 offset 表达）。
 *
 * 缓动 = [curve] + [direction]：入场默认 `cubic/out`（减速抵达），
 * 退场默认 `cubic/in`（进度反向，投影到屏幕上就是"离场加速"）。
 *
 * ```jsonc
 * { "duration_millis": 160, "offset": 16, "curve": "cubic", "direction": "out" }
 * ```
 *
 * 各字段的默认值取 [defaultEnter] —— 只在"该段对象存在但缺键"时兜底；
 * "整段缺失"由 [AlertDialogMeta] 的字段默认值兜底（入场 / 退场各自不同）。
 */
data class AlertDialogAnimationMeta(
    /** 动画时长（毫秒）。 */
    val durationMillis: Int,
    /** 纵向位移距离（dp）：入场从 +offset 滑到 0，退场从 0 滑回 +offset。 */
    val offset: Dp,
    /** 缓动曲线。 */
    val curve: EasingCurve,
    /** 缓动方向。 */
    val direction: EasingDirection,
) {

    /** 该段动画的缓动函数（曲线 + 方向）。 */
    val easing: Ease get() = curve.ease(direction)

    companion object : Codec<AlertDialogAnimationMeta> {

        val defaultEnter = AlertDialogAnimationMeta(
            durationMillis = 160,
            offset = 16.dp,
            curve = EasingCurve.Cubic,
            direction = EasingDirection.Out,
        )

        val defaultExit = AlertDialogAnimationMeta(
            durationMillis = 110,
            offset = 16.dp,
            curve = EasingCurve.Cubic,
            direction = EasingDirection.In,
        )

        private val codec = Codec.create<AlertDialogAnimationMeta>()
            .field(AlertDialogAnimationMeta::durationMillis).default(defaultEnter.durationMillis).codec(Codec.int(0..5000))
            .field(AlertDialogAnimationMeta::offset).default(defaultEnter.offset).codec(Codec.dp(0.dp..256.dp))
            .field(AlertDialogAnimationMeta::curve).default(defaultEnter.curve).codec(EasingCurve)
            .field(AlertDialogAnimationMeta::direction).default(defaultEnter.direction).codec(EasingDirection)
            .build(::AlertDialogAnimationMeta)

        override fun serialization(target: AlertDialogAnimationMeta): SerializeElement = codec.serialization(target)

        override fun deserialization(data: SerializeElement): Result<AlertDialogAnimationMeta> = codec.deserialization(data)
    }
}

/**
 * 提示对话框的 meta：**单位均为 dp**（逻辑像素，消费端转 `.dp`）。
 *
 * ```jsonc
 * ui_meta: {
 *   alert_dialog: {
 *     min_width: 280, max_width: 560,
 *     content_padding: 24, buttons_top_padding: 24, button_spacing: 8,
 *     icon_bottom_padding: 16, title_bottom_padding: 12,
 *     panel_sprite: "ui/surface/float_panel",
 *     enter_animation: { duration_millis: 160, offset: 16, curve: "cubic", direction: "out" },
 *     exit_animation:  { duration_millis: 110, offset: 16, curve: "cubic", direction: "in" }
 *   }
 * }
 * ```
 *
 * 尺寸上限 [maxWidth] 只约束面板宽度；实际可用宽度还受平台对话框限宽
 * （`DialogProperties.usePlatformDefaultWidth`）与屏幕尺寸约束，取三者最小值。
 */
data class AlertDialogMeta(
    /** 面板最小宽度。 */
    val minWidth: Dp,
    /** 面板最大宽度（超出则由内容换行收窄）。 */
    val maxWidth: Dp,
    /** 面板内容内边距（四边统一）。 */
    val contentPadding: PaddingValues,
    /** 按钮行与上方内容之间的间距。 */
    val buttonsTopPadding: Dp,
    /** 两个按钮之间的水平间距。 */
    val buttonSpacing: Dp,
    /** 图标与标题之间的间距。 */
    val iconBottomPadding: Dp,
    /** 标题与正文之间的间距。 */
    val titleBottomPadding: Dp,
    /** 面板精灵：默认复用浮动面板素材（凸起带阴影），与 [SurfaceDefaults.floatingPanel] 同一张。 */
    val panelSprite: Identifier,
    /** 入场动画：默认从下方 16dp 滑入 + 淡入，减速抵达。 */
    val enterAnimation: AlertDialogAnimationMeta,
    /** 退场动画：默认滑回下方 + 淡出，时长更短、进度反向（视觉上加速离场）。 */
    val exitAnimation: AlertDialogAnimationMeta,
) {

    companion object : Codec<AlertDialogMeta> {

        val default = AlertDialogMeta(
            minWidth = 280.dp,
            maxWidth = 560.dp,
            contentPadding = PaddingValues(24.dp),
            buttonsTopPadding = 24.dp,
            buttonSpacing = 8.dp,
            iconBottomPadding = 16.dp,
            titleBottomPadding = 12.dp,
            panelSprite = identifier("ui/surface/float_panel"),
            enterAnimation = AlertDialogAnimationMeta.defaultEnter,
            exitAnimation = AlertDialogAnimationMeta.defaultExit,
        )

        private val codec = Codec.create<AlertDialogMeta>()
            .field(AlertDialogMeta::minWidth).default(default.minWidth).codec(Codec.dp(0.dp..1024.dp))
            .field(AlertDialogMeta::maxWidth).default(default.maxWidth).codec(Codec.dp(0.dp..4096.dp))
            .field(AlertDialogMeta::contentPadding).default(default.contentPadding).codec(Codec.padding(0.dp..256.dp))
            .field(AlertDialogMeta::buttonsTopPadding).default(default.buttonsTopPadding).codec(Codec.dp(0.dp..256.dp))
            .field(AlertDialogMeta::buttonSpacing).default(default.buttonSpacing).codec(Codec.dp(0.dp..256.dp))
            .field(AlertDialogMeta::iconBottomPadding).default(default.iconBottomPadding).codec(Codec.dp(0.dp..256.dp))
            .field(AlertDialogMeta::titleBottomPadding).default(default.titleBottomPadding).codec(Codec.dp(0.dp..256.dp))
            .field(AlertDialogMeta::panelSprite).default(default.panelSprite).codec(Codec.ibukigourdIdentifier)
            .field(AlertDialogMeta::enterAnimation).default(default.enterAnimation).codec(AlertDialogAnimationMeta)
            .field(AlertDialogMeta::exitAnimation).default(default.exitAnimation).codec(AlertDialogAnimationMeta)
            .build(::AlertDialogMeta)

        override fun serialization(target: AlertDialogMeta): SerializeElement = codec.serialization(target)

        override fun deserialization(data: SerializeElement): Result<AlertDialogMeta> = codec.deserialization(data)
    }
}

/**
 * 主题 meta 的 alert_dialog 段：缺失 / 解码失败回落 [AlertDialogMeta] 内置默认。
 */
val SokitsuThemeMeta.alertDialog: AlertDialogMeta
    get() = decodeComponent("alert_dialog", AlertDialogMeta, AlertDialogMeta.default)

/**
 * 转成平台 [DialogTransition]，交给平台 `Dialog` 在图层上执行出现 / 消失过渡
 * （平台侧机制：内容录进 GraphicsLayer 再按进度回放，退场时重托管图层播完再关闭）。
 *
 * 映射：时长、纵向位移、缓动（[EasingCurve] + [EasingDirection] → 平台 [Easing]）逐项对应；
 * 起始不透明度与缩放**用平台默认**（0f / 1f = 完全透明地淡入淡出、不缩放）——
 * 本 meta 目前只管时长 / 位移 / 缓动，要控这两项再给 [AlertDialogAnimationMeta] 加字段。
 */
fun AlertDialogAnimationMeta.toDialogTransition(): DialogTransition {
    val curve = easing
    return DialogTransition(
        durationMillis = durationMillis,
        offset = offset,
        easing = Easing { fraction -> curve(fraction) },
    )
}
