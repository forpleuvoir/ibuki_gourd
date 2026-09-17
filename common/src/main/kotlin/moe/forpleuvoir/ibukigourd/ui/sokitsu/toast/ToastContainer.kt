package moe.forpleuvoir.ibukigourd.ui.sokitsu.toast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import moe.forpleuvoir.ibukigourd.mod.config.IGConfig
import moe.forpleuvoir.ibukigourd.ui.sokitsu.LocalTextStyle
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.TextureFill
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalColorScheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuPixelScale
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.fromToken
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolve
import kotlinx.coroutines.flow.first
import kotlin.time.Duration

/** 倒计时条的不透明度（相对内容色），半透明以免与面板内容抢视线。 */
private const val PROGRESS_ALPHA = 0.6f

/**
 * 面板精灵九宫格边框在左 / 右 / 底边上的厚度（屏幕像素）。
 *
 * 用于把倒计时条内缩到面板描边**内侧**：不缩的话条会与同色描边叠在一起（等于看不见），
 * 两端也会压住左右描边。
 */
private data class PanelBorderPx(val left: Float, val right: Float, val bottom: Float) {

    companion object {
        val Zero = PanelBorderPx(0f, 0f, 0f)
    }
}

/**
 * 解析面板精灵的九宫格边框（屏幕像素）= `素材 border × pixelScale ÷ 素材密度`。
 *
 * 取首个**非阴影**的九宫格图层：描边 / 底色层才是面板本体的可视边界，阴影层不占面板区域。
 * 精灵没有九宫格图层（如纯色或整图拉伸）时返回 [PanelBorderPx.Zero]，调用点即退化为贴边绘制。
 */
private fun SokitsuSprite.panelBorderPx(pixelScale: Int): PanelBorderPx {
    val layer = layers.firstOrNull { !it.isShadow && it.fill is TextureFill.NinePatch }
        ?: return PanelBorderPx.Zero
    val border = (layer.fill as TextureFill.NinePatch).border
    val scale = pixelScale.toFloat() / layer.density.coerceAtLeast(1)
    return PanelBorderPx(border.left * scale, border.right * scale, border.bottom * scale)
}

/**
 * 提示堆叠的落点：由 [IGConfig.Gui.Toast.offset] 决定（`0f~1f`，相对窗口尺寸的百分比）。
 *
 * 语义与旧实现一致：`x` 取 `0.5` 时水平居中（按"剩余空间比例"插值，故面板宽度不影响居中），
 * `y` 取 `0.85` 时面板**中心**落在窗口 85% 高度处。
 *
 * 声明为顶层常量：定位器本身无状态，每次布局读取当前配置值即可响应配置修改。
 */
private val ToastPlacement = Alignment { size, space, _ ->
    val offset = IGConfig.Gui.Toast.offset
    IntOffset(
        x = ((space.width - size.width) * offset.x()).toInt(),
        y = ((space.height * offset.y() - size.height / 2f)).toInt()
    )
}

/**
 * 提示渲染层：铺满场景，把 [ToastHandler.active] 中的每条提示按 [ToastPlacement] 摆放。
 *
 * 组合在提示宿主（[ToastHost]）的常驻场景根，因此**不依赖任何
 * [moe.forpleuvoir.compose_minecraft.platform.screen.ComposeScreen]** —— 打开原版界面、
 * 关闭全部界面乃至 HUD 状态下都持续渲染。
 */
@Composable
fun ToastContainer() {
    Box(Modifier.fillMaxSize()) {
        ToastHandler.active.forEach { state ->
            key(state) {
                Box(Modifier.align(ToastPlacement)) {
                    ToastItem(state)
                }
            }
        }
    }
}

/**
 * 带标准外观的提示容器：面板（主题面板精灵）+ 内边距 + 最小宽度 + 倒计时条。
 *
 * 倒计时条**不占布局**：它在 [Surface] 自身的 [Modifier.drawWithContent] 里于面板绘制完成后叠加
 * —— 与该面板的精灵共用同一个节点尺寸，因此必然落在面板内，既不依赖 intrinsic
 * 测量也不做任何跨节点的尺寸推算（另两种做法实测都会跑偏：`fillMaxWidth` 参与
 * `IntrinsicSize.Max` 的列宽计算会把面板撑成全屏；在面板外层节点按 `size` 自绘会偏出面板）。
 *
 * 倒计时条按面板的九宫格边框厚度**自动内缩**（左 / 右 / 底三边）—— 贴边会与同色描边叠在一起
 * 等于看不见。横向再让出内容内边距（不小于描边厚度）；[ToastMeta.progressInset] 可在纵向
 * 自动内缩之上再抬高。
 *
 * [content] 只负责面板内部的内容；颜色走"调用点传参 > 组件 token"回退链，与
 * [moe.forpleuvoir.ibukigourd.ui.sokitsu.tooltip.TooltipTokens] 同族。
 *
 * @param color 面板染色，未指定按 [ToastTokens.Body] 解析
 * @param contentColor 内容色（倒计时条取其半透明版本），未指定按 [ToastTokens.Content] 解析
 * @param contentAlignment 内容对齐，默认居中
 * @param propagateMinConstraints 是否把最小约束传给内容（默认 true）
 */
@Composable
fun ToastContent(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    contentAlignment: Alignment = Alignment.Center,
    propagateMinConstraints: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val duration = LocalToastDuration.current
    val refreshCounter = LocalToastRefreshCounter.current
    val resolvedContent = contentColor.takeOrElse {
        LocalColorScheme.current.fromToken(ToastTokens.Content)
    }
    val progress = remember { Animatable(1f) }
    val sprite = ToastDefaults.sprite()
    val pixelScale = LocalSokitsuPixelScale.current
    val progressHeight = ToastDefaults.progressHeight
    val progressInset = ToastDefaults.progressInset
    // 面板描边厚度：进度条按它内缩到描边内侧
    val border = sprite.panelBorderPx(pixelScale)
    // 内容内边距：横向再让出这么多，使进度条两端与内容对齐
    val contentPadding = ToastDefaults.padding
    val layoutDirection = LocalLayoutDirection.current

    // 被原地刷新（refreshCounter 变化）时重播倒计时；duration <= 0 的提示不自动消失，无需计时
    LaunchedEffect(duration, refreshCounter) {
        if (duration <= Duration.ZERO) return@LaunchedEffect
        progress.snapTo(1f)
        progress.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = duration.inWholeMilliseconds.toInt().coerceAtLeast(0),
                easing = LinearEasing,
            ),
        )
    }

    Surface(
        modifier = modifier.drawWithContent {
            drawContent()
            if (duration <= Duration.ZERO) return@drawWithContent
            val barHeight = progressHeight.toPx()
            if (barHeight <= 0f) return@drawWithContent
            // 左 / 右：让出内容内边距；下限保底为描边厚度，否则条会压住描边而看不见
            val insetStart = maxOf(contentPadding.calculateLeftPadding(layoutDirection).toPx(), border.left)
            val insetEnd = maxOf(contentPadding.calculateRightPadding(layoutDirection).toPx(), border.right)
            val available = (size.width - insetStart - insetEnd).coerceAtLeast(0f)
            // 绘制期读取动画值 → 每帧按需重绘该节点
            val barWidth = available * progress.value.coerceIn(0f, 1f)
            if (barWidth <= 0f) return@drawWithContent
            // 落在描边内侧（+ meta 的额外量）；不缩会与同色描边叠在一起、等于看不见
            val barBottom = (size.height - border.bottom - progressInset.toPx()).coerceAtLeast(barHeight)
            drawRect(
                color = resolvedContent.copy(alpha = PROGRESS_ALPHA),
                topLeft = Offset(insetStart, barBottom - barHeight),
                size = Size(barWidth, barHeight),
            )
        },
        sprite = sprite,
        color = color.resolve(ToastTokens.Body),
        contentColor = resolvedContent,
        textStyle = LocalTextStyle.current,
        contentAlignment = contentAlignment,
    ) {
        Box(
            Modifier
                .padding(ToastDefaults.padding)
                .widthIn(min = ToastDefaults.minWidth),
            contentAlignment,
            propagateMinConstraints,
        ) {
            content()
        }
    }
}

/**
 * 单条提示：按 [ToastHandler.ToastState.remaining] 驱动进出场动画，并把动画 / 时长 /
 * 刷新计数经 CompositionLocal 下发给内容。
 *
 * `remaining > 0` 时保持可见；转为 `<= 0` 后播放退场动画，动画播完由 [ToastHandler.tick]
 * 在过掉宽限期后移除条目，[ToastContainer] 随之撤组合（不自动消失的提示 remaining 恒为
 * [Duration.INFINITE]，只能被策略替换或 [ToastHandler.dismissAll] 清掉）。
 */
@Composable
private fun ToastItem(state: ToastHandler.ToastState) {
    val localAnimation = LocalToastAnimation.current
    // 默认动画含 EnterTransition 实例：以 meta 为键缓存，避免每帧向 AnimatedVisibility 提交新实例
    val defaultAnimation = remember(ToastDefaults.meta) { ToastDefaults.animation }
    val animation = state.toast.animation ?: localAnimation ?: defaultAnimation
    val transitionState = remember { MutableTransitionState(false) }

    LaunchedEffect(state, state.refreshCounter) {
        transitionState.targetState = state.remaining > Duration.ZERO
        if (state.remaining > Duration.ZERO) {
            snapshotFlow { state.remaining }.first { it <= Duration.ZERO }
            transitionState.targetState = false
        }
    }

    AnimatedVisibility(
        visibleState = transitionState,
        enter = animation.enter,
        exit = animation.exit,
    ) {
        CompositionLocalProvider(
            LocalToastAnimation provides animation,
            LocalToastDuration provides state.toast.duration,
            LocalToastRefreshCounter provides state.refreshCounter,
        ) {
            state.toast.content()
        }
    }
}
