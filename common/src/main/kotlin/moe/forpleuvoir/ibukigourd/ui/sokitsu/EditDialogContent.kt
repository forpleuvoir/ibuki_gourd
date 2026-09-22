package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.ui.util.FabVisibilityDefaults
import moe.forpleuvoir.ibukigourd.ui.util.FabVisibilityState
import moe.forpleuvoir.ibukigourd.ui.util.fabScrollVisibility
import moe.forpleuvoir.ibukigourd.ui.util.rememberFabScrollVisibility
import moe.forpleuvoir.ibukigourd.ui.util.rememberHideActionState

/**
 * 编辑对话框内容区：表头在上、列表在下，右下角可选一个浮动操作按钮。
 *
 * 浮动按钮的行为由本组件承担（调用方只给按钮内容）：
 * - 位置固定在内容区右下角，按 [addButtonPadding] 留边；
 * - 向下滚动累计超过 `FabVisibilityDefaults.hideDistance` 收起、向上滚动立即恢复
 *   （方向判定由 `Modifier.fabScrollVisibility` 的嵌套滚动回调驱动）；
 * - 按住隐藏动作键（`IGConfig.Gui.hideActionKeyCode`）期间强制收起；
 * - [addButtonAnimated] 为 `true`（默认）时用 [AnimatedVisibility] 做淡入 / 上移 / 缩放，
 *   隐藏后按钮移出组合、不再响应点击；置 `false` 则直接按显隐状态控制组合，无过渡。
 *
 * 列表状态由 [lazyListState] 提供并交给 [content]（内部列表用它驱动浮动按钮的显隐）。
 *
 * @param modifier 应用到内容区 [Box] 的 Modifier
 * @param header 表头槽位，默认 [EditDialogContentHeader]
 * @param addButton 浮动按钮内容，null 时不显示
 * @param lazyListState 列表状态，默认 `rememberLazyListState()`
 * @param addButtonPadding 浮动按钮与内容区右下角的间距
 * @param addButtonAnimated 是否对浮动按钮做显隐动画
 * @param content 正文槽位，参数为该内容区的列表状态
 */
@Composable
fun EditDialogContent(
    modifier: Modifier = Modifier,
    header: @Composable (ColumnScope.() -> Unit) = { EditDialogContentHeader() },
    addButton: (@Composable () -> Unit)? = null,
    lazyListState: LazyListState = rememberLazyListState(),
    addButtonPadding: PaddingValues = EditDialogContentDefaults.addButtonPadding,
    addButtonAnimated: Boolean = true,
    content: @Composable ColumnScope.(LazyListState) -> Unit,
) {
    val fabVisibility = rememberFabScrollVisibility(lazyListState)
    val hiddenByKey = rememberHideActionState()
    val visible = fabVisibility.state == FabVisibilityState.Visible && !hiddenByKey

    Box(modifier = modifier.fabScrollVisibility(fabVisibility)) {
        Column(modifier = Modifier) {
            Spacer(Modifier.height(EditDialogContentDefaults.contentTopPadding))
            header()
            Spacer(Modifier.height(EditDialogContentDefaults.headerBottomGap))
            content(lazyListState)
        }

        if (addButton != null) {
            val duration = FabVisibilityDefaults.hideDuration.inWholeMilliseconds.toInt()
            val offsetPx = with(LocalDensity.current) { FabVisibilityDefaults.translationY.roundToPx() }

            // 外层只负责贴右下角，内层只负责留边：留边作用在盒子上，按钮才会被推离边缘
            Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                Box(modifier = Modifier.padding(addButtonPadding)) {
                    if (addButtonAnimated) {
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(tween(duration)) +
                                    scaleIn(tween(duration), initialScale = 0.8f) +
                                    slideInVertically(tween(duration)) { offsetPx },
                            exit = fadeOut(tween(duration)) +
                                    scaleOut(tween(duration), targetScale = 0.8f) +
                                    slideOutVertically(tween(duration)) { offsetPx },
                        ) {
                            addButton()
                        }
                    } else if (visible) {
                        addButton()
                    }
                }
            }
        }
    }
}

/**
 * 编辑对话框表头：可选的首列（移动 / 排序）、居中的内容标题列、可选的尾列（删除）。
 *
 * 三列的宽度可控，首尾列存在时按 [columnSpacing] 与内容列留出间距；三列内容都居中。
 *
 * @param modifier 应用到整个表头的 Modifier
 * @param height 表头行高
 * @param columnSpacing 列间距
 * @param moveColumnWidth 首列宽度
 * @param removeColumnWidth 尾列宽度
 * @param moveHeader 首列内容，null 时不占位
 * @param contentHeader 内容列内容
 * @param removeHeader 尾列内容，null 时不占位
 * @param divider 表头下方的分割线，null 时不显示
 */
@Composable
fun EditDialogContentHeader(
    modifier: Modifier = Modifier,
    height: Dp = EditDialogContentDefaults.headerHeight,
    columnSpacing: Dp = EditDialogContentDefaults.columnSpacing,
    moveColumnWidth: Dp = EditDialogContentDefaults.moveColumnWidth,
    removeColumnWidth: Dp = EditDialogContentDefaults.removeColumnWidth,
    moveHeader: (@Composable () -> Unit)? = { Text(IGLang.ConfigWrapper.move) },
    contentHeader: @Composable () -> Unit = { Text(IGLang.Misc.content) },
    removeHeader: (@Composable () -> Unit)? = { Text(IGLang.Misc.remove) },
    divider: (@Composable () -> Unit)? = { HorizontalDivider() },
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .padding(top = EditDialogContentDefaults.headerTopPadding, bottom = EditDialogContentDefaults.headerBottomPadding),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Bottom,
        ) {
            moveHeader?.let { column ->
                Box(Modifier.width(moveColumnWidth), contentAlignment = Alignment.Center) { column() }
                Spacer(Modifier.width(columnSpacing))
            }

            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { contentHeader() }

            removeHeader?.let { column ->
                Spacer(Modifier.width(columnSpacing))
                Box(Modifier.width(removeColumnWidth), contentAlignment = Alignment.Center) { column() }
            }
        }

        divider?.invoke()
    }
}

/** [EditDialogContent] / [EditDialogContentHeader] 的默认尺寸。 */
object EditDialogContentDefaults {

    /** 表头行高。 */
    val headerHeight: Dp = 56.dp

    /** 表头列之间的水平间距。 */
    val columnSpacing: Dp = 16.dp

    /** 首列（移动 / 排序）宽度。 */
    val moveColumnWidth: Dp = 80.dp

    /** 尾列（删除）宽度。 */
    val removeColumnWidth: Dp = 80.dp

    /** 正文区顶部留白：标题与表头之间的间距已由 `FlexibleDialog` 的 titleBottomPadding 给出，这里不再叠加。 */
    val contentTopPadding: Dp = 0.dp

    /** 表头上内边距。 */
    val headerTopPadding: Dp = 8.dp

    /** 表头下内边距。 */
    val headerBottomPadding: Dp = 8.dp

    /** 表头（含其下分割线）与列表首行之间的留白。 */
    val headerBottomGap: Dp = 12.dp

    /** 浮动按钮与内容区右下角的间距。 */
    val addButtonPadding: PaddingValues = PaddingValues(32.dp)

    /** [EditDialogContentList] 的列表最大高度。 */
    val listMaxHeight: Dp = 640.dp

    /**
     * [EditDialogContentList] 的行高。
     *
     * 取 56dp 与文本输入框的默认最小高度（`text_field.min_size` 的 56dp）一致：
     * 交互按钮默认只有 48dp，不统一的话输入框的条会比左右按钮各高出一截。
     */
    val rowHeight: Dp = 56.dp

    /** [EditDialogContentList] 的行间距。 */
    val rowSpacing: Dp = 12.dp
}
