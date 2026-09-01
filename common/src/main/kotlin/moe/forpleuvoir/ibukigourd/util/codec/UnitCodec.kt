package moe.forpleuvoir.ibukigourd.util.codec

import androidx.compose.ui.unit.IntSize
import moe.forpleuvoir.nebula.serialization.codec.Codec

//region IntSize
fun Codec.Companion.intSize(widthRange: IntRange? = null, heightRange: IntRange? = null) = Codec.create<IntSize>()
    .field<Int>("width").getter(IntSize::width).codec(widthRange?.let { Codec.int(it) } ?: Codec.int)
    .field<Int>("height").getter(IntSize::height).codec(heightRange?.let { Codec.int(it) } ?: Codec.int)
    .build({ w, h -> IntSize(w, h) })

private val intSizeCodec get() = Codec.intSize(null, null)

val Codec.Companion.intSize get() = intSizeCodec
//endregion