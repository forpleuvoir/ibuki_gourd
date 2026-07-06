package moe.forpleuvoir.ibukigourd.ui.skia

import androidx.compose.material3.TextField
import moe.forpleuvoir.ibukigourd.ui.skia.SkiaContext.submit
import moe.forpleuvoir.ibukigourd.ui.toast.ToastHandler
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.nebula.common.api.Initializable
import org.jetbrains.skia.DirectContext
import org.lwjgl.opengl.WGL

/**
 * GL 上下文调度器。
 *
 * 管理一个与主渲染上下文共享显示列表的隐藏 OpenGL 上下文，
 * 所有 Skia GPU 操作必须通过 [submit] 在此上下文中执行。
 *
 * 初始化时机：游戏启动阶段 ([moe.forpleuvoir.ibukigourd.IbukiGourdClient])。
 */
object SkiaContext : Initializable {

    private val logger = logger()

    private var contextId: Long = 0

    override fun init() {
        if (contextId != 0L) {
            logger.info("SkiaContext is already initialized.")
            return
        }
        contextId = WGL.wglCreateContext(null, WGL.wglGetCurrentDC())
        if (contextId == 0L) return

        val current = WGL.wglGetCurrentContext(null)

        WGL.wglShareLists(null, current, contextId)
    }

    /** 共享 Skia GPU 上下文（惰性初始化） */
    val sharedContext: DirectContext by lazy {
        DirectContext.makeGL()
    }

    /**
     * 在共享 GL 上下文中执行指定任务。
     * 自动切换/恢复上下文，确保线程安全。
     */
    fun submit(task: () -> Unit) {
        val previous = WGL.wglGetCurrentContext(null)
        val dc = WGL.wglGetCurrentDC()
        WGL.wglMakeCurrent(null, dc, contextId)
        try {
            task()
        } finally {
            WGL.wglMakeCurrent(null, dc, previous)
        }
    }
}
