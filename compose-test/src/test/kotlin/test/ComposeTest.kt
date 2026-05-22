package test

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ComposeTest {

    private class TestFixture {
        val clock = TestMonotonicFrameClock()
        val scope = CoroutineScope(clock + Dispatchers.Default)
    }

    private fun test(block: suspend TestFixture.() -> Unit) {
        val ctx = TestFixture()
        runBlocking { ctx.block() }
        ctx.scope.cancel()
    }

    private fun TestFixture.advanceFrame() {
        clock.advanceClock(times = 5)
    }

    // ── 1. Applier ──

    @Test
    fun `applier insert remove`() {
        val root = TestNode("root")
        val applier = TestApplier(root)

        applier.insertTopDown(0, TestNode("a"))
        applier.insertTopDown(1, TestNode("b"))
        assertEquals(2, root.children.size)

        applier.remove(0, 1)
        assertEquals(1, root.children.size)
        assertEquals("b", (root.children[0]).name)
    }

    @Test
    fun `applier move`() {
        val root = TestNode("root")
        val applier = TestApplier(root)
        val a = TestNode("a"); val b = TestNode("b"); val c = TestNode("c")
        applier.insertTopDown(0, a); applier.insertTopDown(1, b); applier.insertTopDown(2, c)
        applier.move(0, 2, 1)
        assertEquals(listOf("b", "c", "a"), root.children.map { it.name })
    }

    // ── 2. 初始组合 ──

    @Test
    fun `composable builds node tree`() = test {
        val root = TestNode("root")
        val handle = root.runComposition(scope, clock) {
            TestComposable("A")
            TestComposable("B")
        }
        advanceFrame()
        assertEquals(2, root.children.size)
        assertEquals("A", root.children[0].name)
        assertEquals("B", root.children[1].name)
        handle.dispose()
    }

    @Test
    fun `side effect executes`() = test {
        val root = TestNode("root")
        var executed = false
        val handle = root.runComposition(scope, clock) {
            SideEffect { executed = true }
            TestComposable("x")
        }
        advanceFrame()
        assertTrue(executed)
        handle.dispose()
    }

    @Test
    fun `disposable effect calls onDispose`() = test {
        val root = TestNode("root")
        var disposed = false
        val handle = root.runComposition(scope, clock) {
            DisposableEffect(Unit) {
                onDispose { disposed = true }
            }
            TestComposable("x")
        }
        advanceFrame()
        handle.dispose()
        assertTrue(disposed)
    }

    @Test
    fun `remember works in initial composition`() = test {
        val root = TestNode("root")
        var captured = ""
        val handle = root.runComposition(scope, clock) {
            val value = remember { mutableStateOf("saved") }
            captured = value.value
            TestComposable(value.value)
        }
        advanceFrame()
        assertEquals("saved", captured)
        handle.dispose()
    }

    // ── 3. mutableStateOf ──

    @Test
    fun `mutableStateOf can be read and written`() {
        val state = mutableStateOf("initial")
        assertEquals("initial", state.value)
        state.value = "updated"
        assertEquals("updated", state.value)
    }

    @Test
    fun `remember preserves value`() = test {
        val root = TestNode("root")
        var counter = 0
        val handle = root.runComposition(scope, clock) {
            val count = remember { mutableStateOf(0) }
            counter = count.value
            SideEffect { count.value = 1 }
            TestComposable("x")
        }
        advanceFrame()
        assertEquals(0, counter)
        handle.dispose()
    }

}
