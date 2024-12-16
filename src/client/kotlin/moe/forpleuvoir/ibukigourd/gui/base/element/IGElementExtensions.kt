package moe.forpleuvoir.ibukigourd.gui.base.element

/**
 * 检查目标元素是否在当前元素的父级链中。
 *
 * @param target 目标元素，通过其与当前元素的父级链比较。
 * @return 如果目标元素在父级链中，返回 true；否则返回 false。
 */
fun IGElement.isInParentChain(target: IGElement): Boolean {
    var current: IGElement? = this
    while (current != null && current.parent() != current) {
        if (current == target) {
            return true
        }
        current = current.parent()
    }
    return current == target
}

/**
 * 在父链中查找第一个满足给定条件的元素。
 *
 * 该函数从当前元素开始向上遍历父链，直到找到第一个满足条件的元素或到达链的末尾。
 *
 * @param predicate 判断当前元素是否满足条件的函数。
 * @return 第一个满足条件的元素；如果未找到，则返回 null。
 */
fun IGElement.findFirsInParentChain(predicate: (IGElement) -> Boolean): IGElement? {
    var current: IGElement? = this
    while (current != null && current.parent() != current) {
        if (predicate(current)) {
            return current
        }
        current = current.parent()
    }
    return current
}