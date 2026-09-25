package moe.forpleuvoir.ibukigourd.config.item

import moe.forpleuvoir.nebula.config.ConfigGroup
import moe.forpleuvoir.nebula.config.ConfigSerde
import moe.forpleuvoir.nebula.config.item.ConfigEnum
import moe.forpleuvoir.nebula.serialization.codec.Codec

/**
 * 与 nebula 的 `configEnum` 同名同形，区别是**允许指定枚举的 [Codec]**。
 *
 * nebula 的实现固定走 `Codec.enum()`（按 `Enum.name` 序列化，写出 `"Cubic"`、`"In"`）；
 * 需要与资源包 meta、组件枚举一致的小写字面量（`"cubic"`、`"in"`）时用本重载传入枚举自带的
 * [Codec]（本仓的 `EasingCurve` / `EasingDirection` / `EasingPreset` 都是这种写法）。
 */
context(group: ConfigGroup)
fun <E : Enum<E>> configEnum(name: String, default: E, codec: Codec<E>): ConfigEnum<E> =
    group.addConfig(ConfigEnum(name, default, ConfigSerde.of(codec)))
