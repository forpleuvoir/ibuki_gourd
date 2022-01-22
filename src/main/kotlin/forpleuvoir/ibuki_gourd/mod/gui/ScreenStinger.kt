package forpleuvoir.ibuki_gourd.mod.gui

import forpleuvoir.ibuki_gourd.gui.button.Button
import forpleuvoir.ibuki_gourd.gui.screen.IScreenTabEntry
import forpleuvoir.ibuki_gourd.gui.screen.ScreenTab
import forpleuvoir.ibuki_gourd.gui.widget.LabelText
import forpleuvoir.ibuki_gourd.gui.widget.WidgetIntInput
import forpleuvoir.ibuki_gourd.utils.text
import java.util.*

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.mod.gui

 * 文件名 ScreenStinger

 * 创建时间 2022/1/22 16:22

 * @author forpleuvoir

 */
class ScreenStinger(tabEntry: IScreenTabEntry) : ScreenTab(tabEntry) {

	private lateinit var snakeButton: Button

	private lateinit var gameOfLifeButton: Button

	private lateinit var gameOfLifeCellSize: WidgetIntInput

	private lateinit var gameOfLifeCellSizeText: LabelText

	private lateinit var gameOfLifePeriod: WidgetIntInput

	private lateinit var gameOfLifePeriodText: LabelText

	override fun init() {
		super.init()
		initGameOfLife()
		initSnake()
	}

	private fun initGameOfLife() {
		gameOfLifeButton = Button(15, top + margin + 20 + 10, "GameOfLife".text) {
			openScreen(ScreenGameOfLife(gameOfLifeCellSize.value, gameOfLifePeriod.value).apply {
				parent = this@ScreenStinger
			})
		}
		gameOfLifeCellSizeText =
			LabelText("cell size".text, gameOfLifeButton.x + gameOfLifeButton.width + margin, gameOfLifeButton.y - 11)
		gameOfLifeCellSize =
			WidgetIntInput(
				gameOfLifeCellSizeText.x, gameOfLifeCellSizeText.y + gameOfLifeCellSizeText.height,
				60,
				value = 5,
				minValue = 3
			)

		gameOfLifePeriodText =
			LabelText(
				"period".text,
				gameOfLifeCellSize.x + gameOfLifeCellSize.width + margin * 2,
				gameOfLifeButton.y - 11
			)
		gameOfLifePeriod =
			WidgetIntInput(
				gameOfLifePeriodText.x,
				gameOfLifePeriodText.y + gameOfLifePeriodText.height,
				60,
				value = 2,
				minValue = 1
			)

		addDrawableChild(gameOfLifeButton)
		addDrawableChild(gameOfLifeCellSizeText)
		addDrawableChild(gameOfLifeCellSize)
		addDrawableChild(gameOfLifePeriodText)
		addDrawableChild(gameOfLifePeriod)
	}

	private fun initSnake() {
		val x = 15
		val y = gameOfLifeButton.y + gameOfLifeButton.height + margin
		snakeButton = Button(x, y, "Snake".text) {
			openScreen(ScreenSnakeGame().apply { parent = this@ScreenStinger })
		}.apply {
			setOnHoverCallback {
				it.x = Random().nextInt(15.coerceAtLeast(this@ScreenStinger.width - this.width))
				it.y = Random().nextInt(y.coerceAtLeast(this@ScreenStinger.height - this.height))
			}
		}
		addDrawableChild(snakeButton)
	}


}