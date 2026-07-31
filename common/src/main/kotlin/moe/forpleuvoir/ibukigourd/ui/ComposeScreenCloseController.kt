package moe.forpleuvoir.ibukigourd.ui

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.distinctUntilChanged

enum class ComposeScreenCloseState {
    Open,
    Closing,
    Closed,
}

interface CloseAnimationHandle {
    fun complete()
    fun dispose()
}

class ComposeScreenCloseController {
    private var _state by mutableStateOf(ComposeScreenCloseState.Open)
    private val handles = mutableSetOf<HandleImpl>()
    private var finished = false

    @Volatile
    var completed: Boolean = false
        private set

    val state: ComposeScreenCloseState get() = _state
    val isClosing: Boolean get() = _state == ComposeScreenCloseState.Closing
    val isClosed: Boolean get() = _state == ComposeScreenCloseState.Closed

    fun requestClose() {
        if (_state != ComposeScreenCloseState.Open) return
        _state = ComposeScreenCloseState.Closing
        checkFinish()
    }

    internal fun registerAnimation(): CloseAnimationHandle {
        val handle = HandleImpl()
        handles.add(handle)
        return handle
    }

    /**
     * 立即完成关闭流程（跳过剩余动画参与者）。
     *
     * 用于屏幕被替换后无人再渲染本屏、退出动画无法推进的场景，
     * 强制收尾以触发清理，避免 Compose 场景与 GPU 资源泄漏。
     */
    internal fun finishNow() {
        handles.clear()
        performFinish()
    }

    /**
     * 重置为打开状态。
     *
     * 屏幕被重新展示（如弹窗关闭后回到父屏幕，场景重建）时调用，
     * 使关闭动画系统可复用。
     */
    internal fun reset() {
        if (_state == ComposeScreenCloseState.Open && !finished) return
        handles.clear()
        finished = false
        completed = false
        _state = ComposeScreenCloseState.Open
    }

    private fun checkFinish() {
        if (_state != ComposeScreenCloseState.Closing) return
        if (handles.isNotEmpty()) return
        performFinish()
    }

    private fun performFinish() {
        if (finished) return
        finished = true
        _state = ComposeScreenCloseState.Closed
        completed = true
    }

    private inner class HandleImpl : CloseAnimationHandle {
        private var done = false

        override fun complete() {
            if (done) return
            done = true
            handles.remove(this)
            checkFinish()
        }

        override fun dispose() {
            complete()
        }
    }
}

val LocalComposeScreenCloseController = staticCompositionLocalOf<ComposeScreenCloseController?> { null }

@Composable
fun rememberComposeScreenVisibilityState(): MutableTransitionState<Boolean> {
    val controller = LocalComposeScreenCloseController.current
    val state = remember { MutableTransitionState(false) }
    val handleRef = remember { mutableStateOf<CloseAnimationHandle?>(null) }

    LaunchedEffect(Unit) {
        state.targetState = true
    }

    DisposableEffect(controller) {
        val handle = controller?.registerAnimation()
        handleRef.value = handle
        onDispose {
            handle?.dispose()
            handleRef.value = null
        }
    }

    LaunchedEffect(controller) {
        if (controller == null) return@LaunchedEffect
        snapshotFlow { controller.isClosing }
            .distinctUntilChanged()
            .collect { isClosing ->
                if (isClosing) state.targetState = false
            }
    }

    LaunchedEffect(state.isIdle, state.currentState, state.targetState) {
        if (state.isIdle && !state.targetState) {
            handleRef.value?.complete()
        }
    }

    return state
}
