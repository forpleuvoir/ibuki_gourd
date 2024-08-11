package moe.forpleuvoir.ibukigourd.gui.widget.button

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.layout.BoxLayout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.padding
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.scope.BoxLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGPressableWidgetContainer
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.ibukigourd.gui.widget.theme.PressableTheme
import moe.forpleuvoir.ibukigourd.input.MouseCursor
import moe.forpleuvoir.ibukigourd.util.Tick

open class IGButtonWidget(
    private val theme: PressableTheme = PressableTheme.Button2,
) : IGPressableWidgetContainer(), BoxLayout {

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

    override fun onRenderBackground(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        context.batchRenderTextureColored {
            context.drawWidgetTexture(transform.asWorldBox, status(theme.disabled, theme.idle, theme.hovered, theme.pressed))
        }
    }

}

fun interface ButtonScope : GuiScope<IGButtonWidget>, BoxLayoutScope {

    fun press(action: (IGButtonWidget) -> Unit) = owner().press(action)

    fun longPress(time: Tick, action: (IGButtonWidget) -> Unit) = owner().longPress(time, action)

    fun release(action: (IGButtonWidget) -> Unit) = owner().release(action)

}


fun GuiScope<out WidgetContainer>.button(
    theme: PressableTheme = PressableTheme.Button2,
    modifier: Modifier = Modifier,
    content: ButtonScope.() -> Unit = { }
) = addWidgetChild(IGButtonWidget(theme)) {
    Modifier.padding(6).then(modifier).foldInApply()
    ButtonScope { this }.content()
}