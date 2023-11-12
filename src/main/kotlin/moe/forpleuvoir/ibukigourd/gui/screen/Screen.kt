package moe.forpleuvoir.ibukigourd.gui.screen

import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.tip.Tip
import moe.forpleuvoir.ibukigourd.gui.tip.TipHandler
import moe.forpleuvoir.ibukigourd.input.MousePosition

interface Screen : Element, TipHandler {
    companion object {

        val EMPTY: Screen = object : Screen, Element by Element.EMPTY {
            override val mousePosition: MousePosition = throw NotImplementedError()
            override val preMousePosition: MousePosition = throw NotImplementedError()
            override var parentScreen: Screen? = null
            override var focusedElement: Element? = null
            override val pauseGame: Boolean = false
            override val shouldCloseOnEsc: Boolean = false
            override var resize: (width: Int, height: Int) -> Unit = { _: Int, _: Int -> }
            override fun onResize(width: Int, height: Int) = Unit
            override var close: () -> Unit = {}
            override fun onClose() = Unit
            override val tipList: Iterable<Tip> = emptyList()
            override var maxTip: Int = -1
            override fun pushTip(tip: Tip): Boolean = false
            override fun popTip(tip: Tip): Boolean = false
        }
    }

    val mousePosition: MousePosition

    val preMousePosition: MousePosition

    /**
     * 上一级屏幕
     */
    var parentScreen: Screen?

    /**
     * 当前选中的元素
     */
    var focusedElement: Element?

    /**
     * 打开时是否需要暂停游戏，在服务器中无效
     */
    val pauseGame: Boolean

    /**
     * 是否需要在按下ESC之后关闭当前屏幕
     */
    val shouldCloseOnEsc: Boolean

    /**
     * 重新调整屏幕大小
     */
    var resize: (width: Int, height: Int) -> Unit

    /**
     * 重新调整屏幕大小
     * @param width Int
     * @param height Int
     */
    fun onResize(width: Int, height: Int)

    var close: () -> Unit

    fun onClose()

}