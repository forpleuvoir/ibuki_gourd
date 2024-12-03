package moe.forpleuvoir.ibukigourd.config.item

import moe.forpleuvoir.ibukigourd.input.KeyBind
import moe.forpleuvoir.nebula.common.api.Matchable
import moe.forpleuvoir.nebula.config.ConfigValue
import moe.forpleuvoir.nebula.serialization.Deserializable
import moe.forpleuvoir.nebula.serialization.Serializable
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.extensions.checkType
import moe.forpleuvoir.nebula.serialization.extensions.serializeObject

interface ConfigKeyBindValue : ConfigValue<KeyBind>

interface ConfigKeyBindBooleanValue : ConfigValue<KeyBindWithBoolean>

data class KeyBindWithBoolean(
    val keyBind: KeyBind,
    var value: Boolean
) : Serializable, Deserializable, Matchable {

    override fun serialization(): SerializeElement = serializeObject {
        "key_bind" to keyBind
        "value" to value
    }

    override fun deserialization(serializeElement: SerializeElement) {
        serializeElement.checkType<Unit> {
            check<SerializeObject> {
                keyBind.deserialization(it["key_bind"]!!)
                value = it["value"]!!.asBoolean
            }
        }.getOrThrow()
    }

    override fun matched(regex: Regex): Boolean =
        keyBind matched regex || regex.containsMatchIn(value.toString())


}