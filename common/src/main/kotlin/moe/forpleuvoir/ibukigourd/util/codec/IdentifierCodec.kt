package moe.forpleuvoir.ibukigourd.util.codec

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.nebula.common.util.expectedType
import moe.forpleuvoir.nebula.common.util.requireType
import moe.forpleuvoir.nebula.serialization.DeserializationException
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializePrimitive
import moe.forpleuvoir.nebula.serialization.codec.Codec
import net.minecraft.resources.Identifier

fun Codec.Companion.identifier(defaultNamespace: String? = Identifier.DEFAULT_NAMESPACE): Codec<Identifier> = object : Codec<Identifier> {

    override fun serialization(target: Identifier): SerializeElement = SerializePrimitive(target.toString())

    override fun deserialization(data: SerializeElement): Result<Identifier> = DeserializationException.runCatching {
        val primitive = data.requireType<SerializePrimitive>()
        val str = primitive.asString ?: throw expectedType(primitive.valueType, String::class)
        if (str.contains(':'))
            Identifier.parse(str)
        else
            identifier(defaultNamespace ?: Identifier.DEFAULT_NAMESPACE, str)
    }
}

@PublishedApi
internal val MinecraftIdentifierCodec = Codec.identifier()

inline val Codec.Companion.identifier get() = MinecraftIdentifierCodec

@PublishedApi
internal val IbukigourdIdentifierCodec = Codec.identifier(IbukiGourd.MOD_ID)

inline val Codec.Companion.ibukigourdIdentifier get() = IbukigourdIdentifierCodec
