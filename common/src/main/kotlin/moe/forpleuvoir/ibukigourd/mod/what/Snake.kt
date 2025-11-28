package moe.forpleuvoir.ibukigourd.mod.what

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidgetContainer
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidgetImpl
import moe.forpleuvoir.ibukigourd.gui.base.widget.executeRecompose
import moe.forpleuvoir.ibukigourd.gui.modifier.MousePosition
import moe.forpleuvoir.ibukigourd.gui.modifier.ScreenFPS
import moe.forpleuvoir.ibukigourd.gui.modifier.ScreenRenderTime
import moe.forpleuvoir.ibukigourd.gui.modifier.debugInfo
import moe.forpleuvoir.ibukigourd.gui.screen.RowScreen
import moe.forpleuvoir.ibukigourd.gui.widget.Canvas
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.text.Text
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.mod.what.ecs.*
import moe.forpleuvoir.ibukigourd.text.*
import moe.forpleuvoir.ibukigourd.util.math.Vector2f
import moe.forpleuvoir.ibukigourd.util.math.plus
import moe.forpleuvoir.ibukigourd.util.state.*
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import java.util.*

fun SankeGame(
    modifier: Modifier = Modifier.debugInfo {
        ScreenFPS()
        ScreenRenderTime()
        MousePosition()
    },
    gameSpeed: Int = 3,
    cycle: Boolean = true
) = RowScreen(modifier) {
    Column(
        horizontalAlignment = Alignment.Left,
    ) {
        var scores = 0
        Text(mutableStateBy {
            buildText {
                literal {
                    content { "Scores: $scores" }
                    style {
                        color(Colors.WHITE)
                    }
                }
            }.copyToText()
        })
        Snake({ scores = it }, gameSpeed, Size(80, 40), cycle = cycle)
    }
}

fun ContainerScope.Snake(
    scoresConsumer: (Int) -> Unit,
    gameSpeed: Int = 3,
    grid: Size<Int> = Size(80, 40),
    cycle: Boolean = true,
    gridUnitSize: Float = 5f,
    gridLineWidth: Float = .25f,
    borderColor: State<ARGBColor> = stateOf(Colors.WHITE),
    gridColor: State<ARGBColor> = stateOf(Color(0x2FFFFFFF)),
    bgColor: State<ARGBColor> = stateOf(Color(0xFF000000)),
    snakeHeadColor: State<ARGBColor> = stateOf(Color(0xFFEB57AE)),
    snakePartColor: State<ARGBColor> = stateOf(Color(0xFFFF7F00)),
    foodColor: State<ARGBColor> = stateOf(Color(0xFF17E5E5)),
    wallColor: State<ARGBColor> = stateOf(Colors.WHITE),
): GuiWidgetImpl {

    val mouse = FloatPosition(0f, 0f)
    val canvasPosition = FloatPosition(0f, 0f)

    val world = World {
        +GameController
        +SnakeController
        +MoveSystem
        +CollisionSystem
        +EatSystem

        +RenderSystem
        //Setting
        createEntity {
            +MapGrid(grid.width.coerceAtLeast(1), grid.height.coerceAtLeast(1), cycle)
            +GameState(true)
            +RenderSetting(
                canvasPosition,
                mouse,
                gridUnitSize,
                gridLineWidth,
                borderColor,
                gridColor,
                bgColor,
                snakeHeadColor,
                snakePartColor,
                foodColor,
                wallColor
            )
        }
        //create Snake
        createEntity {
            val pos = Position(grid.halfWidth, grid.halfHeight).apply { +this }
            +SnakeHead(LinkedList<Entity>()).apply {
                repeat(4) {
                    parts.add(world.createEntity {
                        +pos.copy(x = pos.x - 1 * (it + 1))
                        +SnakePart
                        +Collider
                    })
                }
            }
            +SnakeState(Direction.RIGHT.canChangeDirection, mutableStateOf(0).apply { subscribe { scoresConsumer(it) } })
            +Direction.RIGHT
        }

        createEntity {
            +Food
            +Position(Random().nextInt(grid.width), Random().nextInt(grid.height))
        }
    }
    val actualGameSpeed = gameSpeed.coerceAtLeast(1)
    var count = 0
    return Canvas(
        Modifier.size(grid.width * gridUnitSize, grid.height * gridUnitSize)
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
                    world.findFirst<GameRestartTag>()?.let {
                        (this.parent() as? GuiWidgetContainer)?.executeRecompose()
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

//------------ Component ------------\\

private data class GameState(
    var running: Boolean,
    var gameOver: Boolean = false
)

private var World.running
    get() = this.query<GameState>().firstOrNull()?.value?.running ?: false
    set(value) {
        this.query<GameState>().firstOrNull()?.value?.let {
            it.running = value
        }
    }

private var World.gameOver
    get() = this.query<GameState>().firstOrNull()?.value?.gameOver ?: false
    set(value) {
        this.query<GameState>().firstOrNull()?.value?.let {
            it.gameOver = value
            if (value) {
                running = false
            }
        }
    }

private data class MapGrid(val width: Int, val height: Int, val cycle: Boolean) {
    fun inBounds(x: Int, y: Int): Boolean {
        return x in 0 until width && y in 0 until height
    }

    fun inBounds(position: Position) = inBounds(position.x, position.y)

    fun resetOnCycle(position: Position) {
        if (cycle) {
            if (position.x < 0) position.x = width + position.x
            if (position.x >= width) position.x = position.x - width
            if (position.y < 0) position.y = height + position.y
            if (position.y >= height) position.y = position.y - height
            if (!inBounds(position)) resetOnCycle(position)
        }
    }
}

private data class FloatPosition(var x: Float, var y: Float)

private data class RenderSetting(
    val canvasPosition: FloatPosition,
    val mouse: FloatPosition,
    val gridUnitSize: Float,
    val gridLineWidth: Float,
    val borderColor: State<ARGBColor>,
    val gridColor: State<ARGBColor>,
    val bgColor: State<ARGBColor>,
    val snakeHeadColor: State<ARGBColor>,
    val snakePartColor: State<ARGBColor>,
    val foodColor: State<ARGBColor>,
    val wallColor: State<ARGBColor>,
)

private data class Position(var x: Int, var y: Int) {
    fun mapRenderBox(renderSetting: RenderSetting): Box {
        return Box(
            Vector2f(
                renderSetting.gridUnitSize * x + renderSetting.canvasPosition.x,
                renderSetting.gridUnitSize * y + renderSetting.canvasPosition.y
            ),
            renderSetting.gridUnitSize,
            renderSetting.gridUnitSize
        )
    }

    operator fun plusAssign(position: Position) {
        x += position.x
        y += position.y
    }

    fun isPositionOccupied(world: World, position: Position): Boolean {
        return world.getAllComponents<Position>().values.any { it == position }
    }

    companion object {

        fun random(collisionDetection: Iterable<Position>, map: MapGrid): Position {
            val result = Position(Random().nextInt(map.width), Random().nextInt(map.height))
            return if (result in collisionDetection) {
                random(collisionDetection, map)
            } else result
        }
    }

}

private enum class Direction(val velocity: Position) {
    UP(Position(0, -1)),
    DOWN(Position(0, 1)),
    LEFT(Position(-1, 0)),
    RIGHT(Position(1, 0));

    val canChangeDirection: Set<Direction>
        get() = when (this) {
            UP, DOWN    -> _up_down
            LEFT, RIGHT -> _left_right
        }

}

private val _up_down = setOf(Direction.RIGHT, Direction.LEFT)
private val _left_right = setOf(Direction.UP, Direction.DOWN)

private data class SnakeState(
    var canChangeDirection: Set<Direction>,
    val scores: MutableState<Int>
)

private data object SnakePart

private data class SnakeHead(val parts: LinkedList<Entity>)

private data object Food

private data object Wall

private data object Collider

internal data object GameRestartTag

//------------ Input ------------\\

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

        else       -> false
    }
}

private val SnakeController = InputSystem { world, keyCode, action, used ->
    if (used || action != KeyAction.PRESS || keyCode !in inputCode) return@InputSystem used
    world.queryTuple<SnakeHead, Direction, SnakeState>()
        .forEach { (entity, component) ->
            val (_, dir, state) = component
            world.addComponent(
                entity,
                when (keyCode) {
                    Keyboard.UP    -> if (Direction.UP in state.canChangeDirection) Direction.UP else dir
                    Keyboard.DOWN  -> if (Direction.DOWN in state.canChangeDirection) Direction.DOWN else dir
                    Keyboard.LEFT  -> if (Direction.LEFT in state.canChangeDirection) Direction.LEFT else dir
                    Keyboard.RIGHT -> if (Direction.RIGHT in state.canChangeDirection) Direction.RIGHT else dir
                    else           -> dir
                }
            )
        }
    true
}

//------------ System ------------\\

private val inputCode = listOf(Keyboard.UP, Keyboard.DOWN, Keyboard.LEFT, Keyboard.RIGHT)

private val MoveSystem = System { world: World ->
    if (!world.running) return@System
    val map = world.query<MapGrid>().firstOrNull()?.value ?: return@System
    world.queryTuple<SnakeHead, Position, Direction>()
        .forEach { (entity, component) ->
            val (head, pos, dir) = component
            val oldHeadPos = pos.copy()
            pos += dir.velocity
            world.getComponent<SnakeState>(entity)?.let {
                it.canChangeDirection = dir.canChangeDirection
            }
            if (!map.inBounds(pos)) {
                if (map.cycle) {
                    map.resetOnCycle(pos)
                } else {
                    world.gameOver = true
                    return@System
                }
            }

            if (head.parts.isNotEmpty()) {
                //如果第一节身体和第二节身体在同一格,则移动第一节身体
                if (head.parts.size > 1 && world.getComponent<Position>(head.parts[0]) == world.getComponent<Position>(head.parts[1])) {
                    val first = head.parts.first()
                    world.getComponent<Position>(first)?.let {
                        it.x = oldHeadPos.x
                        it.y = oldHeadPos.y
                    }
                } else {
                    val last = head.parts.removeLast()
                    head.parts.addFirst(last)
                    world.getComponent<Position>(last)?.let {
                        it.x = oldHeadPos.x
                        it.y = oldHeadPos.y
                    }
                }
            }
        }
}

private val CollisionSystem = System { world: World ->
    if (!world.running) return@System
    val colliders = world.queryPair<Collider, Position>().map { (entity, component) ->
        entity to component.second
    }
    world.queryPair<SnakeHead, Position>()
        .forEach { (headId, component) ->
            val pos = component.second
            colliders.forEach { (colliderId, position) ->
                if (colliderId != headId && pos == position) {
                    world.gameOver = true
                    return@System
                }
            }
        }
}

private val EatSystem = System { world: World ->
    if (!world.running) return@System
    val foods = world.queryPair<Food, Position>().map { (entity, component) ->
        entity to component.second
    }
    world.queryPair<SnakeHead, Position>()
        .forEach { (headId, component) ->
            val (head, pos) = component
            foods.forEach { (foodId, position) ->
                if (foodId != headId && pos == position) {
                    world.removeComponent<Food>(foodId)
                    world.addComponent(foodId, SnakePart)
                    world.addComponent(foodId, Collider)
                    head.parts.addFirst(foodId)
                    world.createEntity {
                        val map = world.query<MapGrid>().firstOrNull()?.value ?: return@createEntity
                        +Food
                        +Position.random(world.getAllComponents<Position>().values, map)
                    }
                    world.getComponent<SnakeState>(headId)?.let {
                        it.scores.setValue(it.scores.getValue() + 1)
                    }
                }
            }
        }

}

private val text = Literal("GAME OVER").withColor(Colors.RED)
    .appendNewLine()
    .append(Literal("Press 'R' to restart").withColor(Colors.RED))

private val RenderSystem = RenderSystem { world, graphics, _ ->
    val map = world.query<MapGrid>().firstOrNull()?.value ?: return@RenderSystem
    val setting = world.query<RenderSetting>().firstOrNull()?.value ?: return@RenderSystem
    val canvasPosition = Vector2f(setting.canvasPosition.x, setting.canvasPosition.y)
    val mapBox = Box(canvasPosition, map.width * setting.gridUnitSize, map.height * setting.gridUnitSize)
    val lineWith = setting.gridLineWidth
    val gridBox = buildList {
        repeat(map.width) { mx ->
            if (mx == 0) return@repeat
            val x = mapBox.x + mx * setting.gridUnitSize
            add(Box(x - lineWith / 2f, mapBox.y, Size(lineWith, mapBox.height)))
        }
        repeat(map.height) { my ->
            if (my == 0) return@repeat
            val y = mapBox.y + my * setting.gridUnitSize
            add(Box(mapBox.x, y - lineWith / 2f, Size(mapBox.width, lineWith)))
        }
    }

    val snakeHeads = world.queryPair<SnakeHead, Position>().map { it.second.second.mapRenderBox(setting) }
    val snakeParts = world.queryPair<SnakePart, Position>().map { it.second.second.mapRenderBox(setting) }

    val gameOver = world.gameOver
    graphics {
        //BG
        pushBox(mapBox, setting.bgColor.getValue())
        //Grid
        pushBoxOutline(mapBox, setting.borderColor.getValue(), lineWith)
        //Snake
        snakeParts.forEach {
            pushBox(it, setting.snakePartColor.getValue())
        }
        snakeHeads.forEach {
            pushBox(it, setting.snakeHeadColor.getValue())
        }
        // Food
        world.queryPair<Food, Position>().map { it.second.second.mapRenderBox(setting) }.forEach {
            pushBox(it, setting.foodColor.getValue())
        }
        // Wall
        world.queryPair<Wall, Position>().map { it.second.second.mapRenderBox(setting) }.forEach {
            pushBox(it, setting.wallColor.getValue())
        }
        //Grid
        gridBox.forEach {
            pushBox(it, setting.gridColor.getValue())
        }
        //GameOverBG
        if (gameOver) {
            val texts = text.wrapToTextLines(mapBox.width)
            val box = Box(mapBox.position + Alignment.Center.align(mapBox, texts.size(0f)), texts.size(0f)).expandEdges(5f)
            pushRoundBox(box, Colors.BLUE_JAY.alpha(.95f), 4)
        }
    }
    if (gameOver) {
        graphics.pushTextLines(text, mapBox)
    }
}