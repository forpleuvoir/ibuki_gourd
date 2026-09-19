package moe.forpleuvoir.ibukigourd.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList

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
 * 条目容器：持有 [Keyed] 列表与 key 自增计数器。
 *
 * 增删一律经本类方法，key 唯一性由内部计数器保证；[entries] 为只读视图，与内部条目共用
 * 同一实例，元素读操作参与快照观察。计数器不回退（删除条目不回收 key），Long 不会溢出。
 *
 * 实例由 [rememberKeyedList] 创建，不可自行构造。
 */
@Stable
class KeyedListState<T> internal constructor(
    private val mutableEntries: SnapshotStateList<Keyed<T>>,
    private val nextKey: MutableLongState,
) {
    /** 只读条目视图，可直接传给 `items(entries, key = { it.key })` 一类列表 API。 */
    val entries: List<Keyed<T>> get() = mutableEntries

    /** 条目数。 */
    val size: Int get() = mutableEntries.size

    /** 末尾追加条目，key 取计数器当前值并自增。 */
    fun add(value: T): Keyed<T> = add(mutableEntries.size, value)

    /** 在 [index] 处插入条目，key 取计数器当前值并自增。 */
    fun add(index: Int, value: T): Keyed<T> {
        val entry = Keyed(nextKey.longValue++, value)
        mutableEntries.add(index, entry)
        return entry
    }

    /** 移除 [index] 处条目并返回，其 key 不回收。 */
    fun removeAt(index: Int): Keyed<T> = mutableEntries.removeAt(index)

    /**
     * 把 [fromIndex] 处条目移动到 [toIndex]。
     *
     * 语义与 `MutableList.moveElement` 一致：先移除再插入，[toIndex] 按移除后的列表计算。
     */
    fun move(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        val entry = mutableEntries.removeAt(fromIndex)
        mutableEntries.add(toIndex, entry)
    }

    /** 保留 [index] 处条目的 key，替换其值。 */
    fun setValue(index: Int, value: T) {
        mutableEntries[index] = mutableEntries[index].copyValue(value)
    }

    /** 清空全部条目，计数器不回退。 */
    fun clear() {
        mutableEntries.clear()
    }
}

/**
 * 由 [list] 构建带 key 的条目容器，初始 key 依次为 `0 until list.size`。
 *
 * [key] 为 remember 键，默认取 [list] 实例。remember 按键的 `equals` 比较，传入内容会变化的
 * 新列表会导致容器整体重建（key 全部重排），此时应显式传入稳定的键（如配置对象）。
 */
@Composable
fun <T> rememberKeyedList(
    list: List<T>,
    key: Any? = list,
): KeyedListState<T> = remember(key) {
    val entries = mutableStateListOf<Keyed<T>>()
    var nextKey = 0L
    list.forEach { value ->
        entries.add(Keyed(nextKey++, value))
    }
    KeyedListState(entries, mutableLongStateOf(nextKey))
}
