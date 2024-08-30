package moe.forpleuvoir.ibukigourd.gui.util

import moe.forpleuvoir.nebula.common.api.Notifiable
import moe.forpleuvoir.nebula.common.util.primitive.pick
import java.util.function.Consumer


class ScrollState : Notifiable<Float> {

    private var _amount: Float = 0f
        set(value) {
            field = value.coerceIn(0f..maxAmount)
            if (field.isNaN() || field.isInfinite()) {
                field = 0f
            }
        }

    var amount: Float
        get() = _amount
        set(value) {
            if (_amount != value.coerceIn(0f, maxAmount)) {
                _amount = value.coerceIn(0f, maxAmount)
                onChange(_amount)
            }
        }

    fun scroll(amount: Float) {
        this.amount -= amountStep * amount
    }

    var progress: Float
        get() = (amount / maxAmount).let { (it.isNaN() || it.isInfinite()).pick(0f, it.coerceIn(0f..1f)) }
        set(value) {
            amount = maxAmount * value.coerceIn(0f..1f)
        }

    private val observers: MutableList<Consumer<Float>> = ArrayList()

    override fun onChange(value: Float) {
        observers.forEach { it.accept(value) }
    }

    override fun subscribe(callback: Consumer<Float>) {
        observers.add(callback)
    }

    var maxAmount = 0f
        set(value) {
            field = value.coerceAtLeast(0f)
        }

    var amountStep = 0f
        set(value) {
            field = value.coerceAtLeast(0f)
        }

    var barProportion = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
        }

    inline operator fun invoke(scope: ScrollState.() -> Unit) =
        scope.invoke(this)

}