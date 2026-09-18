package moe.forpleuvoir.ibukigourd.ui.selector

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite

/**
 * 多选展开体持有的可变选中集合：展开期间在内部累积，收起后才交回调用方。
 *
 * 与 [selected]（调用方持有的已确认集合）分离，因此"取消"只需丢弃本对象 —— 下次展开时会
 * 用最新的 [selected] 重建（见 [rememberMutableSelection]）。
 *
 * [items] 是 Compose 快照状态支撑的只读列表，读取它的组合会自动响应增删；
 * 增删一律经 [toggle] 整体替换，不做原地修改。
 *
 * @param T 选项类型
 * @param initial 初始选中项（展开时的基准）
 * @param itemEquals 元素相等判定，"是否已选"按它判断，不依赖元素自身 `equals`
 */
class MutableSelectionState<T>(
    initial: Iterable<T>,
    private val itemEquals: (T, T) -> Boolean = { a, b -> a == b },
) {

    private var state by mutableStateOf(initial.toList())

    /** 当前选中项，顺序为加入顺序。 */
    val items: List<T> get() = state

    /** 当前选中项（集合视图，供回调使用）。 */
    fun toSet(): Set<T> = state.toSet()

    /** [item] 是否已选。 */
    fun contains(item: T): Boolean = state.any { itemEquals(it, item) }

    /** [item] 选中态取反。 */
    fun toggle(item: T) {
        state = if (contains(item)) state.filterNot { itemEquals(it, item) } else state + item
    }
}

/**
 * 记住一份多选状态，并在 [key] 变化时**重新同步**基准集合。
 *
 * [key] 传展开标志时：收起（key 变化）即重建，展开期间的改动归属上一次会话，
 * 取消掉的改动不会带到下次展开。
 *
 * @param selected 基准集合（进入展开时的选中项）
 * @param itemEquals 元素相等判定
 * @param key 重新同步的触发键
 */
@Composable
internal fun <T> rememberMutableSelection(
    selected: Iterable<T>,
    itemEquals: (T, T) -> Boolean,
    key: Any?,
): MutableSelectionState<T> = remember(key) { MutableSelectionState(selected, itemEquals) }

/**
 * 多选条目的复选框图标：按选中态取 [Icons.Checked] / [Icons.Unchecked]。
 *
 * 图标色走 [Icon] 的缺省内容色，即条目当前状态（常态 / 悬停 / 按压）解析出的内容色，
 * 与文案保持同一档明度。
 *
 * 素材缺失时 [Icon] 布局尺寸为 0，等于什么都没画（不抛异常）。
 *
 * @param checked 是否选中
 * @param uncheckedIcon 未选中图标
 * @param checkedIcon 选中图标
 * @param tint 图标色，未指定取当前内容色
 */
@Composable
fun SelectorCheckbox(
    checked: Boolean,
    uncheckedIcon: SokitsuSprite = Icons.Unchecked,
    checkedIcon: SokitsuSprite = Icons.Checked,
    tint: Color = Color.Unspecified,
) {
    Icon(if (checked) checkedIcon else uncheckedIcon, tint = tint)
}
