package moe.forpleuvoir.ibukigourd.config.item

import moe.forpleuvoir.nebula.config.ConfigBase
import moe.forpleuvoir.nebula.config.ConfigValue
import moe.forpleuvoir.nebula.config.container.ConfigContainer
import moe.forpleuvoir.nebula.serialization.base.SerializeArray
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializePrimitive
import moe.forpleuvoir.nebula.serialization.extensions.checkType
import moe.forpleuvoir.nebula.serialization.extensions.serializeArray

class ConfigPair<A, B>(
    override val key: String,
    override val defaultValue: Pair<A, B>,
    private val aSerializer: (A) -> SerializeElement,
    private val aDeserializer: (SerializeElement) -> A,
    private val bSerializer: (B) -> SerializeElement,
    private val bDeserializer: (SerializeElement) -> B
) : ConfigBase<Pair<A, B>, ConfigPair<A, B>>(), ConfigValue<Pair<A, B>> {

    override var configValue: Pair<A, B> = defaultValue

    override fun serialization(): SerializeElement = serializeArray(aSerializer(configValue.first), bSerializer(configValue.second))

    override fun deserialization(serializeElement: SerializeElement) {
        serializeElement.checkType<Pair<A, B>> {
            check<SerializeArray> {
                aDeserializer(it[0]) to bDeserializer(it[1])
            }
        }.getOrThrow().let { setValue(it) }
    }
}

fun ConfigContainer.stringPair(key: String, defaultValue: Pair<String, String>): ConfigPair<String, String> =
    addConfig(ConfigPair(key, defaultValue, { SerializePrimitive(it) }, { it.asString }, { SerializePrimitive(it) }, { it.asString }))
