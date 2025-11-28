package moe.forpleuvoir.ibukigourd.util

import net.minecraft.server.packs.resources.PreparableReloadListener
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.function.Consumer

abstract class SimpleResourceReloaderListener<T> : PreparableReloadListener {
    override fun reload(
        sharedState: PreparableReloadListener.SharedState,
        exectutor: Executor,
        barrier: PreparableReloadListener.PreparationBarrier,
        applyExectutor: Executor
    ): CompletableFuture<Void> {
        val prepareStep = CompletableFuture.supplyAsync<T>({ this.prepare(sharedState) }, exectutor)
        Objects.requireNonNull(barrier)
        return prepareStep.thenCompose<T> { barrier.wait(it) }
            .thenAcceptAsync(Consumer { prepared: T -> this.apply(prepared, sharedState) }, applyExectutor)
    }


    protected abstract fun prepare(sharedState: PreparableReloadListener.SharedState): T


    protected abstract fun apply(prepared: T, sharedState: PreparableReloadListener.SharedState)
}