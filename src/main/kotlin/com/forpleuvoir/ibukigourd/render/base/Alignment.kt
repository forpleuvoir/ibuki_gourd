package com.forpleuvoir.ibukigourd.render.base

import com.forpleuvoir.ibukigourd.render.base.math.Vector3f

interface Alignment {

	/**
	 * 计算对齐之后的位置信息
	 * @param parent 外部矩形
	 * @param thisRect 内部需要被对齐的矩形宽度
	 * @return [Vector3f]对齐之后的位置信息,应该为内部矩形的左上角顶点
	 */
	fun align(parent: Rectangle, thisRect: Rectangle): Vector3f

}


enum class PlanarAlignment : Alignment {
	TopLeft {
		override fun align(parent: Rectangle, thisRect: Rectangle): Vector3f {
			return parent.position.vector3f()
		}
	},
	TopCenter {
		override fun align(parent: Rectangle, thisRect: Rectangle): Vector3f {
			return parent.position.vector3f().x(parent.center.x - (thisRect.width / 2))
		}
	},
	TopRight {
		override fun align(parent: Rectangle, thisRect: Rectangle): Vector3f {
			return parent.position.vector3f().x(parent.right - thisRect.width)
		}
	},
	CenterLeft {
		override fun align(parent: Rectangle, thisRect: Rectangle): Vector3f {
			return parent.position.vector3f().y(parent.center.y - (thisRect.height / 2))
		}
	},
	Center {
		override fun align(parent: Rectangle, thisRect: Rectangle): Vector3f {
			return parent.position.vector3f().xyz(
				parent.center.x - (thisRect.width / 2),
				parent.center.y - (thisRect.height / 2)
			)
		}
	},
	CenterRight {
		override fun align(parent: Rectangle, thisRect: Rectangle): Vector3f {
			return parent.position.vector3f().xyz(
				parent.right - thisRect.width,
				parent.center.y - (thisRect.height / 2)
			)
		}
	},
	BottomLeft {
		override fun align(parent: Rectangle, thisRect: Rectangle): Vector3f {
			return parent.position.vector3f().y(parent.bottom - thisRect.height)
		}
	},
	BottomCenter {
		override fun align(parent: Rectangle, thisRect: Rectangle): Vector3f {
			return parent.position.vector3f().xyz(
				parent.center.x - (thisRect.width / 2),
				parent.bottom - thisRect.height
			)
		}
	},
	BottomRight {
		override fun align(parent: Rectangle, thisRect: Rectangle): Vector3f {
			return parent.position.vector3f().xyz(
				parent.right - thisRect.width,
				parent.bottom - thisRect.height
			)
		}
	}
}

enum class HorizontalAlignment : Alignment {
	Left {
		override fun align(parent: Rectangle, thisRect: Rectangle): Vector3f =
			thisRect.position.vector3f().x(parent.left)
	},
	Center {
		override fun align(parent: Rectangle, thisRect: Rectangle): Vector3f =
			thisRect.position.vector3f().x(parent.center.x - (thisRect.width / 2))
	},
	Right {
		override fun align(parent: Rectangle, thisRect: Rectangle): Vector3f =
			thisRect.position.vector3f().x(parent.right - thisRect.width)
	},
}

enum class VerticalAlignment : Alignment {
	Top {
		override fun align(parent: Rectangle, thisRect: Rectangle): Vector3f =
			thisRect.position.vector3f().y(parent.top)
	},
	Center {
		override fun align(parent: Rectangle, thisRect: Rectangle): Vector3f =
			thisRect.position.vector3f().y(parent.center.y - (thisRect.height / 2))
	},
	Bottom {
		override fun align(parent: Rectangle, thisRect: Rectangle): Vector3f =
			thisRect.position.vector3f().y(parent.bottom - thisRect.height)
	},
}