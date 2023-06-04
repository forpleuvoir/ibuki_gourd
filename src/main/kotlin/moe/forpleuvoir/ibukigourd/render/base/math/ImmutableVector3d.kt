package moe.forpleuvoir.ibukigourd.render.base.math

open class ImmutableVector3d(
	override val x: Double = 0.0,
	override val y: Double = 0.0,
	override val z: Double = 0.0,
) : Vector3<Double> {

	constructor(vector3: Vector3<Double>) : this(vector3.x, vector3.y, vector3.z)

	override fun x(x: Double): ImmutableVector3d {
		return ImmutableVector3d(x, this.y, this.z)
	}

	override fun y(y: Double): ImmutableVector3d {
		return ImmutableVector3d(this.x, y, this.z)
	}

	override fun z(z: Double): ImmutableVector3d {
		return ImmutableVector3d(this.x, this.y, z)
	}

	override fun xyz(x: Double, y: Double, z: Double): ImmutableVector3d {
		return ImmutableVector3d(x, y, z)
	}

	override fun plus(vector3: Vector3<Double>): ImmutableVector3d {
		return this.plus(vector3.x, vector3.y, vector3.z)
	}

	override fun minus(vector3: Vector3<Double>): ImmutableVector3d {
		return this.minus(vector3.x, vector3.y, vector3.z)
	}

	override fun times(vector3: Vector3<Double>): ImmutableVector3d {
		return this.times(vector3.x, vector3.y, vector3.z)
	}

	override fun div(vector3: Vector3<Double>): ImmutableVector3d {
		return this.div(vector3.x, vector3.y, vector3.z)
	}

	override fun rem(vector3: Vector3<Double>): ImmutableVector3d {
		return this.rem(vector3.x, vector3.y, vector3.z)
	}

	override fun plus(x: Double, y: Double, z: Double): ImmutableVector3d {
		return ImmutableVector3d(this.x + x, this.y + y, this.z + z)
	}

	override fun unaryPlus(): ImmutableVector3d {
		return ImmutableVector3d(+x, +y, +z)
	}

	override fun minus(x: Double, y: Double, z: Double): ImmutableVector3d {
		return ImmutableVector3d(this.x - x, this.y - y, this.z - z)
	}

	override fun unaryMinus(): ImmutableVector3d {
		return ImmutableVector3d(-x, -y, -z)
	}

	override fun times(x: Double, y: Double, z: Double): ImmutableVector3d {
		return ImmutableVector3d(this.x * x, this.y * y, this.z * z)
	}

	override fun div(x: Double, y: Double, z: Double): ImmutableVector3d {
		return ImmutableVector3d(this.x / x, this.y / y, this.z / z)
	}

	override fun rem(x: Double, y: Double, z: Double): ImmutableVector3d {
		return ImmutableVector3d(this.x % x, this.y % y, this.z % z)
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