package moe.forpleuvoir.ibukigourd.config.item.impl

import moe.forpleuvoir.nebula.common.api.Matchable
import moe.forpleuvoir.nebula.config.ConfigBase
import moe.forpleuvoir.nebula.config.ConfigValue
import moe.forpleuvoir.nebula.config.container.ConfigContainer
import moe.forpleuvoir.nebula.serialization.Deserializer
import moe.forpleuvoir.nebula.serialization.Serializable
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.extensions.checkType
import moe.forpleuvoir.nebula.serialization.extensions.serializeObject
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

data class DurationObject(
    val duration: Long,
    val unit: DurationUnit
) : Serializable, Matchable {

    companion object : Deserializer<DurationObject> {
        override fun deserialization(serializeElement: SerializeElement): DurationObject {
            return serializeElement.checkType<SerializeObject, DurationObject> {
                DurationObject(
                    it["duration"]!!.asLong,
                    DurationUnit.valueOf(it["unit"]!!.asString)
                )
            }.getOrThrow()
        }
    }

    val value: Duration get() = duration.toDuration(unit)

    override fun serialization(): SerializeElement = serializeObject {
        "duration" to duration
        "unit" to unit.name
    }

    override fun matched(regex: Regex): Boolean =
        regex.containsMatchIn(duration.toString()) || regex.containsMatchIn(unit.name)

}

class ConfigDurationObject(
    override val key: String,
    override val defaultValue: DurationObject
) : ConfigBase<DurationObject, ConfigDurationObject>(), ConfigValue<DurationObject> {

    override var configValue: DurationObject = defaultValue

    val duration: Duration get() = configValue.value

    override fun deserialization(serializeElement: SerializeElement) {
        setValue(DurationObject.deserialization(serializeElement))
    }

    override fun serialization(): SerializeElement {
        return configValue.serialization()
    }

    override fun matched(regex: Regex): Boolean {
        return super.matched(regex) || configValue.matched(regex)
    }

}

fun ConfigContainer.durationObject(key: String, defaultValue: DurationObject) = addConfig(ConfigDurationObject(key, defaultValue))
