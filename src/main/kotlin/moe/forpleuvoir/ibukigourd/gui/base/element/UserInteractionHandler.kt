package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.api.Tickable
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.Mouse

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
    fun onMouseMove(mouseX: Float, mouseY: Float)

    /**
     * 鼠标点击
     * @param button Mouse
     * @param mouseX Float
     * @param mouseY Float
     */
    fun onMouseClick(mouseX: Float, mouseY: Float, button: Mouse)

    /**
     * 鼠标释放
     * @param button Mouse
     * @param mouseX Float
     * @param mouseY Float
     */
    fun onMouseRelease(mouseX: Float, mouseY: Float, button: Mouse)

    /**
     * 鼠标拖动
     * @param mouseX Float
     * @param mouseY Float
     * @param button Mouse
     * @param deltaX Float
     * @param deltaY Float
     */
    fun onMouseDragging(mouseX: Float, mouseY: Float, button: Mouse, deltaX: Float, deltaY: Float)

    /**
     * 鼠标滚动
     * @param mouseX Float
     * @param mouseY Float
     * @param amount Float
     */
    fun onMouseScrolling(mouseX: Float, mouseY: Float, amount: Float)

    /**
     * 按键按下
     * @param keyCode KeyCode
     */
    fun onKeyPress(keyCode: KeyCode)

    /**
     * 按键释放
     * @param keyCode KeyCode
     */
    fun onKeyRelease(keyCode: KeyCode)

    /**
     * 字符输入
     * @param chr Char
     */
    fun onCharTyped(chr: Char)
}