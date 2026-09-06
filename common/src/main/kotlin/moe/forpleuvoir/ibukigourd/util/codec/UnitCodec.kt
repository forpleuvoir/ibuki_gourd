package moe.forpleuvoir.ibukigourd.util.codec

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.nebula.common.util.expectedType
import moe.forpleuvoir.nebula.common.util.requireKey
import moe.forpleuvoir.nebula.serialization.DeserializationException
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.base.SerializePrimitive
import moe.forpleuvoir.nebula.serialization.base.builder.build
import moe.forpleuvoir.nebula.serialization.codec.Codec

//region IntSize
fun Codec.Companion.intSize(widthRange: IntRange? = null, heightRange: IntRange? = null) = Codec.create<IntSize>()
    .field(IntSize::width).codec(widthRange?.let { Codec.int(it) } ?: Codec.int)
    .field(IntSize::height).codec(heightRange?.let { Codec.int(it) } ?: Codec.int)
    .build({ w, h -> IntSize(w, h) })

@PublishedApi
internal val intSizeCodec get() = Codec.intSize(null, null)

inline val Codec.Companion.intSize get() = intSizeCodec
//endregion

//region Size
fun Codec.Companion.size(widthRange: ClosedRange<Float>? = null, heightRange: ClosedRange<Float>? = null) = Codec.create<Size>()
    .field(Size::width).codec(widthRange?.let { Codec.float(it) } ?: Codec.float)
    .field(Size::height).codec(heightRange?.let { Codec.float(it) } ?: Codec.float)
    .build({ w, h -> Size(w, h) })

@PublishedApi
internal val sizeCodec get() = Codec.size(null, null)

inline val Codec.Companion.size get() = sizeCodec
//endregion

//region DpSize
fun Codec.Companion.dpSize(widthRange: ClosedRange<Dp>? = null, heightRange: ClosedRange<Dp>? = null) = Codec.create<DpSize>()
    .field(DpSize::width).codec(widthRange?.let { Codec.dp(it) } ?: Codec.dp)
    .field(DpSize::height).codec(heightRange?.let { Codec.dp(it) } ?: Codec.dp)
    .build({ w, h -> DpSize(w, h) })

@PublishedApi
internal val dpSizeCodec get() = Codec.dpSize(null, null)

inline val Codec.Companion.dpSize get() = dpSizeCodec
//endregion

//region Dp
fun Codec.Companion.dp(range: ClosedRange<Dp>? = null) = object : Codec<Dp> {
    private val delegate = range?.let { Codec.float(it.start.value..it.endInclusive.value) } ?: Codec.float
    override fun serialization(target: Dp): SerializeElement = delegate.serialization(target.value)

    override fun deserialization(data: SerializeElement): Result<Dp> = delegate.deserialization(data).map { it.dp }
}

@PublishedApi
internal val dpCodec get() = Codec.dp(null)

inline val Codec.Companion.dp get() = dpCodec
//endregion


//region PaddingValues
fun Codec.Companion.padding(range: ClosedRange<Dp>? = null) = object : Codec<PaddingValues> {

    private val delegate = range?.let { Codec.dp(it) } ?: Codec.dp

    override fun serialization(target: PaddingValues): SerializeElement {
        val start = target.calculateStartPadding(LayoutDirection.Ltr)
        val top = target.calculateTopPadding()
        val end = target.calculateEndPadding(LayoutDirection.Ltr)
        val bottom = target.calculateBottomPadding()
        return when (start) {
            //四边全等：编码为单个数值
            end if end == top && top == bottom ->
                delegate.serialization(start)
            //上下相等且左右相等（四边不全等）：编码为 {horizontal, vertical}
            end if top == bottom               ->
                SerializeObject.build {
                    "horizontal"(start.value)
                    "vertical"(top.value)
                }
            //四边不全等：编码为 {start, top, end, bottom}
            else                               -> SerializeObject.build {
                "start"(start.value)
                "top"(top.value)
                "end"(end.value)
                "bottom"(bottom.value)
            }
        }
    }

    override fun deserialization(data: SerializeElement): Result<PaddingValues> = DeserializationException.runCatching {
        when (data) {
            //单个数值：四边统一
            is SerializePrimitive -> PaddingValues(delegate.deserialization(data).getOrThrow())
            is SerializeObject    -> when {
                //{horizontal, vertical}
                data.containsKey("horizontal", "vertical") -> PaddingValues(
                    horizontal = delegate.deserialization(data.requireKey("horizontal")).getOrThrow(),
                    vertical = delegate.deserialization(data.requireKey("vertical")).getOrThrow(),
                )
                //{start, top, end, bottom}
                else -> PaddingValues(
                    start = delegate.deserialization(data.requireKey("start")).getOrThrow(),
                    top = delegate.deserialization(data.requireKey("top")).getOrThrow(),
                    end = delegate.deserialization(data.requireKey("end")).getOrThrow(),
                    bottom = delegate.deserialization(data.requireKey("bottom")).getOrThrow(),
                )
            }
            else -> throw expectedType(data::class, SerializePrimitive::class, SerializeObject::class)
        }
    }
}

@PublishedApi
internal val paddingCodec get() = Codec.padding(null)

inline val Codec.Companion.padding get() = paddingCodec
//endregion


//region Offset
fun Codec.Companion.offset(xRange: ClosedRange<Float>? = null, yRange: ClosedRange<Float>? = null) = Codec.create<Offset>()
    .field(Offset::x).codec(xRange?.let { Codec.float(it) } ?: Codec.float)
    .field(Offset::y).codec(yRange?.let { Codec.float(it) } ?: Codec.float)
    .build { x, y -> Offset(x, y) }

@PublishedApi
internal val offsetCodec get() = Codec.offset(null, null)

inline val Codec.Companion.offset get() = offsetCodec
//endregion