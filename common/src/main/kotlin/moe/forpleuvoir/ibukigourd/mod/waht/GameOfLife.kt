package moe.forpleuvoir.ibukigourd.mod.waht

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceAtMost
import kotlinx.coroutines.isActive
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.mod.waht.ecs.*
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Button
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IntField
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.util.toComposeColor
import moe.forpleuvoir.nebula.common.color.Colors
import kotlin.random.Random
import moe.forpleuvoir.nebula.common.color.Color as NebulaColor


@Composable
fun GameOfLife(
    gameSpace: Float = 1f / 15,
    initRatio: Double = 0.25,
    modifier: Modifier = Modifier,
) {
    Surface(modifier) {
        var resetKey by remember { mutableIntStateOf(0) }
        var grid by remember { mutableStateOf(IntSize(360, 180)) }
        Column(Modifier.padding(24.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                var gw by remember { mutableStateOf(grid.width) }
                var gh by remember { mutableStateOf(grid.height) }
                IntField(
                    value = gw,
                    onValueChange = { gw = it },
                    valueRange = 50..500,
                    leadingIcon = { Text("Column") },
                    modifier = Modifier.width(150.dp),
                )
                IntField(
                    value = gh,
                    onValueChange = { gh = it },
                    valueRange = 50..500,
                    leadingIcon = { Text("Row") },
                    modifier = Modifier.width(150.dp),
                )
                Button({
                    grid = IntSize(gw, gh)
                    resetKey++
                }) { Text("Apply") }
                Button({
                    grid = IntSize(360, 180)
                    gw = grid.width
                    gh = grid.height
                    resetKey++
                }) { Text("Rest Grid") }
                Text("Control: set cell:mouse left button, clear:C, pause:P, restart:R")
            }
            Spacer(Modifier.height(12.dp))
            key(resetKey) {
                var delta by remember { mutableStateOf(0f) }
                var lastFrameTime by remember { mutableStateOf(0L) }
                LaunchedEffect(Unit) {
                    while (isActive) {
                        withFrameNanos {
                            if (lastFrameTime != 0L) {
                                delta = ((it - lastFrameTime) / 1_000_000_000f).fastCoerceAtMost(0.05f)
                            }
                            lastFrameTime = it
                        }
                    }
                }
                Box(modifier = Modifier.fillMaxHeight(0.95f).aspectRatio(grid.width.toFloat() / grid.height)) {
                    val world by remember {
                        mutableStateOf(
                            gameOfLife(
                                gameSpace = gameSpace,
                                grid = grid,
                                initRatio = initRatio,
                            )
                        )
                    }
                    Canvas(
                        Modifier.fillMaxSize()
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        if (event.type == PointerEventType.Press && event.buttons.isPrimaryPressed) {
                                            val position = event.changes.first().position // 本地坐标，单位 px
                                            world.emit(MouseClickEvent(position))
                                        }
                                    }
                                }
                            }
                    ) {
                        // 清屏 / 暂停 / 重开走仓库自己的输入系统轮询 —— compose-minecraft 没有 UI 键盘事件通道
                        if (InputHandler.wasKeyPressed(Keyboard.C)) world.emit(Unit, Channel("clear"))
                        if (InputHandler.wasKeyPressed(Keyboard.P)) world.emit(Unit, Channel("pause"))
                        if (InputHandler.wasKeyPressed(Keyboard.R)) world.emit(GameRestartTag)
                        world.query<DrawScopeSupplier>().firstOrNull()?.add(DrawScopeSupplier(this)) ?: run {
                            world.entity {
                                +DrawScopeSupplier(this@Canvas)
                            }
                        }
                        world.update(delta)
                        world.singleton<GameRestartTag>()?.let {
                            resetKey++
                        }
                    }
                }

            }
        }
    }
}

internal fun gameOfLife(
    gameSpace: Float = 1f / 10f,
    grid: IntSize = IntSize(320, 180),
    initRatio: Double = 0.25,
    gridLineWidth: Float = 1.5f,
    borderColor: Color = Colors.WHITE.toComposeColor(),
    gridColor: Color = NebulaColor.fromARGB(0x2FFFFFFF).toComposeColor(),
    bgColor: Color = NebulaColor.fromARGB(0xFF000000).toComposeColor(),
    cellColor: Color = NebulaColor.fromARGB(0xFF17E5E5).toComposeColor(),
) = world {
    fixedStep(gameSpace)

    GameController
    CellController
    CellSystem
    RenderSystem

    //Setting
    entity {
        +GameOfLifeState(true)
        +GameSetting(
            gridLineWidth,
            borderColor,
            gridColor,
            cellColor,
            bgColor,
        )
    }

    //Create Snake
    entity {
        +Cells(grid.width, grid.height).apply {
            randomInit(initRatio)
        }
    }
}

//region Cells
@Suppress("NOTHING_TO_INLINE")
private class Cells private constructor(val width: Int, val height: Int, private var current: BooleanArray) : Component {

    constructor(width: Int, height: Int) : this(width, height, BooleanArray(width * height))

    private var buffer: BooleanArray = BooleanArray(width * height)

    inline fun index(x: Int, y: Int): Int = y * width + x

    inline fun get(x: Int, y: Int): Boolean = current[index(x, y)]

    inline fun getBuffer(x: Int, y: Int): Boolean = buffer[index(x, y)]

    inline fun set(x: Int, y: Int, value: Boolean) {
        current[index(x, y)] = value
    }

    inline fun setBuffer(x: Int, y: Int, value: Boolean) {
        buffer[index(x, y)] = value
    }

    fun swap() {
        val temp = current
        current = buffer
        buffer = temp
    }

    inline fun forEachCell(action: (x: Int, y: Int, value: Boolean) -> Unit) {
        for (y in 0 until height) {
            for (x in 0 until width) {
                action(x, y, get(x, y))
            }
        }
    }

    fun randomInit(ratio: Double) {
        val r = ratio.coerceIn(0.0, 1.0)
        repeat(current.size) {
            current[it] = Random.nextDouble() < r
        }
    }

    fun clear() {
        current.fill(false)
        buffer.fill(false)
    }

    fun getCountOfSurroundingCells(x: Int, y: Int): Int {
        var count = 0
        for (dy in -1..1) {
            for (dx in -1..1) {
                if (dx == 0 && dy == 0) continue
                val nx = x + dx
                val ny = y + dy
                if (nx !in 0..<width || ny < 0 || ny >= height) continue
                if (get(nx, ny)) count++
            }
        }
        return count
    }

    fun updateCell(x: Int, y: Int, life: Boolean) {
        val size: Int = getCountOfSurroundingCells(x, y)
        val state = when (size) {
            0, 1 -> false
            2    -> life
            3    -> true
            else -> false
        }
        setBuffer(x, y, state)
    }
}


//endregion

private data class GameOfLifeState(
    var running: Boolean
) : Component

private var World.running
    get() = this.singleton<GameOfLifeState>()?.running ?: false
    set(value) {
        this.singleton<GameOfLifeState>()?.let {
            it.running = value
        }
    }


private val WorldBuilder.CellSystem
    get() = system {
        onFixedUpdate { world, f ->
            if (!world.running) return@onFixedUpdate
            val cells = world.singleton<Cells>() ?: return@onFixedUpdate
            cells.forEachCell { x, y, cell ->
                cells.updateCell(x, y, cell)
            }
            cells.swap()
        }
    }

private data class GameSetting(
    val gridLineWidth: Float,
    val borderColor: Color,
    val gridColor: Color,
    val cellColor: Color,
    val bgColor: Color,
) : Component

private data class CanvasInfo(var canvasSize: Size, var cellSize: Size) : Component

private class MouseClickEvent(val mouseOffset: Offset)

private operator fun Size.contains(offset: Offset): Boolean =
    offset.x in 0f..width && offset.y in 0f..height

private val WorldBuilder.CellController
    get() = system {
        onInit { world ->
            world.onEvent<MouseClickEvent> {
                val mouseOffset = it.mouseOffset
                val cells = world.singleton<Cells>() ?: return@onEvent
                val (canvasSize, cellSize) = world.singleton<CanvasInfo>() ?: return@onEvent
                if (mouseOffset !in canvasSize) return@onEvent

                val x = (mouseOffset.x / cellSize.width).toInt()
                val y = (mouseOffset.y / cellSize.height).toInt()
                cells.set(x, y, !cells.get(x, y))
            }
        }
    }

private val WorldBuilder.GameController
    get() = system {
        onInit { world ->
            world.onEvent<Unit>(Channel("pause")) {
                world.running = !world.running
            }
            world.onEvent<GameRestartTag> {
                world.createEntity { +it }
            }
            world.onEvent<Unit>(Channel("clear")) {
                world.singleton<Cells>()?.clear()
            }
        }
    }

private val WorldBuilder.RenderSystem
    get() = system {
        onUpdate { world, f ->
            world.singleton<DrawScopeSupplier>()?.scope?.apply {
                val cells = world.singleton<Cells>() ?: return@onUpdate
                val setting = world.singleton<GameSetting>() ?: return@onUpdate
                val lineWith = setting.gridLineWidth
                val boldLineWidth = lineWith * 2f

                val canvasSize = this.size
                val mapRect = Rect(Offset.Zero, canvasSize)
                val cellSize = Size(canvasSize.width / cells.width, canvasSize.height / cells.height)

                world.singleton<CanvasInfo>()?.let {
                    it.canvasSize = canvasSize
                    it.cellSize = cellSize
                } ?: world.createEntity {
                    +CanvasInfo(canvasSize, cellSize)
                }

                //BG
                drawRect(setting.bgColor, mapRect.topLeft, mapRect.size)
                //Grid
                drawRect(setting.borderColor, mapRect.topLeft, mapRect.size, style = Stroke(width = lineWith * 2))

                cells.forEachCell { x, y, cell ->
                    if (cell) {
                        drawRect(setting.cellColor, Offset(x * cellSize.width, y * cellSize.height), cellSize)
                    }
                }
                //Grid
                (1..cells.width).forEach { mx ->
                    val x = mapRect.left + mx * cellSize.width
                    drawLine(
                        setting.gridColor,
                        Offset(x, mapRect.top),
                        Offset(x, mapRect.bottom),
                        strokeWidth = if (mx % 10 == 0) boldLineWidth else lineWith
                    )
                }
                (1..cells.height).forEach { my ->
                    val y = mapRect.top + my * cellSize.height
                    drawLine(
                        setting.gridColor,
                        Offset(mapRect.left, y),
                        Offset(mapRect.right, y),
                        strokeWidth = if (my % 10 == 0) boldLineWidth else lineWith
                    )
                }

            }
        }
    }