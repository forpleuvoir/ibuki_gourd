package moe.forpleuvoir.ibukigourd.ui.skia

import moe.forpleuvoir.ibukigourd.platform.RenderBackend
import moe.forpleuvoir.ibukigourd.ui.skia.backend.SkiaRenderBackend
import moe.forpleuvoir.ibukigourd.ui.skia.backend.SkiaRenderBackends
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.nebula.common.api.Initializable
import org.jetbrains.skia.DirectContext

/**
 * Skia 渲染后端门面。
 *
 * 持有当前 [SkiaRenderBackend]（见 [SkiaRenderBackends]），
 * 向后兼容地暴露 [sharedContext] / [submit] 等历史 API。
 *
 * 初始化时机：游戏启动阶段 ([moe.forpleuvoir.ibukigourd.IbukiGourdClient])。
 *
 * Vulkan 图形后端下本模组的 UI 功能已被禁用，任何对 [sharedContext] / [submit]
 * 的访问都会抛出不支持异常，避免消费方模组触碰无效的 GL 上下文。
 */
object SkiaContext : Initializable {

    private val logger = logger()

    private var backend: SkiaRenderBackend? = null

    /** 当前渲染后端（惰性创建，兼容未显式 [init] 的访问）。 */
    val current: SkiaRenderBackend
        get() = backend ?: SkiaRenderBackends.current.also { backend = it }

    /** 共享 Skia GPU 上下文。 */
    val sharedContext: DirectContext
        get() = current.sharedContext

    override fun init() {
        if (backend != null) {
            logger.info("SkiaContext is already initialized.")
            return
        }
        val created = SkiaRenderBackends.current
        created.init()
        backend = created
    }

    /**
     * 在共享 GPU 上下文中执行指定任务。
     * 自动切换/恢复上下文，确保线程安全。
     */
    fun submit(task: () -> Unit) {
        if (RenderBackend.isVulkan) return
        current.submit(task)
    }
}
