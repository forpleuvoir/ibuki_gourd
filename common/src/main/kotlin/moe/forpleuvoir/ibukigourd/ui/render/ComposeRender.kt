package moe.forpleuvoir.ibukigourd.ui.render

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.asComposeCanvas
import androidx.compose.ui.scene.CanvasLayersComposeScene
import androidx.compose.ui.scene.ComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import com.mojang.blaze3d.opengl.GlStateManager
import com.mojang.blaze3d.textures.GpuTextureView
import kotlinx.coroutines.*
import moe.forpleuvoir.ibukigourd.util.mc
import org.jetbrains.skiko.FrameDispatcher
import org.lwjgl.glfw.GLFW.glfwDestroyWindow
import java.util.concurrent.Executors
import java.util.function.Consumer

@OptIn(InternalComposeUiApi::class)
class ComposeRender {
    @Volatile
    private var destroyed = false

    private val threadPool = Executors.newSingleThreadExecutor { Thread(it, "ComposeRenderThread") }
    private val composeScope = CoroutineScope(SupervisorJob() + threadPool.asCoroutineDispatcher())
    private val frameDispatcher = FrameDispatcher(composeScope) { render() }

    private val skiaRender = SkiaRender()

    val gpuView: GpuTextureView?
        get() = skiaRender.gpuView

    private val composeScene: ComposeScene = CanvasLayersComposeScene(
        coroutineContext = composeScope.coroutineContext,
        invalidate = { frameDispatcher.scheduleFrame() }
    )
    lateinit var composeCanvas: Canvas; private set

    @Volatile
    private var width = 0

    @Volatile
    private var height = 0

    fun init(width: Int, height: Int, uiScale: Float, fontScale: Float, content: @Composable () -> Unit) {
        this.width = width
        this.height = height

        skiaRender.prepareTexture()

        mc.submit {
            skiaRender.ensureRenderType(width, height)
        }

        // 直接在当前线程（game thread）配置场景，此时场景还没启动渲染
        composeScene.density = Density(uiScale, fontScale)
        composeScene.size = IntSize(width, height)
        composeScene.setContent(content)

        composeScope.launch {
            val glId = skiaRender.textureId
            skiaRender.prepareRender(width, height, glId)
            composeCanvas = skiaRender.surface.canvas.asComposeCanvas()
            composeScene.density = Density(uiScale, fontScale)
            composeScene.size = IntSize(width, height)
            composeScene.setContent(content)
//            render()  // ← 立即渲染第一帧，不等 FrameDispatcher
        }
    }

    fun render() {
        if (destroyed || !skiaRender.isSurfaceInitialized) return
        Snapshot.sendApplyNotifications()
        skiaRender.render {
            try {
                composeScene.render(composeCanvas, System.nanoTime())
            } catch (e: Exception) {
                if (!destroyed) e.printStackTrace()
            }
        }
    }

    fun resize(width: Int, height: Int, uiScale: Float, fontScale: Float) {
        composeScope.launch {
            // 单线程池，这个 launch 会等当前 render 完成后执行
            this@ComposeRender.width = width
            this@ComposeRender.height = height
            skiaRender.resize(width, height)
            composeCanvas = skiaRender.surface.canvas.asComposeCanvas()
            composeScene.density = Density(uiScale, fontScale)
            composeScene.size = IntSize(width, height)
        }
    }


    fun post(task: Consumer<ComposeScene>) {
        composeScope.launch { task.accept(composeScene) }
    }

    fun destroy() {
        destroyed = true
        frameDispatcher.cancel()
        composeScope.cancel()
        // 不 shutdown 线程池，也不等 composeScope
        // 只释放 GPU 资源——这些不走线程池，直接在当前线程执行
        mc.textureManager.release(SkiaRender.SKIA_OUTPUT_ID)
        if (skiaRender.textureId != -1) GlStateManager._deleteTexture(skiaRender.textureId)
        if (skiaRender.windowId != -1L) glfwDestroyWindow(skiaRender.windowId)
    }

}
