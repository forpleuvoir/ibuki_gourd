package moe.forpleuvoir.ibukigourd.config.item

import moe.forpleuvoir.ibukigourd.util.math.*
import moe.forpleuvoir.nebula.config.ConfigBase
import moe.forpleuvoir.nebula.config.ConfigValue
import moe.forpleuvoir.nebula.config.container.ConfigContainer
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import org.joml.*

//------------ Vector3i ------------\\

class ConfigVector3i(
    override val key: String,
    override val defaultValue: Vector3ic,
    val minValue: Vector3ic,
    val maxValue: Vector3ic
) : ConfigBase<Vector3ic, ConfigVector3i>(), ConfigValue<Vector3ic> {

    constructor(
        key: String,
        defaultX: Number, defaultY: Number, defaultZ: Number,
        minValue: Vector3ic, maxValue: Vector3ic
    ) : this(key, Vector3i(defaultX.toInt(), defaultY.toInt(), defaultZ.toInt()), minValue, maxValue)

    override var configValue: Vector3ic = defaultValue

    override fun setValue(value: Vector3ic) {
        super.setValue(value.coerceIn(minValue, maxValue))
    }

    override fun deserialization(serializeElement: SerializeElement) {
        setValue(Vector3icDeserializer.deserialization(serializeElement))
    }

    override fun serialization(): SerializeElement = configValue.serialization()
}

fun ConfigContainer.vector3i(key: String, defaultValue: Vector3ic, minValue: Vector3ic, maxValue: Vector3ic) =
    addConfig(ConfigVector3i(key, defaultValue, minValue, maxValue))

fun ConfigContainer.vector3i(key: String, defaultX: Number, defaultY: Number, defaultZ: Number, minValue: Vector3ic, maxValue: Vector3ic) =
    addConfig(ConfigVector3i(key, defaultX, defaultY, defaultZ, minValue, maxValue))


//------------ Vector3f ------------\\

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

//------------ Vector3d ------------\\

class ConfigVector3d(
    override val key: String,
    override val defaultValue: Vector3dc,
    val minValue: Vector3dc,
    val maxValue: Vector3dc
) : ConfigBase<Vector3dc, ConfigVector3d>(), ConfigValue<Vector3dc> {

    constructor(
        key: String,
        defaultX: Number, defaultY: Number, defaultZ: Number,
        minValue: Vector3dc, maxValue: Vector3dc
    ) : this(key, Vector3d(defaultX.toDouble(), defaultY.toDouble(), defaultZ.toDouble()), minValue, maxValue)

    override var configValue: Vector3dc = defaultValue

    override fun setValue(value: Vector3dc) {
        super.setValue(value.coerceIn(minValue, maxValue))
    }

    override fun deserialization(serializeElement: SerializeElement) {
        setValue(Vector3dcDeserializer.deserialization(serializeElement))
    }

    override fun serialization(): SerializeElement = configValue.serialization()
}

fun ConfigContainer.vector3d(key: String, defaultValue: Vector3dc, minValue: Vector3dc, maxValue: Vector3dc) =
    addConfig(ConfigVector3d(key, defaultValue, minValue, maxValue))

fun ConfigContainer.vector3d(key: String, defaultX: Number, defaultY: Number, defaultZ: Number, minValue: Vector3dc, maxValue: Vector3dc) =
    addConfig(ConfigVector3d(key, defaultX, defaultY, defaultZ, minValue, maxValue))
