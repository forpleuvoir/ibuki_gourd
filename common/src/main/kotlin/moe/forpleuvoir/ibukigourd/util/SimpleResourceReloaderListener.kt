package moe.forpleuvoir.ibukigourd.util

import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.util.profiling.Profiler
import net.minecraft.util.profiling.ProfilerFiller
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

abstract class SimpleResourceReloaderListener<T> : PreparableReloadListener {
    override fun reload(
        barrier: PreparableReloadListener.PreparationBarrier,
        manager: ResourceManager,
        applyExectutor: Executor,
        gameExecutor: Executor
    ): CompletableFuture<Void> {
        return CompletableFuture.supplyAsync<T>({ this.prepare(manager, Profiler.get()) }, gameExecutor)
            .thenCompose<T> { result -> barrier.wait(result!!) }
            .thenAcceptAsync({ prepared -> this.apply(prepared, manager, Profiler.get()) }, gameExecutor)
    }

    protected abstract fun prepare(resourceManager: ResourceManager, profiler: ProfilerFiller): T

    protected abstract fun apply(prepared: T, resourceManager: ResourceManager, profiler: ProfilerFiller)
}