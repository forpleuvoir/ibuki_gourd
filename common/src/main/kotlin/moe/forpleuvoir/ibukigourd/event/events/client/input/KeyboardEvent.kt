package moe.forpleuvoir.ibukigourd.event.events.client.input

import moe.forpleuvoir.ibukigourd.event.CancellableContext
import moe.forpleuvoir.ibukigourd.event.CancellableContextImpl
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.KeyEnvironment
import moe.forpleuvoir.ibukigourd.input.currentEnv

object KeyboardEvent {

    @JvmField
    val Pressed = CancellableContext.createEvent<PressedContext>()

    /**
     * 表示键盘按下事件的类。
     *
     * 当一个键被按下或重复触发时，该事件将被广播。通过监听此事件，可以捕获并处理键盘输入。
     * 该事件可被取消，取消后键盘输入将不会被进一步处理。
     *
     * @property keyCode 表示被按下键的键码。
     * @property name 表示被按下键的本地化名称，默认为键码对应的本地化字符串。
     * @property env 表示触发事件时的键盘环境，用于区分事件发生在游戏内还是屏幕上的情况。
     */
    class PressedContext(
        @JvmField
        val keyCode: KeyCode,
        @JvmField
        val name: String = keyCode.keyName,
        @JvmField
        val env: KeyEnvironment = currentEnv()
    ) : CancellableContextImpl()

    @JvmField
    val Released = CancellableContext.createEvent<ReleasedContext>()

    /**
     * 表示键盘按键释放事件的类。
     *
     * 此事件会在键盘按键被释放时触发，包含有关键码、按键名称和当前按键环境的信息。
     *
     * @property keyCode 按键对应的键码对象。
     * @property name 按键的本地化名称。
     * @property env 当前按键的运行环境。
     */
    class ReleasedContext(
        @JvmField
        val keyCode: KeyCode,
        @JvmField
        val name: String = keyCode.keyName,
        @JvmField
        val env: KeyEnvironment = currentEnv()
    ) : CancellableContextImpl()
}