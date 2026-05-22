package moe.forpleuvoir.ibukigourd.config.item

import moe.forpleuvoir.nebula.config.ConfigGroup
import moe.forpleuvoir.nebula.config.ConfigSerde
import moe.forpleuvoir.nebula.config.config
import moe.forpleuvoir.nebula.config.item.configList
import moe.forpleuvoir.nebula.serialization.codec.Codec
import moe.forpleuvoir.nebula.serialization.codec.default

fun <A : Any, B : Any> Codec.Companion.pair(
    codecA: Codec<A>,
    codecB: Codec<B>
): Codec<Pair<A, B>> = Codec.create<Pair<A, B>>()
    .field<A>("first").getter(Pair<A, B>::first).codec(codecA)
    .field<B>("second").getter(Pair<A, B>::second).codec(codecB)
    .build { a, b -> a to b }

context(group: ConfigGroup)
fun <A : Any, B : Any> configPair(
    name: String,
    defaultValue: Pair<A, B>,
    serdeA: ConfigSerde<A>,
    serdeB: ConfigSerde<B>
) = config(
    name,
    defaultValue,
    Codec.pair(serdeA.asCodec.default(defaultValue.first), serdeB.asCodec.default(defaultValue.second))
)

context(group: ConfigGroup)
fun configStringPair(
    name: String,
    defaultValue: Pair<String, String>,
) = config(
    name,
    defaultValue,
    Codec.pair(Codec.string(defaultValue.first), Codec.string(defaultValue.second))
)

context(group: ConfigGroup)
fun <A : Any, B : Any> configPairList(
    name: String,
    defaultValue: List<Pair<A, B>>,
    serdeA: ConfigSerde<A>,
    serdeB: ConfigSerde<B>
) = configList(
    name,
    defaultValue,
    Codec.pair(serdeA.asCodec, serdeB.asCodec)
)

