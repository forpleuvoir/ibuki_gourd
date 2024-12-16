package moe.forpleuvoir.ibukigourd.config.item

import moe.forpleuvoir.ibukigourd.util.math.Vector3fc
import moe.forpleuvoir.ibukigourd.util.math.serialization
import moe.forpleuvoir.nebula.config.ConfigBase
import moe.forpleuvoir.nebula.config.ConfigValue
import moe.forpleuvoir.nebula.config.container.ConfigContainer
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import org.joml.Vector3f

class ConfigVector3f(
    override val key: String,
    override val defaultValue: Vector3f
) : ConfigBase<Vector3f, ConfigVector3f>(), ConfigValue<Vector3f> {

    constructor(key: String, defaultX: Number, defaultY: Number, defaultZ: Number) : this(
        key, Vector3f(defaultX.toFloat(), defaultY.toFloat(), defaultZ.toFloat())
    )

    override var configValue: Vector3f = defaultValue

    override fun deserialization(serializeElement: SerializeElement) {
        setValue(Vector3fc.deserialization(serializeElement) as Vector3f)
    }

    override fun serialization(): SerializeElement = configValue.serialization()
}

fun ConfigContainer.vector3f(key: String, defaultValue: Vector3f) = addConfig(ConfigVector3f(key, defaultValue))