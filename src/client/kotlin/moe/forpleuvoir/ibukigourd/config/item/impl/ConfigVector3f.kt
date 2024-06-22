package moe.forpleuvoir.ibukigourd.config.item.impl

import moe.forpleuvoir.ibukigourd.render.math.Vector3f
import moe.forpleuvoir.ibukigourd.render.math.deserialization
import moe.forpleuvoir.ibukigourd.render.math.serialization
import moe.forpleuvoir.nebula.config.ConfigBase
import moe.forpleuvoir.nebula.config.ConfigValue
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import org.joml.Vector3f

class ConfigVector3f(
    override val key: String,
    override val defaultValue: Vector3f
) : ConfigBase<Vector3f, ConfigVector3f>(), ConfigValue<Vector3f> {

    constructor(key: String, defaultX: Number, defaultY: Number, defaultZ: Number) : this(key, Vector3f(defaultX, defaultY, defaultZ))

    override var configValue: Vector3f = defaultValue

    override fun deserialization(serializeElement: SerializeElement) {
        configValue.deserialization(serializeElement)
    }

    override fun serialization(): SerializeElement = configValue.serialization()
}