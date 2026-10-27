package moe.forpleuvoir.ibukigourd.input

import com.mojang.blaze3d.platform.InputConstants
import moe.forpleuvoir.ibukigourd.text.MutableText
import moe.forpleuvoir.ibukigourd.text.Translatable
import moe.forpleuvoir.ibukigourd.text.plainText

/**
 * 鼠标按键。`code` 为 [InputConstants.Type.MOUSE] 的取值（左 1 / 中 2 / 右 3，其余自 4 起），
 * 与 `MouseButtonInfo.button()` 的数值同一命名空间。
 */
enum class MouseButton(override val code: Int) : KeyCode {
    LEFT(1),
    RIGHT(3),
    MIDDLE(2),
    BUTTON_4(4),
    BUTTON_5(5),
    BUTTON_6(6),
    BUTTON_7(7),
    BUTTON_8(8);

    companion object {

        internal val keyMap: Map<Int, MouseButton> by lazy { entries.associateBy { it.code } }

        @JvmStatic
        fun fromCode(code: Int): MouseButton? = keyMap[code]
    }

    override val keyNameText: MutableText
        get() = _keyNameText.plainCopy()

    override val keyName: String by lazy { _keyNameText.plainText }

    override val translationKey: String by lazy { InputConstants.Type.MOUSE.getOrCreate(code).name }

    private val _keyNameText: MutableText by lazy {
        when (this) {
            LEFT, RIGHT, MIDDLE -> Translatable(translationKey)
            else                -> Translatable("key.mouse.$code")
        }
    }
}
