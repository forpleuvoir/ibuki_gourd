package moe.forpleuvoir.ibukigourd.util

import net.minecraft.server.packs.resources.PreparableReloadListener
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor

abstract class SimpleResourceReloaderListener<T> : PreparableReloadListener {
    override fun reload(
        sharedState: PreparableReloadListener.SharedState,
        exectutor: Executor,
        barrier: PreparableReloadListener.PreparationBarrier,
        applyExectutor: Executor
    ): CompletableFuture<Void> {
        val prepareStep: CompletableFuture<T> = CompletableFuture.supplyAsync({ this.prepare(sharedState) }, exectutor)
        val composeStep: CompletableFuture<T> = prepareStep.thenCompose { result ->
            barrier.wait(result!!)
        }
        return composeStep.thenAcceptAsync({ this.apply(it, sharedState) }, applyExectutor)
    }

    protected abstract fun prepare(sharedState: PreparableReloadListener.SharedState): T

    protected abstract fun apply(prepared: T, sharedState: PreparableReloadListener.SharedState)
}