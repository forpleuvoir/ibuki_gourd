package moe.forpleuvoir.ibukigourd.gui.widget.button

import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.layout.BoxLayout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.scope.BoxLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGPressableWidgetContainer
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.ibukigourd.gui.widget.theme.PressableTheme
import moe.forpleuvoir.ibukigourd.util.Tick

open class IGButtonWidget(
    private val theme: PressableTheme = PressableTheme.Button2,
) : IGPressableWidgetContainer(), BoxLayout {

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

@JvmInline
value class IGButtonScope(private val button: IGButtonWidget) : GuiScope<IGButtonWidget>, BoxLayoutScope {

    override fun owner(): IGButtonWidget = button

    fun press(action: (IGButtonWidget) -> Unit) = button.press(action)

    fun longPress(time: Tick, action: (IGButtonWidget) -> Unit) = button.longPress(time, action)

    fun release(action: (IGButtonWidget) -> Unit) = button.release(action)

}

fun GuiScope<out WidgetContainer>.button(
    theme: PressableTheme = PressableTheme.Button2,
    modifier: Modifier? = null,
    content: (IGButtonScope.() -> Unit)? = null
) = owner().addWidgetChild(IGButtonWidget(theme)) {
    padding = Padding(horizontal = 6, vertical = 6)
    content?.let { IGButtonScope(this).it() }
    modifier?.foldIn(Unit) { _, e ->
        e.tryApplyModify(this)
    }
}