package moe.forpleuvoir.ibukigourd.gui.widget.button

import moe.forpleuvoir.ibukigourd.gui.base.layout.RowLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.RowLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.PressableWidgetContainer

open class IGButtonWidget(
    override val arrangement: Arrangement.Horizontal,
    override val alignment: Alignment.Vertical
) : PressableWidgetContainer(), RowLayout {

    //------------ IGButton ------------\\

    open var longPressTime: Long = 20

    open var pressTickCounter: Long = 0
        protected set

    override fun onTick() {
        super.onTick()
        if (pressed) {
            pressTickCounter++
            if (longPressTime == pressTickCounter) {
                longPress(this)
            }
        } else if (pressTickCounter != 0L) {
            pressTickCounter = 0
        }
    }

    protected var onClick: (IGButtonWidget) -> Unit = {}
        private set

    protected var longPress: (IGButtonWidget) -> Unit = {}
        private set

    protected var onRelease: (IGButtonWidget) -> Unit = {}
        private set

    override fun onPress() {
        onClick(this)
    }

    override fun onRelease() {
        onRelease(this)
    }

    fun longPress(time: Long, action: (IGButtonWidget) -> Unit): IGButtonWidget {
        longPressTime = time
        longPress = action
        return this
    }

    fun click(action: (IGButtonWidget) -> Unit): IGButtonWidget {
        onClick = action
        return this
    }

    fun release(action: (IGButtonWidget) -> Unit): IGButtonWidget {
        onRelease = action
        return this
    }

    fun interface Scope : GuiScope<IGButtonWidget>, RowLayoutScope {

        fun click(action: (IGButtonWidget) -> Unit) = owner().click(action)

        fun longPress(time: Long, action: (IGButtonWidget) -> Unit) = owner().longPress(time, action)

        fun release(action: (IGButtonWidget) -> Unit) = owner().release(action)

    }

}

typealias ButtonScope = IGButtonWidget.Scope
