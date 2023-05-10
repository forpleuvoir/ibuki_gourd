package com.forpleuvoir.ibukigourd.render.base

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


		fun create(width: Float, height: Float): Size<Float> = object : SizeFloat {
			override val width: Float get() = width
			override val height: Float get() = height
		}

		fun create(width: Double, height: Double): Size<Double> = object : SizeDouble {
			override val width: Double get() = width
			override val height: Double get() = height
		}

		fun create(width: Int, height: Int): Size<Int> = object : SizeInt {
			override val width: Int get() = width
			override val height: Int get() = height
		}

		fun create(width: Long, height: Long): Size<Long> = object : SizeLong {
			override val width: Long get() = width
			override val height: Long get() = height
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

	companion object {

		fun create(width: Float, height: Float): MutableSize<Float> = object : MutableSizeFloat {
			override var width: Float = width
			override var height: Float = height
		}

		fun create(width: Double, height: Double): MutableSize<Double> = object : MutableSizeDouble {
			override var width: Double = width
			override var height: Double = height
		}

		fun create(width: Int, height: Int): MutableSize<Int> = object : MutableSizeInt {
			override var width: Int = width
			override var height: Int = height
		}

		fun create(width: Long, height: Long): MutableSize<Long> = object : MutableSizeLong {
			override var width: Long = width
			override var height: Long = height
		}
	}
}

interface MutableSizeFloat : MutableSize<Float> {

	override var width: Float

	override var height: Float
	override val halfWidth: Float get() = width / 2

	override val halfHeight: Float get() = height / 2
}

interface MutableSizeDouble : MutableSize<Double> {

	override var width: Double

	override var height: Double

	override val halfWidth: Double get() = width / 2

	override val halfHeight: Double get() = height / 2
}

interface MutableSizeInt : MutableSize<Int> {

	override var width: Int

	override var height: Int
	override val halfWidth: Int get() = width / 2

	override val halfHeight: Int get() = height / 2
}

interface MutableSizeLong : MutableSize<Long> {

	override var width: Long

	override var height: Long
	override val halfWidth: Long get() = width / 2

	override val halfHeight: Long get() = height / 2
}
