package moe.forpleuvoir.ibukigourd.ui.selector

/**
 * 选择器展开体的呈现载体。
 *
 * 分发规则（在 [Selector] 展开时求值，按下列顺序取首个命中项）：
 * 1. 定义了搜索过滤器（`searchFilter != null`）→ [Dialog]（搜索栏只在弹窗里提供）；
 * 2. [style] 为 [Dialog] → [Dialog]；
 * 3. [style] 为 [Dropdown] → 下拉菜单；
 * 4. [style] 为 [Auto] → 选项总数 `> maxItems` 取 [Dialog]，否则下拉菜单。
 *
 * 判定只用**选项总数**（不随搜索词变化），因此展开期间载体不会中途切换。
 */
sealed class SelectorExpandStyle {

    /**
     * 自动：按选项总数判定载体。
     *
     * @param maxItems 选项总数上限；超过则用 [Dialog]，否则用下拉菜单
     */
    data class Auto(val maxItems: Int = 10) : SelectorExpandStyle()

    /** 强制下拉菜单。 */
    data object Dropdown : SelectorExpandStyle()

    /** 强制弹窗。 */
    data object Dialog : SelectorExpandStyle()
}
