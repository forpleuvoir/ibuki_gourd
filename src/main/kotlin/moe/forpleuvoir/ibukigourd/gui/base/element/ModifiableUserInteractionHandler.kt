package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.Mouse

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
    var mouseMove: (mouseX: Float, mouseY: Float) -> Unit

    /**
     * 鼠标点击
     * @param button Mouse
     * @param mouseX Float
     * @param mouseY Float
     */
    var mouseClick: (mouseX: Float, mouseY: Float, button: Mouse) -> Unit

    /**
     * 鼠标释放
     * @param button Mouse
     * @param mouseX Float
     * @param mouseY Float
     */
    var mouseRelease: (mouseX: Float, mouseY: Float, button: Mouse) -> Unit

    /**
     * 鼠标拖动
     * @param mouseX Float
     * @param mouseY Float
     * @param button Mouse
     * @param deltaX Float
     * @param deltaY Float
     */
    var mouseDragging: (mouseX: Float, mouseY: Float, button: Mouse, deltaX: Float, deltaY: Float) -> Unit

    /**
     * 鼠标滚动
     * @param mouseX Float
     * @param mouseY Float
     * @param amount Float
     */
    var mouseScrolling: (mouseX: Float, mouseY: Float, amount: Float) -> Unit

    /**
     * 按键按下
     * @param keyCode KeyCode
     */
    var keyPress: (keyCode: KeyCode) -> Unit

    /**
     * 按键释放
     * @param keyCode KeyCode
     */
    var keyRelease: (keyCode: KeyCode) -> Unit

    /**
     * 字符输入
     * @param chr Char
     */
    var charTyped: (chr: Char) -> Unit

}