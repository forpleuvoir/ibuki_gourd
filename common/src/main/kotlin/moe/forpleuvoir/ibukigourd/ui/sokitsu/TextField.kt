package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import moe.forpleuvoir.compose_minecraft.mc
import moe.forpleuvoir.compose_minecraft.platform.ui.text.platformDefaultFontSizeSp
import moe.forpleuvoir.compose_minecraft.platform.ui.text.withColor
import moe.forpleuvoir.ibukigourd.ui.sokitsu.draw.sokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeToken
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorTone
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalColorScheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.contentColor
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.fromToken
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolve
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolveFaded
import moe.forpleuvoir.ibukigourd.util.contrasting
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.network.chat.Style
import net.minecraft.sounds.SoundEvents

/**
 * 输入框（TextField）：凹槽背景精灵 + [BasicTextField]（[TextFieldState] 输入会话）。
 *
 * 参数面与 M3 `OutlinedTextField`（state 版）对齐：[state] / [leadingIcon] /
 * [trailingIcon] / [lineLimits] / [contentPadding] / [isError] / [readOnly] /
 * [inputTransformation] / [outputTransformation] / [onKeyboardAction]；
 * 本组件只把容器装饰换成凹槽背景精灵（[TextFieldColors.containerColor] 染色，
 * 九宫格拉伸），编辑能力全部委托 [BasicTextField]。
 *
 * 布局（容器与文本解耦）：
 * - 容器 = 本组件的 [Box] + 最小尺寸（[TextFieldDefaults.meta]）+ 背景精灵，尺寸完全由
 *   调用方约束决定（未给高度时收敛到 meta 最小高度），**不随文本内容变化**；
 * - 内层 [BasicTextField] 取 `matchParentSize()` 撑满容器，[contentPadding] 内缩出内容区：
 *   焦点/点击热区 = 整块内容区（空文本也不会塌缩成贴着文本的一条窄带）；
 * - 内容区高度同时成为输入框内部的行数视口：单行文本（含空文本）垂直居中，
 *   多行自顶部起排，超出内容区时由平台内部滚动 + 裁剪承载（光标自动滚入视野）。
 *
 * 状态：
 * - **聚焦**时容器 outline 层染 [TextFieldColors.selectedOutlineColor]
 *   （与 Button/Slider 同款描边机制，[BasicTextField] 经传入的交互源上报聚焦）；
 * - [isError] = true 时 outline 改染 [TextFieldColors.errorOutlineColor]，优先级高于聚焦；
 * - [enabled] = false：容器与文本压暗，不接收输入，指针形态保持不变
 *   （[TextFieldDefaults.LocalDisableIcon]）；
 * - [readOnly] = true：可选择/复制但不可编辑。
 *
 * 文本样式经 [textStyle]（MC [Style]）自定义字体等；颜色由本组件按状态统一注入，
 * 传入 [textStyle] 上的颜色会被覆盖。
 *
 * @param state 输入状态，调用方经 `rememberTextFieldState(initial)` 创建；
 *   当前文本读 [TextFieldState.text]
 * @param modifier 修饰，通常给宽度与（多行时）高度；不给则取 [TextFieldDefaults.meta] 最小尺寸
 * @param enabled 是否可用（禁用态压暗配色且不接收输入）
 * @param readOnly 只读（可选择/复制，不可编辑）
 * @param isError 错误态（描边改染错误色）
 * @param leadingIcon 头部图标/前缀槽
 * @param trailingIcon 尾部图标/后缀槽（如单位、清除按钮）
 * @param lineLimits 行数限制（单行/多行），默认单行；多行的行数上限见 [withoutMaxLineClamp]
 * @param contentPadding 内容内边距，默认 [TextFieldDefaults.contentPadding]
 * @param textStyle 编辑文本的 MC 样式（字体/加粗等）；颜色由组件按状态注入
 * @param fontSize 编辑文本字号；默认 = 当前默认字体的自然尺寸
 *   （[platformDefaultFontSizeSp]，像素字体 1x 自然渲染，无需非整数缩放）。
 *   内部按 `fontSize / 平台默认字号` 派生渲染缩放（T.26）
 * @param colors 配色集，默认 [TextFieldDefaults.colors]
 * @param backgroundSprite 容器背景纹理，默认 [TextFieldDefaults.backgroundSprite]
 * @param interactionSource 交互源，不传则内部新建；聚焦状态经此驱动描边
 */
@Composable
fun TextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.SingleLine,
    contentPadding: PaddingValues = TextFieldDefaults.contentPadding(),
    textStyle: Style = Style.EMPTY,
    fontSize: TextUnit = platformDefaultFontSizeSp().sp,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    inputTransformation: InputTransformation? = null,
    outputTransformation: OutputTransformation? = null,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    backgroundSprite: SokitsuSprite = TextFieldDefaults.backgroundSprite(),
    interactionSource: MutableInteractionSource? = null,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    val pressed by source.collectIsPressedAsState()
    val pressSound = TextFieldDefaults.LocalPressSound.current

    // 平台的点按手势把 PressInteraction 发进交互源：鼠标按下即播音效。
    // 键盘聚焦不产生 Press（自然不出声）；禁用态不接收输入，也不出声
    LaunchedEffect(pressed, pressSound, enabled) {
        if (pressed && enabled) pressSound?.let { mc.soundManager.play(it) }
    }

    val containerTone = colors.containerColor
    val outlineTone = when {
        isError -> containerTone.copy(outline = colors.errorOutlineColor)
        focused -> containerTone.copy(outline = colors.selectedOutlineColor)
        else    -> containerTone
    }
    val contentColor = if (enabled) colors.contentColor else colors.disabledContentColor
    val singleLine = lineLimits == TextFieldLineLimits.SingleLine
    val hoverIcon =
        if (enabled) TextFieldDefaults.LocalHoverIcon.current else TextFieldDefaults.LocalDisableIcon.current

    Box(
        modifier = modifier
            // 平台内层输入框无条件挂 PointerIcon.Text：禁用态覆盖其后代，指针形态保持不变；
            // 启用态不覆盖，头尾槽图标（如清除按钮）保留各自的悬停指针
            .pointerHoverIcon(hoverIcon, overrideDescendants = !enabled)
            .defaultMinSize(minWidth = TextFieldDefaults.meta.minSize.width, minHeight = TextFieldDefaults.meta.minSize.height)
            .sokitsuSprite(backgroundSprite, outlineTone),
    ) {
        BasicTextField(
            state = state,
            // matchParentSize：只撑满容器、不参与容器测量。焦点/点击热区因此覆盖整块内容区；
            // 同时把内容区高度作为内部约束下限，平台据此得到正确的行数视口（超出即滚动）
            modifier = Modifier
                .matchParentSize()
                .padding(contentPadding),
            enabled = enabled,
            readOnly = readOnly,
            inputTransformation = inputTransformation,
            textStyle = textStyle.withColor(contentColor),
            fontSize = fontSize,
            keyboardOptions = keyboardOptions,
            onKeyboardAction = onKeyboardAction,
            lineLimits = lineLimits.withoutMaxLineClamp(),
            interactionSource = source,
            cursorBrush = SolidColor(colors.cursorColor),
            outputTransformation = outputTransformation,
            decorator = { inner ->
                // 行撑满内容区：单行（含空文本）垂直居中；多行自顶部起排，
                // 行数超出内容区时继续向下排并由平台滚动 + 裁剪承载
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
                ) {
                    leadingIcon?.invoke()
                    Box(Modifier.weight(1f)) { inner() }
                    trailingIcon?.invoke()
                }
            },
        )
    }
}

/**
 * 输入框配色集：容器色板 + 文本/光标/描边三色。
 *
 * 结构对齐 [ButtonColors] —— 全部字段都已解析，`copy(...)` 即为精准覆盖；
 * "哪些槽位映射到主题哪里"由 [TextFieldDefaults.colors] 承担。
 *
 * [selectedOutlineColor] 在聚焦时染容器精灵的 outline 层（Mask），
 * [errorOutlineColor] 在 isError 时接管同一层，优先级高于聚焦。
 */
data class TextFieldColors(
    val containerColor: ColorTone,
    val disabledContainerColor: ColorTone,
    val contentColor: Color,
    val disabledContentColor: Color,
    val cursorColor: Color,
    val selectedOutlineColor: Color,
    val errorOutlineColor: Color,
)

object TextFieldDefaults {

    inline val meta get() = SokitsuThemeMeta.textField

    /** 点击（按下）音效；`null` = 静音。 */
    val LocalPressSound = compositionLocalOf<SoundInstance?> { SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f) }

    /**
     * 悬停指针：启用态 = 文本光标（I 形），禁用态 = 系统默认指针（形态不变）。
     *
     * 平台内层输入框无条件挂了 `PointerIcon.Text`，组件在容器上以
     * `overrideDescendants = !enabled` 覆盖，使整块容器（含内缩边框区）统一生效；
     * 与 Button/Switch/Slider 的 `LocalHoverIcon` / `LocalDisableIcon` 同形，可经
     * [androidx.compose.runtime.CompositionLocalProvider] 覆盖。
     */
    val LocalHoverIcon = compositionLocalOf { PointerIcon.Text }
    val LocalDisableIcon = compositionLocalOf { PointerIcon.Default }

    fun backgroundSprite(): SokitsuSprite = meta.backgroundSprite.toSprite()

    /**
     * 内容内边距（M3 OutlinedTextField 同名参数）：须盖过纹理的边框 + 斜面，
     * 让文本不贴着凹槽边。默认水平 12dp / 垂直 10dp。
     */
    fun contentPadding(): PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 10.dp)

    /**
     * 默认输入框配色：
     * - 容器 → [TextFieldTokens.Container]（surfaceVariant），禁用压 [TextFieldTokens.DisabledContainerOpacity]
     * - 文本 → 容器色板的配对内容色（onSurfaceVariant），禁用压 [TextFieldTokens.DisabledContentOpacity]
     * - 光标 → 主色 base
     * - 聚焦描边 → 容器 base 的对比色（[contrasting]）；错误描边 → 错误色 base
     */
    @Composable
    fun colors(
        containerColor: ColorTone = ColorTone.Unspecified,
        disabledContainerColor: ColorTone = ColorTone.Unspecified,
        contentColor: Color = Color.Unspecified,
        disabledContentColor: Color = Color.Unspecified,
        cursorColor: Color = Color.Unspecified,
        selectedOutlineColor: Color = Color.Unspecified,
        errorOutlineColor: Color = Color.Unspecified,
    ): TextFieldColors {
        val resolvedContainer = containerColor.resolve(TextFieldTokens.Container)
        return TextFieldColors(
            containerColor = resolvedContainer,
            disabledContainerColor = disabledContainerColor.resolveFaded(
                TextFieldTokens.DisabledContainer,
                TextFieldTokens.DisabledContainerOpacity,
            ),
            contentColor = contentColor.takeOrElse { resolvedContainer.contentColor() },
            disabledContentColor = disabledContentColor.takeOrElse {
                resolvedContainer.contentColor().copy(alpha = TextFieldTokens.DisabledContentOpacity)
            },
            cursorColor = cursorColor.takeOrElse {
                LocalColorScheme.current.fromToken(TextFieldTokens.Cursor).base
            },
            selectedOutlineColor = selectedOutlineColor.takeOrElse { resolvedContainer.base.contrasting() },
            errorOutlineColor = errorOutlineColor.takeOrElse {
                LocalColorScheme.current.fromToken(TextFieldTokens.ErrorOutline).base
            },
        )
    }
}

/**
 * 去掉多行的 `maxHeightInLines` 上限，只保留 `minHeightInLines`。
 *
 * 平台适配点：`heightInLines` 的行高取自 [net.minecraft.network.chat.Style] 的固定度量
 * （未带字号，按 1x 行高折算），而输入框实际行高 = `fontSize`（默认 24px/行）。
 * 该上限经 `Constraints.constrain` 生效于文本测量，于是有限的 `maxHeightInLines`
 * 会把多行内容裁到「1x 行高 × 行数」的窄带（实测 `MultiLine(1, 3)` 只出约一行）。
 * 行数上限改由容器高度承担：文本超出内容区时由平台内部滚动 + 裁剪处理。
 *
 * [TextFieldLineLimits.SingleLine] 不经过该换算（其下限取自真实行高），原样透传。
 */
private fun TextFieldLineLimits.withoutMaxLineClamp(): TextFieldLineLimits = when (this) {
    TextFieldLineLimits.SingleLine        -> TextFieldLineLimits.SingleLine
    is TextFieldLineLimits.MultiLine      -> TextFieldLineLimits.MultiLine(minHeightInLines, Int.MAX_VALUE)
}
