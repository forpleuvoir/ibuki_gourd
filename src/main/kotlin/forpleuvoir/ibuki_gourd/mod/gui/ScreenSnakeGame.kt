package forpleuvoir.ibuki_gourd.mod.gui

import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.render.RenderUtil
import forpleuvoir.ibuki_gourd.utils.color.Color4i
import net.minecraft.client.util.InputUtil.*
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import java.util.*


/**
 * 贪吃蛇小游戏

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.mod.gui

 * 文件名 ScreenSnakeGame

 * 创建时间 2021/12/14 22:13

 * @author forpleuvoir

 */
class ScreenSnakeGame : ScreenBase(Text.of("Snake")) {

	/**
	 * 蛇的身体
	 */
	private val snake: LinkedList<Vector2d> = LinkedList()
	private val head: Vector2d
		get() = snake.first
	private val neck: Vector2d
		get() = snake[1]
	private var runnable: Boolean = true

	private val defaultSize = 3
	private val size: Int = 10
	private var score: Int = 0
	private val horizontalPadding = 10
	private val bottomPadding = 10
	private val speed = 5
	private var tickCounter: Int = 0
	private var scale: Int = guiScale
	private val gameWidth: Int = 186 / scale
	private val gameHeight: Int = 87 / scale
	private var isGameOver: Boolean = false

	/**
	 * 食物
	 */
	private var food: Vector2d

	//0:up 1:down 2:left 3:right
	private var direction: Int = 0

	init {
		food = createFood()
		initSnake()
	}

	private fun initialize() {
		food = createFood()
		initSnake()
		runnable = true
		isGameOver = false
		direction = 0
		score = 0
	}

	private fun initSnake() {
		snake.clear()
		for (i in 1..defaultSize) {
			snake.addFirst(Vector2d(gameWidth / 2, gameHeight / 2 - i))
		}
	}

	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		when (keyCode) {
			GLFW_KEY_UP    -> {
				if (direction != 1 && neck.y == head.y)
					direction = 0
			}
			GLFW_KEY_DOWN  -> {
				if (direction != 0 && neck.y == head.y)
					direction = 1
			}
			GLFW_KEY_LEFT  -> {
				if (direction != 3 && neck.x == head.x)
					direction = 2
			}
			GLFW_KEY_RIGHT -> {
				if (direction != 2 && neck.x == head.x)
					direction = 3
			}
			GLFW_KEY_P     -> {
				runnable = !runnable
			}
			GLFW_KEY_R     -> {
				initialize()
			}
		}

		return super.keyPressed(keyCode, scanCode, modifiers)
	}


	override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		renderBord()
		super.render(matrices, mouseX, mouseY, delta)
		renderScore(matrices)
		renderFood()
		renderSnake()
		if (isGameOver) renderGameOverMessage(matrices)
	}

	private fun renderGameOverMessage(matrices: MatrixStack) {
		textRenderer.drawWithShadow(matrices, "Game Over", this.width.toFloat() / 2, this.height.toFloat() / 2, Color4i.WHITE.rgba)
		textRenderer.drawWithShadow(
			matrices,
			"Press R to restart",
			this.width.toFloat() / 2,
			this.height.toFloat() / 2 + textRenderer.fontHeight,
			Color4i.WHITE.rgba
		)
	}

	private fun renderScore(matrices: MatrixStack) {
		textRenderer.drawWithShadow(
			matrices,
			Text.of("score:"),
			titleLeftPadding + textRenderer.getWidth(title) + 10,
			titleTopPadding,
			Color4i.WHITE.rgba
		)
		textRenderer.drawWithShadow(
			matrices,
			Text.of(score.toString()),
			titleLeftPadding + textRenderer.getWidth(title) + textRenderer.getWidth("score:") + 10,
			titleTopPadding,
			Color4i.WHITE.rgba
		)
	}

	override fun tick() {
		if (tickCounter % (20 / speed) == 0) {
			if (runnable)
				if (!isGameOver)
					update()
		}
		tickCounter++
	}


	private fun update() {
		move()
		isDead()
		eat()
	}

	private fun eat() {
		if (head.x == food.x && head.y == food.y) {
			snake.addLast(food)
			food = createFood()
			score++
		}
	}

	private fun renderSnake() {
		snake.forEach {
			if (it != head)
				renderVector2d(it, Color4i().fromInt(-0x14a852))
			else
				renderVector2d(it, Color4i().fromInt(-0x8100))
		}
	}

	private fun renderFood() {
		renderVector2d(food, Color4i().fromInt(-0xe81a1b))
	}

	private fun renderVector2d(vector2d: Vector2d, bgColor: Color4i, lineColor: Color4i = Color4i.WHITE) {
		RenderUtil.drawOutlinedBox(horizontalPadding + vector2d.x * size, topPadding + vector2d.y * size, size - 2, size - 2, bgColor, lineColor)
	}


	private fun move() {
		val head = Vector2d(head.x, head.y)
		var x = 0
		var y = 0
		when (direction) {
			0 -> {
				y -= 1
			}
			1 -> {
				y += 1
			}
			2 -> {
				x -= 1
			}
			3 -> {
				x += 1
			}
		}
		head.x += x
		head.y += y
		snake.addFirst(head)
		snake.removeLast()
	}

	private fun isDead(): Boolean {
		val temp: LinkedList<Vector2d> = LinkedList(snake)
		temp.removeFirst()
		var isDead = temp.contains(head)
		if (!isDead) {
			isDead = head.x > gameWidth - 1 || head.x < 0 || head.y > gameHeight - 1 || head.y < 0
		}
		if (isDead) onDeath()
		return isDead
	}

	private fun onDeath() {
		runnable = false
		isGameOver = true

	}

	private fun isInSnake(vector2d: Vector2d): Boolean {
		return snake.contains(vector2d)
	}

	private fun renderBord() {
		val bgColor = Color4i.BLACK
		bgColor.alpha = 127
		val bordColor = Color4i.WHITE
		RenderUtil.drawOutlinedBox(
			horizontalPadding,
			topPadding,
			gameWidth * size,
			gameHeight * size,
			bgColor,
			bordColor
		)

	}

	private fun createFood(): Vector2d {
		val foodX = (1..gameWidth).random() - 1
		val foodY = (1..gameHeight).random() - 1
		val food = Vector2d(foodX, foodY)
		return if (!isInSnake(food))
			Vector2d(foodX, foodY)
		else
			createFood()
	}

	data class Vector2d(var x: Int, var y: Int)
}
