package moe.forpleuvoir.ibukigourd.config.item

import moe.forpleuvoir.ibukigourd.util.math.*
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.ConfigGroup
import moe.forpleuvoir.nebula.config.ConfigItem
import moe.forpleuvoir.nebula.config.ConfigSerde
import moe.forpleuvoir.nebula.serialization.codec.Codec
import moe.forpleuvoir.nebula.serialization.codec.default
import org.joml.*

private class ConfigVector<T : Any>(
    name: String,
    defaultValue: T,
    private val clamp: (T) -> T,
    serde: ConfigSerde<T>
) : ConfigItem<T>(name, defaultValue, serde) {

    init {
        require(clamp(defaultValue) valueEquals defaultValue) { "Default value out of clamp range, please adjust the default value or clamp bounds" }
    }

    override fun setValue(value: T) {
        super.setValue(clamp(value))
    }
}

context(group: ConfigGroup)
private fun <T : Any> vector(
    name: String,
    defaultValue: T,
    clamp: (T) -> T,
    serde: ConfigSerde<T>
): Config<T> = group.addConfig(ConfigVector(name, defaultValue, clamp, serde))

//region Vector2i
context(group: ConfigGroup)
fun configVector2i(
    name: String,
    defaultValue: Vector2ic,
    minValue: Vector2ic,
    maxValue: Vector2ic
): Config<Vector2ic> = vector(
    name,
    defaultValue,
    { it.coerceIn(minValue, maxValue) },
    ConfigSerde.of(Codec.vector2ic(minValue, maxValue).default(defaultValue))
)
//endregion

//region Vector2f
context(group: ConfigGroup)
fun configVector2f(
    name: String,
    defaultValue: Vector2fc,
    minValue: Vector2fc,
    maxValue: Vector2fc
): Config<Vector2fc> = vector(
    name,
    defaultValue,
    { it.coerceIn(minValue, maxValue) },
    ConfigSerde.of(Codec.vector2fc(minValue, maxValue).default(defaultValue))
)
//endregion

//region Vector2d
context(group: ConfigGroup)
fun configVector2d(
    name: String,
    defaultValue: Vector2dc,
    minValue: Vector2dc,
    maxValue: Vector2dc
): Config<Vector2dc> = vector(
    name,
    defaultValue,
    { it.coerceIn(minValue, maxValue) },
    ConfigSerde.of(Codec.vector2dc(minValue, maxValue).default(defaultValue))
)
//endregion

//region Vector3i
context(group: ConfigGroup)
fun configVector3i(
    name: String,
    defaultValue: Vector3ic,
    minValue: Vector3ic,
    maxValue: Vector3ic
): Config<Vector3ic> = vector(
    name,
    defaultValue,
    { it.coerceIn(minValue, maxValue) },
    ConfigSerde.of(Codec.vector3ic(minValue, maxValue).default(defaultValue))
)
//endregion

//region Vector3f
context(group: ConfigGroup)
fun configVector3f(
    name: String,
    defaultValue: Vector3fc,
    minValue: Vector3fc,
    maxValue: Vector3fc
): Config<Vector3fc> = vector(
    name,
    defaultValue,
    { it.coerceIn(minValue, maxValue) },
    ConfigSerde.of(Codec.vector3fc(minValue, maxValue).default(defaultValue))
)
//endregion

//region Vector3d
context(group: ConfigGroup)
fun configVector3d(
    name: String,
    defaultValue: Vector3dc,
    minValue: Vector3dc,
    maxValue: Vector3dc
): Config<Vector3dc> = vector(
    name,
    defaultValue,
    { it.coerceIn(minValue, maxValue) },
    ConfigSerde.of(Codec.vector3dc(minValue, maxValue).default(defaultValue))
)
//endregion