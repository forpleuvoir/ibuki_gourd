package moe.forpleuvoir.ibukigourd.config.item

import moe.forpleuvoir.nebula.config.container.ConfigContainer
import moe.forpleuvoir.nebula.config.item.impl.ConfigList
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.base.SerializePrimitive
import moe.forpleuvoir.nebula.serialization.extensions.checkType
import moe.forpleuvoir.nebula.serialization.extensions.serializeArray
import moe.forpleuvoir.nebula.serialization.extensions.serializeObject

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
        serializeObject {
            "first" to it.first
            "second" to it.second
        }
    },
    deserializer = {
        it.checkType<Pair<A, B>> {
            check<SerializeObject> {
                aDeserializer(it["first"]!!) to bDeserializer(it["second"]!!)
            }
        }.getOrThrow()
    }
)

fun ConfigContainer.stringPairList(key: String, defaultValue: List<Pair<String, String>>): ConfigPairList<String, String> =
    addConfig(ConfigPairList(key, defaultValue, { SerializePrimitive(it) }, { it.asString }, { SerializePrimitive(it) }, { it.asString }))
