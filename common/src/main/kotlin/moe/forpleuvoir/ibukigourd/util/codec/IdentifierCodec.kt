package moe.forpleuvoir.ibukigourd.util.codec

import moe.forpleuvoir.nebula.common.util.expectedType
import moe.forpleuvoir.nebula.common.util.requireType
import moe.forpleuvoir.nebula.serialization.DeserializationException
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializePrimitive
import moe.forpleuvoir.nebula.serialization.codec.Codec
import net.minecraft.resources.Identifier

object IdentifierCodec : Codec<Identifier> {
    override fun serialization(target: Identifier): SerializeElement = SerializePrimitive(target.toString())

    override fun deserialization(data: SerializeElement): Result<Identifier> = DeserializationException.runCatching {
        Identifier.parse(data.requireType<SerializePrimitive>().let {
            it.asString ?: throw expectedType(it.valueType, String::class)
        })
    }
}

inline val Codec.Companion.identifier get() = IdentifierCodec