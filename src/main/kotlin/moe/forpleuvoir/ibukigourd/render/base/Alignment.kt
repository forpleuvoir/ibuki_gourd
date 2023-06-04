@file:Suppress("DuplicatedCode", "unused")

package moe.forpleuvoir.ibukigourd.render.base

import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3f
import moe.forpleuvoir.ibukigourd.render.base.rectangle.Rectangle
import moe.forpleuvoir.ibukigourd.render.base.rectangle.rect
import moe.forpleuvoir.nebula.common.sumOf

interface Alignment {

	/**
	 * 排列方式
	 */
	val arrangement: Arrangement

	/**
	 * 计算对齐之后的位置信息
	 * @param parent 外部矩形
	 * @param rectangles 内部需要被对齐的矩形大小
	 * @return [Vector3f]对齐之后的位置信息,应该为内部矩形的左上角顶点
	 */
	fun align(parent: Rectangle<Vector3<Float>>, rectangles: List<Rectangle<Vector3<Float>>>): List<Vector3<Float>>

	fun align(parent: Rectangle<Vector3<Float>>, rectangle: Rectangle<Vector3<Float>>): Vector3<Float> = align(parent, listOf(rectangle))[0]

}

enum class Arrangement {

	Vertical {
		override fun contentSize(rectangles: List<Rectangle<Vector3<Float>>>): Size<Float> =
			Size.create(rectangles.maxOf { it.width }, rectangles.sumOf { it.height })

		override fun calcPosition(position: Vector3<Float>, rectangles: List<Rectangle<Vector3<Float>>>): List<Vector3<Float>> {
			return buildList {
				var y = position.y
				for (rectangle in rectangles) {
					add(position.y(y))
					y += rectangle.height
				}
			}
		}
	},
	Horizontal {
		override fun contentSize(rectangles: List<Rectangle<Vector3<Float>>>): Size<Float> =
			Size.create(rectangles.sumOf { it.width }, rectangles.maxOf { it.height })

		override fun calcPosition(position: Vector3<Float>, rectangles: List<Rectangle<Vector3<Float>>>): List<Vector3<Float>> {
			return buildList {
				var x = position.x
				for (rectangle in rectangles) {
					add(position.x(x))
					x += rectangle.width
				}
			}
		}
	};

	inline fun <R> switch(vertical: (Arrangement) -> R, horizontal: (Arrangement) -> R): R {
		return when (this) {
			Vertical -> vertical(this)
			Horizontal -> horizontal(this)
		}
	}

	/**
	 * 排列之后的总大小
	 * @param rectangles List<Rectangle>
	 * @return Size<Float>
	 */
	abstract fun contentSize(rectangles: List<Rectangle<Vector3<Float>>>): Size<Float>

	/**
	 * 计算排列之后的每一个元素的位置
	 * @param position Vector3<Float>
	 * @param rectangles List<Rectangle>
	 */
	abstract fun calcPosition(position: Vector3<Float>, rectangles: List<Rectangle<Vector3<Float>>>): List<Vector3<Float>>

	fun calcPosition(
		position: Vector3<Float>,
		rectangles: List<Rectangle<Vector3<Float>>>,
		map: (Vector3<Float>, Rectangle<Vector3<Float>>) -> Vector3<Float>
	): List<Vector3<Float>> {
		return buildList {
			calcPosition(position, rectangles).forEachIndexed { index, pos ->
				add(map(pos, rectangles[index]))
			}
		}
	}

}


sealed class PlanarAlignment(override val arrangement: Arrangement = Arrangement.Vertical) : Alignment {
	class TopLeft(arrangement: Arrangement = Arrangement.Vertical) : PlanarAlignment(arrangement) {
		override fun align(parent: Rectangle<Vector3<Float>>, rectangles: List<Rectangle<Vector3<Float>>>): List<Vector3<Float>> {
			return arrangement.calcPosition(parent.position, rectangles)
		}
	}

	class TopCenter(arrangement: Arrangement = Arrangement.Vertical) : PlanarAlignment(arrangement) {
		override fun align(parent: Rectangle<Vector3<Float>>, rectangles: List<Rectangle<Vector3<Float>>>): List<Vector3<Float>> {
			var list: List<Vector3<Float>> = ArrayList(rectangles.size)
			arrangement.switch({
				list = arrangement.calcPosition(parent.position, rectangles) { pos, rect ->
					pos.x(x = parent.center.x - rect.halfWidth)
				}
			}, {
				val contentSize = arrangement.contentSize(rectangles)
				list = arrangement.calcPosition(parent.position.x(parent.center.x - contentSize.halfWidth), rectangles)
			})
			return list
		}
	}

	class TopRight(arrangement: Arrangement = Arrangement.Vertical) : PlanarAlignment(arrangement) {
		override fun align(parent: Rectangle<Vector3<Float>>, rectangles: List<Rectangle<Vector3<Float>>>): List<Vector3<Float>> {
			var list: List<Vector3<Float>> = ArrayList(rectangles.size)
			arrangement.switch({
				list = arrangement.calcPosition(parent.position, rectangles) { pos, rect ->
					pos.x(x = parent.right - rect.width)
				}
			}, {
				val contentSize = arrangement.contentSize(rectangles)
				list = arrangement.calcPosition(parent.position.x(parent.right - contentSize.width), rectangles)
			})
			return list
		}
	}

	class CenterLeft(arrangement: Arrangement = Arrangement.Vertical) : PlanarAlignment(arrangement) {
		override fun align(parent: Rectangle<Vector3<Float>>, rectangles: List<Rectangle<Vector3<Float>>>): List<Vector3<Float>> {
			var list: List<Vector3<Float>> = ArrayList(rectangles.size)
			val size = arrangement.contentSize(rectangles)
			val y = parent.center.y - size.halfHeight
			val x = parent.left
			val rect = rect(parent.position.xyz(x, y), size)
			arrangement.switch({
				list = arrangement.calcPosition(rect.position, rectangles)
			}, {
				list = arrangement.calcPosition(rect.position, rectangles) { pos, r ->
					pos.y(rect.center.y - r.halfHeight)
				}
			})
			return list
		}
	}

	class Center(arrangement: Arrangement = Arrangement.Vertical) : PlanarAlignment(arrangement) {
		override fun align(parent: Rectangle<Vector3<Float>>, rectangles: List<Rectangle<Vector3<Float>>>): List<Vector3<Float>> {
			var list: List<Vector3<Float>> = ArrayList(rectangles.size)
			val size = arrangement.contentSize(rectangles)
			val y = parent.center.y - size.halfHeight
			val x = parent.center.x - size.halfWidth
			val rect = rect(parent.position.xyz(x, y), size)
			arrangement.switch({
				list = arrangement.calcPosition(rect.position, rectangles) { pos, r ->
					pos.x(rect.center.x - r.halfWidth)
				}
			}, {
				list = arrangement.calcPosition(rect.position, rectangles) { pos, r ->
					pos.y(rect.center.y - r.halfHeight)
				}
			})
			return list
		}
	}

	class CenterRight(arrangement: Arrangement) : PlanarAlignment(arrangement) {
		override fun align(parent: Rectangle<Vector3<Float>>, rectangles: List<Rectangle<Vector3<Float>>>): List<Vector3<Float>> {
			var list: List<Vector3<Float>> = ArrayList(rectangles.size)
			val size = arrangement.contentSize(rectangles)
			val y = parent.center.y - size.halfHeight
			val x = parent.right - size.width
			val rect = rect(parent.position.xyz(x, y), size)
			arrangement.switch({
				list = arrangement.calcPosition(rect.position, rectangles) { pos, r ->
					pos.x(rect.right - r.width)
				}
			}, {
				list = arrangement.calcPosition(rect.position, rectangles) { pos, r ->
					pos.y(rect.center.y - r.halfHeight)
				}
			})
			return list
		}
	}

	class BottomLeft(arrangement: Arrangement) : PlanarAlignment(arrangement) {
		override fun align(parent: Rectangle<Vector3<Float>>, rectangles: List<Rectangle<Vector3<Float>>>): List<Vector3<Float>> {
			var list: List<Vector3<Float>> = ArrayList(rectangles.size)
			val size = arrangement.contentSize(rectangles)
			val y = parent.bottom - size.height
			val x = parent.left
			val rect = rect(parent.position.xyz(x, y), size)
			arrangement.switch({
				list = arrangement.calcPosition(rect.position, rectangles)
			}, {
				list = arrangement.calcPosition(rect.position, rectangles) { pos, r ->
					pos.y(rect.center.y - r.halfHeight)
				}
			})
			return list
		}
	}

	class BottomCenter(arrangement: Arrangement) : PlanarAlignment(arrangement) {
		override fun align(parent: Rectangle<Vector3<Float>>, rectangles: List<Rectangle<Vector3<Float>>>): List<Vector3<Float>> {
			var list: List<Vector3<Float>> = ArrayList(rectangles.size)
			val size = arrangement.contentSize(rectangles)
			val y = parent.bottom - size.height
			val x = parent.center.x - size.halfWidth
			val rect = rect(parent.position.xyz(x, y), size)
			arrangement.switch({
				list = arrangement.calcPosition(rect.position, rectangles) { pos, r ->
					pos.x(rect.center.x - r.halfWidth)
				}
			}, {
				list = arrangement.calcPosition(rect.position, rectangles) { pos, r ->
					pos.y(rect.center.y - r.halfHeight)
				}
			})
			return list
		}
	}

	class BottomRight(arrangement: Arrangement) : PlanarAlignment(arrangement) {
		override fun align(parent: Rectangle<Vector3<Float>>, rectangles: List<Rectangle<Vector3<Float>>>): List<Vector3<Float>> {
			var list: List<Vector3<Float>> = ArrayList(rectangles.size)
			val size = arrangement.contentSize(rectangles)
			val y = parent.bottom - size.height
			val x = parent.right - size.width
			val rect = rect(parent.position.xyz(x, y), size)
			arrangement.switch({
				list = arrangement.calcPosition(rect.position, rectangles) { pos, r ->
					pos.x(rect.right - r.width)
				}
			}, {
				list = arrangement.calcPosition(rect.position, rectangles) { pos, r ->
					pos.y(rect.center.y - r.halfHeight)
				}
			})
			return list
		}
	}
}
