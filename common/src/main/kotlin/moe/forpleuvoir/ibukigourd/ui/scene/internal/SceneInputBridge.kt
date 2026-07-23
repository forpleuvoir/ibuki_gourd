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
import moe.forpleuvoir.ibukigourd.mod.config.IGConfig
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

/**
 * 将 GLFW key code 转换为 AWT VK 编码。
 *
 * 映射策略分为三类：
 * 1. 显式映射——GLFW 编码与 AWT 编码不一致的键（APOSTROPHE、GRAVE_ACCENT、导航键、功能键、小键盘键等）
 * 2. 已验证的直通——GLFW 编码与 AWT VK 编码数值一致的键（A-Z、0-9、Space、基本标点）
 * 3. 安全降级——无法映射的键返回 [AWTKeyEvent.VK_UNDEFINED]
 */
internal fun glfwKeyToAwtKey(glfwKey: Int): Int = when (glfwKey) {
    // ── 导航与编辑键 ──
    GLFW.GLFW_KEY_UP        -> AWTKeyEvent.VK_UP
    GLFW.GLFW_KEY_DOWN      -> AWTKeyEvent.VK_DOWN
    GLFW.GLFW_KEY_LEFT      -> AWTKeyEvent.VK_LEFT
    GLFW.GLFW_KEY_RIGHT     -> AWTKeyEvent.VK_RIGHT
    GLFW.GLFW_KEY_HOME      -> AWTKeyEvent.VK_HOME
    GLFW.GLFW_KEY_END       -> AWTKeyEvent.VK_END
    GLFW.GLFW_KEY_PAGE_UP   -> AWTKeyEvent.VK_PAGE_UP
    GLFW.GLFW_KEY_PAGE_DOWN -> AWTKeyEvent.VK_PAGE_DOWN
    GLFW.GLFW_KEY_INSERT    -> AWTKeyEvent.VK_INSERT
    GLFW.GLFW_KEY_DELETE    -> AWTKeyEvent.VK_DELETE
    GLFW.GLFW_KEY_BACKSPACE -> AWTKeyEvent.VK_BACK_SPACE
    GLFW.GLFW_KEY_TAB       -> AWTKeyEvent.VK_TAB
    GLFW.GLFW_KEY_ENTER     -> AWTKeyEvent.VK_ENTER
    GLFW.GLFW_KEY_KP_ENTER  -> AWTKeyEvent.VK_ENTER

    // ── Escape ──
    GLFW.GLFW_KEY_ESCAPE    -> AWTKeyEvent.VK_ESCAPE

    // ── 与 AWT 编码不一致的标点键 ──
    GLFW.GLFW_KEY_APOSTROPHE   -> AWTKeyEvent.VK_QUOTE
    GLFW.GLFW_KEY_GRAVE_ACCENT -> AWTKeyEvent.VK_BACK_QUOTE

    // ── 修饰键（AWT 无左右区分）──
    GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT       -> AWTKeyEvent.VK_SHIFT
    GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL   -> AWTKeyEvent.VK_CONTROL
    GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT           -> AWTKeyEvent.VK_ALT
    GLFW.GLFW_KEY_LEFT_SUPER, GLFW.GLFW_KEY_RIGHT_SUPER       -> AWTKeyEvent.VK_META

    // ── 锁定与系统键 ──
    GLFW.GLFW_KEY_CAPS_LOCK    -> AWTKeyEvent.VK_CAPS_LOCK
    GLFW.GLFW_KEY_SCROLL_LOCK  -> AWTKeyEvent.VK_SCROLL_LOCK
    GLFW.GLFW_KEY_NUM_LOCK     -> AWTKeyEvent.VK_NUM_LOCK
    GLFW.GLFW_KEY_PRINT_SCREEN -> AWTKeyEvent.VK_PRINTSCREEN
    GLFW.GLFW_KEY_PAUSE        -> AWTKeyEvent.VK_PAUSE

    // ── 功能键 F1-F24 ──
    GLFW.GLFW_KEY_F1  -> AWTKeyEvent.VK_F1
    GLFW.GLFW_KEY_F2  -> AWTKeyEvent.VK_F2
    GLFW.GLFW_KEY_F3  -> AWTKeyEvent.VK_F3
    GLFW.GLFW_KEY_F4  -> AWTKeyEvent.VK_F4
    GLFW.GLFW_KEY_F5  -> AWTKeyEvent.VK_F5
    GLFW.GLFW_KEY_F6  -> AWTKeyEvent.VK_F6
    GLFW.GLFW_KEY_F7  -> AWTKeyEvent.VK_F7
    GLFW.GLFW_KEY_F8  -> AWTKeyEvent.VK_F8
    GLFW.GLFW_KEY_F9  -> AWTKeyEvent.VK_F9
    GLFW.GLFW_KEY_F10 -> AWTKeyEvent.VK_F10
    GLFW.GLFW_KEY_F11 -> AWTKeyEvent.VK_F11
    GLFW.GLFW_KEY_F12 -> AWTKeyEvent.VK_F12
    GLFW.GLFW_KEY_F13 -> AWTKeyEvent.VK_F13
    GLFW.GLFW_KEY_F14 -> AWTKeyEvent.VK_F14
    GLFW.GLFW_KEY_F15 -> AWTKeyEvent.VK_F15
    GLFW.GLFW_KEY_F16 -> AWTKeyEvent.VK_F16
    GLFW.GLFW_KEY_F17 -> AWTKeyEvent.VK_F17
    GLFW.GLFW_KEY_F18 -> AWTKeyEvent.VK_F18
    GLFW.GLFW_KEY_F19 -> AWTKeyEvent.VK_F19
    GLFW.GLFW_KEY_F20 -> AWTKeyEvent.VK_F20
    GLFW.GLFW_KEY_F21 -> AWTKeyEvent.VK_F21
    GLFW.GLFW_KEY_F22 -> AWTKeyEvent.VK_F22
    GLFW.GLFW_KEY_F23 -> AWTKeyEvent.VK_F23
    GLFW.GLFW_KEY_F24 -> AWTKeyEvent.VK_F24
    // GLFW_KEY_F25 (315) — AWT 无对应常量，降级为 VK_UNDEFINED

    // ── 小键盘键 ──
    GLFW.GLFW_KEY_KP_0        -> AWTKeyEvent.VK_NUMPAD0
    GLFW.GLFW_KEY_KP_1        -> AWTKeyEvent.VK_NUMPAD1
    GLFW.GLFW_KEY_KP_2        -> AWTKeyEvent.VK_NUMPAD2
    GLFW.GLFW_KEY_KP_3        -> AWTKeyEvent.VK_NUMPAD3
    GLFW.GLFW_KEY_KP_4        -> AWTKeyEvent.VK_NUMPAD4
    GLFW.GLFW_KEY_KP_5        -> AWTKeyEvent.VK_NUMPAD5
    GLFW.GLFW_KEY_KP_6        -> AWTKeyEvent.VK_NUMPAD6
    GLFW.GLFW_KEY_KP_7        -> AWTKeyEvent.VK_NUMPAD7
    GLFW.GLFW_KEY_KP_8        -> AWTKeyEvent.VK_NUMPAD8
    GLFW.GLFW_KEY_KP_9        -> AWTKeyEvent.VK_NUMPAD9
    GLFW.GLFW_KEY_KP_DECIMAL  -> AWTKeyEvent.VK_DECIMAL
    GLFW.GLFW_KEY_KP_DIVIDE   -> AWTKeyEvent.VK_DIVIDE
    GLFW.GLFW_KEY_KP_MULTIPLY -> AWTKeyEvent.VK_MULTIPLY
    GLFW.GLFW_KEY_KP_SUBTRACT -> AWTKeyEvent.VK_SUBTRACT
    GLFW.GLFW_KEY_KP_ADD      -> AWTKeyEvent.VK_ADD
    GLFW.GLFW_KEY_KP_EQUAL    -> AWTKeyEvent.VK_EQUALS

    // ── 已验证直通区间 ──
    // 以下 GLFW 编码与 AWT VK 常量数值一致，可直接使用：
    //   GLFW_KEY_SPACE (32) == VK_SPACE
    //   GLFW_KEY_COMMA (44) == VK_COMMA
    //   GLFW_KEY_MINUS (45) == VK_MINUS
    //   GLFW_KEY_PERIOD (46) == VK_PERIOD
    //   GLFW_KEY_SLASH (47) == VK_SLASH
    //   GLFW_KEY_0 ~ GLFW_KEY_9 (48~57) == VK_0 ~ VK_9
    //   GLFW_KEY_SEMICOLON (59) == VK_SEMICOLON
    //   GLFW_KEY_EQUAL (61) == VK_EQUALS
    //   GLFW_KEY_A ~ GLFW_KEY_Z (65~90) == VK_A ~ VK_Z
    //   GLFW_KEY_LEFT_BRACKET (91) == VK_OPEN_BRACKET
    //   GLFW_KEY_BACKSLASH (92) == VK_BACK_SLASH
    //   GLFW_KEY_RIGHT_BRACKET (93) == VK_CLOSE_BRACKET
    // 被排除的冲突码：GLFW_KEY_APOSTROPHE (39) ≠ VK_RIGHT (39)，已在上面单独处理
    in GLFW.GLFW_KEY_0..GLFW.GLFW_KEY_9,
    in GLFW.GLFW_KEY_A..GLFW.GLFW_KEY_Z,
    GLFW.GLFW_KEY_SPACE,
    GLFW.GLFW_KEY_COMMA, GLFW.GLFW_KEY_MINUS, GLFW.GLFW_KEY_PERIOD, GLFW.GLFW_KEY_SLASH,
    GLFW.GLFW_KEY_SEMICOLON, GLFW.GLFW_KEY_EQUAL,
    GLFW.GLFW_KEY_LEFT_BRACKET, GLFW.GLFW_KEY_BACKSLASH, GLFW.GLFW_KEY_RIGHT_BRACKET,
        -> glfwKey

    // ── 安全降级 ──
    else -> AWTKeyEvent.VK_UNDEFINED
}

@OptIn(InternalComposeUiApi::class)
internal fun MCKeyEvent.asCompose(type: KeyEventType) =
    KeyEvent(
        key = Key(glfwKeyToAwtKey(key)),
        type = type,
        isCtrlPressed = this.hasControlDown(),
        isMetaPressed = (modifiers and GLFW.GLFW_MOD_SUPER) != 0,
        isShiftPressed = this.hasShiftDown(),
        isAltPressed = this.hasAltDown(),
    )