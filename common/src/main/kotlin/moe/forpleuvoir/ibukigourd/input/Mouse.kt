package moe.forpleuvoir.ibukigourd.input

import com.mojang.blaze3d.platform.InputConstants
import moe.forpleuvoir.ibukigourd.input.KeyCode.Companion.keyMap
import moe.forpleuvoir.ibukigourd.text.MutableText
import moe.forpleuvoir.ibukigourd.text.Translatable
import moe.forpleuvoir.ibukigourd.util.math.Vector2f
import moe.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW.*


val Minecraft.mouseX: Float get() = mouseHandler.getScaledXPos(window).toFloat()

val Minecraft.mouseY: Float get() = mouseHandler.getScaledYPos(window).toFloat()

val Minecraft.mousePosition: MousePosition
    get() = MousePosition(mouseX, mouseY)

interface MousePosition {

    companion object {

        operator fun invoke(x: Number, y: Number) =
            object : MousePosition {
                override val x: Float
                    get() = x.toFloat()
                override val y: Float
                    get() = y.toFloat()
            }
    }

    val x: Float
    val y: Float

    operator fun component1(): Float = x

    operator fun component2(): Float = y

    val asVector2fc get() = Vector2f(x, y)
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

    companion object {
        @JvmStatic
        fun fromCode(code: Int): Mouse = keyMap[code] as Mouse
    }

    override val translationKey: String
        get() = InputConstants.Type.MOUSE.getOrCreate(code).name

    override val keyNameText: MutableText
        get() = when (this) {
            LEFT, RIGHT, MIDDLE -> Translatable(translationKey)
            else -> Translatable("key.mouse", null, this.code + 1)
        }
}

enum class MouseCursor(val value: Int) {
    ARROW_CURSOR(GLFW_ARROW_CURSOR),
    IBEAM_CURSOR(GLFW_IBEAM_CURSOR),
    CROSSHAIR_CURSOR(GLFW_CROSSHAIR_CURSOR),
    POINTING_HAND_CURSOR(GLFW_POINTING_HAND_CURSOR),
    RESIZE_EW_CURSOR(GLFW_RESIZE_EW_CURSOR),
    RESIZE_NS_CURSOR(GLFW_RESIZE_NS_CURSOR),
    RESIZE_NWSE_CURSOR(GLFW_RESIZE_NWSE_CURSOR),
    RESIZE_NESW_CURSOR(GLFW_RESIZE_NESW_CURSOR),
    RESIZE_ALL_CURSOR(GLFW_RESIZE_ALL_CURSOR),
    NOT_ALLOWED_CURSOR(GLFW_NOT_ALLOWED_CURSOR);

    companion object {

        val default = ARROW_CURSOR

        fun clear() {
            current = default
        }

        var current: MouseCursor = default
            set(value) {
                if (value == field) return
                field = value
                glfwSetCursor(mc.window.window, glfwCreateStandardCursor(value.value))
            }
    }

}

fun interface MouseCursorMapping<T : Any> {
    operator fun invoke(input: T): MouseCursor
}