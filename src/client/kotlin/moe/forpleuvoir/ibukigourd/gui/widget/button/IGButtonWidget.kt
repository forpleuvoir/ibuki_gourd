package moe.forpleuvoir.ibukigourd.gui.widget.button

import moe.forpleuvoir.ibukigourd.gui.base.layout.ColumnLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.LinearLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGPressableWidgetContainer
import moe.forpleuvoir.ibukigourd.input.MouseCursor
import moe.forpleuvoir.ibukigourd.util.Tick

open class IGButtonWidget : IGPressableWidgetContainer(), ColumnLayout {

    override val mouseOverCursor: MouseCursor.Cursor
        get() = MouseCursor.Cursor.POINTING_HAND_CURSOR

    //------------ IGButton ------------\\

    open var longPressTime: Tick = 20

    open var pressTickCounter: Tick = 0
        protected set

    override fun onTick() {
        if (pressed) {
            pressTickCounter++
            if (longPressTime == pressTickCounter) {
                longPress(this)
            }
        } else if (pressTickCounter != 0L) {
            pressTickCounter = 0
        }
    }

    protected var onPress: (IGButtonWidget) -> Unit = {}
        private set

    protected var longPress: (IGButtonWidget) -> Unit = {}
        private set

    protected var onRelease: (IGButtonWidget) -> Unit = {}
        private set

    override fun onPress() {
        onPress(this)
    }

    override fun onRelease() {
        onRelease(this)
    }

    fun longPress(time: Tick, action: (IGButtonWidget) -> Unit): IGButtonWidget {
        longPressTime = time
        longPress = action
        return this
    }

    fun press(action: (IGButtonWidget) -> Unit): IGButtonWidget {
        onPress = action
        return this
    }

    fun release(action: (IGButtonWidget) -> Unit): IGButtonWidget {
        onRelease = action
        return this
    }

    final override var arrangement: Arrangement = Arrangement.Center
        private set

    companion object {}

    data class ButtonScope(private val button: IGButtonWidget) : GuiScope<IGButtonWidget>, LinearLayoutScope {

        override fun owner(): IGButtonWidget = button

        fun arrangement(arrangement: Arrangement) {
            owner().arrangement = arrangement
        }

        fun press(action: (IGButtonWidget) -> Unit) = owner().press(action)

        fun longPress(time: Tick, action: (IGButtonWidget) -> Unit) = owner().longPress(time, action)

        fun release(action: (IGButtonWidget) -> Unit) = owner().release(action)

    }


}

typealias ButtonScope = IGButtonWidget.ButtonScope
