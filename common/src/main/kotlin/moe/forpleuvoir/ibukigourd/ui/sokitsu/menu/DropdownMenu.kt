package moe.forpleuvoir.ibukigourd.ui.sokitsu.menu

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import moe.forpleuvoir.compose_minecraft.platform.ui.popup.LocalPopupHost
import moe.forpleuvoir.compose_minecraft.platform.ui.popup.register
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlatButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlatButtonDefaults
import moe.forpleuvoir.ibukigourd.ui.sokitsu.LocalTextStyle
import moe.forpleuvoir.ibukigourd.ui.sokitsu.VerticalOverlayScroller
import moe.forpleuvoir.ibukigourd.ui.sokitsu.draw.sokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.rememberScrollerAdapter
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.*
import kotlin.math.roundToInt

/**
 * 菜单项点击后用于自我关闭的控制器。
 *
 * 由 [DropdownMenu] 下发；使用 [DropdownMenuItem] 时无需调用方处理关闭。
 */
fun interface DropdownMenuController {
    fun dismiss()
}

/** 当前菜单的控制器；在 [DropdownMenu] 的 content 作用域内可用。 */
val LocalDropdownMenuController = staticCompositionLocalOf<DropdownMenuController?> { null }

/**
 * 菜单的展开标志与触发锚点。
 *
 * 锚点由 [Modifier.dropdownMenuAnchor] 记录；本类只是把两者打包，等价于分别持有
 * `Boolean` 与 [Rect] 两个状态。
 */
@Stable
class DropdownMenuState {

    /** 菜单是否展开。 */
    var expanded by mutableStateOf(false)
        private set

    /** 触发组件在 root 坐标系中的 bounds；首帧可能为 [Rect.Zero]。 */
    var anchorBounds by mutableStateOf(Rect.Zero)
        internal set

    fun open() {
        expanded = true
    }

    fun dismiss() {
        expanded = false
    }

    fun toggle() {
        expanded = !expanded
    }
}

@Composable
fun rememberDropdownMenuState(): DropdownMenuState = remember { DropdownMenuState() }

/**
 * 在**触发组件**上记录它在 root 坐标系中的 bounds，作为菜单锚点。
 *
 * 只记录位置，不新增布局节点。
 */
fun Modifier.dropdownMenuAnchor(onBoundsChange: (Rect) -> Unit): Modifier =
    onGloballyPositioned { onBoundsChange(it.boundsInRoot()) }

/** [Modifier.dropdownMenuAnchor] 的状态版：把锚点写入 [DropdownMenuState.anchorBounds]。 */
fun Modifier.dropdownMenuAnchor(state: DropdownMenuState): Modifier =
    dropdownMenuAnchor { state.anchorBounds = it }

/**
 * 菜单定位器：水平与触发组件**中心对齐**；垂直优先贴下方，下方放不下则翻到上方；
 * 最后连同 [restEdgeSpacing] 夹取进窗口。
 *
 * [below] 记录本次落点在锚点的哪一侧，渲染时据此选择箭头朝向。
 *
 * @param anchorBounds 触发组件 bounds 的惰性读取器
 * @param spacing 菜单与触发组件之间的间距
 * @param restEdgeSpacing 菜单与窗口其余边缘的安全间距
 */
@Immutable
private class DropdownMenuPositionProvider(
    private val anchorBounds: () -> Rect,
    private val spacing: Int,
    private val restEdgeSpacing: Int = DropdownMenuRestEdgeSpacing,
) : PopupPositionProvider {

    /** 落点是否在锚点下方。 */
    var below: Boolean = true
        private set

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val measured = this.anchorBounds()
        // 锚点尚未记录（首帧）时使用传入的窗口锚点
        val anchor = if (measured.width == 0f && measured.height == 0f) {
            anchorBounds
        } else {
            IntRect(
                measured.left.roundToInt(), measured.top.roundToInt(),
                measured.right.roundToInt(), measured.bottom.roundToInt(),
            )
        }

        val menuWidth = popupContentSize.width.coerceAtLeast(0)
        val menuHeight = popupContentSize.height.coerceAtLeast(0)
        val rootWidth = windowSize.width
        val rootHeight = windowSize.height

        val spaceBelow = rootHeight - anchor.bottom - spacing
        val spaceAbove = anchor.top - spacing
        below = spaceBelow >= menuHeight || spaceBelow >= spaceAbove
        val y = if (below) anchor.bottom + spacing else anchor.top - menuHeight - spacing

        // 与锚点中心对齐
        val x = anchor.left + (anchor.width - menuWidth) / 2

        val xRange = restEdgeSpacing..(rootWidth - menuWidth - restEdgeSpacing).coerceAtLeast(restEdgeSpacing)
        val yRange = restEdgeSpacing..(rootHeight - menuHeight - restEdgeSpacing).coerceAtLeast(restEdgeSpacing)
        return IntOffset(x.coerceIn(xRange), y.coerceIn(yRange))
    }
}

/**
 * 菜单高度限制：按锚点上/下两侧的可用空间钳制高度（取空间更大的一侧），
 * 保证菜单翻转后不覆盖锚点，超出部分由内容区滚动。
 * 入参约束为弹层根约束（maxHeight = 窗口高）；高度无界时不钳制。
 * 与 [DropdownMenuPositionProvider] 的落点判定同规则，两者对锚点侧的选择保持一致。
 */
/**
 * 菜单与窗口其余边缘的安全间距（与 [DropdownMenuPositionProvider] 共用）。
 * 高度钳制时预留它，定位器的边缘夹取才不会再吃掉锚点侧的 [spacing]。
 */
private const val DropdownMenuRestEdgeSpacing = 4

/**
 * 菜单高度限制：按锚点上/下两侧的可用空间钳制高度（取空间更大的一侧），
 * 保证菜单翻转后不覆盖锚点，超出部分由内容区滚动。
 * 入参约束为弹层根约束（maxHeight = 窗口高）；高度无界时不钳制。
 * 与 [DropdownMenuPositionProvider] 的落点判定同规则，两者对锚点侧的选择保持一致。
 * 两侧各预留 [DropdownMenuRestEdgeSpacing]，使定位器边缘夹取后仍保留完整的 [spacing] 间隙。
 */
private fun Modifier.sideHeightLimit(anchorBounds: Rect, spacingPx: Int): Modifier =
    layout { measurable, constraints ->
        if (!constraints.hasBoundedHeight) {
            val placeable = measurable.measure(constraints)
            layout(placeable.width, placeable.height) { placeable.placeRelative(0, 0) }
        } else {
            val spaceBelow = (constraints.maxHeight - anchorBounds.bottom.roundToInt()
                - spacingPx - DropdownMenuRestEdgeSpacing).coerceAtLeast(0)
            val spaceAbove = (anchorBounds.top.roundToInt()
                - spacingPx - DropdownMenuRestEdgeSpacing).coerceAtLeast(0)
            val maxH = if (spaceBelow >= spaceAbove) spaceBelow else spaceAbove
            val placeable = measurable.measure(
                constraints.copy(minHeight = 0, maxHeight = minOf(constraints.maxHeight, maxH))
            )
            layout(placeable.width, placeable.height) { placeable.placeRelative(0, 0) }
        }
    }

/**
 * 弹出菜单：在 [anchorBounds] 指定的位置附近弹出 [content]。
 *
 * 本组件负责弹出与关闭：定位（与触发组件中心对齐、下方不足翻上方）、点击外部与 Esc 关闭、
 * 面板外观、可选的内容区滚动。菜单内容放什么、每项长什么样完全由 [content] 决定，
 * 可用 [DropdownMenuItem]，也可直接组合任意 Compose 内容。
 *
 * 面板为气泡体：宽高由内容撑开（只受 [DropdownMenuDefaults.minSize] 下限约束），
 * 各项宽度即各自内容宽度。外观参数均为可覆盖的默认值，取自 [DropdownMenuDefaults] 与主题 meta。
 *
 * @param expanded 是否展开
 * @param onDismissRequest 请求关闭（点击外部、Esc、以及默认条目点击之后）
 * @param anchorBounds 触发组件在 root 坐标系中的 bounds
 * @param color 面板染色，未指定按 [DropdownMenuTokens.Panel] 解析
 * @param contentColor 内容色，未指定按 [DropdownMenuTokens.Content] 解析
 * @param padding 面板内边距，默认 [DropdownMenuDefaults.padding]
 * @param maxHeight 内容区最大高度，超出后滚动；传 [Dp.Infinity] 即不限制
 * @param content 菜单内容
 */
@Composable
fun DropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    anchorBounds: Rect,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    padding: PaddingValues = DropdownMenuDefaults.padding,
    maxHeight: Dp = DropdownMenuDefaults.maxHeight,
    content: @Composable ColumnScope.() -> Unit,
) {
    val popupHost = LocalPopupHost.current ?: return
    if (!expanded) return

    val density = LocalDensity.current
    val scheme = LocalColorScheme.current
    val spacing = with(density) { DropdownMenuDefaults.spacing.roundToPx() }
    // anchorBounds 纳入 key：锚点移动后重建 provider（闭包捕获最新 bounds），
    // 否则首次组合的快照值会让弹层在 measure 时读到旧坐标、不跟随目标。
    val provider = remember(anchorBounds, spacing) { DropdownMenuPositionProvider({ anchorBounds }, spacing) }
    val popupKey = remember { Any() }

    val resolvedColor = color.takeOrElse { scheme.fromToken(DropdownMenuTokens.Panel) }
    val resolvedContent = contentColor.takeOrElse { scheme.fromToken(DropdownMenuTokens.Content) }

    popupHost.register(
        key = popupKey,
        positionProvider = provider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(
            focusable = true,
            dismissOnClickOutside = true,
            dismissOnBackPress = true,
        ),
    ) {
        ProvideContentColorTextStyle(
            contentColor = resolvedContent,
            textStyle = LocalTextStyle.current,
        ) {
            // 面板只画气泡体（九宫格），不要箭头：用单精灵 sokitsuSprite，而非带箭头的 BubblePanel
            Box(
                modifier
                    .sideHeightLimit(anchorBounds, spacing)
                    .sokitsuSprite(DropdownMenuDefaults.panelSprite(), color = resolvedColor)
                    .sizeIn(
                        minWidth = DropdownMenuDefaults.minSize.width,
                        minHeight = DropdownMenuDefaults.minSize.height,
                    )
                    .padding(padding),
            ) {
                val scrollState = rememberScrollState()
                // 滚动条与内容共用同一 ScrollState（经 ScrollerAdapter 桥接），滑块位置、
                // 拖拽、滚轮都与内容严格同步 —— 不引入任何耦合列表的参数；
                // 内容溢出与否由 autoHide 判定（maxScrollOffset ≤ 0 时空组合）。
                // overlay 样式 + 并列占位：滚动条与内容同行、占据自身厚度，
                // 列与滚动条之间留 pixelScale*2 的间距；autoHide 空组合时间距一并消失。
                val scrollSpacing = (LocalSokitsuPixelScale.current * 2).dp
                Row(
                    Modifier.height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(scrollSpacing),
                ) {
                    Column(
                        Modifier
                            // 列宽取最宽子项的整行固有宽：条目不被挤压换行，
                            // 分割线等 fillMaxWidth 子项据此对齐，而不是铺满弹层的可用宽度
                            .width(IntrinsicSize.Max)
                            .heightIn(max = maxHeight)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(DropdownMenuDefaults.itemSpacing),
                    ) {
                        CompositionLocalProvider(
                            LocalDropdownMenuController provides remember(onDismissRequest) {
                                DropdownMenuController { onDismissRequest() }
                            },
                        ) {
                            content()
                        }
                    }
                    VerticalOverlayScroller(
                        adapter = rememberScrollerAdapter(scrollState),
                        modifier = Modifier.fillMaxHeight(),
                        autoHide = true,
                        autoFade = true,
                    )
                }
            }
        }
    }
}

/** [DropdownMenu] 的状态版重载：各参数含义与主重载一致。 */
@Composable
fun DropdownMenu(
    state: DropdownMenuState,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    padding: PaddingValues = DropdownMenuDefaults.padding,
    maxHeight: Dp = DropdownMenuDefaults.maxHeight,
    content: @Composable ColumnScope.() -> Unit,
) {
    DropdownMenu(
        expanded = state.expanded,
        onDismissRequest = state::dismiss,
        anchorBounds = state.anchorBounds,
        modifier = modifier,
        color = color,
        contentColor = contentColor,
        padding = padding,
        maxHeight = maxHeight,
        content = content,
    )
}

/**
 * 一行可点击的菜单条目。
 *
 * 宽度即内容宽度（不填充父级）；所有视觉参数都是默认值，可逐个覆盖，[modifier] 里的尺寸
 * 约束优先于组件默认值。
 *
 * - 高度按 [minHeight] 取**下限**，内容更高时条目随之撑开；
 * - 点击后先执行 [onClick]，随后关闭所属菜单；
 * - [enabled] 为 false 时不可点击、不响应悬停，内容色按不透明度弱化；
 * - 图标槽位按 [iconScale] 渲染像素放大倍率。
 *
 * @param onClick 点击回调
 * @param minHeight 条目最小高度，默认 [DropdownMenuDefaults.itemMinHeight]
 * @param padding 条目内边距，默认 [DropdownMenuDefaults.itemPadding]
 * @param iconSpacing 图标槽位与内容之间的间距，默认 [DropdownMenuDefaults.iconSpacing]
 * @param iconScale 图标槽位的像素放大倍率，默认 [DropdownMenuDefaults.iconScale]
 * @param leadingIcon 前置图标槽位
 * @param trailingIcon 后置图标槽位
 * @param content 条目内容
 */
@Composable
fun DropdownMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minHeight: Dp = DropdownMenuDefaults.itemMinHeight,
    padding: PaddingValues = DropdownMenuDefaults.itemPadding,
    iconSpacing: Dp = DropdownMenuDefaults.iconSpacing,
    iconScale: Int = DropdownMenuDefaults.iconScale,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val controller = LocalDropdownMenuController.current
    val interactionSource = remember { MutableInteractionSource() }
    val resolvedContent = LocalContentColor.current

    // FlatButton 自身即 Row（content 处于 RowScope），无需再套一层 Row；
    // 悬停高亮由 FlatButton 的 focused 状态精灵承担，这里不叠加 background，避免双重背景。
    FlatButton(
        onClick = {
            onClick()
            controller?.dismiss()
        },
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        sprite = DropdownMenuDefaults.itemSprite(),
        contentPadding = padding,
        minSize = DpSize(Dp.Hairline, minHeight),
        colors = FlatButtonDefaults.colors(contentColor = resolvedContent),
    ) {
        leadingIcon?.let {
            CompositionLocalProvider(LocalSokitsuPixelScale provides iconScale) { it() }
            Spacer(Modifier.width(iconSpacing))
        }
        content()
        trailingIcon?.let {
            Spacer(Modifier.width(iconSpacing))
            CompositionLocalProvider(LocalSokitsuPixelScale provides iconScale) { it() }
        }
    }
}
