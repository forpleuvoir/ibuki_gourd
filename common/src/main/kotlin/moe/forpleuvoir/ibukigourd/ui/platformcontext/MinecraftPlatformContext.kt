@file:OptIn(InternalComposeUiApi::class, ExperimentalComposeUiApi::class)

package moe.forpleuvoir.ibukigourd.ui.platformcontext

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.platform.PlatformContext
import androidx.compose.ui.platform.PlatformTextInputMethodRequest
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.text.input.EditCommand
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import moe.forpleuvoir.ibukigourd.input.MouseCursor
import moe.forpleuvoir.ibukigourd.platform.PLATFORM
import moe.forpleuvoir.ibukigourd.ui.platformcontext.compat.imblocker.IMBlockerCompat
import moe.forpleuvoir.ibukigourd.ui.platformcontext.compat.imblocker.IMBlockerCompatImpl
import moe.forpleuvoir.ibukigourd.ui.platformcontext.compat.imblocker.IMBlockerFocusSession
import moe.forpleuvoir.ibukigourd.util.mc
import org.lwjgl.glfw.GLFW

/**
 * Compose 运行时平台服务绑定。
 *
 * 职责范围：
 * - 窗口信息 ([WindowInfo])
 * - 输入模式管理 ([InputModeManager])
 * - 系统剪贴板读写
 * - 光标样式切换
 * - 输入法会话管理
 * - 游戏语言环境跟踪
 *
 * 此类实例由 [moe.forpleuvoir.ibukigourd.ui.scene.internal.SceneContext] 持有，供内部组件使用。
 */
class MinecraftPlatformContext : PlatformContext {

    override val windowInfo: MinecraftWindowInfo = MinecraftWindowInfo()

    override val inputModeManager: InputModeManager = InputModeManagerImpl()

    /** 当前活动输入法会话的编辑命令回调 */
    var inputCommandSink: ((List<EditCommand>) -> Unit)? = null
        private set

    override suspend fun startInputMethod(request: PlatformTextInputMethodRequest): Nothing {
        var imBlockerFocus: IMBlockerFocusSession? = null
        try {
            inputCommandSink = request.onEditCommand

            imBlockerFocus = IMBlockerCompat.requestTextInputFocus(request)
            if (imBlockerFocus == null) {
                coroutineScope {
                    launch {
                        snapshotFlow { request.textFieldRectInRoot() to request.textLayoutResult() }
                            .collect { rect ->
                                val (rect, result) = rect
                                if (rect != null && result != null) {
                                    GLFW.glfwSetPreeditCursorRectangle(
                                        mc.window.handle(),
                                        rect.left.toInt(),
                                        rect.top.toInt() - 60,
                                        0,
                                        0
                                    )
                                }
                            }
                    }
                }
            }
            awaitCancellation()
        } finally {
            imBlockerFocus?.close()
            inputCommandSink = null
        }
    }

    override fun setPointerIcon(pointerIcon: PointerIcon) {
        when (pointerIcon) {
            PointerIcon.Hand      -> MouseCursor.PointingHandCursor
            PointerIcon.Text      -> MouseCursor.IbeamCursor
            PointerIcon.Crosshair -> MouseCursor.CrosshairCursor
            else                  -> MouseCursor.ArrowCursor
        }.apply()
    }

    fun resetCursors() {
        MouseCursor.reset()
    }
}


internal class InputModeManagerImpl : InputModeManager {
    override var inputMode: InputMode by mutableStateOf(InputMode.Keyboard)

    @ExperimentalComposeUiApi
    override fun requestInputMode(inputMode: InputMode) =
        if (inputMode == InputMode.Touch || inputMode == InputMode.Keyboard) {
            this.inputMode = inputMode
            true
        } else {
            false
        }
}
