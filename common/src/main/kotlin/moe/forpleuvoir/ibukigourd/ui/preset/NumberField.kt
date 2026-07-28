package moe.forpleuvoir.ibukigourd.ui.preset

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.Keyboard

/**
 * 数值步进配置，根据键盘修饰键计算实际步进值
 *
 * @param baseFactor 基础步进
 * @param shiftRatio Shift 键倍率
 * @param ctrlRatio Ctrl 键倍率
 * @param altRatio Alt 键倍率
 */
data class ValueStep<T>(val baseFactor: T, val shiftRatio: T, val ctrlRatio: T, val altRatio: T) where T : Comparable<T> {
    fun process(times: (T, T) -> T): T {
        return when {
            InputHandler.wasKeyPressed(Keyboard.LEFT_SHIFT)   -> times(baseFactor, shiftRatio)
            InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL) -> times(baseFactor, ctrlRatio)
            InputHandler.wasKeyPressed(Keyboard.LEFT_ALT)     -> times(baseFactor, altRatio)
            else                                              -> baseFactor
        }
    }
}

enum class NumberFieldStyle {
    Default {
        @Composable
        override fun defaultShape(): Shape = TextFieldDefaults.shape

        @Composable
        override fun defaultTextFieldColors(): TextFieldColors = TextFieldDefaults.colors()

        @Composable
        override fun contentPadding(label: Boolean): PaddingValues = if (label) {
            TextFieldDefaults.contentPaddingWithoutLabel()
        } else {
            TextFieldDefaults.contentPaddingWithLabel()
        }
    },
    Outlined {
        @Composable
        override fun defaultShape(): Shape = OutlinedTextFieldDefaults.shape

        @Composable
        override fun defaultTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors()

        @Composable
        override fun contentPadding(label: Boolean): PaddingValues = OutlinedTextFieldDefaults.contentPadding()

    };

    @Composable
    abstract fun defaultShape(): Shape

    @Composable
    abstract fun defaultTextFieldColors(): TextFieldColors

    @Composable
    abstract fun contentPadding(label: Boolean): PaddingValues
}

val LocalNumberFieldStyle = compositionLocalOf {
    NumberFieldStyle.Outlined
}

/**
 * 通用数值编辑器，支持键盘滚轮步进、输入校验、范围限制。
 *
 * @param value 当前值
 * @param onValueChange 值变更回调
 * @param valueParser 输入解析函数，返回 null 表示格式无效，非 null 为有效值
 * @param valueFix 修正数值
 * @param valueDisplay 数值到显示文本的转换函数
 * @param valueStep 步进配置
 * @param valuePlus 加法运算（用于步进增加）
 * @param valueMinus 减法运算（用于步进减少）
 * @param valueTimes 乘法运算（用于修饰键倍率）
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <T : Comparable<T>> ComparableField(
    value: T,
    onValueChange: (T) -> Unit,
    valueParser: (String) -> T?,
    valueFix: (T?) -> T,
    valueDisplay: (T) -> String,
    valueStep: ValueStep<T>,
    valuePlus: (T, T) -> T,
    valueMinus: (T, T) -> T,
    valueTimes: (T, T) -> T,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    labelPosition: TextFieldLabelPosition = TextFieldLabelPosition.Attached(true),
    label: @Composable (TextFieldLabelScope.(Boolean) -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    inputTransformation: InputTransformation? = null,
    outputTransformation: OutputTransformation? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.SingleLine,
    onTextLayout: (Density.(getResult: () -> TextLayoutResult?) -> Unit)? = null,
    scrollState: ScrollState = rememberScrollState(),
    shape: Shape = LocalNumberFieldStyle.current.defaultShape(),
    colors: TextFieldColors = LocalNumberFieldStyle.current.defaultTextFieldColors(),
    contentPadding: PaddingValues = LocalNumberFieldStyle.current.contentPadding(
        label == null || labelPosition is TextFieldLabelPosition.Above
    ),
    interactionSource: MutableInteractionSource? = null,
) {
    /*
     * snapshotFlow 所在的 LaunchedEffect 是长寿命协程。
     * 使用 rememberUpdatedState，保证协程始终访问最新的参数，
     * 避免 onValueChange 闭包继续持有旧的外部 value。
     */
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentValueParser by rememberUpdatedState(valueParser)
    val currentValueFix by rememberUpdatedState(valueFix)
    val currentValueDisplay by rememberUpdatedState(valueDisplay)
    val currentValueStep by rememberUpdatedState(valueStep)
    val currentValuePlus by rememberUpdatedState(valuePlus)
    val currentValueMinus by rememberUpdatedState(valueMinus)
    val currentValueTimes by rememberUpdatedState(valueTimes)

    val initialValue = remember {
        valueFix(value)
    }

    val textFieldState = rememberTextFieldState(
        valueDisplay(initialValue)
    )

    var isError by remember {
        mutableStateOf(false)
    }

    var focused by remember {
        mutableStateOf(false)
    }

    /*
     * 上一次已经同步的值。
     *
     * 它既可能来自外部 value，也可能来自当前输入框触发的
     * onValueChange，用于区分外部更新和自身回显。
     */
    var lastSyncedValue by remember {
        mutableStateOf(initialValue)
    }

    /*
     * 外部 value 发生变化时，同步输入框。
     *
     * 如果 value 等于 lastSyncedValue，说明它只是当前输入框
     * onValueChange 后的状态回显，不重新设置文本，避免打断输入。
     */
    LaunchedEffect(value) {
        if (value != lastSyncedValue) {
            val fixed = currentValueFix(value)

            lastSyncedValue = fixed
            isError = false

            textFieldState.setTextAndPlaceCursorAtEnd(
                currentValueDisplay(fixed)
            )
        }
    }

    /*
     * 监听手动输入。
     *
     * 这里必须通过 rememberUpdatedState 后的函数调用参数，
     * 否则 LaunchedEffect 会一直持有首次组合时的回调。
     */
    LaunchedEffect(textFieldState) {
        snapshotFlow {
            textFieldState.text.toString()
        }.collect { text ->
            val parsed = currentValueParser(text)
            val fixed = currentValueFix(parsed)

            isError = parsed == null || fixed != parsed

            if (fixed != lastSyncedValue) {
                lastSyncedValue = fixed
                currentOnValueChange(fixed)
            }
        }
    }

    /*
     * 统一提交数值：
     *
     * 1. 修正数值；
     * 2. 更新同步标记；
     * 3. 通知外部；
     * 4. 格式化输入框文本；
     * 5. 清除错误状态。
     */
    val commitValue: (T?) -> Unit = { newValue ->
        val fixed = currentValueFix(newValue)

        lastSyncedValue = fixed
        isError = false

        currentOnValueChange(fixed)

        textFieldState.setTextAndPlaceCursorAtEnd(
            currentValueDisplay(fixed)
        )
    }

    val actualInteractionSource =
        interactionSource ?: remember { MutableInteractionSource() }

    val hovered by actualInteractionSource.collectIsHoveredAsState()

    val modifierApplied = modifier
        .hoverable(actualInteractionSource)
        .onPointerEvent(PointerEventType.Scroll) { event ->
            if (
                enabled &&
                !readOnly &&
                lineLimits == TextFieldLineLimits.SingleLine &&
                hovered &&
                focused
            ) {
                val change = event.changes.firstOrNull()
                    ?: return@onPointerEvent

                val scrollDelta =
                    if (event.keyboardModifiers.isShiftPressed) {
                        change.scrollDelta.x
                    } else {
                        change.scrollDelta.y
                    }

                if (scrollDelta == 0f) {
                    return@onPointerEvent
                }

                val step = currentValueStep.process(currentValueTimes)

                val newValue =
                    if (scrollDelta < 0f) {
                        currentValuePlus(lastSyncedValue, step)
                    } else {
                        currentValueMinus(lastSyncedValue, step)
                    }

                commitValue(newValue)
                change.consume()
            }
        }
        .onFocusChanged { focusState ->
            if (focused && !focusState.isFocused) {
                val currentText = textFieldState.text.toString()
                commitValue(currentValueParser(currentText))
            }

            focused = focusState.isFocused
        }

    val fieldLabel:
            (@Composable TextFieldLabelScope.() -> Unit)? =
        label?.let { labelContent ->
            {
                labelContent(!isError)
            }
        }

    when (LocalNumberFieldStyle.current) {
        NumberFieldStyle.Outlined -> {
            OutlinedTextField(
                state = textFieldState,
                modifier = modifierApplied,
                enabled = enabled,
                readOnly = readOnly,
                textStyle = textStyle,
                labelPosition = labelPosition,
                label = fieldLabel,
                placeholder = placeholder,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                prefix = prefix,
                suffix = suffix,
                supportingText = supportingText,
                isError = isError,
                inputTransformation = inputTransformation,
                outputTransformation = outputTransformation,
                keyboardOptions = keyboardOptions,
                onKeyboardAction = onKeyboardAction,
                lineLimits = lineLimits,
                onTextLayout = onTextLayout,
                scrollState = scrollState,
                shape = shape,
                colors = colors,
                contentPadding = contentPadding,
                interactionSource = actualInteractionSource,
            )
        }

        NumberFieldStyle.Default -> {
            TextField(
                state = textFieldState,
                modifier = modifierApplied,
                enabled = enabled,
                readOnly = readOnly,
                textStyle = textStyle,
                labelPosition = labelPosition,
                label = fieldLabel,
                placeholder = placeholder,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                prefix = prefix,
                suffix = suffix,
                supportingText = supportingText,
                isError = isError,
                inputTransformation = inputTransformation,
                outputTransformation = outputTransformation,
                keyboardOptions = keyboardOptions,
                onKeyboardAction = onKeyboardAction,
                lineLimits = lineLimits,
                onTextLayout = onTextLayout,
                scrollState = scrollState,
                shape = shape,
                colors = colors,
                contentPadding = contentPadding,
                interactionSource = actualInteractionSource,
            )
        }
    }
}

/**
 * 整数输入框
 *
 * @param value 当前值
 * @param onValueChange 值变更回调
 * @param valueStep 步进配置，默认基础步进 1、Shift ×10、Ctrl ×15、Alt ×30
 * @param range 可选范围限制
 */
@Composable
fun IntField(
    value: Int,
    onValueChange: (Int) -> Unit,
    valueDisplay: (Int) -> String = { it.toString() },
    valueStep: ValueStep<Int> = ValueStep(1, 10, 15, 30),
    range: IntRange? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    labelPosition: TextFieldLabelPosition = TextFieldLabelPosition.Attached(true),
    label: @Composable (TextFieldLabelScope.(Boolean) -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    inputTransformation: InputTransformation? = null,
    outputTransformation: OutputTransformation? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.SingleLine,
    onTextLayout: (Density.(getResult: () -> TextLayoutResult?) -> Unit)? = null,
    scrollState: ScrollState = rememberScrollState(),
    shape: Shape = LocalNumberFieldStyle.current.defaultShape(),
    colors: TextFieldColors = LocalNumberFieldStyle.current.defaultTextFieldColors(),
    contentPadding: PaddingValues = LocalNumberFieldStyle.current.contentPadding(label == null || labelPosition is TextFieldLabelPosition.Above),
    interactionSource: MutableInteractionSource? = null
) {
    ComparableField(
        value = value,
        onValueChange = onValueChange,
        valueFix = { value ->
            //如果有范围限制那么限制值在范围内,如果值解析失败则返回最小值,没有范围则返回值本身,如果还解析失败则返回0
            range?.let { value?.coerceIn(it) ?: it.first } ?: value ?: 0
        },
        valueParser = { str ->
            //尝试解析为 Int
            str.toIntOrNull()
        },
        valueDisplay = valueDisplay,
        valueStep = valueStep,
        valuePlus = Int::plus,
        valueMinus = Int::minus,
        valueTimes = Int::times,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        labelPosition = labelPosition,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        inputTransformation = inputTransformation,
        outputTransformation = outputTransformation,
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onKeyboardAction,
        lineLimits = lineLimits,
        onTextLayout = onTextLayout,
        scrollState = scrollState,
        shape = shape,
        colors = colors,
        contentPadding = contentPadding,
        interactionSource = interactionSource
    )
}

/**
 * 长整数输入框
 *
 * @param value 当前值
 * @param onValueChange 值变更回调
 * @param valueStep 步进配置，默认基础步进 1、Shift ×10、Ctrl ×15、Alt ×30
 * @param range 可选范围限制
 */
@Composable
fun LongField(
    value: Long,
    onValueChange: (Long) -> Unit,
    valueDisplay: (Long) -> String = { it.toString() },
    valueStep: ValueStep<Long> = ValueStep(1L, 10L, 15L, 30L),
    range: LongRange? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    labelPosition: TextFieldLabelPosition = TextFieldLabelPosition.Attached(true),
    label: @Composable (TextFieldLabelScope.(Boolean) -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    inputTransformation: InputTransformation? = null,
    outputTransformation: OutputTransformation? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.SingleLine,
    onTextLayout: (Density.(getResult: () -> TextLayoutResult?) -> Unit)? = null,
    scrollState: ScrollState = rememberScrollState(),
    shape: Shape = LocalNumberFieldStyle.current.defaultShape(),
    colors: TextFieldColors = LocalNumberFieldStyle.current.defaultTextFieldColors(),
    contentPadding: PaddingValues = LocalNumberFieldStyle.current.contentPadding(label == null || labelPosition is TextFieldLabelPosition.Above),
    interactionSource: MutableInteractionSource? = null
) {
    ComparableField(
        value = value,
        onValueChange = onValueChange,
        valueFix = { value ->
            //如果有范围限制那么限制值在范围内,如果值解析失败则返回最小值,没有范围则返回值本身,如果还解析失败则返回0
            range?.let { value?.coerceIn(it) ?: it.first } ?: value ?: 0L
        },
        valueParser = { str ->
            //尝试解析为 Long
            str.toLongOrNull()
        },
        valueDisplay = valueDisplay,
        valueStep = valueStep,
        valuePlus = Long::plus,
        valueMinus = Long::minus,
        valueTimes = Long::times,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        labelPosition = labelPosition,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        inputTransformation = inputTransformation,
        outputTransformation = outputTransformation,
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onKeyboardAction,
        lineLimits = lineLimits,
        onTextLayout = onTextLayout,
        scrollState = scrollState,
        shape = shape,
        colors = colors,
        contentPadding = contentPadding,
        interactionSource = interactionSource
    )
}

/**
 * 浮点数输入框
 *
 * @param value 当前值
 * @param onValueChange 值变更回调
 * @param valueStep 步进配置，默认基础步进 1、Shift ×10、Ctrl ×15、Alt ×30
 * @param range 可选范围限制
 */
@Composable
fun FloatField(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueDisplay: (Float) -> String = { it.toString() },
    valueStep: ValueStep<Float> = ValueStep(1f, 10f, 15f, 30f),
    range: ClosedFloatingPointRange<Float>? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    labelPosition: TextFieldLabelPosition = TextFieldLabelPosition.Attached(true),
    label: @Composable (TextFieldLabelScope.(Boolean) -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    inputTransformation: InputTransformation? = null,
    outputTransformation: OutputTransformation? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.SingleLine,
    onTextLayout: (Density.(getResult: () -> TextLayoutResult?) -> Unit)? = null,
    scrollState: ScrollState = rememberScrollState(),
    shape: Shape = LocalNumberFieldStyle.current.defaultShape(),
    colors: TextFieldColors = LocalNumberFieldStyle.current.defaultTextFieldColors(),
    contentPadding: PaddingValues = LocalNumberFieldStyle.current.contentPadding(label == null || labelPosition is TextFieldLabelPosition.Above),
    interactionSource: MutableInteractionSource? = null
) {
    ComparableField(
        value = value,
        onValueChange = onValueChange,
        valueFix = { value ->
            //如果有范围限制那么限制值在范围内,如果值解析失败则返回最小值,没有范围则返回值本身,如果还解析失败则返回0
            range?.let { value?.coerceIn(it) ?: it.start } ?: value ?: 0f
        },
        valueParser = { str ->
            //尝试解析为 Long
            str.toFloatOrNull()
            //如果成功检测是否在范围内,否则返回解析结果
        },
        valueDisplay = valueDisplay,
        valueStep = valueStep,
        valuePlus = Float::plus,
        valueMinus = Float::minus,
        valueTimes = Float::times,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        labelPosition = labelPosition,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        inputTransformation = inputTransformation,
        outputTransformation = outputTransformation,
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onKeyboardAction,
        lineLimits = lineLimits,
        onTextLayout = onTextLayout,
        scrollState = scrollState,
        shape = shape,
        colors = colors,
        contentPadding = contentPadding,
        interactionSource = interactionSource
    )
}

/**
 * 双精度浮点数输入框
 *
 * @param value 当前值
 * @param onValueChange 值变更回调
 * @param valueStep 步进配置，默认基础步进 1、Shift ×10、Ctrl ×15、Alt ×30
 * @param range 可选范围限制
 */
@Composable
fun DoubleField(
    value: Double,
    onValueChange: (Double) -> Unit,
    valueDisplay: (Double) -> String = { it.toString() },
    valueStep: ValueStep<Double> = ValueStep(1.0, 10.0, 15.0, 30.0),
    range: ClosedFloatingPointRange<Double>? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    labelPosition: TextFieldLabelPosition = TextFieldLabelPosition.Attached(true),
    label: @Composable (TextFieldLabelScope.(Boolean) -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    inputTransformation: InputTransformation? = null,
    outputTransformation: OutputTransformation? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.SingleLine,
    onTextLayout: (Density.(getResult: () -> TextLayoutResult?) -> Unit)? = null,
    scrollState: ScrollState = rememberScrollState(),
    shape: Shape = LocalNumberFieldStyle.current.defaultShape(),
    colors: TextFieldColors = LocalNumberFieldStyle.current.defaultTextFieldColors(),
    contentPadding: PaddingValues = LocalNumberFieldStyle.current.contentPadding(label == null || labelPosition is TextFieldLabelPosition.Above),
    interactionSource: MutableInteractionSource? = null
) {
    ComparableField(
        value = value,
        onValueChange = onValueChange,
        valueFix = { value ->
            //如果有范围限制那么限制值在范围内,如果值解析失败则返回最小值,没有范围则返回值本身,如果还解析失败则返回0
            range?.let { value?.coerceIn(it) ?: it.start } ?: value ?: 0.0
        },
        valueParser = { str ->
            //尝试解析为 Long
            str.toDoubleOrNull()
            //如果成功检测是否在范围内,否则返回解析结果
        },
        valueDisplay = valueDisplay,
        valueStep = valueStep,
        valuePlus = Double::plus,
        valueMinus = Double::minus,
        valueTimes = Double::times,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        labelPosition = labelPosition,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        inputTransformation = inputTransformation,
        outputTransformation = outputTransformation,
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onKeyboardAction,
        lineLimits = lineLimits,
        onTextLayout = onTextLayout,
        scrollState = scrollState,
        shape = shape,
        colors = colors,
        contentPadding = contentPadding,
        interactionSource = interactionSource
    )
}
