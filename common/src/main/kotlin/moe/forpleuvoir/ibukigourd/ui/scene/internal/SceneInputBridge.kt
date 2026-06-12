package moe.forpleuvoir.ibukigourd.ui.scene.internal

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.text.input.BackspaceCommand
import moe.forpleuvoir.ibukigourd.mod.IGConfig
import net.minecraft.client.input.MouseButtonEvent
import org.lwjgl.glfw.GLFW
import net.minecraft.client.input.KeyEvent as MCKeyEvent
import java.awt.event.KeyEvent as AWTKeyEvent

/**
 * 场景输入桥接器。
 *
 * 负责将 Minecraft 的鼠标/键盘事件转发到 Compose 场景，
 * 包括按键转换、滚动状态累积等。
 */
@OptIn(InternalComposeUiApi::class, ExperimentalComposeUiApi::class)
internal class SceneInputBridge(
    private val ctx: SceneContext,
) {

    private inline val scale get() = ctx.scale


    // ── 鼠标事件 ──────────────────────────────────────────────

    /**
     * 鼠标按下。
     * 对应 [moe.forpleuvoir.ibukigourd.ui.scene.ComposeSceneHost.mouseClicked]。
     */
    fun mouseClicked(event: MouseButtonEvent): Boolean {
        ctx.scene.sendPointerEvent(
            PointerEventType.Press,
            Offset((event.x * scale).toFloat(), (event.y * scale).toFloat()),
            button = PointerButton(event.button())
        )
        return true
    }

    /**
     * 鼠标释放。
     * 对应 [moe.forpleuvoir.ibukigourd.ui.scene.ComposeSceneHost.mouseReleased]。
     */
    fun mouseReleased(event: MouseButtonEvent): Boolean {
        ctx.scene.sendPointerEvent(
            PointerEventType.Release,
            Offset((event.x * scale).toFloat(), (event.y * scale).toFloat()),
            button = PointerButton(event.button())
        )
        return true
    }

    /**
     * 鼠标滚轮。
     * 对应 [moe.forpleuvoir.ibukigourd.ui.scene.ComposeSceneHost.mouseScrolled]。
     */
    fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        ctx.scene.sendPointerEvent(
            PointerEventType.Scroll,
            button = PointerButton.Tertiary,
            position = Offset(
                (mouseX * scale).toFloat(),
                (mouseY * scale).toFloat()
            ),
            scrollDelta = Offset(
                IGConfig.Gui.Scroller.applyScrollMultiplier(scrollX.toFloat() * 2),
                IGConfig.Gui.Scroller.applyScrollMultiplier(-scrollY.toFloat() * 2)
            )
        )
        return true
    }

    // ── 键盘事件 ──────────────────────────────────────────────

    /**
     * 按键按下。
     * 对应 [moe.forpleuvoir.ibukigourd.ui.scene.ComposeSceneHost.keyPressed]。
     */
    fun keyPressed(event: MCKeyEvent): Boolean {
        val result = ctx.scene.sendKeyEvent(event.asCompose(KeyEventType.KeyDown))
        return if (result) {
            true
        } else if (event.key == GLFW.GLFW_KEY_BACKSPACE) {
            ctx.platformContext.inputCommandSink?.invoke(listOf(BackspaceCommand()))
            true
        } else {
            false
        }
    }

    /**
     * 按键释放。
     * 对应 [moe.forpleuvoir.ibukigourd.ui.scene.ComposeSceneHost.keyReleased]。
     */
    fun keyReleased(event: MCKeyEvent): Boolean {
        return ctx.scene.sendKeyEvent(event.asCompose(KeyEventType.KeyUp))
    }

}

@OptIn(InternalComposeUiApi::class)
internal fun MCKeyEvent.asCompose(type: KeyEventType) =
    KeyEvent(
        key = Key(
            when (key) {
                GLFW.GLFW_KEY_UP        -> AWTKeyEvent.VK_UP
                GLFW.GLFW_KEY_LEFT      -> AWTKeyEvent.VK_LEFT
                GLFW.GLFW_KEY_DOWN      -> AWTKeyEvent.VK_DOWN
                GLFW.GLFW_KEY_RIGHT     -> AWTKeyEvent.VK_RIGHT
                GLFW.GLFW_KEY_TAB       -> AWTKeyEvent.VK_TAB
                GLFW.GLFW_KEY_ENTER     -> AWTKeyEvent.VK_ENTER
                GLFW.GLFW_KEY_KP_ENTER  -> AWTKeyEvent.VK_ENTER
                GLFW.GLFW_KEY_BACKSPACE -> AWTKeyEvent.VK_BACK_SPACE
                GLFW.GLFW_KEY_HOME      -> AWTKeyEvent.VK_HOME
                GLFW.GLFW_KEY_END       -> AWTKeyEvent.VK_END
                else                    -> key
            }
        ),
        type = type,
        isCtrlPressed = this.hasControlDown(),
        isMetaPressed = (modifiers and GLFW.GLFW_MOD_SUPER) != 0,
        isShiftPressed = this.hasShiftDown(),
        isAltPressed = this.hasAltDown(),
    )