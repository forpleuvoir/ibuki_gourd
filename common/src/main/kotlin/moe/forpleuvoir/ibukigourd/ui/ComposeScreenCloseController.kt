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
