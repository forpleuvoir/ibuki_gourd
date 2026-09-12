package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpSize
import moe.forpleuvoir.ibukigourd.ui.sokitsu.draw.snapToDensity
import moe.forpleuvoir.ibukigourd.ui.sokitsu.draw.sokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorTone
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalContentColor
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuPixelScale
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.contentColor
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolve
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolveFaded
import moe.forpleuvoir.ibukigourd.util.contrasting
import moe.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.sounds.SoundEvents
import kotlin.math.roundToInt

/**
 * 滑条（Slider）：**凹槽轨道 + 按进度裁剪的凸起填充**两张精灵叠放，数值文字骑在两者之上。
 *
 * 结构自下而上：
 * 1. 轨道精灵 [trackSprite]（凹槽）铺满整条，作为背景；
 * 2. 填充精灵 [fillSprite]（凸起）同样按整条宽度绘制，但绘制时裁掉进度边界右侧 ——
 *    因此素材要画**完整两端带角**的版本，中间态靠裁剪切平，`progress = 100%` 时
 *    其右端才与轨道右端逐像素重合；
 * 3. [label] 内容**画两遍**，各自裁到进度边界的内 / 外，分别用
 *    [SliderColors.filledLabelColor] 与 [SliderColors.unfilledLabelColor] ——
 *    同一串文字因此不会同时压在两种底色上（分界线扫过文字时两侧各保持可读）。
 *
 * 填充与文字共用同一条边界（同一函数求值），任一进度下都不会错位；边界按
 * [LocalSokitsuPixelScale] 的整数倍吸附，裁剪边始终落在像素块边界上。
 *
 * 交互：按下即定位（无触摸阈值），随后按住拖动实时跟随；[steps] > 0 时吸附到
 * 等分档位。手势结束时回调 [onValueChangeFinished]。本组件无悬停/按下态素材，
 * 不引入四态精灵——悬停、聚焦与拖动中的反馈是轨道 outline 层染色为选中描边色
 * （[SliderColors.selectedOutlineColor]，与 Button/Switch 同款；拖动时鼠标移出轨道
 * 描边保持，松手复位），禁用态只反映在配色上。拖动经交互源以 [DragInteraction]
 * 上报（裸 pointerInput 不会自动上报，故手动发出）。
 *
 * 尺寸：最小尺寸取 [SliderDefaults.trackMinSize]（资源包可覆盖）；[modifier] 给出的
 * 约束优先。填充与文字均按该尺寸铺满 / 居中，`label` 超出轨道宽度时会溢出显示。
 *
 * @param value 当前值（会被收敛到 [valueRange] 内用于绘制与语义）
 * @param onValueChange 值变化回调；null = 只读展示（不装手势、指针不变手型，配色仍按 [enabled] 走）
 * @param modifier 修饰，尺寸按传入约束收敛
 * @param enabled 是否可交互（禁用态压暗配色且不接收手势）
 * @param valueRange 取值区间，[valueRange.endInclusive] <= start 时进度恒为 0
 * @param steps 两端的等分数（0 = 连续）
 * @param onValueChangeFinished 手势结束回调，可用于提交/保存
 * @param colors 配色集，默认 [SliderDefaults.colors]
 * @param label 轨道内居中的内容槽（通常是一段 [Text]）；内容色由本组件下发，
 *   槽内组件取 [LocalContentColor] 即可自动获得"分界两侧各一色"的效果
 * @param interactionSource 交互源，不传则内部新建；经 hoverable/focusable 驱动
 *   悬停/聚焦描边，调用方需要观察交互状态时可传入自己持有的实例
 */
@Composable
fun Slider(
    value: Float,
    onValueChange: ((Float) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    trackSprite: SokitsuSprite = SliderDefaults.trackSprite(),
    fillSprite: SokitsuSprite = SliderDefaults.fillSprite(),
    label: (@Composable () -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
) {
    val span = valueRange.endInclusive - valueRange.start
    val fraction = if (span > 0f) ((value - valueRange.start) / span).coerceIn(0f, 1f) else 0f

    // 裁剪边界吸附用的像素放大倍率：1 源像素 = pixelScale 个屏幕像素
    val pixelScale = LocalSokitsuPixelScale.current

    // 手势协程内要读到最新回调，避免 pointerInput 因 lambda 身份变化而重启
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentOnValueChangeFinished by rememberUpdatedState(onValueChangeFinished)

    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val dragged by interactionSource.collectIsDraggedAsState()

    val clickSound = SliderDefaults.LocalPressSound.current

    val trackTone = colors.trackColor(enabled)
    val fillTone = colors.fillColor(enabled)
    val filledLabelColor = colors.labelColor(enabled, filled = true)
    val unfilledLabelColor = colors.labelColor(enabled, filled = false)

    // 悬停/聚焦/拖动中：仅轨道 outline 层覆盖为选中描边色（与 Button/Switch 同款），
    // 其余层保持色板结构；填充精灵无启用中的 outline 层，无需覆盖
    val trackToneWithSelection = when {
        hovered || focused || dragged -> trackTone.copy(outline = colors.selectedOutlineColor)
        else                          -> trackTone
    }

    // 无回调 = 只读展示：不装手势（onValueChange == null 时不接收交互），也不给"手型"指针
    val interactive = enabled && onValueChange != null
    val icon = when {
        interactive                    -> SliderDefaults.LocalHoverIcon.current
        enabled                        -> PointerIcon.Default
        else                           -> SliderDefaults.LocalDisableIcon.current
    }

    /**
     * 进度边界（屏幕像素）。100% 时不裁剪：吸附到最近的整数倍可能落在宽度之内，
     * 会把填充最右一列削掉一块。
     */
    fun boundaryOf(width: Float): Float =
        if (fraction >= 1f) width
        else snapToDensity(width * fraction, pixelScale).coerceIn(0f, width)

    Box(
        modifier = modifier
            .pointerHoverIcon(icon)
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = (if (value.isNaN()) valueRange.start else value).coerceIn(valueRange),
                    range = valueRange,
                    steps = steps,
                )
                if (!enabled) disabled()
                setProgress { target ->
                    currentOnValueChange?.let { change ->
                        change(target.coerceIn(valueRange))
                        true
                    } ?: false
                }
            }
            .pointerInput(interactive, valueRange.start, valueRange.endInclusive, steps) {
                if (!interactive) return@pointerInput
                awaitEachGesture {
                    // 按下即定位：不设触摸阈值，点击任意位置先跳到该进度
                    val down = awaitFirstDown(requireUnconsumed = false)
                    // 裸 pointerInput 不会像 draggable 那样自动上报拖动交互，
                    // 这里手动发入交互源，驱动 collectIsDraggedAsState（拖动中描边）
                    // 并供持有该交互源的调用方观察
                    val dragInteraction = DragInteraction.Start()
                    interactionSource.tryEmit(dragInteraction)
                    try {
                        clickSound?.let { sound -> mc.soundManager.play(sound) }
                        currentOnValueChange?.invoke(valueAt(down.position.x, size.width, valueRange, steps))
                        down.consume()
                        drag(down.id) { change ->
                            currentOnValueChange?.invoke(valueAt(change.position.x, size.width, valueRange, steps))
                            change.consume()
                        }
                        // drag() 对抬起与取消都是静默返回，统一以 Stop 收尾
                        interactionSource.tryEmit(DragInteraction.Stop(dragInteraction))
                        currentOnValueChangeFinished?.invoke()
                    } catch (e: Throwable) {
                        // 协程被取消或回调抛错时补发 Cancel，避免交互源残留未结束的 Start
                        interactionSource.tryEmit(DragInteraction.Cancel(dragInteraction))
                        throw e
                    }
                }
            }
            // hover/focus 交互源：驱动选中描边（与 Button 的 clickable、Switch 的 hoverable 同路）
            .hoverable(interactionSource, enabled = interactive)
            .focusable(enabled = interactive, interactionSource = interactionSource)
            .defaultMinSize(SliderDefaults.trackMinSize.width, SliderDefaults.trackMinSize.height)
            .sokitsuSprite(trackSprite, trackToneWithSelection),
    ) {
        // 填充层：整条精灵，绘制时裁掉进度边界右侧
        Box(
            Modifier.matchParentSize()
                .clipHorizontal(end = { boundaryOf(it) })
                .sokitsuSprite(fillSprite, fillTone)
        )

        if (label != null) {
            // 边界内侧的一份（填充分区配色）
            Box(
                Modifier.matchParentSize().clipHorizontal(end = { boundaryOf(it) }),
                contentAlignment = Alignment.Center,
            ) {
                CompositionLocalProvider(LocalContentColor provides filledLabelColor) { label() }
            }
            // 边界外侧的一份（轨道分区配色）
            Box(
                Modifier.matchParentSize().clipHorizontal(start = { boundaryOf(it) }),
                contentAlignment = Alignment.Center,
            ) {
                CompositionLocalProvider(LocalContentColor provides unfilledLabelColor) { label() }
            }
        }
    }
}

/**
 * 把本节点及其子树的绘制裁剪到 x ∈ [[start], [end]) 内。
 *
 * 边界在**绘制时**按节点当前宽度求值（[start] / [end] 接收节点宽度、返回屏幕像素），
 * 因此进度变化无需重新测量即可重新裁剪。两个边界求值后都收敛进
 * `[0, width]`；裁剪区间为空时跳过 `drawContent()`，不产生退化裁剪命令。
 *
 * 依赖 compose-minecraft 的 `DrawScope.clipRect` → `MinecraftCanvas` 裁剪栈：
 * 子节点（精灵命令、文本命令）在记录时各自捕获当时裁剪矩形，渲染端按矩形提交 scissor。
 */
private fun Modifier.clipHorizontal(
    start: (Float) -> Float = { 0f },
    end: (Float) -> Float = { it },
): Modifier = drawWithContent {
    val width = size.width
    val left = start(width).coerceIn(0f, width)
    val right = end(width).coerceIn(0f, width)
    if (right > left) {
        clipRect(left = left, right = right) { this@drawWithContent.drawContent() }
    }
}

/**
 * 把指针横坐标换算成取值区间内的值；[steps] > 0 时吸附到等分档位
 * （档位 = [steps] + 1 段，含两端）。宽度为 0 时返回区间起点。
 */
private fun valueAt(
    x: Float,
    width: Int,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
): Float {
    if (width <= 0) return valueRange.start
    var fraction = (x / width).coerceIn(0f, 1f)
    if (steps > 0) {
        val segments = steps + 1
        fraction = (fraction * segments).roundToInt() / segments.toFloat()
    }
    return valueRange.start + (valueRange.endInclusive - valueRange.start) * fraction
}

object SliderDefaults {

    inline val meta get() = SokitsuThemeMeta.slider

    val LocalPressSound = compositionLocalOf<SoundInstance?> { SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f) }

    val LocalHoverIcon = compositionLocalOf { PointerIcon.Hand }
    val LocalDisableIcon = compositionLocalOf { PointerIcon.NotAllowed }

    fun trackSprite(): SokitsuSprite = meta.trackSprite.toSprite()

    fun fillSprite(): SokitsuSprite = meta.fillSprite.toSprite()

    /** 滑条最小尺寸：来自主题 meta 的 slider 段（**单位 dp**，资源包可覆盖）。 */
    val trackMinSize: DpSize get() = meta.trackMinSize

    /**
     * 默认滑条配色：轨道 / 填充两套色板 + 四档数值文字色。
     *
     * 参数默认 [ColorTone.Unspecified] / [Color.Unspecified]，语义是"按 [SliderTokens]
     * 映射表结合当前主题解析"；回退顺序：`调用点传参` >
     * [moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuTone] 作用域 >
     * [SliderTokens] > [ColorScheme]。
     *
     * - [trackColor] 凹槽底 → [SliderTokens.Track]（surfaceVariant）
     * - [fillColor] 凸起进度段 → [SliderTokens.Fill]（primary）
     * - 启用态两档文字色默认取**各自底色色板的配对内容色**（[ColorTone.contentColor]）：
     *   填充侧 → onPrimary，轨道侧 → onSurfaceVariant。这样作用域整体换色板
     *   （如把滑条切成 secondary）时文字色自动跟随
     * - 禁用态：轨道 / 填充底色走 [resolveFaded]（12% / 38%）；文字**两半统一**取
     *   onSurfaceVariant 压 [SliderTokens.DisabledLabelOpacity]（38%）——
     *   Tint 模式下禁用填充的明度同样由纹理决定（偏亮），文字必须压深才可读，
     *   取 onPrimary 会在浅色主题（白）下与浅灰底同化
     * - [selectedOutlineColor] 悬停/聚焦描边 → 未指定时取**填充色板** base 的对比色
     *   （[contrasting]，对齐 Switch 取主色家族、不随轨道状态漂移）
     */
    @Composable
    fun colors(
        trackColor: ColorTone = ColorTone.Unspecified,
        fillColor: ColorTone = ColorTone.Unspecified,
        disabledTrackColor: ColorTone = ColorTone.Unspecified,
        disabledFillColor: ColorTone = ColorTone.Unspecified,
        filledLabelColor: Color = Color.Unspecified,
        unfilledLabelColor: Color = Color.Unspecified,
        disabledFilledLabelColor: Color = Color.Unspecified,
        disabledUnfilledLabelColor: Color = Color.Unspecified,
        selectedOutlineColor: Color = Color.Unspecified,
    ): SliderColors {
        val resolvedTrack = trackColor.resolve(SliderTokens.Track)
        val resolvedFill = fillColor.resolve(SliderTokens.Fill)
        val labelAlpha = SliderTokens.DisabledLabelOpacity
        return SliderColors(
            trackColor = resolvedTrack,
            fillColor = resolvedFill,
            disabledTrackColor = disabledTrackColor.resolveFaded(
                SliderTokens.DisabledTrack,
                SliderTokens.DisabledTrackOpacity,
            ),
            disabledFillColor = disabledFillColor.resolveFaded(
                SliderTokens.DisabledFill,
                SliderTokens.DisabledFillOpacity,
            ),
            filledLabelColor = filledLabelColor.takeOrElse { resolvedFill.contentColor() },
            unfilledLabelColor = unfilledLabelColor.takeOrElse { resolvedTrack.contentColor() },
            disabledFilledLabelColor = disabledFilledLabelColor.takeOrElse {
                resolvedTrack.contentColor().copy(alpha = labelAlpha)
            },
            disabledUnfilledLabelColor = disabledUnfilledLabelColor.takeOrElse {
                resolvedTrack.contentColor().copy(alpha = labelAlpha)
            },
            selectedOutlineColor = selectedOutlineColor.takeOrElse { resolvedFill.base.contrasting() },
        )
    }
}

/**
 * 滑条配色集：两套底色色板（轨道 / 填充）+ 四档数值文字色。
 *
 * 结构对齐 [ButtonColors]——**全部字段都已解析**，因此是普通 data class，
 * `copy(...)` 即为精准覆盖；"哪些槽位映射到主题哪里"由 [SliderDefaults.colors] 承担。
 *
 * [filledLabelColor] 用于进度边界**内侧**的文字，[unfilledLabelColor] 用于**外侧**；
 * [disabledFilledLabelColor] / [disabledUnfilledLabelColor] 是禁用态的两份 ——
 * 禁用填充的明度同样由纹理决定（偏亮），默认与轨道侧同源（onSurfaceVariant 压低不透明度），
 * 两个槽位分开是为了让调用方可以只给填充半区换色。
 *
 * [selectedOutlineColor] 为悬停/聚焦时轨道 outline 层的染色（选中描边，只影响轨道精灵 ——
 * 填充精灵没有启用中的 outline 层），默认取填充色板 base 的对比色。
 *
 * "状态 → 取值"的映射由 `trackColor` / `fillColor` / `labelColor` 三个方法承担（对齐
 * [SwitchColors]），调用点不自行按 [enabled] 做 if 判断，配色语义只有这一处定义。
 */
@Immutable
data class SliderColors(
    val trackColor: ColorTone,
    val fillColor: ColorTone,
    val disabledTrackColor: ColorTone,
    val disabledFillColor: ColorTone,
    val filledLabelColor: Color,
    val unfilledLabelColor: Color,
    val disabledFilledLabelColor: Color,
    val disabledUnfilledLabelColor: Color,
    val selectedOutlineColor: Color,
) {

    /** 轨道底色（按启用状态二选一）。 */
    @Stable
    internal fun trackColor(enabled: Boolean): ColorTone = if (enabled) trackColor else disabledTrackColor

    /** 填充底色（按启用状态二选一）。 */
    @Stable
    internal fun fillColor(enabled: Boolean): ColorTone = if (enabled) fillColor else disabledFillColor

    /**
     * 数值文字色：按启用状态与该文字落在进度边界的内侧（[filled] = true）还是外侧二选一。
     */
    @Stable
    internal fun labelColor(enabled: Boolean, filled: Boolean): Color = when {
        enabled  -> if (filled) filledLabelColor else unfilledLabelColor
        filled   -> disabledFilledLabelColor
        else     -> disabledUnfilledLabelColor
    }
}
