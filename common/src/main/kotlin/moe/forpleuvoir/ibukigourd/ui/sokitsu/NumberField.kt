package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.*
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import net.minecraft.network.chat.Style
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 类型化数字输入框：在单行 [TextField] 之上做「文本 ↔ 数值」双向换算与校验，对齐迁移前
 * `ui/preset/NumberField.kt`（Material3 版）的入口集合与交互：
 *
 * 1. **受控值**：[value] + `onValueChange`，配各自的 `fix`——解析失败或越界都收敛成合法值
 *    （有范围取范围端点，无范围取 0），外部拿到的永远是合法值；
 * 2. **输入期只切错误态**：边输边解析，只改 `isError`（格式非法 / 越界），**不回写文本框**
 *    ——边输边格式化会打断输入（`-`、`.`、`1.` 这些中间态）；
 * 3. **失焦 / 回车提交**：收敛后回写格式化文本并把光标置于末尾，同时清错误态；
 * 4. **滚轮步进**：悬停且聚焦时滚轮一格 = 一步，Shift/Ctrl/Alt 放大步长（见 [ValueStep]），
 *    事件在 Initial 阶段消费掉，不影响外层可滚动容器与输入框自身的水平滚动；
 * 5. **类型化字符过滤**：各入口默认只放行本类型合法字符，调用方传入的 `inputTransformation`
 *    在其后叠加。
 *
 * 与 [NumberSlider] 同构：入口命名与参数面保持一致，`onValueChange == null` 表示只读展示。
 * `valueToText` 同时承担"编辑态显示"，格式化差异（`1.0` / `1`）只影响显示，不改变提交结果。
 */

/**
 * 滚轮步进配置：基础步长 + 修饰键倍率。
 *
 * [factor] 按当前修饰键给出倍率（优先级 Shift > Ctrl > Alt），修饰键取自滚轮事件本身；
 * [resolve] 再用类型的整数乘法（如 [Int::times]、[Duration.times]）算出实际步长。
 *
 * @param base 基础步长（无修饰键）
 * @param shift Shift 按下时的倍率
 * @param ctrl Ctrl 按下时的倍率
 * @param alt Alt 按下时的倍率
 */
data class ValueStep<T>(val base: T, val shift: Int = 10, val ctrl: Int = 15, val alt: Int = 30) {

    fun factor(modifiers: PointerKeyboardModifiers): Int = when {
        modifiers.isShiftPressed -> shift
        modifiers.isCtrlPressed  -> ctrl
        modifiers.isAltPressed   -> alt
        else                     -> 1
    }

    fun resolve(modifiers: PointerKeyboardModifiers, times: (T, Int) -> T): T =
        times(base, factor(modifiers))
}

/**
 * 整数输入框。
 *
 * @param value 当前值
 * @param onValueChange 值变化回调；null = 只读展示
 * @param valueRange 可选范围限制；解析失败或越界都会收敛进该区间
 * @param valueToText 数值 → 编辑态文本
 * @param valueStep 滚轮步进：基础 1，Shift ×10、Ctrl ×15、Alt ×30
 * @param readOnly 只读（可选中/复制，不可编辑）；[onValueChange] 为 null 时同样只读
 * @param inputTransformation 额外输入变换，在类型化字符过滤之后执行
 * @param onKeyboardAction 键盘动作（回车等）附加处理，提交后回调
 */
@Composable
fun IntField(
    value: Int,
    onValueChange: ((Int) -> Unit)?,
    valueRange: IntRange? = null,
    valueToText: (Int) -> String = { it.toString() },
    valueStep: ValueStep<Int> = ValueStep(1),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: Style = Style.EMPTY,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    inputTransformation: InputTransformation? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    backgroundSprite: SokitsuSprite = TextFieldDefaults.backgroundSprite(),
    interactionSource: MutableInteractionSource? = null,
) = NumberFieldImpl(
    value = value,
    onValueChange = onValueChange,
    parse = { it.trim().toIntOrNull() },
    fix = { parsed -> valueRange?.let { parsed?.coerceIn(it) ?: it.first } ?: parsed ?: 0 },
    toText = valueToText,
    step = valueStep,
    times = Int::times,
    plus = Int::plus,
    minus = Int::minus,
    isValidChar = { it.isDigit() || it == '-' },
    modifier = modifier,
    enabled = enabled,
    readOnly = readOnly,
    textStyle = textStyle,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    inputTransformation = inputTransformation,
    keyboardOptions = keyboardOptions,
    onKeyboardAction = onKeyboardAction,
    colors = colors,
    backgroundSprite = backgroundSprite,
    interactionSource = interactionSource,
)

/**
 * 长整数输入框，行为同 [IntField]。
 *
 * @param valueRange 可选范围限制；解析失败或越界都会收敛进该区间
 * @param valueStep 滚轮步进：基础 1，Shift ×10、Ctrl ×15、Alt ×30
 */
@Composable
fun LongField(
    value: Long,
    onValueChange: ((Long) -> Unit)?,
    valueRange: LongRange? = null,
    valueToText: (Long) -> String = { it.toString() },
    valueStep: ValueStep<Long> = ValueStep(1L),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: Style = Style.EMPTY,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    inputTransformation: InputTransformation? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    backgroundSprite: SokitsuSprite = TextFieldDefaults.backgroundSprite(),
    interactionSource: MutableInteractionSource? = null,
) = NumberFieldImpl(
    value = value,
    onValueChange = onValueChange,
    parse = { it.trim().toLongOrNull() },
    fix = { parsed -> valueRange?.let { parsed?.coerceIn(it) ?: it.first } ?: parsed ?: 0L },
    toText = valueToText,
    step = valueStep,
    times = Long::times,
    plus = Long::plus,
    minus = Long::minus,
    isValidChar = { it.isDigit() || it == '-' },
    modifier = modifier,
    enabled = enabled,
    readOnly = readOnly,
    textStyle = textStyle,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    inputTransformation = inputTransformation,
    keyboardOptions = keyboardOptions,
    onKeyboardAction = onKeyboardAction,
    colors = colors,
    backgroundSprite = backgroundSprite,
    interactionSource = interactionSource,
)

/**
 * 浮点数输入框。
 *
 * @param valueRange 可选范围限制；解析失败或越界都会收敛进该区间
 * @param valueStep 滚轮步进：基础 1，Shift ×10、Ctrl ×15、Alt ×30
 */
@Composable
fun FloatField(
    value: Float,
    onValueChange: ((Float) -> Unit)?,
    valueRange: ClosedFloatingPointRange<Float>? = null,
    valueToText: (Float) -> String = { it.toString() },
    valueStep: ValueStep<Float> = ValueStep(1f),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: Style = Style.EMPTY,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    inputTransformation: InputTransformation? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    backgroundSprite: SokitsuSprite = TextFieldDefaults.backgroundSprite(),
    interactionSource: MutableInteractionSource? = null,
) = NumberFieldImpl(
    value = value,
    onValueChange = onValueChange,
    parse = { it.trim().toFloatOrNull() },
    fix = { parsed -> valueRange?.let { parsed?.coerceIn(it) ?: it.start } ?: parsed ?: 0f },
    toText = valueToText,
    step = valueStep,
    times = Float::times,
    plus = Float::plus,
    minus = Float::minus,
    isValidChar = { it.isDigit() || it == '-' || it == '.' },
    modifier = modifier,
    enabled = enabled,
    readOnly = readOnly,
    textStyle = textStyle,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    inputTransformation = inputTransformation,
    keyboardOptions = keyboardOptions,
    onKeyboardAction = onKeyboardAction,
    colors = colors,
    backgroundSprite = backgroundSprite,
    interactionSource = interactionSource,
)

/**
 * 双精度浮点数输入框，行为同 [FloatField]。
 *
 * @param valueRange 可选范围限制；解析失败或越界都会收敛进该区间
 * @param valueStep 滚轮步进：基础 1，Shift ×10、Ctrl ×15、Alt ×30
 */
@Composable
fun DoubleField(
    value: Double,
    onValueChange: ((Double) -> Unit)?,
    valueRange: ClosedFloatingPointRange<Double>? = null,
    valueToText: (Double) -> String = { it.toString() },
    valueStep: ValueStep<Double> = ValueStep(1.0),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: Style = Style.EMPTY,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    inputTransformation: InputTransformation? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    backgroundSprite: SokitsuSprite = TextFieldDefaults.backgroundSprite(),
    interactionSource: MutableInteractionSource? = null,
) = NumberFieldImpl(
    value = value,
    onValueChange = onValueChange,
    parse = { it.trim().toDoubleOrNull() },
    fix = { parsed -> valueRange?.let { parsed?.coerceIn(it) ?: it.start } ?: parsed ?: 0.0 },
    toText = valueToText,
    step = valueStep,
    times = Double::times,
    plus = Double::plus,
    minus = Double::minus,
    isValidChar = { it.isDigit() || it == '-' || it == '.' },
    modifier = modifier,
    enabled = enabled,
    readOnly = readOnly,
    textStyle = textStyle,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    inputTransformation = inputTransformation,
    keyboardOptions = keyboardOptions,
    onKeyboardAction = onKeyboardAction,
    colors = colors,
    backgroundSprite = backgroundSprite,
    interactionSource = interactionSource,
)

/**
 * 百分比输入框：取值与 [PercentSlider] 同为区间占比（默认 `0f..1f`），文本按占比显示为
 * `xx%`，输入同样按百分比解释（`50` 与 `50%` 都等于占比 0.5）。
 *
 * @param valueRange 取值区间；解析失败或越界都会收敛进该区间
 * @param valueToText 自定义文本；null = 区间占比四舍五入到整数百分比
 * @param valueStep 滚轮步进：基础 1%，Shift ×10、Ctrl ×15、Alt ×30
 */
@Composable
fun PercentField(
    value: Float,
    onValueChange: ((Float) -> Unit)?,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    valueToText: ((Float) -> String)? = null,
    valueStep: ValueStep<Float> = ValueStep(0.01f),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: Style = Style.EMPTY,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    inputTransformation: InputTransformation? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    backgroundSprite: SokitsuSprite = TextFieldDefaults.backgroundSprite(),
    interactionSource: MutableInteractionSource? = null,
) = NumberFieldImpl(
    value = value,
    onValueChange = onValueChange,
    parse = { text -> text.trim().removeSuffix("%").trim().toFloatOrNull()?.div(100f) },
    fix = { parsed -> parsed?.coerceIn(valueRange) ?: valueRange.start },
    toText = { current ->
        valueToText?.invoke(current) ?: run {
            val span = valueRange.endInclusive - valueRange.start
            val percent = if (span > 0f) ((current - valueRange.start) / span * 100).roundToInt() else 0
            "$percent%"
        }
    },
    step = valueStep,
    times = Float::times,
    plus = Float::plus,
    minus = Float::minus,
    isValidChar = { it.isDigit() || it == '-' || it == '.' || it == '%' },
    modifier = modifier,
    enabled = enabled,
    readOnly = readOnly,
    textStyle = textStyle,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    inputTransformation = inputTransformation,
    keyboardOptions = keyboardOptions,
    onKeyboardAction = onKeyboardAction,
    colors = colors,
    backgroundSprite = backgroundSprite,
    interactionSource = interactionSource,
)

/**
 * 时长输入框：文本与默认 [valueToText] 走 [Duration.toString] / [Duration.parseOrNull]
 * （如 `1h 30m`、`500ms`，可往返）。
 *
 * @param valueRange 取值区间；解析失败或越界都会收敛进该区间
 * @param valueStep 滚轮步进：基础 1 秒，Shift ×10、Ctrl ×15、Alt ×30
 */
@Composable
fun DurationField(
    value: Duration,
    onValueChange: ((Duration) -> Unit)?,
    valueRange: ClosedRange<Duration>? = null,
    valueToText: (Duration) -> String = { it.toString() },
    valueStep: ValueStep<Duration> = ValueStep(1.seconds),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: Style = Style.EMPTY,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    inputTransformation: InputTransformation? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    backgroundSprite: SokitsuSprite = TextFieldDefaults.backgroundSprite(),
    interactionSource: MutableInteractionSource? = null,
) = NumberFieldImpl(
    value = value,
    onValueChange = onValueChange,
    parse = { Duration.parseOrNull(it.trim()) },
    fix = { parsed -> valueRange?.let { parsed?.coerceIn(it) ?: it.start } ?: parsed ?: Duration.ZERO },
    toText = valueToText,
    step = valueStep,
    times = Duration::times,
    plus = Duration::plus,
    minus = Duration::minus,
    isValidChar = { it.isDigit() || it == '.' || it == ' ' || it in "dhms" },
    modifier = modifier,
    enabled = enabled,
    readOnly = readOnly,
    textStyle = textStyle,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    inputTransformation = inputTransformation,
    keyboardOptions = keyboardOptions,
    onKeyboardAction = onKeyboardAction,
    colors = colors,
    backgroundSprite = backgroundSprite,
    interactionSource = interactionSource,
)

/**
 * [IntField] / [LongField] / [FloatField] / [DoubleField] / [PercentField] / [DurationField]
 * 的公共实现：只做「文本 ↔ 数值」的同步、校验、提交与滚轮步进，渲染全部委托 [TextField]。
 *
 * 同步规则（与迁移前 Material3 版一致）：
 * - 外部 [value] 变化 → 回写文本（带 `lastSynced` 守卫，不打断正在进行的输入）；
 * - 文本变化 → 解析 + 收敛 → 只更新错误态并回调外部，**不动文本**；
 * - 失焦 / 回车 / 滚轮 → 提交：收敛、回调、回写格式化文本、光标置尾。
 *
 * @param parse 文本 → 数值；null 表示格式非法
 * @param fix 解析结果（可为 null）→ 合法值：越界收敛、非法取默认
 * @param step 滚轮步长配置，与 [times] 一起解析修饰键倍率
 */
@Composable
private fun <T> NumberFieldImpl(
    value: T,
    onValueChange: ((T) -> Unit)?,
    parse: (String) -> T?,
    fix: (T?) -> T,
    toText: (T) -> String,
    step: ValueStep<T>,
    times: (T, Int) -> T,
    plus: (T, T) -> T,
    minus: (T, T) -> T,
    isValidChar: (Char) -> Boolean,
    modifier: Modifier,
    enabled: Boolean,
    readOnly: Boolean,
    textStyle: Style,
    leadingIcon: (@Composable () -> Unit)?,
    trailingIcon: (@Composable () -> Unit)?,
    inputTransformation: InputTransformation?,
    keyboardOptions: KeyboardOptions,
    onKeyboardAction: KeyboardActionHandler?,
    colors: TextFieldColors,
    backgroundSprite: SokitsuSprite,
    interactionSource: MutableInteractionSource?,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    val hovered by source.collectIsHoveredAsState()

    val state: TextFieldState = rememberTextFieldState(toText(fix(value)))
    var isError by remember { mutableStateOf(false) }
    var lastSynced by remember { mutableStateOf(fix(value)) }
    var wasFocused by remember { mutableStateOf(false) }

    // 回调经 rememberUpdatedState 供长寿命协程/手势读取最新实现
    val latestOnValueChange = rememberUpdatedState(onValueChange)
    val latestFix = rememberUpdatedState(fix)
    val latestParse = rememberUpdatedState(parse)
    val latestToText = rememberUpdatedState(toText)

    /** 提交：收敛 → 回调 → 回写格式化文本 + 光标置尾（失焦 / 回车 / 滚轮共用） */
    fun commit(candidate: T?) {
        val fixed = latestFix.value(candidate)
        lastSynced = fixed
        isError = false
        if (fixed != value) latestOnValueChange.value?.invoke(fixed)
        state.setTextAndPlaceCursorAtEnd(latestToText.value(fixed))
    }

    // 外部值变化 → 同步文本；值与 lastSynced 相同则不写，避免打断输入
    LaunchedEffect(value) {
        val fixed = fix(value)
        if (fixed != lastSynced) {
            lastSynced = fixed
            isError = false
            state.setTextAndPlaceCursorAtEnd(toText(fixed))
        }
    }

    // 输入期：只切错误态 + 回调外部，不回写文本
    LaunchedEffect(state) {
        snapshotFlow { state.text.toString() }.collect { text ->
            val parsed = latestParse.value(text)
            val fixed = latestFix.value(parsed)
            isError = parsed == null || fixed != parsed
            if (fixed != lastSynced) {
                lastSynced = fixed
                latestOnValueChange.value?.invoke(fixed)
            }
        }
    }

    // 失焦提交
    LaunchedEffect(focused) {
        if (wasFocused && !focused) commit(latestParse.value(state.text.toString()))
        wasFocused = focused
    }

    // 类型化字符过滤 + 调用方的 inputTransformation 叠加
    val filteredInput = InputTransformation {
        val buffer = this
        if (buffer.asCharSequence().any { !isValidChar(it) }) {
            buffer.revertAllChanges()
        } else if (inputTransformation != null) {
            with(inputTransformation) { buffer.transformInput() }
        }
    }

    val interactive = enabled && !readOnly && onValueChange != null

    TextField(
        state = state,
        modifier = modifier
            .hoverable(source, enabled = enabled)
            .numberWheelStep(
                active = interactive && hovered && focused,
                onStep = { direction, modifiers ->
                    val stepValue = step.resolve(modifiers, times)
                    commit(if (direction > 0) plus(lastSynced, stepValue) else minus(lastSynced, stepValue))
                },
            ),
        enabled = enabled,
        readOnly = readOnly || onValueChange == null,
        isError = isError,
        textStyle = textStyle,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        onKeyboardAction = KeyboardActionHandler { performDefaultAction ->
            commit(latestParse.value(state.text.toString()))
            if (onKeyboardAction == null) performDefaultAction() else onKeyboardAction.onKeyboardAction(performDefaultAction)
        },
        inputTransformation = filteredInput,
        keyboardOptions = keyboardOptions,
        colors = colors,
        backgroundSprite = backgroundSprite,
        interactionSource = source,
    )
}

/**
 * 滚轮步进：在 **Initial** 阶段截获滚动事件并消费。
 *
 * 该阶段事件自根向外派发，本节点位于输入框内部滚动节点之上、外层可滚动容器之下：
 * 先于输入框自身的水平滚动拿到事件（滚轮不会把文本横向顶走），消费后外层容器与输入框
 * 的滚轮逻辑都会因 `isConsumed` 直接跳过。[active] 为 false（未悬停 / 未聚焦 / 非交互）
 * 时不消费，滚轮行为交还原链路。
 *
 * @param onStep 回调：+1 = 加一步，-1 = 减一步，[PointerKeyboardModifiers] 为本次事件的修饰键
 */
private fun Modifier.numberWheelStep(
    active: Boolean,
    onStep: (direction: Int, keyboardModifiers: PointerKeyboardModifiers) -> Unit,
): Modifier = pointerInput(active) {
    if (!active) return@pointerInput
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            if (event.type != PointerEventType.Scroll) continue
            val change = event.changes.firstOrNull { !it.isConsumed } ?: continue
            val delta = if (event.keyboardModifiers.isShiftPressed) {
                change.scrollDelta.x
            } else {
                change.scrollDelta.y
            }
            if (delta == 0f) continue
            // 一格 = 一步：只取符号。平台约定 scrollDelta.y 正值 = 向上滚
            // （ComposeInputBridge.scrollDelta 原样透传 MC 符号），故上滚加、下滚减
            onStep(if (delta > 0f) 1 else -1, event.keyboardModifiers)
            event.changes.forEach { it.consume() }
        }
    }
}
