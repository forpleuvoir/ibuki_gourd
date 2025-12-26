package moe.forpleuvoir.ibukigourd.util

import com.google.common.collect.ImmutableMap
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.MapLike
import com.mojang.serialization.RecordBuilder
import it.unimi.dsi.fastutil.bytes.ByteArrayList
import moe.forpleuvoir.nebula.serialization.base.SerializeArray
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeNull
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.base.SerializePrimitive
import moe.forpleuvoir.nebula.serialization.extensions.serializeArray
import moe.forpleuvoir.nebula.serialization.extensions.serializeObject
import java.nio.ByteBuffer
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.stream.IntStream
import java.util.stream.LongStream
import java.util.stream.Stream
import kotlin.collections.all
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach
import kotlin.collections.map

/**
 * Not fully tested
 */
object NebulaOps : DynamicOps<SerializeElement> {

    override fun empty(): SerializeElement = SerializeNull

    override fun emptyMap(): SerializeElement = SerializeObject()

    override fun emptyList(): SerializeElement = SerializeArray()

    override fun <U> convertTo(outOps: DynamicOps<U>, input: SerializeElement): U {
        return when (input) {
            is SerializeNull      -> outOps.empty()
            is SerializeObject    -> convertMap(outOps, input)
            is SerializeArray     -> convertList(outOps, input)
            is SerializePrimitive -> {
                if (input.isInt) outOps.createInt(input.asInt)
                else if (input.isByte) outOps.createByte(input.asByte)
                else if (input.isLong) outOps.createLong(input.asLong)
                else if (input.isString) outOps.createShort(input.asShort)
                else if (input.isFloat) outOps.createFloat(input.asFloat)
                else if (input.isDouble) outOps.createDouble(input.asDouble)
                else if (input.isString) outOps.createString(input.asString)
                else if (input.isBoolean) outOps.createBoolean(input.asBoolean)
                else if (input.isNumber) outOps.createNumeric(input.asNumber)
                else throw IllegalStateException("Don't know how to convert $input")
            }
        }
    }

    override fun getNumberValue(input: SerializeElement?): DataResult<Number?>? {
        if (input is SerializePrimitive && input.isNumber) {
            return DataResult.success(input.asNumber)
        }
        return DataResult.error { "Not a number: $input" }
    }

    override fun get(
        input: SerializeElement?,
        key: String?
    ): DataResult<SerializeElement?>? {
        return super.get(input, key)
    }

    override fun createNumeric(value: Number): SerializeElement = SerializePrimitive(value)

    override fun createByte(value: Byte): SerializeElement = SerializePrimitive(value)

    override fun createShort(value: Short): SerializeElement = SerializePrimitive(value)

    override fun createInt(value: Int): SerializeElement = SerializePrimitive(value)

    override fun createLong(value: Long): SerializeElement = SerializePrimitive(value)

    override fun createFloat(value: Float): SerializeElement = SerializePrimitive(value)

    override fun createDouble(value: Double): SerializeElement = SerializePrimitive(value)

    override fun getBooleanValue(input: SerializeElement?): DataResult<Boolean> {
        if (input is SerializePrimitive && input.isBoolean) {
            return DataResult.success(input.asBoolean)
        }
        return DataResult.error { "Not a boolean: $input" }
    }

    override fun createBoolean(value: Boolean): SerializeElement = SerializePrimitive(value)

    override fun getStringValue(input: SerializeElement?): DataResult<String> {
        if (input is SerializePrimitive && input.isString) {
            return DataResult.success(input.asString)
        }
        return DataResult.error { "Not a string: $input" }
    }

    override fun createString(value: String): SerializeElement = SerializePrimitive(value)

    override fun mergeToList(input: SerializeElement, value: SerializeElement): DataResult<SerializeElement> {
        if (input == empty()) {
            return DataResult.success(SerializeArray(value))
        }
        if (input is SerializeArray) {
            if (input.isEmpty()) {
                return DataResult.success(SerializeArray(value))
            }
            return DataResult.success(serializeArray {
                addAll(input)
                add(value)
            })
        }
        return DataResult.error { "Not a list: $input" }
    }

    override fun mergeToList(input: SerializeElement, values: List<SerializeElement>): DataResult<SerializeElement> {
        if (input == empty()) {
            return DataResult.success(SerializeArray(*values.toTypedArray()))
        }
        if (input is SerializeArray) {
            if (values.isEmpty()) {
                return DataResult.success(input)
            }
            if (input.isEmpty()) {
                return DataResult.success(SerializeArray(SerializeArray(*values.toTypedArray())))
            }
            return DataResult.success(serializeArray {
                addAll(input)
                addAll(values)
            })
        }
        return DataResult.error { "Not a list: $input" }
    }

    override fun mergeToMap(input: SerializeElement, key: SerializeElement, value: SerializeElement): DataResult<SerializeElement> {
        if (input == empty()) {
            return DataResult.success(serializeObject { key.asString to value })
        }
        if (input is SerializeObject) {
            if (input.isEmpty()) {
                return DataResult.success(serializeObject { key.asString to value })
            }
            serializeObject {
                input.forEach { (key, value) ->
                    key to value
                }
                key.asString to value
            }.let {
                return DataResult.success(it)
            }
        }
        return DataResult.error { "Not a map: $input" }
    }

    override fun mergeToMap(input: SerializeElement, values: Map<SerializeElement, SerializeElement>): DataResult<SerializeElement> {
        if (input == empty()) {
            return DataResult.success(serializeObject {
                values.forEach { (key, value) -> key.asString to value }
            })
        }
        if (input is SerializeObject) {
            if (values.isEmpty()) {
                return DataResult.success(input)
            }
            if (input.isEmpty()) {
                return DataResult.success(serializeObject {
                    values.forEach { (key, value) -> key.asString to value }
                })
            }
            serializeObject {
                input.forEach { (key, value) -> key to value }
                values.forEach { (key, value) -> key.asString to value }
            }.let {
                return DataResult.success(it)
            }
        }
        return DataResult.error { "Not a map: $input" }
    }

    private fun MapLike<SerializeElement>.toObject() = serializeObject {
        entries().forEach { it.first.asString to it.second }
    }


    override fun mergeToMap(input: SerializeElement, values: MapLike<SerializeElement>): DataResult<SerializeElement> {
        if (input == empty()) {
            return DataResult.success(values.toObject())
        }
        if (input is SerializeObject) {
            if (input.isEmpty()) {
                return DataResult.success(values.toObject())
            }
            val valuesIterator = values.entries().iterator()
            if (!valuesIterator.hasNext()) {
                return DataResult.success(input)
            }
            serializeObject {
                input.forEach { (string, element) -> string to element }
                valuesIterator.forEachRemaining { it.first to it.second }
            }
        }
        return DataResult.error { "Not a map: $input" }
    }

    private fun SerializeObject.getMapEntries(): Stream<Pair<SerializeElement, SerializeElement>> = entries.stream().map {
        Pair.of(SerializePrimitive(it.key), it.value)
    }

    override fun getMapValues(input: SerializeElement): DataResult<Stream<Pair<SerializeElement, SerializeElement>>> {
        if (input is SerializeObject) {
            return DataResult.success(input.getMapEntries())
        }
        return DataResult.error { "Not a map: $input" }
    }

    override fun getMapEntries(input: SerializeElement): DataResult<Consumer<BiConsumer<SerializeElement, SerializeElement>>> {
        if (input is SerializeObject) {
            return DataResult.success(Consumer {
                input.forEach { (k, v) -> it.accept(createString(k), v) }
            })
        }
        return DataResult.error { "Not a map: $input" }
    }

    override fun createMap(map: Stream<Pair<SerializeElement, SerializeElement>>): SerializeElement {
        return serializeObject {
            map.forEach {
                it.first.asString to it.second
            }
        }
    }

    override fun getMap(input: SerializeElement?): DataResult<MapLike<SerializeElement>> {
        if (input is SerializeObject) {
            return DataResult.success(object : MapLike<SerializeElement> {
                override fun get(key: SerializeElement): SerializeElement? = input[key.asString]
                override fun get(key: String): SerializeElement? = input[key]
                override fun entries(): Stream<Pair<SerializeElement, SerializeElement>> = input.getMapEntries()
                override fun toString(): String = "MapLike[$input]"
            })
        }
        return DataResult.error { "Not a map: $input" }
    }

    override fun createMap(map: Map<SerializeElement, SerializeElement>): SerializeElement {
        return serializeObject {
            map.forEach { (k, v) -> k.asString to v }
        }
    }

    override fun getStream(input: SerializeElement): DataResult<Stream<SerializeElement>> {
        if (input is SerializeArray) {
            return DataResult.success(input.stream())
        }
        return DataResult.error { "Not a list: $input" }
    }

    override fun getList(input: SerializeElement): DataResult<Consumer<Consumer<SerializeElement>>> {
        if (input is SerializeArray) {
            return DataResult.success(Consumer { c ->
                input.forEach { c.accept(it) }
            })
        }
        return DataResult.error { "Not a list: $input" }
    }


    override fun createList(input: Stream<SerializeElement>): SerializeElement {
        return serializeArray(input.iterator())
    }

    override fun getByteBuffer(input: SerializeElement?): DataResult<ByteBuffer?>? {
        if (input is SerializeArray && input.all { it is SerializePrimitive && it.isByte }) {
            return DataResult.success(ByteBuffer.wrap(input.map { it.asByte }.toByteArray()))
        }
        return DataResult.error { "Not a byte list: $input" }
    }

    override fun createByteList(input: ByteBuffer): SerializeElement {
        val wholeBuff = input.duplicate().clear()
        val result = ByteArrayList()
        result.size(wholeBuff.capacity())
        wholeBuff.get(0, result.elements(), 0, result.size)
        return serializeArray { result.elements().forEach { add(createByte(it)) } }
    }

    override fun getIntStream(input: SerializeElement): DataResult<IntStream> {
        if (input is SerializeArray && input.all { it is SerializePrimitive && it.isInt }) {
            val builder = IntStream.builder().apply {
                input.forEach { this.add(it.asInt) }
            }
            return DataResult.success(builder.build())
        }
        return DataResult.error { "Not a list: $input" }
    }

    override fun createIntList(input: IntStream): SerializeElement = serializeArray { input.forEach { add(createInt(it)) } }

    override fun getLongStream(input: SerializeElement?): DataResult<LongStream?>? {
        if (input is SerializeArray && input.all { it is SerializePrimitive && it.isLong }) {
            val builder = LongStream.builder().apply {
                input.forEach {
                    this.add(it.asLong)
                }
            }
            return DataResult.success(builder.build())
        }
        return DataResult.error { "Not a list: $input" }
    }

    override fun createLongList(input: LongStream): SerializeElement = serializeArray { input.forEach { add(createLong(it)) } }

    override fun remove(input: SerializeElement, key: String): SerializeElement {
        if (input is SerializeObject) {
            val result = input.deepCopy()
            result.remove(key)
            return result
        }
        return input
    }

    override fun mapBuilder(): RecordBuilder<SerializeElement> {
        return FixedMapBuilder(this)
    }

    override fun toString(): String = "Nebula"

    private class FixedMapBuilder(ops: DynamicOps<SerializeElement>) :
        RecordBuilder.AbstractUniversalBuilder<SerializeElement, ImmutableMap.Builder<SerializeElement, SerializeElement>>(ops) {

        override fun initBuilder(): ImmutableMap.Builder<SerializeElement, SerializeElement>? {
            return ImmutableMap.builder()
        }

        override fun append(
            key: SerializeElement,
            value: SerializeElement,
            builder: ImmutableMap.Builder<SerializeElement, SerializeElement>
        ): ImmutableMap.Builder<SerializeElement, SerializeElement> {
            return builder.put(key, value)
        }

        override fun build(
            builder: ImmutableMap.Builder<SerializeElement, SerializeElement>,
            prefix: SerializeElement
        ): DataResult<SerializeElement> {
            val result = builder.buildKeepingLast()
            return ops().mergeToMap(prefix, result)
        }

    }

}