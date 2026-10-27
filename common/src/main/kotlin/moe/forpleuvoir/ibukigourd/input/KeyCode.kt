package moe.forpleuvoir.ibukigourd.input

import com.mojang.blaze3d.platform.InputConstants
import moe.forpleuvoir.ibukigourd.text.MutableText
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.nebula.common.api.Matchable
import moe.forpleuvoir.nebula.common.util.checkType
import moe.forpleuvoir.nebula.serialization.DeserializationException
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializePrimitive
import moe.forpleuvoir.nebula.serialization.codec.Codec

sealed interface KeyCode : Matchable<Regex> {

    val code: Int

    val keyName: String

    val keyNameText: MutableText

    val translationKey: String

    override fun matched(target: Regex): Boolean {
        return target.containsMatchIn(keyName)
                || target.containsMatchIn(keyNameText.plainText)
                || target.containsMatchIn(translationKey)
                || target.containsMatchIn(code.toString())
    }

    companion object : Codec<KeyCode> {

        val ALL_KEYS by lazy {
            Keyboard.entries.map { it as KeyCode } + MouseButton.entries.map { it as KeyCode }
        }

        /**
         * 按 [InputConstants.Key] 的类型分派到键盘或鼠标命名空间；
         * 两者的取值区间重叠，不能只凭数值判定。
         */
        @JvmStatic
        fun fromKey(key: InputConstants.Key): KeyCode =
            if (key.type == InputConstants.Type.MOUSE) MouseButton.fromCode(key.value) ?: Keyboard.UNKNOWN
            else Keyboard.fromCode(key.value)

        /** 按键盘扫描码解析；鼠标按键请用 [fromKey]。 */
        @JvmStatic
        fun fromCode(code: Int): KeyCode = Keyboard.fromCode(code)

        override fun serialization(target: KeyCode): SerializeElement = SerializePrimitive(target.translationKey)

        override fun deserialization(data: SerializeElement): Result<KeyCode> = DeserializationException.runCatching {
            data.checkType<SerializePrimitive, KeyCode> {
                fromKey(InputConstants.getKey(it.asString!!))
            }
        }
    }

}
