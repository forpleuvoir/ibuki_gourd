package moe.forpleuvoir.ibukigourd.config.item

import moe.forpleuvoir.ibukigourd.util.math.*
import moe.forpleuvoir.nebula.config.ConfigBase
import moe.forpleuvoir.nebula.config.ConfigValue
import moe.forpleuvoir.nebula.config.container.ConfigContainer
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import org.joml.*

//------------ Vector2i ------------\\

class ConfigVector2i(
    override val key: String,
    override val defaultValue: Vector2ic,
    val minValue: Vector2ic,
    val maxValue: Vector2ic
) : ConfigBase<Vector2ic, ConfigVector2i>(), ConfigValue<Vector2ic> {

    constructor(
        key: String,
        defaultX: Number, defaultY: Number,
        minValue: Vector2ic, maxValue: Vector2ic
    ) : this(key, Vector2i(defaultX.toInt(), defaultY.toInt()), minValue, maxValue)

    override var configValue: Vector2ic = defaultValue

    override fun setValue(value: Vector2ic) {
        super.setValue(value.coerceIn(minValue, maxValue))
    }

    override fun deserialization(serializeElement: SerializeElement) {
        setValue(Vector2icDeserializer.deserialization(serializeElement))
    }

    override fun serialization(): SerializeElement = configValue.serialization()
}

fun ConfigContainer.vector2i(key: String, defaultValue: Vector2ic, minValue: Vector2ic, maxValue: Vector2ic) =
    addConfig(ConfigVector2i(key, defaultValue, minValue, maxValue))

fun ConfigContainer.vector2i(key: String, defaultX: Number, defaultY: Number, minValue: Vector2ic, maxValue: Vector2ic) =
    addConfig(ConfigVector2i(key, defaultX, defaultY, minValue, maxValue))

//------------ Vector2f ------------\\

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

//------------ Vector2d ------------\\

class ConfigVector2d(
    override val key: String,
    override val defaultValue: Vector2dc,
    val minValue: Vector2dc,
    val maxValue: Vector2dc
) : ConfigBase<Vector2dc, ConfigVector2d>(), ConfigValue<Vector2dc> {

    constructor(
        key: String,
        defaultX: Number, defaultY: Number,
        minValue: Vector2dc, maxValue: Vector2dc
    ) : this(key, Vector2d(defaultX.toDouble(), defaultY.toDouble()), minValue, maxValue)

    override var configValue: Vector2dc = defaultValue

    override fun setValue(value: Vector2dc) {
        super.setValue(value.coerceIn(minValue, maxValue))
    }

    override fun deserialization(serializeElement: SerializeElement) {
        setValue(Vector2dcDeserializer.deserialization(serializeElement))
    }

    override fun serialization(): SerializeElement = configValue.serialization()
}

fun ConfigContainer.vector2d(key: String, defaultValue: Vector2dc, minValue: Vector2dc, maxValue: Vector2dc) =
    addConfig(ConfigVector2d(key, defaultValue, minValue, maxValue))

fun ConfigContainer.vector2d(key: String, defaultX: Number, defaultY: Number, minValue: Vector2dc, maxValue: Vector2dc) =
    addConfig(ConfigVector2d(key, defaultX, defaultY, minValue, maxValue))
