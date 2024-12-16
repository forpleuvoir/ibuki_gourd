package moe.forpleuvoir.ibukigourd.config.item

import moe.forpleuvoir.ibukigourd.util.math.Vector2fc
import moe.forpleuvoir.ibukigourd.util.math.serialization
import moe.forpleuvoir.nebula.config.ConfigBase
import moe.forpleuvoir.nebula.config.ConfigValue
import moe.forpleuvoir.nebula.config.container.ConfigContainer
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import org.joml.Vector2f

class ConfigVector2f(
    override val key: String,
    override val defaultValue: Vector2f
) : ConfigBase<Vector2f, ConfigVector2f>(), ConfigValue<Vector2f> {

    constructor(key: String, defaultX: Number, defaultY: Number) : this(key, Vector2f(defaultX.toFloat(), defaultY.toFloat()))

    override var configValue: Vector2f = defaultValue

    override fun deserialization(serializeElement: SerializeElement) {
        setValue(Vector2fc.deserialization(serializeElement) as Vector2f)
    }

    override fun serialization(): SerializeElement = configValue.serialization()
}

fun ConfigContainer.vector2f(key: String, defaultValue: Vector2f) = addConfig(ConfigVector2f(key, defaultValue))