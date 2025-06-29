package moe.forpleuvoir.ibukigourd.mod.what

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidgetImpl
import moe.forpleuvoir.ibukigourd.gui.modifier.MousePosition
import moe.forpleuvoir.ibukigourd.gui.modifier.ScreenFPS
import moe.forpleuvoir.ibukigourd.gui.modifier.ScreenRenderTime
import moe.forpleuvoir.ibukigourd.gui.modifier.debugInfo
import moe.forpleuvoir.ibukigourd.gui.screen.RowScreen
import moe.forpleuvoir.ibukigourd.gui.widget.Canvas
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.mod.what.ecs.*
import moe.forpleuvoir.ibukigourd.util.state.State
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import org.joml.Vector2f
import kotlin.random.Random

fun GameOfLifeScreen(
    modifier: Modifier = Modifier.debugInfo {
        ScreenFPS()
        ScreenRenderTime()
        MousePosition()
    },
    gameSpeed: Int = 2
) = RowScreen(modifier) {
    GameOfLife(Size(320, 180), gameSpeed, gridUnitSize = 1.5f)
}


fun ContainerScope.GameOfLife(
    gridSize: Size<Int> = Size(160, 90),
    gameSpeed: Int = 2,
    initRatio: Double = 0.25,
    gridUnitSize: Float = 3f,
    gridLineWidth: Float = .25f,
    borderColor: State<ARGBColor> = stateOf(Colors.WHITE),
    gridColor: State<ARGBColor> = stateOf(Color(0x2FFFFFFF)),
    cellColor: State<ARGBColor> = stateOf(Color(0xFF17E5E5)),
    bgColor: State<ARGBColor> = stateOf(Color(0xFF000000)),
): IGWidgetImpl {
    val canvasPosition = Vector2f(0f, 0f)
    val mouse = Vector2f(0f, 0f)

    val world = World {
        +CellController
        +GameController
        +CellSystem
        +RenderSystem

        createEntity {
            +GameOfLifeState(true)
            +GameSetting(
                canvasPosition,
                mouse,
                gridUnitSize,
                gridLineWidth,
                borderColor,
                gridColor,
                cellColor,
                bgColor
            )
        }
        createEntity {
            +Cells(gridSize.width, gridSize.height).apply {
                randomInit(initRatio)
            }
        }
    }
    val actualGameSpeed = gameSpeed.coerceAtLeast(1)
    var count = 0
    return Canvas(
        Modifier.size(gridSize.width * gridUnitSize, gridSize.height * gridUnitSize)
            .placeCompletion {
                canvasPosition.x = this.transform.worldX
                canvasPosition.y = this.transform.worldY
            }
            .keyPress { event ->
                world.onInput(event.keyCode, KeyAction.PRESS, event.used)
            }
            .keyRelease { event ->
                world.onInput(event.keyCode, KeyAction.RELEASE, event.used)
            }
            .mousePress { event ->
                world.onInput(event.button, KeyAction.PRESS, event.used)
            }
            .mouseRelease { event ->
                world.onInput(event.button, KeyAction.RELEASE, event.used)
            }
            .tick {
                count++
                if (count % actualGameSpeed == 0) {
                    world.query<GameRestartTag>().firstOrNull()?.let {
                        world.findFirst<Cells>()?.randomInit(initRatio)
                        world.removeAllComponent(it.key)
                    }
                    world.tick()
                }
            }
    ) { ctx, mouseX, mouseY, delta ->
        mouse.x = mouseX
        mouse.y = mouseY
        world.render(ctx, delta)
    }
}


@ConsistentCopyVisibility
private data class Cells private constructor(val cells: Array<BooleanArray>) {

    constructor(width: Int, height: Int) : this(Array(width.coerceAtLeast(1)) { BooleanArray(height.coerceAtLeast(1)) })

    fun randomInit(ratio: Double) {
        val ratio = ratio.coerceIn(0.0, 1.0)
        for (row in cells.indices) {
            for (col in cells[row].indices) {
                cells[row][col] = Random.nextDouble() < ratio
            }
        }
    }

    fun clear() {
        for (row in cells.indices) {
            for (col in cells[row].indices) {
                cells[row][col] = false
            }
        }
    }

    fun generateTemp(): Array<BooleanArray> {
        return Array(cells.size) { row ->
            BooleanArray(cells[row].size) { col ->
                cells[row][col]
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Cells

        return cells.contentDeepEquals(other.cells)
    }

    override fun hashCode(): Int {
        return cells.contentDeepHashCode()
    }
}

private fun getCountOfSurroundingCells(x: Int, y: Int, temp: Array<BooleanArray>): Int {
    val width = temp.size
    val height = temp[0].size
    var size = 0
    if (x != 0) {
        if (y != 0) size += temp[x - 1][y - 1]
        if (y != height - 1) size += temp[x - 1][y + 1]
        size += temp[x - 1][y]
    }
    if (x != width - 1) {
        if (y != 0) size += temp[x + 1][y - 1]
        if (y != height - 1) size += temp[x + 1][y + 1]
        size += temp[x + 1][y]
    }
    if (y != 0) size += temp[x][y - 1]
    if (y != height - 1) size += temp[x][y + 1]
    return size
}

private fun updateCell(x: Int, y: Int, temp: Array<BooleanArray>, life: Boolean): Boolean {
    val size: Int = getCountOfSurroundingCells(x, y, temp)
    return when (size) {
        0, 1 -> false
        2    -> life
        3    -> true
        else -> false
    }
}

operator fun Int.plus(boolean: Boolean): Int {
    return if (boolean) this + 1 else this
}

private data class GameOfLifeState(
    var running: Boolean
)

private var World.running
    get() = this.findFirst<GameOfLifeState>()?.running ?: false
    set(value) {
        this.findFirst<GameOfLifeState>()?.let {
            it.running = value
        }
    }

private val CellSystem = System { world: World ->
    if (!world.running) return@System
    val cells = world.findFirst<Cells>() ?: return@System
    val temp = cells.generateTemp()
    val data = cells.cells
    data.forEachIndexed { row, columns ->
        columns.forEachIndexed { column, cell ->
            data[row][column] = updateCell(row, column, temp, cell)
        }
    }
}


private data class GameSetting(
    val canvasPosition: Vector2f,
    val mouse: Vector2f,
    val gridUnitSize: Float,
    val gridLineWidth: Float,
    val borderColor: State<ARGBColor>,
    val gridColor: State<ARGBColor>,
    val cellColor: State<ARGBColor>,
    val bgColor: State<ARGBColor>,
)

private val CellController = InputSystem { world, keyCode, action, used ->
    if (used || action != KeyAction.PRESS || keyCode != Mouse.LEFT) return@InputSystem used
    val cells = world.findFirst<Cells>() ?: return@InputSystem false
    val (width, height) = cells.let { it.cells.size to it.cells[0].size }
    val setting = world.query<GameSetting>().firstOrNull()?.value ?: return@InputSystem false
    val canvasPosition = Vector2f(setting.canvasPosition.x, setting.canvasPosition.y)
    val gridUnitSize = setting.gridUnitSize
    val mapBox = Box(canvasPosition, width * gridUnitSize, height * gridUnitSize)
    if (setting.mouse !in mapBox) return@InputSystem false

    val x = (setting.mouse.x - canvasPosition.x) / gridUnitSize
    val y = (setting.mouse.y - canvasPosition.y) / gridUnitSize
    cells.cells[x.toInt()][y.toInt()] = !cells.cells[x.toInt()][y.toInt()]
    true
}

private val GameController = InputSystem { world, keyCode, action, used ->
    if (used || action != KeyAction.PRESS) return@InputSystem used
    when (keyCode) {
        Keyboard.P -> {
            world.running = !world.running
            true
        }

        Keyboard.R -> {
            world.createEntity { +GameRestartTag }
            true
        }

        Keyboard.C -> {
            world.findFirst<Cells>()?.clear()
            true
        }

        else       -> false
    }
}

private val RenderSystem = RenderSystem { world, ctx, deltaTime ->
    val (width, height) = world.findFirst<Cells>()?.let { it.cells.size to it.cells[0].size } ?: return@RenderSystem
    val setting = world.findFirst<GameSetting>() ?: return@RenderSystem
    val canvasPosition = Vector2f(setting.canvasPosition.x, setting.canvasPosition.y)
    val gridUnitSize = setting.gridUnitSize
    val mapBox = Box(canvasPosition, width * gridUnitSize, height * gridUnitSize)
    val lineWith = setting.gridLineWidth
    val gridBox = buildList {
        repeat(width) { mx ->
            if (mx == 0) return@repeat
            val x = mapBox.x + mx * gridUnitSize
            add(Box(x - lineWith / 2f, mapBox.y, Size(lineWith, mapBox.height)))
        }
        repeat(height) { my ->
            if (my == 0) return@repeat
            val y = mapBox.y + my * gridUnitSize
            add(Box(mapBox.x, y - lineWith / 2f, Size(mapBox.width, lineWith)))
        }
    }
    val cells = world.findFirst<Cells>()!!.cells.let {
        buildList {
            it.forEachIndexed { row, columns ->
                columns.forEachIndexed { column, cell ->
                    if (cell) {
                        val x = mapBox.x + row * gridUnitSize
                        val y = mapBox.y + column * gridUnitSize
                        add(Box(x, y, Size(gridUnitSize, gridUnitSize)))
                    }
                }
            }
        }
    }

    ctx.batchRenderBox {
        //BG
        pushBox(mapBox, setting.bgColor.getValue())
        //Grid
        pushBoxOutline(mapBox, setting.borderColor.getValue(), lineWith)
        //Cell
        cells.forEach {
            pushBox(it, setting.cellColor.getValue())
        }
        //Grid
        gridBox.forEach {
            pushBox(it, setting.gridColor.getValue())
        }
    }

}