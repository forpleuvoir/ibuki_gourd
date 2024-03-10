package moe.forpleuvoir.ibukigourd.gui.render

interface Size<T : Number> {

    val width: T

    val height: T

    val halfWidth: T

    val halfHeight: T

    companion object {

        val Size<out Number>.string: String
            get() {
                return "Size(width : $width,halfWidth : $halfWidth, height : $height, halfHeight : $halfHeight )"
            }

        operator fun invoke(width: Float, height: Float): SizeFloat = object : SizeFloat {
            override val width: Float get() = width
            override val height: Float get() = height
        }

        operator fun invoke(width: Double, height: Double): SizeDouble = object : SizeDouble {
            override val width: Double get() = width
            override val height: Double get() = height
        }

        operator fun invoke(width: Int, height: Int): SizeInt = object : SizeInt {
            override val width: Int get() = width
            override val height: Int get() = height
        }

        operator fun invoke(width: Long, height: Long): SizeLong = object : SizeLong {
            override val width: Long get() = width
            override val height: Long get() = height
        }

        fun equals(s1: Size<out Number>, s2: Size<out Number>): Boolean {
            return s1.width == s2.width && s1.height == s2.height
        }
    }
}

interface SizeFloat : Size<Float> {

    override val width: Float

    override val height: Float

    override val halfWidth: Float get() = width / 2

    override val halfHeight: Float get() = height / 2

}

interface SizeDouble : Size<Double> {

    override val width: Double

    override val height: Double

    override val halfWidth: Double get() = width / 2

    override val halfHeight: Double get() = height / 2
}

interface SizeInt : Size<Int> {

    override val width: Int

    override val height: Int

    override val halfWidth: Int get() = width / 2

    override val halfHeight: Int get() = height / 2
}

interface SizeLong : Size<Long> {

    override val width: Long

    override val height: Long

    override val halfWidth: Long get() = width / 2

    override val halfHeight: Long get() = height / 2
}


interface MutableSize<T : Number> : Size<T> {

    override var width: T

    override var height: T

    override var halfWidth: T

    override var halfHeight: T

    fun set(width: T, height: T) {
        this.width = width
        this.height = height
    }

    companion object {

        operator fun invoke(width: Float, height: Float): MutableSizeFloat = object : MutableSizeFloat {
            override var width: Float = width
            override var height: Float = height
        }

        operator fun invoke(width: Double, height: Double): MutableSizeDouble = object : MutableSizeDouble {
            override var width: Double = width
            override var height: Double = height
        }

        operator fun invoke(width: Int, height: Int): MutableSizeInt = object : MutableSizeInt {
            override var width: Int = width
            override var height: Int = height
        }

        operator fun invoke(width: Long, height: Long): MutableSizeLong = object : MutableSizeLong {
            override var width: Long = width
            override var height: Long = height
        }
    }
}

interface MutableSizeFloat : MutableSize<Float> {

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

interface MutableSizeDouble : MutableSize<Double> {

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

interface MutableSizeInt : MutableSize<Int> {

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

interface MutableSizeLong : MutableSize<Long> {

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
