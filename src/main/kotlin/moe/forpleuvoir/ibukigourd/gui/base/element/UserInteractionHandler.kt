package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.api.Tickable
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.util.NextAction

interface UserInteractionHandler : Tickable {
    /**
     * 当鼠标移动到元素内时
     * @param mouseX Float
     * @param mouseY Float
     */
    fun onMouseMoveIn(mouseX: Float, mouseY: Float)

    /**
     * 当鼠标移动到元素外时
     * @param mouseX Float
     * @param mouseY Float
     */
    fun onMouseMoveOut(mouseX: Float, mouseY: Float)

    /**
     * 鼠标移动
     * @param mouseX Float
     * @param mouseY Float
     */
    fun onMouseMove(mouseX: Float, mouseY: Float): NextAction

    /**
     * 鼠标点击
     * @param button Mouse
     * @param mouseX Float
     * @param mouseY Float
     * @return 是否处理之后的同类操作
     */
    fun onMouseClick(mouseX: Float, mouseY: Float, button: Mouse): NextAction

    /**
     * 鼠标释放
     * @param button Mouse
     * @param mouseX Float
     * @param mouseY Float
     * @return 是否处理之后的同类操作
     */
    fun onMouseRelease(mouseX: Float, mouseY: Float, button: Mouse): NextAction

    /**
     * 鼠标拖动
     * @param mouseX Float
     * @param mouseY Float
     * @param button Mouse
     * @param deltaX Float
     * @param deltaY Float
     * @return 是否处理之后的同类操作
     */
    fun onMouseDragging(mouseX: Float, mouseY: Float, button: Mouse, deltaX: Float, deltaY: Float): NextAction

    /**
     * 鼠标滚动
     * @param mouseX Float
     * @param mouseY Float
     * @param amount Float
     * @return 是否处理之后的同类操作
     */
    fun onMouseScrolling(mouseX: Float, mouseY: Float, amount: Float): NextAction

    /**
     * 按键按下
     * @param keyCode KeyCode
     * @return 是否处理之后的同类操作
     */
    fun onKeyPress(keyCode: KeyCode): NextAction

    /**
     * 按键释放
     * @param keyCode KeyCode
     * @return 是否处理之后的同类操作
     */
    fun onKeyRelease(keyCode: KeyCode): NextAction

    /**
     * 字符输入
     * @param chr Char
     * @return 是否处理之后的同类操作
     */
    fun onCharTyped(chr: Char): NextAction
}