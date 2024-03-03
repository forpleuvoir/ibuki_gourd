package moe.forpleuvoir.ibukigourd.render.base

interface Dimension<T : Number> {

    val width: T

    val height: T

    val halfWidth: T

    val halfHeight: T

    companion object {

        val Dimension<out Number>.string: String
            get() {
                return "Size(width : $width,halfWidth : $halfWidth, height : $height, halfHeight : $halfHeight )"
            }

        fun of(width: Float, height: Float): Dimension<Float> = object : DimensionFloat {
            override val width: Float get() = width
            override val height: Float get() = height
        }

        fun of(width: Double, height: Double): Dimension<Double> = object : DimensionDouble {
            override val width: Double get() = width
            override val height: Double get() = height
        }

        fun of(width: Int, height: Int): Dimension<Int> = object : DimensionInt {
            override val width: Int get() = width
            override val height: Int get() = height
        }

        fun of(width: Long, height: Long): Dimension<Long> = object : DimensionLong {
            override val width: Long get() = width
            override val height: Long get() = height
        }
    }
}

interface DimensionFloat : Dimension<Float> {

    override val width: Float

    override val height: Float

    override val halfWidth: Float get() = width / 2

    override val halfHeight: Float get() = height / 2

}

interface DimensionDouble : Dimension<Double> {

    override val width: Double

    override val height: Double

    override val halfWidth: Double get() = width / 2

    override val halfHeight: Double get() = height / 2
}

interface DimensionInt : Dimension<Int> {

    override val width: Int

    override val height: Int

    override val halfWidth: Int get() = width / 2

    override val halfHeight: Int get() = height / 2
}

interface DimensionLong : Dimension<Long> {

    override val width: Long

    override val height: Long

    override val halfWidth: Long get() = width / 2

    override val halfHeight: Long get() = height / 2
}


interface MutableDimension<T : Number> : Dimension<T> {

    override var width: T

    override var height: T

    override var halfWidth: T

    override var halfHeight: T

    fun set(width: T, height: T) {
        this.width = width
        this.height = height
    }

    companion object {

        fun of(width: Float, height: Float): MutableDimension<Float> = object : MutableDimensionFloat {
            override var width: Float = width
            override var height: Float = height
        }

        fun of(width: Double, height: Double): MutableDimension<Double> = object : MutableDimensionDouble {
            override var width: Double = width
            override var height: Double = height
        }

        fun of(width: Int, height: Int): MutableDimension<Int> = object : MutableDimensionInt {
            override var width: Int = width
            override var height: Int = height
        }

        fun of(width: Long, height: Long): MutableDimension<Long> = object : MutableDimensionLong {
            override var width: Long = width
            override var height: Long = height
        }
    }
}

interface MutableDimensionFloat : MutableDimension<Float> {

    override var width: Float

    override var height: Float

    override var halfWidth: Float
        get() = width / 2
        set(value) {
            width = value * 2
        }

    override var halfHeight: Float
        get() = height / 2
        set(value) {
            height = value * 2
        }
}

interface MutableDimensionDouble : MutableDimension<Double> {

    override var width: Double

    override var height: Double

    override var halfWidth: Double
        get() = width / 2
        set(value) {
            width = value * 2
        }

    override var halfHeight: Double
        get() = height / 2
        set(value) {
            height = value * 2
        }
}

interface MutableDimensionInt : MutableDimension<Int> {

    override var width: Int

    override var height: Int

    override var halfWidth: Int
        get() = width / 2
        set(value) {
            width = value * 2
        }

    override var halfHeight: Int
        get() = height / 2
        set(value) {
            height = value * 2
        }
}

interface MutableDimensionLong : MutableDimension<Long> {

    override var width: Long

    override var height: Long

    override var halfWidth: Long
        get() = width / 2
        set(value) {
            width = value * 2
        }

    override var halfHeight: Long
        get() = height / 2
        set(value) {
            height = value * 2
        }
}
