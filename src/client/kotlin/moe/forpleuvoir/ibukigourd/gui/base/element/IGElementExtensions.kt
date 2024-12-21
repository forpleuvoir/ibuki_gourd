package moe.forpleuvoir.ibukigourd.gui.base.element

val IGElement.parentChain: List<IGElement>
    get() = generateSequence(this) { if (it.parent() != it) it.parent() else null }

        .toList()

/**
 * 检查目标元素是否在当前元素的父级链中。
 *
 * @param target 目标元素，通过其与当前元素的父级链比较。
 * @return 如果目标元素在父级链中，返回 true；否则返回 false。
 */
fun IGElement.isInParentChain(target: IGElement, self: Boolean = true): Boolean {
    return target in parentChain.filter { self || it != this }
}

/**
 * 在父链中查找第一个满足给定条件的元素。
 *
 * 该函数从当前元素开始向上遍历父链，直到找到第一个满足条件的元素或到达链的末尾。
 *
 * @param predicate 判断当前元素是否满足条件的函数。
 * @return 第一个满足条件的元素；如果未找到，则返回 null。
 */
fun IGElement.findFirsInParentChain(self: Boolean = true, predicate: (IGElement) -> Boolean): IGElement? {
    return parentChain.filter { self || it != this }.find(predicate)
}

/**
 * 在当前元素的父链中搜索满足指定条件的最后一个元素。
 *
 * 此方法通过依次遍历当前元素及其父元素，查找符合给定条件的最后一个元素并返回。
 * 如果没有找到符合条件的元素，则返回 `null`。
 *
 * @param predicate 用于判断元素是否符合条件的函数。
 *                  接受一个 `IGElement` 作为参数并返回一个布尔值。
 * @return 符合条件的最后一个 `IGElement`，如果未找到符合条件的元素，则返回 `null`。
 */
fun IGElement.findLastInParentChain(self: Boolean = true, predicate: (IGElement) -> Boolean): IGElement? {
    return parentChain.filter { self || it != this }.findLast(predicate)
}