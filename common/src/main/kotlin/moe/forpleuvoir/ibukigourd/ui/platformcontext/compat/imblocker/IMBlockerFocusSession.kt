package moe.forpleuvoir.ibukigourd.ui.platformcontext.compat.imblocker

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.PlatformTextInputMethodRequest
import io.github.reserveword.imblocker.common.gui.FocusContainer
import io.github.reserveword.imblocker.common.gui.FocusableWidget
import io.github.reserveword.imblocker.common.gui.Point
import io.github.reserveword.imblocker.common.gui.Rectangle
import moe.forpleuvoir.ibukigourd.util.mc


fun interface IMBlockerFocusSession : AutoCloseable {

    override fun close()
}

internal object IMBlockerCompatImpl {

    @JvmStatic
    fun requestTextInputFocus(
        request: PlatformTextInputMethodRequest,
    ): IMBlockerFocusSession {
        val widget = ComposeFocusableWidget(request)

        FocusContainer.MINECRAFT.requestFocus(widget)

        return IMBlockerFocusSession {
            FocusContainer.MINECRAFT.removeFocus(widget)
        }
    }
}

internal class ComposeFocusableWidget(
    private val request: PlatformTextInputMethodRequest,
) : FocusableWidget {

    override fun getFocusContainer(): FocusContainer =
        FocusContainer.MINECRAFT

    override fun getPreferredState(): Boolean = true

    override fun getPreferredEnglishState(): Boolean = false

    override fun getGuiScale(): Double = 1.0

    @OptIn(ExperimentalComposeUiApi::class)
    override fun getBoundsAbs(): Rectangle {
        val rect = request.textFieldRectInRoot() ?: return Rectangle.EMPTY

        return Rectangle(
            getGuiScale(),
            rect.left.toInt(),
            rect.top.toInt() - (60 + mc.window.guiScale * 15),
            rect.width.toInt(),
            rect.height.toInt(),
        )
    }

    override fun getCaretPos(): Point =
        Point.TOP_LEFT
}