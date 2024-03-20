package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.api.Tickable
import moe.forpleuvoir.ibukigourd.gui.base.event.*

interface UserInteractionHandler : Tickable {

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
    fun onMouseClick(event: MousePressEvent)

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