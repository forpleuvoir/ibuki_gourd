package test

import androidx.compose.runtime.AbstractApplier

class TestApplier(root: TestNode) : AbstractApplier<TestNode>(root) {

    override fun insertTopDown(index: Int, instance: TestNode) {
        current.children.add(index, instance)
    }

    override fun insertBottomUp(index: Int, instance: TestNode) {
    }

    override fun remove(index: Int, count: Int) {
        repeat(count) { current.children.removeAt(index) }
    }

    override fun move(from: Int, to: Int, count: Int) {
        val items = current.children.subList(from, from + count).toList()
        repeat(count) { current.children.removeAt(from) }
        current.children.addAll(to, items)
    }

    override fun onClear() {
        current.children.clear()
    }

}
