package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.event.*

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

    /**
     * 当鼠标移动到元素内时
     */
    var mouseEnter: (event: MouseEnterEvent) -> Unit

    /**
     * 当鼠标移动到元素外时
     */
    var mouseLeave: (event: MouseLeaveEvent) -> Unit

    /**
     * 鼠标移动
     */
    var mouseMove: (event: MouseMoveEvent) -> Unit

    /**
     * 鼠标点击
     */
    var mouseClick: (event: MousePressEvent) -> Unit

    /**
     * 鼠标释放
     */
    var mouseRelease: (event: MouseReleaseEvent) -> Unit

    /**
     * 鼠标拖动
     */
    var mouseDragging: (event: MouseDragEvent) -> Unit

    /**
     * 鼠标滚动
     */
    var mouseScrolling: (event: MouseScrollEvent) -> Unit

    /**
     * 按键按下
     */
    var keyPress: (event: KeyPressEvent) -> Unit

    /**
     * 按键释放
     */
    var keyRelease: (event: KeyReleaseEvent) -> Unit

    /**
     * 字符输入
     */
    var charTyped: (event: CharTypedEvent) -> Unit

}