package moe.forpleuvoir.ibukigourd.ui.scene.internal

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import net.minecraft.client.gui.GuiGraphicsExtractor
import org.jetbrains.skiko.currentNanoTime

/**
 * 场景渲染器。
 *
 * 负责每帧渲染流程：
 * 1. 同步语言环境
 * 2. 发送鼠标移动和滚动事件
 * 3. 滚动衰减计算
 * 4. 提交 Compose 场景渲染到 Skia 表面
 * 5. 将结果混合到 Minecraft GUI 缓冲区
 */
@OptIn(InternalComposeUiApi::class, ExperimentalComposeUiApi::class)
internal class SceneRenderer(
    private val ctx: SceneContext,
) {

    /**
     * 执行一帧渲染。
     * 对应 [moe.forpleuvoir.ibukigourd.ui.scene.ComposeSceneHost.extractRenderState]。
     */
    fun render(
        guiGraphics: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float,
    ) {
        ctx.scene.sendPointerEvent(
            PointerEventType.Move,
            Offset(mouseX * ctx.scale, mouseY * ctx.scale)
        )

        ctx.surface.update(guiGraphics) {
            ctx.scene.render(it, currentNanoTime())
        }
    }
}
