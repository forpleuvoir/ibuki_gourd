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
    },
    Outlined {
        @Composable
        override fun defaultShape(): Shape = OutlinedTextFieldDefaults.shape

        @Composable
        override fun defaultTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors()

    };

    @Composable
    abstract fun defaultShape(): Shape

    @Composable
    abstract fun defaultTextFieldColors(): TextFieldColors
}

val LocalNumberFieldStyle = compositionLocalOf {
    NumberFieldStyle.Default
}

/**
 * 通用数值编辑器，支持键盘滚轮步进、输入校验、范围限制
 *
 * @param value 当前值
 * @param onValueChange 值变更回调
 * @param valuePredicate 输入字符串校验函数
 * @param valueMapper 输入字符串到类型 T 的转换函数
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
    valuePredicate: (String) -> Boolean,
    valueMapper: (String) -> T,
    valueDisplay: (T) -> String,
    valueStep: ValueStep<T>,
    valuePlus: (T, T) -> T,
    valueMinus: (T, T) -> T,
    valueTimes: (T, T) -> T,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    labelPosition: TextFieldLabelPosition = TextFieldLabelPosition.Attached(),
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
    contentPadding: PaddingValues = OutlinedTextFieldDefaults.contentPadding(),
    interactionSource: MutableInteractionSource? = null
) {
    val textFieldState = rememberTextFieldState(valueDisplay(value))
    var lastValidValue by remember { mutableStateOf(value) }
    var isError by remember { mutableStateOf(false) }
    var focused by remember { mutableStateOf(false) }

    // 外部 value 变更 → 同步到输入框
    LaunchedEffect(value) {
        lastValidValue = value
        val displayText = valueDisplay(value)
        if (textFieldState.text.toString() != displayText) {
            textFieldState.setTextAndPlaceCursorAtEnd(displayText)
        }
        isError = false
    }

    // 输入框内容变更 → 校验 & 同步
    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }
            .collect { newText ->
                if (newText != valueDisplay(lastValidValue) && !valuePredicate(newText)) {
                    isError = true
                } else {
                    lastValidValue = valueMapper(newText)
                    isError = false
                    onValueChange(lastValidValue)
                }
            }
    }

    val interactionSource = remember { interactionSource ?: MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    val modifierApplied = modifier
        .hoverable(interactionSource)
        .onPointerEvent(PointerEventType.Scroll) { event ->
            if (lineLimits == TextFieldLineLimits.SingleLine && hovered && focused) {
                val change = event.changes.first()
                val scrollDelta = if (event.keyboardModifiers.isShiftPressed) change.scrollDelta.x else change.scrollDelta.y
                lastValidValue = if (scrollDelta < 0)
                    valuePlus(lastValidValue, valueStep.process(valueTimes))
                else
                    valueMinus(lastValidValue, valueStep.process(valueTimes))
                textFieldState.setTextAndPlaceCursorAtEnd(valueDisplay(lastValidValue))
                onValueChange(lastValidValue)
                isError = false
                change.consume()
            }
        }
        .onFocusChanged { focusState ->
            if (!focusState.isFocused) {
                if (isError) {
                    textFieldState.setTextAndPlaceCursorAtEnd(valueDisplay(lastValidValue))
                    isError = false
                }
            }
            focused = focusState.isFocused
        }

    val fieldLabel: @Composable (TextFieldLabelScope.() -> Unit)? = label?.let { { it(!isError) } }

    when (LocalNumberFieldStyle.current) {
        NumberFieldStyle.Outlined -> OutlinedTextField(
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
            interactionSource = interactionSource,
        )

        NumberFieldStyle.Default  -> TextField(
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
            interactionSource = interactionSource
        )
    }
}

private val String.isInteger: Boolean
    get() = runCatching {
        toIntOrNull() != null
    }.getOrNull() ?: false

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
    labelPosition: TextFieldLabelPosition = TextFieldLabelPosition.Attached(),
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
    contentPadding: PaddingValues = OutlinedTextFieldDefaults.contentPadding(),
    interactionSource: MutableInteractionSource? = null
) {
    ComparableField(
        value = value,
        onValueChange = onValueChange,
        valuePredicate = { str ->
            //是否为整数     是否在范围内,如果范围为空则不判断                           是否为空字符串,应该解析为0
            (str.isInteger && range?.let { str.toIntOrNull() in it } ?: true) || str.isEmpty()
        },
        valueDisplay = valueDisplay,
        valueMapper = { it.toIntOrNull() ?: 0 },
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

private val String.isLong: Boolean
    get() = runCatching {
        toLongOrNull() != null
    }.getOrNull() ?: false

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
    labelPosition: TextFieldLabelPosition = TextFieldLabelPosition.Attached(),
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
    contentPadding: PaddingValues = OutlinedTextFieldDefaults.contentPadding(),
    interactionSource: MutableInteractionSource? = null
) {
    ComparableField(
        value = value,
        onValueChange = onValueChange,
        valuePredicate = { str ->
            (str.isLong && range?.let { str.toLongOrNull() in it } ?: true) || str.isEmpty()
        },
        valueDisplay = valueDisplay,
        valueMapper = { it.toLongOrNull() ?: 0L },
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

private val String.isFloat: Boolean
    get() = toFloatOrNull()?.isFinite() ?: false

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
    labelPosition: TextFieldLabelPosition = TextFieldLabelPosition.Attached(),
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
    contentPadding: PaddingValues = OutlinedTextFieldDefaults.contentPadding(),
    interactionSource: MutableInteractionSource? = null
) {
    ComparableField(
        value = value,
        onValueChange = onValueChange,
        valuePredicate = { str ->
            (str.isFloat && range?.let { r -> str.toFloatOrNull()?.let { it in r } ?: false } ?: true) || str.isEmpty()
        },
        valueDisplay = valueDisplay,
        valueMapper = { it.toFloatOrNull() ?: 0f },
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

private val String.isDouble: Boolean
    get() = toDoubleOrNull()?.isFinite() ?: false

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
    labelPosition: TextFieldLabelPosition = TextFieldLabelPosition.Attached(),
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
    contentPadding: PaddingValues = OutlinedTextFieldDefaults.contentPadding(),
    interactionSource: MutableInteractionSource? = null
) {
    ComparableField(
        value = value,
        onValueChange = onValueChange,
        valuePredicate = { str ->
            (str.isDouble && range?.let { r -> str.toDoubleOrNull()?.let { it in r } ?: false } ?: true) || str.isEmpty()
        },
        valueDisplay = valueDisplay,
        valueMapper = { it.toDoubleOrNull() ?: 0.0 },
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
