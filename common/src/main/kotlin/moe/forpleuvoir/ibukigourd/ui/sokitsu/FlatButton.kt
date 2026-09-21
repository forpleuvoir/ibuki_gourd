package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpSize
import moe.forpleuvoir.ibukigourd.ui.sokitsu.draw.sokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalColorScheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ProvideContentColorTextStyle
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuTheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolve
import moe.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.sounds.SoundEvents

/**
 * 扁平按钮：**没有常驻背景**的按钮底座，[IconButton] / [TextButton] 都基于它。
 *
 * 与 [Button] 的差别在状态渲染策略：
 * - Button 四态各有素材，**总是**画背景（外框色块）；
 * - FlatButton 只在**有素材的状态**下画背景 —— `flat_button` 素材只画了 `pressed` / `focused`
 *   两张，`normal` / `disabled` 查不到资源，此时**整块不画**（保持透明），
 *   而不是像 [Surface] 那样退化成纯色填充。
 *
 * 背景是否绘制由「该状态精灵是否为空」决定（[moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite.isEmpty]），
 * 所以日后往 `ui/flat_button/` 补 `normal.aseprite` / `disabled.aseprite` 即自动生效，无需改代码。
 *
 * 内容色经 [ProvideContentColorTextStyle] 下发给子树（内部 [Text] 自动取到），按状态修正：
 * - 可交互状态：按 [FlatButtonMeta.contentBlend] 的系数向**纯黑或纯白**线性混合
 *   （方向按主题亮暗：亮色压暗、暗色提亮；不涉及 alpha），拉大与背景的明暗差，系数 0 则不修正；
 * - 禁用态：按 [FlatButtonMeta.disabledBlend] 向**背景色**混合（反向操作：缩小对比度），
 *   让内容"退进背景"以示不可用 —— 与常态拉开区别。
 * 交互用 [clickable] + 点击音效，语义角色默认 `Role.Button`（可用 [role] 覆盖）。
 *
 * @param colors 配色集，默认 [FlatButtonDefaults.colors]（底色与内容色缺省均取主题 primary）
 * @param sprite 四态精灵覆盖，默认取 [FlatButtonMeta.sprite]
 * @param contentPadding 内容内边距（背景铺满整体，内边距只作用于内容）
 * @param contentAlignment 内容对齐，默认 [Alignment.Center]；两级分别落到
 *   `Row.horizontalArrangement` 与 `Row.verticalAlignment`（如 [Alignment.TopStart] =
 *   `Arrangement.Start` + `Alignment.Top`）
 */
@Composable
fun FlatButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: FlatButtonColors = FlatButtonDefaults.colors(),
    sprite: UiStateSprite = FlatButtonDefaults.sprite(),
    contentPadding: PaddingValues = FlatButtonDefaults.contentPadding,
    minSize: DpSize = FlatButtonDefaults.minSize,
    role: Role = Role.Button,
    contentAlignment: Alignment = Alignment.Center,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()

    val state = UiState.resolve(enabled, pressed, hovered, focused)

    // 该状态没有素材 → 不画背景（保持透明）；有素材才提交精灵命令
    val background = sprite[state].takeIf { !it.isEmpty }

    // 内容色修正：按状态系数向纯黑/纯白混合，**拉大与背景的明暗差**（不涉及 alpha）。
    // 方向按主题亮暗定：亮色主题的背景偏亮 → 内容压暗；暗色主题反过来。
    // 注意不能朝"底色的配对内容色"混 —— 背景是底色调淡后叠在父容器上（实际偏亮），
    // 亮色主题下那个配对色本身是亮色，混过去反而把内容洗白。
    val meta = FlatButtonDefaults.meta
    val scheme = LocalColorScheme.current
    val contentBlend = meta.contentBlendOf(state)
    val contentColor = when {
        // 禁用态：反向弱化 —— 向背景色混合，让内容"退到背景里"以示不可用
        state == UiState.Disabled -> lerp(colors.contentColor, scheme.surface, meta.disabledBlend)
        contentBlend <= 0f        -> colors.contentColor
        else                      -> lerp(
            colors.contentColor,
            if (scheme.isLight) Color.Black else Color.White,
            contentBlend,
        )
    }

    val icon = if (enabled) FlatButtonDefaults.LocalHoverIcon.current else FlatButtonDefaults.LocalDisableIcon.current
    // 点击音效需在组合作用域内先取出：onClick lambda 不是 @Composable 上下文
    val pressSound = FlatButtonDefaults.LocalPressSound.current

    ProvideContentColorTextStyle(
        contentColor = contentColor,
        textStyle = SokitsuTheme.typography.button,
    ) {
        Row(
            modifier = modifier
                .pointerHoverIcon(icon)
                .semantics { this.role = role }
                .defaultMinSize(minSize.width, minSize.height)
                // 背景铺满整个按钮（含内边距），故在 padding 之前
                .then(if (background != null) Modifier.sokitsuSprite(background, colors.color) else Modifier)
                // 需在 clickable 之前：Initial 趟自外向内派发，指针观察要先拿到按下事件
                .releaseFocusOnPointerPress(enabled)
                .clickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    enabled = enabled,
                    onClick = {
                        pressSound?.let { mc.soundManager.play(it) }
                        onClick()
                    },
                )
                .padding(contentPadding),
            horizontalArrangement = contentAlignment.toHorizontalArrangement(),
            verticalAlignment = contentAlignment.toVerticalAlignment(),
            content = content,
        )
    }
}

/**
 * 指针按下后清掉焦点：**只有鼠标点击会清**，键盘激活（Enter / Space）保留聚焦反馈。
 *
 * CMP 在非 Android 目标上把 `isRequestFocusOnClickEnabled()` 写死为 `true`（上游 TODO：CMP-5814），
 * `clickable` 于是**按下即请求焦点**，松手后聚焦纹理一直亮着；而键盘激活走 `clickable` 的按键分支、
 * 不经过指针手势 —— 两类交互因此可以按"有没有指针按下"干净区分，不需要拿 `hovered` / `focused` 去猜。
 *
 * 时序：Initial 趟拿到按下（此时 `clickable` 还没处理，故 `requireUnconsumed = false`），
 * 再等这一个事件的 Final 趟 —— `clickable` 在 Main 趟请求焦点，早于此刻清才不会又被它点亮。
 * 手势其余部分交给 [awaitEachGesture] 收尾（等所有指针抬起再进下一次）。
 *
 * 必须放在 `.clickable(...)` **之前**：Initial 趟自外向内派发，指针观察要先于点击处理拿到按下事件。
 *
 * @param enabled 控件禁用时 `clickable` 不会请求焦点，也就不需要挂指针观察
 */
@Composable
private fun Modifier.releaseFocusOnPointerPress(enabled: Boolean): Modifier {
    if (!enabled) return this
    val focusManager: FocusManager = LocalFocusManager.current
    return this.pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            awaitPointerEvent(PointerEventPass.Final)
            focusManager.clearFocus()
        }
    }
}

/**
 * 扁平按钮配色集：只有两个**基准色** —— [color] 为背景精灵的染色色板，
 * [contentColor] 为内容色基准值。
 *
 * 各状态的内容色**不在这里逐个指定**：由 [FlatButtonMeta.contentBlend] 的逐状态系数
 * 乘在 [contentColor] 上得到（可用 data class `copy` 微调基准色）。
 */
@Immutable
data class FlatButtonColors(
    val color: Color,
    val contentColor: Color,
)

/**
 * 把两轴合一的 [Alignment] 拆成行布局的水平排列：[Alignment.Start] 系 → [Arrangement.Start]、
 * [Alignment.End] 系 → [Arrangement.End]，其余（含 [Alignment.CenterHorizontally]）→ [Arrangement.Center]。
 */
private fun Alignment.toHorizontalArrangement(): Arrangement.Horizontal = when (this) {
    Alignment.Start, Alignment.TopStart, Alignment.CenterStart, Alignment.BottomStart -> Arrangement.Start
    Alignment.End, Alignment.TopEnd, Alignment.CenterEnd, Alignment.BottomEnd -> Arrangement.End
    else -> Arrangement.Center
}

/**
 * 把两轴合一的 [Alignment] 拆成行布局的垂直对齐：[Alignment.Top] 系 → [Alignment.Top]、
 * [Alignment.Bottom] 系 → [Alignment.Bottom]，其余（含 [Alignment.CenterVertically]）→
 * [Alignment.CenterVertically]。
 */
private fun Alignment.toVerticalAlignment(): Alignment.Vertical = when (this) {
    Alignment.Top, Alignment.TopStart, Alignment.TopEnd -> Alignment.Top
    Alignment.Bottom, Alignment.BottomStart, Alignment.BottomEnd -> Alignment.Bottom
    else -> Alignment.CenterVertically
}

object FlatButtonDefaults {

    /** 当前主题的扁平按钮 meta（sprite / 最小尺寸 / 内边距）。 */
    inline val meta get() = SokitsuThemeMeta.flatButton

    val LocalPressSound = compositionLocalOf<SoundInstance?> { SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f) }

    val LocalHoverIcon = compositionLocalOf { PointerIcon.Hand }
    val LocalDisableIcon = compositionLocalOf { PointerIcon.NotAllowed }

    /**
     * 默认配色集：底色与内容色**缺省都取主题 primary**（[FlatButtonTokens.Container] / [FlatButtonTokens.Content]），
     * 即"内容用主色、悬停时叠一层主色淡化块"的扁平按钮观感。
     *
     * 两个参数默认 [Color.Unspecified]，语义是"调用方没意见，按 token 解析"；
     * 显式传值即完全接管该槽位。
     *
     * 这里只给**基准色**：各状态的内容色 = 基准色 × [FlatButtonMeta.contentBlend] 对应系数
     * （禁用态默认 0.62，其余状态 1.0），系数在主题 meta 里调，不在此处。
     */
    @Composable
    fun colors(
        color: Color = Color.Unspecified,
        contentColor: Color = Color.Unspecified,
    ): FlatButtonColors {
        val resolvedTone = color.resolve(FlatButtonTokens.Container)
        val resolvedContent = contentColor.resolve(FlatButtonTokens.Content)
        return FlatButtonColors(
            color = resolvedTone,
            contentColor = resolvedContent,
        )
    }

    /** 四态精灵：来自 [FlatButtonMeta.sprite]（`normal` / `disabled` 无素材则为空容器 → 不渲染背景）。 */
    fun sprite(): UiStateSprite = meta.sprite.toSprite()

    /** 最小尺寸（**单位 dp**，来自主题 meta 的 flat_button 段）。 */
    val minSize: DpSize get() = meta.minSize

    /** 内容内边距（来自主题 meta 的 flat_button 段）。 */
    val contentPadding: PaddingValues get() = meta.padding
}
