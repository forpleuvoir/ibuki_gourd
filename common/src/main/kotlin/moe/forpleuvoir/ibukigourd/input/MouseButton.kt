package moe.forpleuvoir.ibukigourd.input

import com.mojang.blaze3d.platform.InputConstants
import moe.forpleuvoir.ibukigourd.input.KeyCode.Companion.keyMap
import moe.forpleuvoir.ibukigourd.text.MutableText
import moe.forpleuvoir.ibukigourd.text.Translatable
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.util.math.Vector2f
import net.minecraft.client.Minecraft


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


enum class MouseButton(override val code: Int) : KeyCode {
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
        fun fromCode(code: Int): MouseButton = keyMap[code] as MouseButton
    }

    override val keyNameText: MutableText
        get() = _keyNameText.plainCopy()

    override val keyName: String by lazy { _keyNameText.plainText }

    override val translationKey: String by lazy { InputConstants.Type.MOUSE.getOrCreate(code).name }

    private val _keyNameText: MutableText by lazy {
        when (this) {
            LEFT, RIGHT, MIDDLE -> Translatable(translationKey)
            else                -> Translatable("key.mouse", null, this.code + 1)
        }
    }
}
