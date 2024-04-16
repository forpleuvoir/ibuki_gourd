package moe.forpleuvoir.ibukigourd.gui.screen

import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.base.element.Layer

interface Screen : Element {
    companion object {

        val EMPTY: Screen = object : Screen, Element by Element {
            override var parentScreen: Screen? = null
            override val layers: List<Layer> = emptyList()
            override var focusedElement: Element? = null
            override val pauseGame: Boolean = false
            override val shouldCloseOnEsc: Boolean = false
            override var resize: (width: Int, height: Int) -> Unit = { _: Int, _: Int -> }
            override fun onResize(width: Int, height: Int) = Unit
            override var close: () -> Unit = {}
            override fun onClose() = Unit
        }
    }

    /**
     * 上一级屏幕
     */
    var parentScreen: Screen?

    /**
     * GUI层
     */
    val layers: List<Layer>

    /**
     * 当前选中的元素
     */
    var focusedElement: Element?

    /**
     * 打开时是否需要暂停游戏，在多人游戏中无效
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