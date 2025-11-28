package moe.forpleuvoir.ibukigourd.event.events.client.input

import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.KeyEnvironment
import moe.forpleuvoir.ibukigourd.input.currentEnv
import moe.forpleuvoir.nebula.event.CancellableEvent

class MouseEvent {

    /**
     * 表示鼠标按下事件的类。
     *
     * 此事件会在鼠标按键被按下时触发，通过广播事件，可以捕获并处理鼠标输入。
     * 该事件可被取消，取消后鼠标输入将不会被进一步处理。
     *
     * @property keyCode 表示被按下鼠标按键的键码。
     * @property name 表示被按下鼠标按键的本地化名称，默认为键码对应的本地化字符串。
     * @property env 表示触发事件时的鼠标运行环境，用于区分事件发生在游戏内还是屏幕上的情况。
     */
    class MousePressEvent(
        @JvmField
        val keyCode: KeyCode,
        @JvmField
        val name: String = keyCode.keyName,
        @JvmField
        val env: KeyEnvironment = currentEnv(),
    ) : CancellableEvent {
        override var canceled: Boolean = false
    }

    /**
     * 表示鼠标按钮释放事件的类。
     *
     * 此事件会在鼠标按键被释放时触发，通过广播事件，可以捕获并处理鼠标释放操作。
     * 该事件可被取消，取消后鼠标释放操作将不会被进一步处理。
     *
     * @property keyCode 表示被释放鼠标按键的键码。
     * @property name 表示被释放鼠标按键的本地化名称，默认为键码对应的本地化字符串。
     * @property env 表示触发事件时的鼠标运行环境，用于区分事件发生在游戏内还是屏幕上的情况。
     */
    class MouseReleaseEvent(
        @JvmField
        val keyCode: KeyCode,
        @JvmField
        val name: String = keyCode.keyName,
        @JvmField
        val env: KeyEnvironment = currentEnv(),
    ) : CancellableEvent {
        override var canceled: Boolean = false
    }

    /**
     * 表示鼠标滚轮事件的类。
     *
     * 当用户滚动鼠标滚轮时触发，用于捕获滚动输入并广播事件以供监听和处理。
     * 此事件包含滚动量与当前触发环境的信息。
     * 该事件可被取消，取消后滚动行为将不会被进一步处理。
     *
     * @property amount 表示鼠标滚动的量，正值向上滚动，负值向下滚动。
     * @property env 表示触发事件时的键盘环境，用于标识滚动操作发生在游戏内还是屏幕上的环境。
     */
    class MouseScrollEvent(
        @JvmField
        val amount: Double,
        @JvmField
        val env: KeyEnvironment = currentEnv(),
    ) : CancellableEvent {
        override var canceled: Boolean = false
    }

    /**
     * 表示鼠标移动事件的类。
     *
     * 当鼠标的光标位置发生变化时，会触发此事件。该事件提供了光标当前的屏幕位置和触发事件时的运行环境。
     * 通过监听此事件，可以捕获并处理鼠标移动行为。
     * 此事件可被取消，若取消则会抑制后续默认的鼠标移动处理。
     *
     * @property x 当前鼠标光标的X轴位置。
     * @property y 当前鼠标光标的Y轴位置。
     * @property env 事件触发时的键盘环境，用于区分鼠标移动发生时的上下文，例如游戏内部还是屏幕交互。
     */
    class MouseMoveEvent(
        @JvmField
        val x: Double,
        @JvmField
        val y: Double,
        @JvmField
        val env: KeyEnvironment = currentEnv(),
    ) : CancellableEvent {
        override var canceled: Boolean = false
    }

    /**
     * 表示鼠标拖拽事件的类。
     *
     * 当鼠标按下某个键后进行拖拽时会触发该事件。该事件包含了鼠标拖拽时的相关信息，例如拖拽的键、鼠标当前位置及触发拖拽时的环境。
     * 该事件是可取消的，如果取消该事件，则此次鼠标拖拽操作将不会被进一步处理。
     *
     * @property keyCode 表示触发拖拽时按下的鼠标按键的键码。
     * @property name 表示按键的本地化名称，默认为键码对应的本地化字符串。
     * @property x 表示拖拽时鼠标的 X 坐标位置。
     * @property y 表示拖拽时鼠标的 Y 坐标位置。
     * @property env 表示触发事件时的环境，包含游戏内和屏幕上的情况。
     */
    class MouseDraggingEvent(
        @JvmField
        val keyCode: KeyCode,
        @JvmField
        val name: String = keyCode.keyName,
        @JvmField
        val x: Double,
        @JvmField
        val y: Double,
        @JvmField
        val env: KeyEnvironment = currentEnv(),
    ) : CancellableEvent {
        override var canceled: Boolean = false
    }

}