package moe.forpleuvoir.ibukigourd.ui.scene.internal

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.text.input.CommitTextCommand
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import org.lwjgl.glfw.GLFW

/**
 * 场景生命周期控制器。
 *
 * 负责场景的初始化和销毁，包括：
 * - [init]    ：配置窗口缩放、场景尺寸密度、注册 GLFW 字符回调
 * - [onClose] ：销毁场景、还原回调、释放光标资源
 */
@OptIn(InternalComposeUiApi::class, ExperimentalComposeUiApi::class)
internal class SceneLifecycle(
    private val ctx: SceneContext,
) {

    /**
     * 初始化场景。
     * 对应 [moe.forpleuvoir.ibukigourd.ui.scene.ComposeSceneHost.init]。
     */
    fun init() {
        val window = ctx.minecraft.window
        ctx.surface.resize(window.width, window.height)
        ctx.scale = window.guiScale.toFloat()

        val newSize = IntSize(window.width, window.height)
        if (ctx.scene.size != newSize) ctx.scene.size = newSize
        val newDensity = Density(ctx.scale * 0.5f, 1.0f)
        if (ctx.scene.density != newDensity) ctx.scene.density = newDensity

        ctx.platformContext.windowInfo.containerSize = newSize
        ctx.platformContext.windowInfo.containerDpSize = newDensity.run { newSize.toSize().toDpSize() }

        if (ctx.charCallback == null) {
            ctx.charCallback = GLFW.glfwSetCharCallback(ctx.minecraft.window.handle()) { _, codepoint ->
                ctx.platformContext.inputCommandSink?.invoke(listOf(CommitTextCommand(Char(codepoint).toString(), 1)))
            }
        }
    }

    /**
     * 销毁场景并释放资源。
     * 对应 [moe.forpleuvoir.ibukigourd.ui.scene.ComposeSceneHost.onClose]。
     */
    fun onClose() {
        ctx.scene.close()
        ctx.surface.dispose()
        GLFW.glfwSetCharCallback(ctx.minecraft.window.handle(), ctx.charCallback)
        ctx.platformContext.resetCursors()
    }
}
