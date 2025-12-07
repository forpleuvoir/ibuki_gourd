package moe.forpleuvoir.ibukigourd.util.state

import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.HSVColor
import moe.forpleuvoir.nebula.common.util.primitive.pick
import java.util.function.Consumer
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

val <T> T.asMutableState: MutableState<T> get() = mutableStateOf(this)

val <T> (() -> T).asMutableState: MutableState<T> get() = mutableStateBy(this)

val <T> KMutableProperty0<T>.asMutableState: MutableState<T> get() = mutableStateOf(this)

fun <T> mutableStateOf(value: T) = MutableState(value)

fun <T> mutableStateBy(value: () -> T) = MutableState(value()).apply { onGetValue = { value() } }

fun <T> mutableStateOf(value: KMutableProperty0<T>) =
    MutableState(value.get()).apply {
        subscribe {
            value.set(it)
        }
    }

fun <A, B> mutableStateOf(state: State<B>, map: (B) -> A): MutableState<A> =
    mutableStateOf(map(state.getValue())).apply { bind(state, map) }

fun <A, B> mutableStateOf(state: MutableState<B>, mapA: (B) -> A, mapB: (A) -> B): MutableState<A> =
    mutableStateOf(mapA(state.getValue())).apply {
        MutableState.bind(this, state, mapB, mapA)
    }

data class MutableState<T>(private var value: T) : State<T> {

    var onSetValue: (T) -> T = { it }

    var onGetValue: (T) -> T = { it }

    private var currentValue: T = onGetValue(value)

    override var enableNotification: Boolean = true

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (this.value == value) return
        val oldValue = this.value
        this.value = onSetValue(value)
        if (oldValue != value) {
            currentValue = value
            onChange(this.value)
        }
    }

    fun setValue(value: T) {
        if (this.value == value) return
        val oldValue = this.value
        this.value = onSetValue(value)
        if (oldValue != value) {
            currentValue = value
            onChange(this.value)
        }
    }

    fun setWithoutNotify(value: T) {
        disableNotification {
            setValue(value)
        }
    }

    override fun getValue(): T {
        val v = onGetValue(value)
        if (!enableNotification) return v
        if (currentValue != v) {
            currentValue = v
            onChange(v)
        }
        return v
    }

    private val observers: MutableList<Consumer<T>> = ArrayList()

    override fun onChange(value: T) {
        if (enableNotification) observers.forEach { it.accept(value) }
    }

    override fun subscribe(callback: Consumer<T>) {
        observers.add(callback)
    }


    fun <A> bind(otherState: State<A>, map: (A) -> T) {
        otherState.subscribe {
            this.setValue(map(it))
        }
    }

    fun bind(otherState: State<T>) {
        otherState.subscribe {
            this.setValue(it)
        }
    }

    override fun toString(): String {
        return getValue().toString()
    }

    companion object {

        fun <A, B> bind(a: MutableState<A>, b: MutableState<B>, mapA2B: (A) -> B, mapB2A: (B) -> A) {
            a.subscribe {
                a.disableNotification {
                    b.setValue(mapA2B(it))
                }
            }
            b.subscribe {
                b.disableNotification {
                    a.setValue(mapB2A(it))
                }
            }
        }

    }

}

operator fun MutableState<String>.plus(other: Any?): MutableState<String> {
    this.setValue(this.getValue() + other.toString())
    return this
}

fun MutableState<String>.append(other: Any?): MutableState<String> {
    this.setValue(this.getValue() + other.toString())
    return this
}

operator fun MutableState<String>.plusAssign(other: Any) {
    this.setValue(this.getValue() + other.toString())
}

fun MutableState<Boolean>.switch(): MutableState<Boolean> {
    this.setValue(!this.getValue())
    return this
}

fun <T> MutableState<Boolean>.pick(v1: T, v2: T) = this.getValue().pick(v1, v2)

fun <R> MutableState<Boolean>.pick(block: () -> R, block2: () -> R) = this.getValue().pick(block, block2)

@JvmName("colorToARGBColorState")
fun MutableState<Color>.toARGBColorState() =
    mutableStateOf(this.getValue() as ARGBColor).apply {
        subscribe {
            this@toARGBColorState.setValue(Color.ofARGB(it.argb))
        }
    }

@JvmName("hsvColorToARGBColorState")
fun MutableState<HSVColor>.toARGBColorState() =
    mutableStateOf(this.getValue() as ARGBColor).apply {
        subscribe {
            this@toARGBColorState.setValue(HSVColor(it.argb))
        }
    }