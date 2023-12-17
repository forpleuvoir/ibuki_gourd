package moe.forpleuvoir.ibukigourd.input

import moe.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW.*


val MinecraftClient.mouseX: Float get() = mouse.x.toFloat() * window.scaledWidth / window.width

val MinecraftClient.mouseY: Float get() = mouse.y.toFloat() * window.scaledHeight / window.height

val MinecraftClient.mousePosition: MousePosition
    get() = object : MousePosition {
        override val x: Float
            get() = this@mousePosition.mouseX
        override val y: Float
            get() = this@mousePosition.mouseY

    }

interface MousePosition {
    val x: Float
    val y: Float
}

fun MousePosition.equals(x: Float, y: Float): Boolean = this.x == x && this.y == y

infix fun MousePosition.equals(mousePosition: MousePosition): Boolean = this.x == mousePosition.x && this.y == mousePosition.y

infix fun MousePosition.notEquals(mousePosition: MousePosition): Boolean = !(this equals mousePosition)

val MousePosition.asString: String get() = "($x, $y)"


enum class Mouse(override val code: Int) : KeyCode {
    LEFT(0),
    RIGHT(1),
    MIDDLE(2),
    BUTTON_4(3),
    BUTTON_5(4),
    BUTTON_6(5),
    BUTTON_7(6),
    BUTTON_8(7);

    override val translationKey: String
        get() = InputUtil.Type.MOUSE.createFromCode(code).translationKey
}

object MouseCursor {

    var current: Cursor = Cursor.ARROW_CURSOR
        set(value) {
            if (value == field) return
            field = value
            glfwSetCursor(mc.window.handle, glfwCreateStandardCursor(value.value))
        }

    enum class Cursor(val value: Int) {
        ARROW_CURSOR(GLFW_ARROW_CURSOR),
        IBEAM_CURSOR(GLFW_IBEAM_CURSOR),
        CROSSHAIR_CURSOR(GLFW_CROSSHAIR_CURSOR),
        POINTING_HAND_CURSOR(GLFW_POINTING_HAND_CURSOR),
        RESIZE_EW_CURSOR(GLFW_RESIZE_EW_CURSOR),
        RESIZE_NS_CURSOR(GLFW_RESIZE_NS_CURSOR),
        RESIZE_NWSE_CURSOR(GLFW_RESIZE_NWSE_CURSOR),
        RESIZE_NESW_CURSOR(GLFW_RESIZE_NESW_CURSOR),
        RESIZE_ALL_CURSOR(GLFW_RESIZE_ALL_CURSOR),
        NOT_ALLOWED_CURSOR(GLFW_NOT_ALLOWED_CURSOR),
    }

}

