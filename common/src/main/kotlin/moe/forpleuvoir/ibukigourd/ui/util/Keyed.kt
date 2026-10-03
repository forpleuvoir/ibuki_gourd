package moe.forpleuvoir.ibukigourd.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.structuralEqualityPolicy

/**
 * 带稳定 key 的列表条目。
 *
 * [key] 用于条目增删、重排时维持 Compose 身份（列表 API 的 `key` 参数），[value] 为业务数据。
 * key 由 [KeyedListState] 单调分配，同一实例内不重复。
 */
data class Keyed<T>(
    val key: Long,
    val value: T,
)

/** 取出全部条目值，保持顺序。 */
fun <T> List<Keyed<T>>.values(): List<T> = map { it.value }

/** 保留 key，替换值。 */
fun <T> Keyed<T>.copyValue(value: T): Keyed<T> = copy(value = value)

/**
 * 条目容器：持有条目载体列表（key + 值的状态）与 key 自增计数器。
 *
 * 每个条目的值各自持有状态，由构造时的 [SnapshotMutationPolicy] 判断"值是否算变化"；
 * 列表本身只维护 key 与顺序，值写入不经过 `T.equals`。增删一律经本类方法，key 唯一性由内部计数器
 * 保证；计数器不回退（删除条目不回收 key），Long 不会溢出。
 *
 * [entries] 为只读视图，其 `get` 读取该条目的值状态，读取落在调用方（行 / 卡片）的重组作用域内。
 *
 * 实例由 [rememberKeyedList] 创建，不可自行构造。
 */
@Stable
class KeyedListState<T> internal constructor(
    initial: List<T>,
    private val policy: SnapshotMutationPolicy<T>,
) {

    /** 条目的 key 与值的状态载体；[key] 终生不变，值只写 [valueState]。 */
    @Stable
    private class Cell<T>(val key: Long, value: T, policy: SnapshotMutationPolicy<T>) {
        val valueState: MutableState<T> = mutableStateOf(value, policy)
    }

    private val cells = mutableStateListOf<Cell<T>>()
    private val nextKey = mutableLongStateOf(0L)

    init {
        initial.forEach { cells.add(Cell(nextKey.longValue++, it, policy)) }
    }

    private val entriesView: List<Keyed<T>> = object : AbstractList<Keyed<T>>() {
        override val size: Int get() = cells.size
        override fun get(index: Int): Keyed<T> = cells[index].let { Keyed(it.key, it.valueState.value) }
    }

    /** 只读条目视图，可直接传给 `items(entries, key = { it.key })` 一类列表 API。 */
    val entries: List<Keyed<T>> get() = entriesView

    /** 条目数。 */
    val size: Int get() = cells.size

    /** 末尾追加条目，key 取计数器当前值并自增。 */
    fun add(value: T): Keyed<T> = add(cells.size, value)

    /** 在 [index] 处插入条目，key 取计数器当前值并自增。 */
    fun add(index: Int, value: T): Keyed<T> {
        val cell = Cell(nextKey.longValue++, value, policy)
        cells.add(index, cell)
        return Keyed(cell.key, value)
    }

    /** 移除 [index] 处条目并返回，其 key 不回收。 */
    fun removeAt(index: Int): Keyed<T> = cells.removeAt(index).let { Keyed(it.key, it.valueState.value) }

    /**
     * 把 [fromIndex] 处条目移动到 [toIndex]。
     *
     * 语义与 `MutableList.moveElement` 一致：先移除再插入，[toIndex] 按移除后的列表计算。
     */
    fun move(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        val cell = cells.removeAt(fromIndex)
        cells.add(toIndex, cell)
    }

    /** 保留 [index] 处条目的 key，把值写入该条目自己的状态。 */
    fun setValue(index: Int, value: T) {
        cells[index].valueState.value = value
    }

    /** 清空全部条目，计数器不回退。 */
    fun clear() {
        cells.clear()
    }
}

/**
 * 由 [list] 构建条目容器，初始 key 依次为 `0 until list.size`。
 *
 * [key] 为 remember 键，默认取 [list] 实例。remember 按键的 `equals` 比较，传入内容会变化的
 * 新列表会导致容器整体重建（key 全部重排），此时应显式传入稳定的键（如配置对象）。
 *
 * [policy] 判定写入的值是否算变化，默认 [structuralEqualityPolicy]（按 `equals` 判定）；
 * 需要"交回新实例即算变化"时传 `referentialEqualityPolicy()`，传 `neverEqualPolicy()` 则每次写入都算变化。
 * 该实例参与 remember 键，应传稳定实例。
 */
@Composable
fun <T> rememberKeyedList(
    list: List<T>,
    key: Any? = list,
    policy: SnapshotMutationPolicy<T> = structuralEqualityPolicy(),
): KeyedListState<T> = remember(key, policy) { KeyedListState(list, policy) }
