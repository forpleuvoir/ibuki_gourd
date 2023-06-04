package moe.forpleuvoir.ibukigourd.render.base.math

open class ImmutableVector3f(
	override val x: Float = 0f,
	override val y: Float = 0f,
	override val z: Float = 0f,
) : Vector3<Float> {

	constructor(vector3: Vector3<Float>) : this(vector3.x, vector3.y, vector3.z)

	constructor(x: Number, y: Number, z: Number) : this(x.toFloat(), y.toFloat(), z.toFloat())

	override fun x(x: Float): ImmutableVector3f {
		return ImmutableVector3f(x, this.y, this.z)
	}

	override fun y(y: Float): ImmutableVector3f {
		return ImmutableVector3f(this.x, y, this.z)
	}

	override fun z(z: Float): ImmutableVector3f {
		return ImmutableVector3f(this.x, this.y, z)
	}

	override fun xyz(x: Float, y: Float, z: Float): ImmutableVector3f {
		return ImmutableVector3f(x, y, z)
	}

	override fun plus(vector3: Vector3<Float>): ImmutableVector3f {
		return this.plus(vector3.x, vector3.y, vector3.z)
	}

	override fun minus(vector3: Vector3<Float>): ImmutableVector3f {
		return this.minus(vector3.x, vector3.y, vector3.z)
	}

	override fun times(vector3: Vector3<Float>): ImmutableVector3f {
		return this.times(vector3.x, vector3.y, vector3.z)
	}

	override fun div(vector3: Vector3<Float>): ImmutableVector3f {
		return this.div(vector3.x, vector3.y, vector3.z)
	}

	override fun rem(vector3: Vector3<Float>): ImmutableVector3f {
		return this.rem(vector3.x, vector3.y, vector3.z)
	}

	override fun plus(x: Float, y: Float, z: Float): ImmutableVector3f {
		return ImmutableVector3f(this.x + x, this.y + y, this.z + z)
	}

	override fun unaryPlus(): ImmutableVector3f {
		return ImmutableVector3f(+x, +y, +z)
	}

	override fun minus(x: Float, y: Float, z: Float): ImmutableVector3f {
		return ImmutableVector3f(this.x - x, this.y - y, this.z - z)
	}

	override fun unaryMinus(): ImmutableVector3f {
		return ImmutableVector3f(-x, -y, -z)
	}

	override fun times(x: Float, y: Float, z: Float): ImmutableVector3f {
		return ImmutableVector3f(this.x * x, this.y * y, this.z * z)
	}

	override fun div(x: Float, y: Float, z: Float): ImmutableVector3f {
		return ImmutableVector3f(this.x / x, this.y / y, this.z / z)
	}

	override fun rem(x: Float, y: Float, z: Float): ImmutableVector3f {
		return ImmutableVector3f(this.x % x, this.y % y, this.z % z)
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Vector3<*>

		if (x != other.x) return false
		if (y != other.y) return false
		return z == other.z
	}

	override fun hashCode(): Int {
		var result = x.hashCode()
		result = 31 * result + y.hashCode()
		result = 31 * result + z.hashCode()
		return result
	}

}