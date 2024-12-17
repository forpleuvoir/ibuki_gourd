package moe.forpleuvoir.ibukigourd.config.item

import moe.forpleuvoir.ibukigourd.util.math.Vector3fcDeserializer
import moe.forpleuvoir.ibukigourd.util.math.coerceIn
import moe.forpleuvoir.ibukigourd.util.math.serialization
import moe.forpleuvoir.nebula.config.ConfigBase
import moe.forpleuvoir.nebula.config.ConfigValue
import moe.forpleuvoir.nebula.config.container.ConfigContainer
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import org.joml.Vector3f
import org.joml.Vector3fc

class ConfigVector3f(
    override val key: String,
    override val defaultValue: Vector3fc,
    val minValue: Vector3fc,
    val maxValue: Vector3fc
) : ConfigBase<Vector3fc, ConfigVector3f>(), ConfigValue<Vector3fc> {

    constructor(
        key: String,
        defaultX: Number, defaultY: Number, defaultZ: Number,
        minValue: Vector3fc, maxValue: Vector3fc
    ) : this(key, Vector3f(defaultX.toFloat(), defaultY.toFloat(), defaultZ.toFloat()), minValue, maxValue)

    override var configValue: Vector3fc = defaultValue

    override fun setValue(value: Vector3fc) {
        super.setValue(value.coerceIn(minValue, maxValue))
    }

    override fun deserialization(serializeElement: SerializeElement) {
        setValue(Vector3fcDeserializer.deserialization(serializeElement))
    }

    override fun serialization(): SerializeElement = configValue.serialization()
}

fun ConfigContainer.vector3f(key: String, defaultValue: Vector3fc, minValue: Vector3fc, maxValue: Vector3fc) =
    addConfig(ConfigVector3f(key, defaultValue, minValue, maxValue))

fun ConfigContainer.vector3f(key: String, defaultX: Number, defaultY: Number, defaultZ: Number, minValue: Vector3fc, maxValue: Vector3fc) =
    addConfig(ConfigVector3f(key, defaultX, defaultY, defaultZ, minValue, maxValue))