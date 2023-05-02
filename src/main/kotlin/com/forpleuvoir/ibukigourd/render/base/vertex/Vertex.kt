@file:Suppress("UNUSED")

package com.forpleuvoir.ibukigourd.render.base.vertex

import com.forpleuvoir.ibukigourd.render.base.math.ImmutableVector3f
import com.forpleuvoir.ibukigourd.render.base.math.Vector3

fun vertex(x: Number, y: Number, z: Number) = ImmutableVector3f(x, y, z)

fun vertex(vector3: Vector3<Float>) = ImmutableVector3f(vector3)

