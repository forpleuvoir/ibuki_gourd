package forpleuvoir.ibuki_gourd.mod.gui

import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.render.RenderUtil.drawRect
import forpleuvoir.ibuki_gourd.utils.color.Color4i
import forpleuvoir.ibuki_gourd.utils.color.IColor
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.util.InputUtil
import java.util.*

/**
 * 生命游戏

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.mod.gui

 * 文件名 ScreenGameOfLife

 * 创建时间 2022/1/22 14:30

 * @author forpleuvoir

 */
class ScreenGameOfLife(private val cellSize: Int = 5, private val period: Int = 2) : ScreenBase("Game of life") {

	private val padding = 10
	private val contentWidth get() = this.width - padding * 2
	private val contentHeight get() = this.height - titleBottom - padding * 2
	private val x get() = padding
	private val y get() = titleBottom + padding

	private val WIDTH get() = contentWidth / cellSize
	private val HEIGHT get() = contentHeight / cellSize
	private var tickCounter: Long = 0
	private var run = true

	private lateinit var cell: Array<IntArray>
	private lateinit var temp: Array<IntArray>

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		super.render(context, mouseX, mouseY, delta)
		draw()
	}

	override fun tick() {
		if (run) {
			if (tickCounter % period == 0L) {
				update()
			}
			tickCounter++
		}
	}


	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		if (keyCode == InputUtil.GLFW_KEY_RIGHT) {
			update()
		}
		return super.keyPressed(keyCode, scanCode, modifiers)
	}

	override fun init() {
		super.init()
		cell = Array(WIDTH) { IntArray(HEIGHT) }
		temp = Array(WIDTH) { IntArray(HEIGHT) }
		initData()
		randomCell()
	}

	private fun draw() {
		for (i in 0 until WIDTH) {
			for (j in 0 until HEIGHT) {
				drawCell(i, j, cell[i][j])
			}
		}
		drawBackground()
	}

	private fun initData() {
		for (i in 0 until WIDTH) {
			for (j in 0 until HEIGHT) {
				cell[i][j] = 0
			}
		}
		update()
	}

	private fun drawBackground() {
		val lineColor = Color4i((20 * 0.3).toInt(), (211 * 0.3).toInt(), (211 * 0.3).toInt())
		for (i in 0..WIDTH) {
			drawRect(x + i * cellSize, y, 1, contentHeight, lineColor)
		}
		for (i in 0..HEIGHT) {
			drawRect(x, y + i * cellSize, contentWidth, 1, lineColor)
		}
	}


	private fun update() {
		initTemp()
		for (i in 0 until WIDTH) {
			for (j in 0 until HEIGHT) {
				updateCell(i, j, cell[i][j])
			}
		}
	}

	private fun randomCell() {
		for (i in 0 until WIDTH) {
			for (j in 0 until HEIGHT) {
				cell[i][j] = if (Random().nextBoolean()) 0 else 1

			}
		}

	}

	private fun initTemp() {
		for (i in 0 until WIDTH) {
			for (j in 0 until HEIGHT) {
				temp[i][j] = cell[i][j]
			}
		}
	}

	private fun updateCell(x: Int, y: Int, tag: Int) {
		val size: Int = getCountOfSurroundingCells(x, y)
		if (tag == 0) {
			if (size == 3) {
				cell[x][y] = 1
			}
		} else if (tag == 1) {
			if (size < 2) {
				cell[x][y] = 0
			}
			if (size == 2 || size == 3) {
				cell[x][y] = 1
			}
			if (size > 3) {
				cell[x][y] = 0
			}
		}

	}

	private fun getCountOfSurroundingCells(x: Int, y: Int): Int {
		var size = 0
		if (x != 0) {
			if (y != 0) size += temp[x - 1][y - 1]
			if (y != HEIGHT - 1) size += temp[x - 1][y + 1]
			size += temp[x - 1][y]
		}
		if (x != WIDTH - 1) {
			if (y != 0) size += temp[x + 1][y - 1]
			if (y != HEIGHT - 1) size += temp[x + 1][y + 1]
			size += temp[x + 1][y]
		}
		if (y != 0) size += temp[x][y - 1]
		if (y != HEIGHT - 1) size += temp[x][y + 1]
		return size
	}

	private fun drawCell(x: Int, y: Int, tag: Int) {
		val color: IColor<out Number> =
			if (tag == 0) {
				Color4i.BLACK
			} else {
				Color4i(177, 177, 177)
			}
		drawRect(this.x + (x * cellSize), this.y + (y * cellSize), cellSize, cellSize, color)
	}

}