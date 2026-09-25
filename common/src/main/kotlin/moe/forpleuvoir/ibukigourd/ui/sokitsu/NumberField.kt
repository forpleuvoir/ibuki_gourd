package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.*
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import net.minecraft.network.chat.Style
import kotlin.math.pow
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
 * 5. **方向键按位步进**（C4D 式）：聚焦时上下方向键只改**光标所指示的那一位数字**
 *    （`NumberFieldDigits.kt`），不做整数值加减、也不移动光标；
 * 6. **类型化字符过滤**：各入口默认只放行本类型合法字符，调用方传入的 `inputTransformation`
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
    // 整数没有小数位：位权至少为 1（末尾光标那条会算出 10^-1，取整会变 0）
    placeValue = { text, cursor -> 10.0.pow(placeExponent(text, cursor)).toInt().coerceAtLeast(1) },
    plus = { a, b -> a + b },
    minus = { a, b -> a - b },
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
    placeValue = { text, cursor -> 10.0.pow(placeExponent(text, cursor)).toLong().coerceAtLeast(1L) },
    plus = { a, b -> a + b },
    minus = { a, b -> a - b },
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
    placeValue = { text, cursor -> 10f.pow(placeExponent(text, cursor)) },
    plus = { a, b -> a + b },
    minus = { a, b -> a - b },
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
    placeValue = { text, cursor -> 10.0.pow(placeExponent(text, cursor)) },
    plus = { a, b -> a + b },
    minus = { a, b -> a - b },
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
 * 文本保留 [decimals] 位小数，这样"按位步进"改到十分位 / 百分位时（如 `50.1%`）显示得出来，
 * 否则低位步进会被整数格式化吞掉、看起来像没反应。
 *
 * @param valueRange 取值区间；解析失败或越界都会收敛进该区间
 * @param valueToText 自定义文本；null = 按 [decimals] 位小数显示区间占比
 * @param decimals 默认文本的小数位数；`1` = 十分位（`50.1%`），`0` = 整数（`50%`）
 * @param valueStep 滚轮步进：基础 1%，Shift ×10、Ctrl ×15、Alt ×30
 */
@Composable
fun PercentField(
    value: Float,
    onValueChange: ((Float) -> Unit)?,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    valueToText: ((Float) -> String)? = null,
    decimals: Int = 1,
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
            val percent = if (span > 0f) (current - valueRange.start) / span * 100 else 0f
            "%.${decimals.coerceAtLeast(0)}f%%".format(percent)
        }
    },
    step = valueStep,
    times = Float::times,
    // PercentField 显示是「值 × 100 + %」（内部 0..1），所以文本位权要再除 100：
    // 文本十位 +1（50% → 60%）对应内部值只 +0.1。
    placeValue = { text, cursor -> 10f.pow(placeExponent(text, cursor) - 2) },
    plus = { a, b -> a + b },
    minus = { a, b -> a - b },
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
    placeValue = null,
    plus = { a, b -> a + b },
    minus = { a, b -> a - b },
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
 * @param placeValue 光标所指示数位的位权（该位的"1"值多少，如十位 = 10、十分位 = 0.1）：`(文本, 光标下标) -> 位权`。
 *   方向键按位步进就是把**这个位权**加/减到当前值上，不做任何字符串进位。
 *   `null` = 该类型没有位权概念（如 [DurationField] 的 `1h 30m`），不接管方向键。
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
    /** 行数限制；按位步进只在 [TextFieldLineLimits.SingleLine] 下接管方向键。 */
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.SingleLine,
    placeValue: ((text: String, cursor: Int) -> T)?,
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

    /**
     * 方向键按位步进：光标位置决定这一步的数值量（个位 1、十分位 0.1、百分位 0.01 …），
     * 然后把该位权加/减到当前值上。
     *
     * 位权由 `placeValue` 给出：光标右边有数字时取那一位的位权（`|35` → 10、`0|.25` → 0.1）；
     * 右边没有数字时取**再下一位**（`16|` → 0.1、`16.1|` → 0.01）。
     *
     * 之后就是对数值做一次普通加减 —— 没有字符串进位 / 借位，`0` 退一位自然是 `-1`，
     * 跨位进位（`9.09` 的个位 +1 → `10.09`）由数值运算本身承担。
     *
     * 基于 [lastSynced]（已规范化的值）运算，回写后再把光标落回原位（长度变化时按位次找回），
     * 使下一次按键仍指向同一位。
     *
     * **只要 `placeValue` 存在就消费方向键**：数值可能因 `valueRange` 收敛而没变化（钳制在边界），
     * 此时依然消费并让光标留在原处，避免事件漏给输入框自身的上下行命令、把光标顶走而叠加出
     * 第二套语义。`placeValue` 为 `null`（如 [DurationField] 的 `1h 30m`）时才不消费。
     *
     * @return 是否消费方向键
     */
    fun stepDigit(delta: Int): Boolean {
        val place = placeValue ?: return false
        val text = state.text.toString()
        val cursor = state.selection.start
        val unit = place(text, cursor)
        // 步长恒为**正的位权**，方向只由下面的 if 分支决定：`delta` 不能再乘进位权，
        // 否则会双重取负（`minus(x, -unit)` == `x + unit`），表现就是「按 ⬇️ 数值反而变大」。
        val rightDigits = digitsRightOf(text, cursor)
        val candidate = if (delta > 0) plus(lastSynced, unit) else minus(lastSynced, unit)
        commit(candidate)
        // 光标按"右侧数字个数不变"落回同一位（`-`、`.` 不算数字，见 restoreDigitCursor）
        state.restoreDigitCursor(rightDigits)
        return true
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
    // 方向键按位步进只对单行字段成立；多行字段保留输入框默认的上下行走（跨行移动光标）
    val digitStepEnabled = interactive && lineLimits == TextFieldLineLimits.SingleLine

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
            )
            // 方向键按位步进：Preview 阶段先于平台输入框自身的按键处理拿到事件，命中数字即消费
            // （未命中时返回 false，方向键照常用于移动光标/选择）
            .onPreviewKeyEvent onPreviewKey@{ event ->
                if (event.type != KeyEventType.KeyDown || !digitStepEnabled || event.isShiftPressed || event.isAltPressed || event.isCtrlPressed) {
                    return@onPreviewKey false
                }
                when (event.key) {
                    Key.DirectionUp   -> stepDigit(+1)
                    Key.DirectionDown -> stepDigit(-1)
                    else              -> false
                }
            },
        enabled = enabled,
        readOnly = readOnly || onValueChange == null,
        isError = isError,
        textStyle = textStyle,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        lineLimits = lineLimits,
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
