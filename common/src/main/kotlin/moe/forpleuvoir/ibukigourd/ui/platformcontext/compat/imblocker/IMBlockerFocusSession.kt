package moe.forpleuvoir.ibukigourd.ui.platformcontext.compat.imblocker

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.PlatformTextInputMethodRequest
import io.github.reserveword.imblocker.common.IMManager
import io.github.reserveword.imblocker.common.gui.FocusContainer
import io.github.reserveword.imblocker.common.gui.FocusableWidget
import io.github.reserveword.imblocker.common.gui.Point
import io.github.reserveword.imblocker.common.gui.Rectangle
import moe.forpleuvoir.ibukigourd.util.mc
import kotlin.math.ceil
import kotlin.math.floor


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

    @JvmStatic
    fun updateCaretPosition() {
        IMManager.updateCaretPosition()
    }
}

internal class ComposeFocusableWidget(
    private val request: PlatformTextInputMethodRequest,
) : FocusableWidget {

    override fun getFocusContainer(): FocusContainer =
        FocusContainer.MINECRAFT

    override fun getPreferredState(): Boolean =
        true

    override fun getPreferredEnglishState(): Boolean =
        false

    @OptIn(ExperimentalComposeUiApi::class)
    override fun getBoundsAbs(): Rectangle {
        val rect = request.textFieldRectInRoot()
            ?: return Rectangle.EMPTY

        val left = floor(rect.left).toInt()
        val top = floor(rect.top).toInt() - imeVerticalOffset()
        val right = ceil(rect.right).toInt()
        val bottom = ceil(rect.bottom).toInt()

        return Rectangle(
            left,
            top,
            right - left,
            bottom - floor(rect.top).toInt(),
        )
    }

    override fun getCaretPos(): Point =
        Point.TOP_LEFT

    /*
     * textFieldRectInRoot() 已经是 Compose Scene 的窗口像素坐标，
     * 不能再次乘 Minecraft GUI Scale。
     */
    override fun getGuiScale(): Double = 1.0

    private fun imeVerticalOffset(): Int =
        60 + mc.window.guiScale * 15
}