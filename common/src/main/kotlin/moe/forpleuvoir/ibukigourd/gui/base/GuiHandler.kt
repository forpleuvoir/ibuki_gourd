package moe.forpleuvoir.ibukigourd.gui.base

import moe.forpleuvoir.ibukigourd.api.Tickable
import moe.forpleuvoir.ibukigourd.gui.base.event.*

interface GuiHandler : Tickable {

    /**
     * 仅内部实现使用
     *
     * 如果想在外部覆写请使用 [ModifiableGuiHandler.process]高阶函数
     * ```kotlin
     * //widget is [ModifiableGuiHandler]
     * widget.process = { delta->
     *    //do something
     * }
     * ```
     */
    @Deprecated("如果要覆写,请使用onProcess方法", replaceWith = ReplaceWith("onProcess(delta)"))
    fun process(delta: Float)

    /**
     * 处理逻辑
     *
     * 此方法会在每一帧渲染之前调用
     *
     * @param delta 距离上一次调用此函数经过的时间,单位秒
     */
    fun onProcess(delta: Float)

    /**
     * 当鼠标移动到元素内时
     */
    fun onMouseEnter(event: MouseEnterEvent)

    /**
     * 当鼠标移动到元素外时
     */
    fun onMouseLeave(event: MouseLeaveEvent)

    /**
     * 鼠标移动
     */
    fun onMouseMove(event: MouseMoveEvent)

    /**
     * 鼠标点击
     */
    fun onMousePress(event: MousePressEvent)

    /**
     * 获得焦点
     */
    fun onFocused(event: FocusedEvent)

    /**
     * 鼠标释放
     */
    fun onMouseRelease(event: MouseReleaseEvent)

    /**
     * 鼠标拖动
     */
    fun onMouseDragging(event: MouseDragEvent)

    /**
     * 鼠标滚动
     */
    fun onMouseScrolling(event: MouseScrollEvent)

    /**
     * 按键按下
     */
    fun onKeyPress(event: KeyPressEvent)

    /**
     * 按键释放
     */
    fun onKeyRelease(event: KeyReleaseEvent)

    /**
     * 字符输入
     */
    fun onCharTyped(event: CharTypedEvent)
}