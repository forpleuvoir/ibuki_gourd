package test

open class TestNode(val name: String) {
    val children = mutableListOf<TestNode>()
}

inline fun <reified T : TestNode> TestNode.find(index: Int): T? =
    children.getOrNull(index) as? T
