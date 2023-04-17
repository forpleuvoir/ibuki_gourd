@file:Suppress("DuplicatedCode", "unused")

package com.forpleuvoir.ibukigourd.render.base

import com.forpleuvoir.ibukigourd.render.base.math.Vector3f

interface Alignment {

	/**
	 * 计算对齐之后的位置信息
	 * @param parent 外部矩形
	 * @param rectangles 内部需要被对齐的矩形大小
	 * @return [Vector3f]对齐之后的位置信息,应该为内部矩形的左上角顶点
	 */
	fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3f>

	fun align(parent: Rectangle, rectangle: Rectangle): Vector3f = align(parent, listOf(rectangle))[0]

}

enum class PlanarAlignment : Alignment {
	TopLeft {
		override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3f> {
			val list = ArrayList<Vector3f>(rectangles.size)
			var y = parent.top
			for (rectangle in rectangles) {
				list.add(parent.position.vector3f().y(y))
				y += rectangle.height
			}
			return list
		}
	},
	TopCenter {
		override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3f> {
			val list = ArrayList<Vector3f>(rectangles.size)
			var y = parent.y
			for (rectangle in rectangles) {
				list.add(parent.position.vector3f().xyz(x = parent.center.x - (rectangle.width / 2), y = y))
				y += rectangle.height
			}
			return list
		}
	},
	TopRight {
		override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3f> {
			val list = ArrayList<Vector3f>(rectangles.size)
			var y = parent.y
			for (rectangle in rectangles) {
				list.add(parent.position.vector3f().xyz(x = parent.right - rectangle.width, y = y))
				y += rectangle.height
			}
			return list
		}
	},
	CenterLeft {
		override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3f> {
			val list = ArrayList<Vector3f>(rectangles.size)
			var totalHeight = 0f
			for (rectangle in rectangles) totalHeight += rectangle.height
			var y = parent.center.y - (totalHeight / 2)
			for (rectangle in rectangles) {
				list.add(parent.position.vector3f().y(y))
				y += rectangle.height
			}
			return list
		}
	},
	Center {
		override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3f> {
			val list = ArrayList<Vector3f>(rectangles.size)
			var totalHeight = 0f
			for (rectangle in rectangles) totalHeight += rectangle.height
			var y = parent.center.y - (totalHeight / 2)
			for (rectangle in rectangles) {
				list.add(parent.position.vector3f().xyz(x = parent.center.x - (rectangle.width / 2), y = y))
				y += rectangle.height
			}
			return list
		}
	},
	CenterRight {
		override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3f> {
			val list = ArrayList<Vector3f>(rectangles.size)
			var totalHeight = 0f
			for (rectangle in rectangles) totalHeight += rectangle.height
			var y = parent.center.y - (totalHeight / 2)
			for (rectangle in rectangles) {
				list.add(parent.position.vector3f().xyz(x = parent.right - rectangle.width, y = y))
				y += rectangle.height
			}
			return list
		}
	},
	BottomLeft {
		override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3f> {
			val list = ArrayList<Vector3f>(rectangles.size)
			var totalHeight = 0f
			for (rectangle in rectangles) totalHeight += rectangle.height
			var y = parent.bottom - totalHeight
			for (rectangle in rectangles) {
				list.add(parent.position.vector3f().y(y))
				y += rectangle.height
			}
			return list
		}
	},
	BottomCenter {
		override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3f> {
			val list = ArrayList<Vector3f>(rectangles.size)
			var totalHeight = 0f
			for (rectangle in rectangles) totalHeight += rectangle.height
			var y = parent.bottom - totalHeight
			for (rectangle in rectangles) {
				list.add(parent.position.vector3f().xyz(x = parent.center.x - (rectangle.width / 2), y = y))
				y += rectangle.height
			}
			return list
		}
	},
	BottomRight {
		override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3f> {
			val list = ArrayList<Vector3f>(rectangles.size)
			var totalHeight = 0f
			for (rectangle in rectangles) totalHeight += rectangle.height
			var y = parent.bottom - totalHeight
			for (rectangle in rectangles) {
				list.add(parent.position.vector3f().xyz(x = parent.right - rectangle.width, y = y))
				y += rectangle.height
			}
			return list
		}
	}
}

enum class HorizontalAlignment : Alignment {
	Left {
		override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3f> {
			val list = ArrayList<Vector3f>(rectangles.size)
			var x = parent.left
			for (rectangle in rectangles) {
				list.add(parent.position.vector3f().x(x))
				x += rectangle.width
			}
			return list
		}
	},
	Center {
		override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3f> {
			val list = ArrayList<Vector3f>(rectangles.size)
			var totalWidth = 0f
			for (rectangle in rectangles) totalWidth += rectangle.width
			var x = parent.center.x - (totalWidth / 2)
			for (rectangle in rectangles) {
				list.add(parent.position.vector3f().x(x))
				x += rectangle.width
			}
			return list
		}
	},
	Right {
		override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3f> {
			val list = ArrayList<Vector3f>(rectangles.size)
			var totalWidth = 0f
			for (rectangle in rectangles) totalWidth += rectangle.width
			var x = parent.right - totalWidth
			for (rectangle in rectangles) {
				list.add(parent.position.vector3f().x(x))
				x += rectangle.width
			}
			return list
		}
	},
}

enum class VerticalAlignment : Alignment {
	Top {
		override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3f> {
			val list = ArrayList<Vector3f>(rectangles.size)
			var y = parent.top
			for (rectangle in rectangles) {
				list.add(parent.position.vector3f().y(y))
				y += rectangle.height
			}
			return list
		}
	},
	Center {
		override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3f> {
			val list = ArrayList<Vector3f>(rectangles.size)
			var totalHeight = 0f
			for (rectangle in rectangles) totalHeight += rectangle.height
			var y = parent.center.y - (totalHeight / 2)
			for (rectangle in rectangles) {
				list.add(parent.position.vector3f().y(y))
				y += rectangle.height
			}
			return list
		}
	},
	Bottom {
		override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3f> {
			val list = ArrayList<Vector3f>(rectangles.size)
			var totalHeight = 0f
			for (rectangle in rectangles) totalHeight += rectangle.height
			var y = parent.bottom - totalHeight
			for (rectangle in rectangles) {
				list.add(parent.position.vector3f().y(y))
				y += rectangle.height
			}
			return list
		}
	},
}