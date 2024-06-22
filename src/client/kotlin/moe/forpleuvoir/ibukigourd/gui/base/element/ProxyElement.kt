package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * 只有一个可切换的子元素,一般用于实现Tab选项卡的内容元素
 */
class ProxyElement(
    modifier: Modifier = Modifier
) : AbstractElement(modifier) {

    private var content: Element? = null
        set(value) {
            if (value == field) return
            if (value != null) {
                if (field != null) super.removeElement(field!!)
                super.addElement(value)
            } else if (field != null) {
                super.removeElement(field!!)
            }
            field = value
        }

    @Deprecated("use initContent() or switchContent() instead")
    override fun <T : Element> addElement(element: T): T {
        throw UnsupportedOperationException("ProxyElement can't add element, use initContent() or switchContent() instead")
    }

    @Deprecated("use initContent() or switchContent() instead")
    override fun removeElement(element: Element): Boolean {
        throw UnsupportedOperationException("ProxyElement can't remove element,use initContent() or switchContent() instead")
    }

    fun initContent(element: Element?) {
        content = element
    }

    fun switchContent(element: Element?) {
        content = element
        init.invoke()
    }
}

@OptIn(ExperimentalContracts::class)
inline fun ElementContainer.proxy(
    modifier: Modifier = Modifier,
    scope: ProxyElement.() -> Element
): ProxyElement {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return addElement(Proxy(modifier, scope))
}

@Suppress("FunctionName")
@OptIn(ExperimentalContracts::class)
inline fun Proxy(
    modifier: Modifier = Modifier,
    scope: ProxyElement.() -> Element
): ProxyElement {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return ProxyElement(modifier).apply {
        initContent(scope())
    }
}