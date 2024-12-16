package moe.forpleuvoir.ibukigourd.config.item

import moe.forpleuvoir.nebula.config.container.ConfigContainer
import moe.forpleuvoir.nebula.config.item.impl.ConfigList
import moe.forpleuvoir.nebula.serialization.base.SerializeArray
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializePrimitive
import moe.forpleuvoir.nebula.serialization.extensions.checkType
import moe.forpleuvoir.nebula.serialization.extensions.serializeArray

class ConfigPairList<A, B>(
    override val key: String,
    defaultValue: List<Pair<A, B>>,
    private val aSerializer: (A) -> SerializeElement,
    private val aDeserializer: (SerializeElement) -> A,
    private val bSerializer: (B) -> SerializeElement,
    private val bDeserializer: (SerializeElement) -> B
) : ConfigList<Pair<A, B>>(
    key, defaultValue,
    serializer = {
        serializeArray(aSerializer(it.first), bSerializer(it.second))
    },
    deserializer = {
        it.checkType<Pair<A, B>> {
            check<SerializeArray> {
                aDeserializer(it[0]) to bDeserializer(it[1])
            }
        }.getOrThrow()
    }
)

fun ConfigContainer.stringPairList(key: String, defaultValue: List<Pair<String, String>>): ConfigPairList<String, String> =
    addConfig(ConfigPairList(key, defaultValue, { SerializePrimitive(it) }, { it.asString }, { SerializePrimitive(it) }, { it.asString }))
