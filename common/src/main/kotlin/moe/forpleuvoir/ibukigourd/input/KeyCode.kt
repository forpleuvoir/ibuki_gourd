@file:Suppress("unused")

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

        internal val keyMap: Map<Int, KeyCode> by lazy {
            buildMap {
                putAll(Mouse.entries.map { it.code to it })
                putAll(Keyboard.entries.map { it.code to it })
            }
        }

        @JvmStatic
        fun fromCode(code: Int): KeyCode = keyMap[code] ?: Keyboard.UNKNOWN

        override fun serialization(target: KeyCode): SerializeElement = SerializePrimitive(target.translationKey)

        override fun deserialization(data: SerializeElement): Result<KeyCode> = DeserializationException.runCatching {
            data.checkType<SerializePrimitive, KeyCode> {
                fromCode(InputConstants.getKey(it.asString!!).value)
            }
        }
    }

}

