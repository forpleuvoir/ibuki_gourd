package moe.forpleuvoir.ibukigourd.input

import com.mojang.blaze3d.platform.InputConstants
import moe.forpleuvoir.ibukigourd.input.KeyCode.Companion.keyMap
import moe.forpleuvoir.ibukigourd.text.MutableText
import moe.forpleuvoir.ibukigourd.text.Translatable
import moe.forpleuvoir.ibukigourd.text.plainText

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
