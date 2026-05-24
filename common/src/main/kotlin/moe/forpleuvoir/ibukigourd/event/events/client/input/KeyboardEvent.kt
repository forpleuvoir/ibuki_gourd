package moe.forpleuvoir.ibukigourd.event.events.client.input

import moe.forpleuvoir.ibukigourd.event.CancellableContext
import moe.forpleuvoir.ibukigourd.event.CancellableContextImpl
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.KeyEnvironment
import moe.forpleuvoir.ibukigourd.input.currentEnv

object KeyboardEvent {

    @JvmField
    val Pressed = CancellableContext.createEvent<KeyboardContext>()

    @JvmField
    val Released = CancellableContext.createEvent<KeyboardContext>()

    /**
     *
     * 当一个键被按下或重复触发以及释放时，该事件将被广播。通过监听此事件，可以捕获并处理键盘输入。
     * 该事件可被取消，取消后键盘输入将不会被进一步处理。
     *
     * @property keyCode 键码。
     * @property modifiers 按键修饰符
     * @property isPressed 是否为按下状态
     * @property isRepeat 是否为按下重复触发状态
     * @property name 表本地化名称，默认为键码对应的本地化字符串。
     * @property env 表示触发事件时的键盘环境，用于区分事件发生在游戏内还是屏幕上的情况。
     */
    class KeyboardContext(
        @JvmField
        val keyCode: KeyCode,
        @JvmField
        val modifiers: Int,
        @JvmField
        val isPressed: Boolean,
        @JvmField
        val isRepeat: Boolean,
        @JvmField
        val name: String = keyCode.keyName,
        @JvmField
        val env: KeyEnvironment = currentEnv()
    ) : CancellableContextImpl() {

        @JvmField
        val isReleased = !isPressed

        fun isShiftPressed() = (modifiers and 1) != 0

        fun isCtrlPressed() = (modifiers and 2) != 0

        fun isAltPressed() = (modifiers and 4) != 0

    }

}