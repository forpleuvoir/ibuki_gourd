package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta

/**
 * 单选按钮（RadioButton）：分段控件的一段，本质是**按位置换纹理的 [Button]**。
 *
 * 渲染、交互与音效整块委托给 [Button]；本组件只负责 radio 特有的四件事：
 * - 按 [index]/[count] 解析位置精灵：首段用 left、末段用 right、中间段用 center、
 *   [count] <= 1 用 single；RTL 布局下首尾交换（见 [RadioButtonDefaults.sprite]）；
 * - 选中态视觉：选中段直接以 `enabled = false` 交给 [Button]，落进其 disabled 状态
 *   （disabled 纹理 + 禁用内容色），不引入额外的配色机制——选中段随之不可再点击；
 * - 语义：`Role.RadioButton` + `selected` 属性；
 * - 尺寸与内边距取自 radio_button 主题段。
 *
 * 注意：组内 `enabled = false` 的禁用段与选中段共用 disabled 视觉，二者靠语义属性区分。
 *
 * 单独使用时 [index]/[count] 用默认值即可（count = 1 → single 纹理）；
 * 组内使用请走 [RadioButtonGroup]，由容器自动编号。
 *
 * @param selected 是否选中（选中段以禁用态渲染且不可再点击）
 * @param onSelect 点击回调（null = 只读展示，同 [Switch] 的 null 回调约定）
 * @param index 本段在组内的序号（0 起）
 * @param count 组内总段数
 * @param colors 配色集，默认 [ButtonDefaults.colors]
 * @param sprite 位置精灵覆盖；null = 按 [index]/[count]/布局方向解析
 */
@Composable
fun RadioButton(
    selected: Boolean,
    onSelect: (() -> Unit)?,
    index: Int = 0,
    count: Int = 1,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.colors(),
    sprite: UiStateSprite? = null,
    contentPadding: PaddingValues = RadioButtonDefaults.contentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit = {},
) {
    // RTL 时 Row 自动镜像摆放（index 0 落在视觉最右），首尾纹理映射随之交换：
    // 视觉最左端永远用 left 纹理，与 index 无关
    val layoutDirection = LocalLayoutDirection.current
    val resolvedSprite = sprite ?: RadioButtonDefaults.sprite(index, count, layoutDirection)

    val isSelected = selected

    Button(
        onClick = { onSelect?.invoke() },
        modifier = modifier.semantics { this.selected = isSelected },
        enabled = enabled && !selected,
        colors = colors,
        sprite = resolvedSprite,
        contentPadding = contentPadding,
        minSize = RadioButtonDefaults.minSize,
        role = Role.RadioButton,
        interactionSource = interactionSource,
    ) {
        content()
    }
}

object RadioButtonDefaults {

    inline val meta get() = SokitsuThemeMeta.radioButton

    /**
     * 按 [index]/[count]/[layoutDirection] 解析位置精灵：
     * 首段 → left、末段 → right、中间 → center、[count] <= 1 → single。
     *
     * RTL（[LayoutDirection.Rtl]）下首尾交换——[Row] 会镜像摆放子项（index 0 在视觉最右），
     * 交换后视觉最左端仍然命中 left 纹理，与布局方向解耦。
     */
    fun sprite(index: Int, count: Int, layoutDirection: LayoutDirection): UiStateSprite = meta.run {
        val first = if (layoutDirection == LayoutDirection.Rtl) rightSprite else leftSprite
        val last = if (layoutDirection == LayoutDirection.Rtl) leftSprite else rightSprite
        when {
            count <= 1         -> singleSprite
            index == 0         -> first
            index == count - 1 -> last
            else               -> centerSprite
        }
    }.toSprite()

    /**
     * 单选按钮最小尺寸：来自主题 meta 的 radio_button 段（**单位 dp**，资源包可覆盖）。
     */
    val minSize: DpSize get() = meta.minSize

    /**
     * 内容内边距（水平 = [RadioButtonMeta.padding]，垂直 = [RadioButtonMeta.paddingVertical]）。
     */
    val contentPadding: PaddingValues get() = meta.padding

    /**
     * 组内相邻段的间距：来自主题 meta 的 radio_button 段，0 = 无缝拼接。
     */
    val spacing: Dp get() = meta.spacing
}

/** 标记 [RadioButtonGroup] 的 `item { }` DSL，禁止在 scope 外随意混用其它接收者。 */
@DslMarker
annotation class RadioButtonGroupDsl

/**
 * [RadioButtonGroup] 的子项收集器：`item { }` 按声明顺序编号（0 起），
 * 组内部自动向每个 [RadioButton] 传入 index/count，调用方无需手数。
 */
@RadioButtonGroupDsl
class RadioButtonGroupScope internal constructor() {

    internal class Item(
        val enabled: Boolean,
        val content: @Composable RowScope.() -> Unit,
    )

    internal val items = mutableListOf<Item>()

    /**
     * 声明一个选项，按调用顺序编号。
     *
     * @param enabled 本段是否可交互（叠加在组级 [RadioButtonGroup.enabled] 之上）
     */
    fun item(
        enabled: Boolean = true,
        content: @Composable RowScope.() -> Unit = {},
    ) {
        items += Item(enabled, content)
    }
}

/**
 * 单选按钮组（RadioButtonGroup）：持有受控选中态的 [RadioButton] 横向容器。
 *
 * 布局为 [Row] + [spacing] 间距；`Row` 在 RTL 布局方向下
 * 自动镜像摆放，位置精灵映射由 [RadioButtonDefaults.sprite] 同步交换，两端纹理始终贴视觉边缘。
 * 选中段以禁用态渲染（见 [RadioButton]）。
 *
 * 子项经 [RadioButtonGroupScope.item] 声明，自动按声明顺序编号并传入 [RadioButton]，
 * 调用方不接触 index/count。
 *
 * @param selected 当前选中的序号（null = 全不选）；状态归调用方，本组件不持有
 * @param onSelect 点击某段时回调其序号
 * @param enabled 组级开关，叠加在每个子项的 [RadioButtonGroupScope.item] enabled 之上
 */
@Composable
fun RadioButtonGroup(
    selected: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.colors(),
    spacing: Dp = RadioButtonDefaults.spacing,
    content: RadioButtonGroupScope.() -> Unit,
) {
    // 每次重组按 content 重建收集结果：lambda 身份随捕获状态变化时自动重新收集
    val scope = remember(content) { RadioButtonGroupScope().apply(content) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        scope.items.forEachIndexed { index, item ->
            RadioButton(
                selected = selected == index,
                onSelect = { onSelect(index) },
                index = index,
                count = scope.items.size,
                enabled = enabled && item.enabled,
                colors = colors,
                content = item.content,
            )
        }
    }
}
