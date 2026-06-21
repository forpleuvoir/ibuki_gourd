package moe.forpleuvoir.ibukigourd.mod.waht.ecs

import kotlin.reflect.KClass

/**
 * 原型(Archetype):拥有相同组件类型集合的一组实体的列存储容器。
 *
 * 采用 **SoA(Structure of Arrays)** 布局:每种组件类型对应一个连续列([columns]),
 * 同 archetype 内第 `i` 个实体的各组件分别存于各列的第 `i` 个槽位,行对齐。
 *
 * **缓存友好性**:同类型组件在内存中聚集,System 在列上线性扫描时 L1 缓存命中率高。
 * 对组件组合固定的场景(如生命游戏的 cell),所有实体落入单一 archetype,
 * 遍历即对稠密列数组顺序读取。
 *
 * 不直接对外暴露,所有变更经由 [World] 调度,以保证 [Location] 一致性。
 */
class Archetype internal constructor(
    /** 该 archetype 的不可变组件类型签名。 */
    val typeSet: Set<KClass<out Component>>
) {

    /**
     * 列存储:组件类型 → 该列的稠密数组(按行对齐)。
     * 实体 `row` 的某类型组件为 `columns[type]!![row]`。
     */
    internal val columns: Map<KClass<*>, MutableList<Component>> =
        typeSet.associateWithTo(HashMap()) { ArrayList() }

    /** 该 archetype 内的实体数(行数)。 */
    var size: Int = 0
        private set

    /** 行对齐的全局实体 ID,用于回指实体句柄。 */
    internal val rowToId = ArrayList<EntityId>()

    /**
     * 在末尾追加一行。
     *
     * @param id 该行实体的全局 ID
     * @param components 初始组件映射(键必须 ⊆ [typeSet]);未提供的类型暂置占位 `null`,
     *                   由调用方在迁移完成后补齐——当前实现要求完整提供,缺失将抛异常
     * @return 新行号
     */
    internal fun appendRow(id: EntityId, components: Map<KClass<*>, Component>): Int {
        val row = size
        for (type in typeSet) {
            val col = columns[type]!!
            val comp = components[type]
            // 缺失组件用占位:调用方保证完整,这里防御性地写入一个标记
            check(comp != null) { "Archetype 追加行缺少类型 $type 的组件" }
            col.add(comp)
        }
        rowToId.add(id)
        size++
        return row
    }

    /**
     * swap-remove 第 [row] 行:把末行数据搬到 [row],缩短一行。
     *
     * @return 被搬移实体信息(若 [row] 本身就是末行则为 null)。
     *         调用方需据此更新被搬移实体的 [Location.row]。
     */
    internal fun removeRow(row: Int): RemovedRow {
        val lastRow = size - 1
        for (col in columns.values) {
            if (row != lastRow) {
                col[row] = col[lastRow]
            }
            col.removeAt(lastRow)
        }
        rowToId[row] = rowToId[lastRow]
        rowToId.removeAt(lastRow)
        size--
        return if (row == lastRow) {
            RemovedRow(movedId = null, newRow = -1)
        } else {
            RemovedRow(movedId = rowToId[row], newRow = row)
        }
    }

    /** 取第 [row] 行的指定类型组件;类型不在签名内返回 null。 */
    @Suppress("UNCHECKED_CAST")
    internal operator fun <T : Component> get(row: Int, type: KClass<out T>): T? {
        val col = columns[type] ?: return null
        if (row >= col.size) return null
        return col[row] as? T
    }

    /**
     * 位置句柄:实体当前所在 archetype + 行号。
     *
     * 增删组件触发迁移时,[row] 会被更新;若另一实体被 swap-remove 搬移,
     * 其 [row] 也需同步修正。
     */
    internal data class Location(val archetype: Archetype, var row: Int)

    /** swap-remove 的搬移结果。 */
    internal data class RemovedRow(val movedId: EntityId?, val newRow: Int)
}
