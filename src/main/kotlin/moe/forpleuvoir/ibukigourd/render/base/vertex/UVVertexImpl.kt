package moe.forpleuvoir.ibukigourd.render.base.vertex

import moe.forpleuvoir.ibukigourd.render.base.math.Vector3

class UVVertexImpl(vector3: Vector3<Float>, override val u: Float, override val v: Float) : UVVertex {

	constructor(x: Number, y: Number, z: Number, u: Number, v: Number) : this(vertex(x, y, z), u.toFloat(), v.toFloat())

	override val x: Float = vector3.x

	override val y: Float = vector3.y

	override val z: Float = vector3.z

	override fun x(x: Float): UVVertexImpl {
		return UVVertexImpl(x, y, z, u, v)
	}

	override fun y(y: Float): UVVertexImpl {
		return UVVertexImpl(x, y, z, u, v)
	}

	override fun z(z: Float): UVVertexImpl {
		return UVVertexImpl(x, y, z, u, v)
	}

	override fun xyz(x: Float, y: Float, z: Float): UVVertexImpl {
		return UVVertexImpl(x, y, z, u, v)
	}

	override fun plus(vector3: Vector3<Float>): UVVertexImpl {
		return this.plus(vector3.x, vector3.y, vector3.z)
	}

	override fun minus(vector3: Vector3<Float>): UVVertexImpl {
		return this.minus(vector3.x, vector3.y, vector3.z)
	}

	override fun times(vector3: Vector3<Float>): UVVertexImpl {
		return this.times(vector3.x, vector3.y, vector3.z)
	}

	override fun div(vector3: Vector3<Float>): UVVertexImpl {
		return this.div(vector3.x, vector3.y, vector3.z)
	}

	override fun rem(vector3: Vector3<Float>): UVVertexImpl {
		return this.rem(vector3.x, vector3.y, vector3.z)
	}

	override fun plus(x: Float, y: Float, z: Float): UVVertexImpl {
		return UVVertexImpl(this.x + x, this.y + y, this.z + z, u, v)
	}

	override fun unaryPlus(): UVVertexImpl {
		return UVVertexImpl(+x, +y, +z, u, v)
	}

	override fun minus(x: Float, y: Float, z: Float): UVVertexImpl {
		return UVVertexImpl(this.x - x, this.y - y, this.z - z, u, v)
	}

	override fun unaryMinus(): UVVertexImpl {
		return UVVertexImpl(-x, -y, -z, u, v)
	}

	override fun times(x: Float, y: Float, z: Float): UVVertexImpl {
		return UVVertexImpl(this.x * x, this.y * y, this.z * z, u, v)
	}

	override fun div(x: Float, y: Float, z: Float): UVVertexImpl {
		return UVVertexImpl(this.x / x, this.y / y, this.z / z, u, v)
	}

	override fun rem(x: Float, y: Float, z: Float): UVVertexImpl {
		return UVVertexImpl(this.x % x, this.y % y, this.z % z, u, v)
	}

	override fun u(u: Float): UVVertexImpl {
		return UVVertexImpl(this, u, v)
	}

	override fun v(v: Float): UVVertexImpl {
		return UVVertexImpl(this, u, v)
	}

	override fun uv(u: Float, v: Float): UVVertexImpl {
		return UVVertexImpl(this, u, v)
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as UVVertex

		if (u != other.u) return false
		if (v != other.v) return false
		if (x != other.x) return false
		if (y != other.y) return false
		return z == other.z
	}

	override fun hashCode(): Int {
		var result = u.hashCode()
		result = 31 * result + v.hashCode()
		result = 31 * result + x.hashCode()
		result = 31 * result + y.hashCode()
		result = 31 * result + z.hashCode()
		return result
	}


}