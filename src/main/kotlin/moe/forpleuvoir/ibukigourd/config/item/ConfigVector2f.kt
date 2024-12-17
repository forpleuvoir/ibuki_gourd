package moe.forpleuvoir.ibukigourd.config.item

import moe.forpleuvoir.ibukigourd.util.math.Vector2fcDeserializer
import moe.forpleuvoir.ibukigourd.util.math.coerceIn
import moe.forpleuvoir.ibukigourd.util.math.serialization
import moe.forpleuvoir.nebula.config.ConfigBase
import moe.forpleuvoir.nebula.config.ConfigValue
import moe.forpleuvoir.nebula.config.container.ConfigContainer
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import org.joml.Vector2f
import org.joml.Vector2fc

class ConfigVector2f(
    override val key: String,
    override val defaultValue: Vector2fc,
    val minValue: Vector2fc,
    val maxValue: Vector2fc
) : ConfigBase<Vector2fc, ConfigVector2f>(), ConfigValue<Vector2fc> {

    constructor(
        key: String,
        defaultX: Number, defaultY: Number,
        minValue: Vector2fc, maxValue: Vector2fc
    ) : this(key, Vector2f(defaultX.toFloat(), defaultY.toFloat()), minValue, maxValue)

    override var configValue: Vector2fc = defaultValue

    override fun setValue(value: Vector2fc) {
        super.setValue(value.coerceIn(minValue, maxValue))
    }

    override fun deserialization(serializeElement: SerializeElement) {
        setValue(Vector2fcDeserializer.deserialization(serializeElement))
    }

    override fun serialization(): SerializeElement = configValue.serialization()
}

fun ConfigContainer.vector2f(key: String, defaultValue: Vector2fc, minValue: Vector2fc, maxValue: Vector2fc) =
    addConfig(ConfigVector2f(key, defaultValue, minValue, maxValue))

fun ConfigContainer.vector2f(key: String, defaultX: Number, defaultY: Number, minValue: Vector2fc, maxValue: Vector2fc) =
    addConfig(ConfigVector2f(key, defaultX, defaultY, minValue, maxValue))