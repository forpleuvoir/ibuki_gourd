package moe.forpleuvoir.ibukigourd.gui.widget.button

import moe.forpleuvoir.ibukigourd.gui.base.layout.ColumnLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.scope.ColumnLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGPressableWidgetContainer
import moe.forpleuvoir.ibukigourd.util.Tick

open class IGButtonWidget(
    override val arrangement: Arrangement.Horizontal,
    override val alignment: Alignment.Vertical
) : IGPressableWidgetContainer(), ColumnLayout {

    //------------ IGButton ------------\\

    open var longPressTime: Tick = 20

    open var pressTickCounter: Tick = 0
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

    fun longPress(time: Tick, action: (IGButtonWidget) -> Unit): IGButtonWidget {
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

    fun interface Scope : GuiScope<IGButtonWidget>, ColumnLayoutScope {

        fun click(action: (IGButtonWidget) -> Unit) = owner().click(action)

        fun longPress(time: Tick, action: (IGButtonWidget) -> Unit) = owner().longPress(time, action)

        fun release(action: (IGButtonWidget) -> Unit) = owner().release(action)

    }

}

typealias ButtonScope = IGButtonWidget.Scope
