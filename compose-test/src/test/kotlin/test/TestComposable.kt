package test

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun TestComposable(name: String) {
    ComposeNode<TestNode, TestApplier>(factory = { TestNode(name) }, update = {})
}

internal class CompositionHandle(
    val composition: Composition,
    private val recomposer: Recomposer,
    private val job: kotlinx.coroutines.Job,
    private val clock: TestMonotonicFrameClock
) {
    fun dispose() {
        composition.dispose()
        recomposer.close()
        clock.close()
        job.cancel()
    }
}

internal fun TestNode.runComposition(
    scope: CoroutineScope,
    clock: TestMonotonicFrameClock,
    content: @Composable () -> Unit
): CompositionHandle {
    val applier = TestApplier(this)
    val recomposer = Recomposer(scope.coroutineContext)
    val composition = Composition(applier, recomposer)
    val job = scope.launch { recomposer.runRecomposeAndApplyChanges() }
    composition.setContent(content)
    return CompositionHandle(composition, recomposer, job, clock)
}
