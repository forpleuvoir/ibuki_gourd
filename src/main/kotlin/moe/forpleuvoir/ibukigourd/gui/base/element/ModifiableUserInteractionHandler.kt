package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.api.Tickable
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.util.NextAction

/**
 *
 * 以下方法和对应作为对象的高阶函数
 *
 * 应该是高阶函数作为被系统内部调用的方法
 *
 * 普通方法由子类实现
 *
 * 如果在DSL场景需要给对应方法添加代码，只需要给对应的高阶函数重新赋值
 *
 * 子类重写方法,调用者调用高阶函数
 *
 * 例:
 *
 * render={matrixStack,delta ->
 *
 *     onRender(matrixStack,delta)
 *     code
 * }
 *
 */
interface ModifiableUserInteractionHandler :UserInteractionHandler {

    var tick: () -> Unit

    /**
     * 当鼠标移动到元素内时
     */
    var mouseMoveIn: (mouseX: Float, mouseY: Float) -> Unit

    /**
     * 当鼠标移动到元素外时
     */
    var mouseMoveOut: (mouseX: Float, mouseY: Float) -> Unit

    /**
     * 鼠标移动
     * @param mouseX Float
     * @param mouseY Float
     */
    var mouseMove: (mouseX: Float, mouseY: Float) -> NextAction

    /**
     * 鼠标点击
     * @param button Mouse
     * @param mouseX Float
     * @param mouseY Float
     * @return 是否处理之后的同类操作
     */
    var mouseClick: (mouseX: Float, mouseY: Float, button: Mouse) -> NextAction

    /**
     * 鼠标释放
     * @param button Mouse
     * @param mouseX Float
     * @param mouseY Float
     * @return 是否处理之后的同类操作
     */
    var mouseRelease: (mouseX: Float, mouseY: Float, button: Mouse) -> NextAction

    /**
     * 鼠标拖动
     * @param mouseX Float
     * @param mouseY Float
     * @param button Mouse
     * @param deltaX Float
     * @param deltaY Float
     * @return 是否处理之后的同类操作
     */
    var mouseDragging: (mouseX: Float, mouseY: Float, button: Mouse, deltaX: Float, deltaY: Float) -> NextAction

    /**
     * 鼠标滚动
     * @param mouseX Float
     * @param mouseY Float
     * @param amount Float
     * @return 是否处理之后的同类操作
     */
    var mouseScrolling: (mouseX: Float, mouseY: Float, amount: Float) -> NextAction

    /**
     * 按键按下
     * @param keyCode KeyCode
     * @return 是否处理之后的同类操作
     */
    var keyPress: (keyCode: KeyCode) -> NextAction

    /**
     * 按键释放
     * @param keyCode KeyCode
     * @return 是否处理之后的同类操作
     */
    var keyRelease: (keyCode: KeyCode) -> NextAction

    /**
     * 字符输入
     * @param chr Char
     * @return 是否处理之后的同类操作
     */
    var charTyped: (chr: Char) -> NextAction

}