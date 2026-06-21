package moe.forpleuvoir.ibukigourd.mod.waht

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceAtMost
import kotlinx.coroutines.isActive
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.mod.waht.ecs.*
import moe.forpleuvoir.ibukigourd.ui.util.toComposeColor
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.color.Color as NebulaColor
import java.util.*

@Composable
fun SnakeGame(
    gameSpace: Float = 1f / 6.5f,
    cycle: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Card(modifier) {
        var resetKey by remember { mutableIntStateOf(0) }
        key(resetKey) {
            val grid = remember { IntSize(80, 40) }
            var scores: Int by remember { mutableIntStateOf(0) }
            var gameOver by remember { mutableStateOf(false) }
            val world = remember {
                snake(
                    gameSpace = gameSpace,
                    grid = grid,
                    cycle = cycle,
                ).apply {
                    onEvent<SnakeState> {
                        scores = it.scores
                    }
                    onEvent<Unit>(Channel("game_over")) {
                        gameOver = true
                    }
                }
            }
            Column(Modifier.padding(24.dp)) {
                Row {
                    Text("scores:$scores")
                    Spacer(Modifier.width(12.dp))
                    Text("Control: up:⬆,down:⬇,left:⬅,right:➡,pause:P,restart:R")
                }
                var delta by remember { mutableStateOf(0f) }
                var lastFrameTime by remember { mutableStateOf(0L) }
                val focusRequester = remember { FocusRequester() }
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                    while (isActive) {
                        withFrameNanos {
                            if (lastFrameTime != 0L) {
                                delta = ((it - lastFrameTime) / 1_000_000_000f).fastCoerceAtMost(0.05f)
                            }
                            lastFrameTime = it
                        }
                    }
                }
                Box(modifier = Modifier.fillMaxHeight(0.9f).aspectRatio(grid.width.toFloat() / grid.height)) {
                    Canvas(
                        Modifier.fillMaxSize()
                            .focusRequester(focusRequester)
                            .focusable()
                            .onKeyEvent { event ->
                                if (event.type == KeyEventType.KeyDown) {
                                    when (event.key) {
                                        Key.P -> {
                                            world.emit(Unit, Channel("pause"))
                                            true
                                        }

                                        Key.R -> {
                                            world.emit(GameRestartTag)
                                            true
                                        }

                                        else  -> false
                                    }
                                } else false
                            }
                    ) {
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
                    if (gameOver) {
                        Button({
                            resetKey++
                        }, Modifier.align(Alignment.Center)) {
                            Text("Game Over\nscores: $scores\npares to restart", textAlign = TextAlign.Center)
                        }
                    }
                }

            }
        }
    }
}

internal fun snake(
    gameSpace: Float = 1f / 6.5f,
    grid: IntSize = IntSize(80, 40),
    cycle: Boolean = true,
    gridLineWidth: Float = 1.5f,
    borderColor: Color = Colors.WHITE.toComposeColor,
    gridColor: Color = NebulaColor.fromARGB(0x2FFFFFFF).toComposeColor,
    bgColor: Color = NebulaColor.fromARGB(0xFF000000).toComposeColor,
    snakeHeadColor: Color = NebulaColor.fromARGB(0xFFEB57AE).toComposeColor,
    snakePartColor: Color = NebulaColor.fromARGB(0xFFFF7F00).toComposeColor,
    foodColor: Color = NebulaColor.fromARGB(0xFF17E5E5).toComposeColor,
    wallColor: Color = Colors.WHITE.toComposeColor,
) = world {
    fixedStep(gameSpace)

    GameController
    SnakeController
    MoveSystem
    CollisionSystem
    EatSystem
    RenderSystem

    var map: MapGrid? = null

    //Setting
    entity {
        +MapGrid(grid.width.coerceAtLeast(1), grid.height.coerceAtLeast(1), cycle).apply { map = this }
        +GameState(true)
        +RenderSetting(
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

    //Create Snake
    entity {
        val pos = Position(grid.width / 2, grid.height / 2).apply { +this }
        +SnakeHead(LinkedList<Entity>()).apply {
            repeat(4) {
                tails.add(this@world.world.createEntity {
                    +pos.copy(x = pos.x - 1 * (it + 1))
                    +SnakeTail
                    +Collider
                })
            }
        }
        +SnakeState(Direction.RIGHT.canChangeDirection, 0)
        +Direction.RIGHT
    }

    entity {
        +Food
        +Position.random(this@world.world.allComponents<Position>(), map!!)
    }

}


//region Component

private class GameState(
    var running: Boolean,
    var gameOver: Boolean = false
) : Component

private var World.running
    get() = this.firstComponent<GameState>()?.running ?: false
    set(value) {
        this.firstComponent<GameState>()?.let { it.running = value }
    }

private var World.gameOver
    get() = this.firstComponent<GameState>()?.gameOver ?: false
    set(value) {
        this.firstComponent<GameState>()?.let { it.gameOver = value }
    }

private data class RenderSetting(
    val gridLineWidth: Float,
    val borderColor: Color,
    val gridColor: Color,
    val bgColor: Color,
    val snakeHeadColor: Color,
    val snakePartColor: Color,
    val foodColor: Color,
    val wallColor: Color,
) : Component

private data class Position(var x: Int, var y: Int) : Component {
    fun mapRenderRect(gridUnitSize: Size): Rect = Rect(
        Offset(gridUnitSize.width * x, gridUnitSize.height * y),
        Size(gridUnitSize.width, gridUnitSize.height)
    )

    operator fun plusAssign(position: Position) {
        x += position.x
        y += position.y
    }

    fun isPositionOccupied(world: World, position: Position): Boolean =
        world.allComponents<Position>().any { it == position }

    companion object {

        fun random(collisionDetection: Iterable<Position>, map: MapGrid): Position {
            val result = Position(Random().nextInt(map.width), Random().nextInt(map.height))
            return if (result in collisionDetection) {
                random(collisionDetection, map)
            } else result
        }
    }

}

private enum class Direction(val velocity: Position) : Component {
    UP(Position(0, -1)),
    DOWN(Position(0, 1)),
    LEFT(Position(-1, 0)),
    RIGHT(Position(1, 0));

    val canChangeDirection: Set<Direction>
        get() = when (this) {
            UP, DOWN    -> up_down
            LEFT, RIGHT -> left_right
        }

    companion object {
        private val up_down = setOf(RIGHT, LEFT)
        private val left_right = setOf(UP, DOWN)
    }
}

private class MapGrid(val width: Int, val height: Int, val cycle: Boolean) : Component {
    fun inBounds(x: Int, y: Int): Boolean = x in 0 until width && y in 0 until height

    fun inBounds(position: Position) = inBounds(position.x, position.y)

    fun resetOnCycle(position: Position) {
        if (cycle) {
            if (position.x < 0) position.x += width
            if (position.x >= width) position.x -= width
            if (position.y < 0) position.y += height
            if (position.y >= height) position.y -= height
            if (!inBounds(position)) resetOnCycle(position)
        }
    }
}

private class SnakeState(
    var canChangeDirection: Set<Direction>,
    var scores: Int
) : Component

private data object SnakeTail : Component

private data class SnakeHead(val tails: LinkedList<Entity>) : Component

private data object Food : Component

private data object Wall : Component

private data object Collider : Component

internal data object GameRestartTag : Component

//endregion

//region System

//region Input
private val WorldBuilder.GameController
    get() = system {
        onInit { world ->
            world.onEvent<Unit>(Channel("pause")) {
                world.running = !world.running
            }
            world.onEvent<GameRestartTag> {
                world.createEntity { +it }
            }
        }
    }

private val inputCode = listOf(Keyboard.UP, Keyboard.DOWN, Keyboard.LEFT, Keyboard.RIGHT)
private val WorldBuilder.SnakeController
    get() = system {
        onUpdate { world, _ ->
            val pressed = inputCode.any { InputHandler.wasKeyPressed(it) }
            if (pressed) {
                world.query<SnakeHead, Direction, SnakeState>().each {
                    val dir = it.get<Direction>()!!
                    val state = it.get<SnakeState>()!!
                    it + when {
                        InputHandler.wasKeyPressed(Keyboard.UP)    -> if (Direction.UP in state.canChangeDirection) Direction.UP else dir
                        InputHandler.wasKeyPressed(Keyboard.DOWN)  -> if (Direction.DOWN in state.canChangeDirection) Direction.DOWN else dir
                        InputHandler.wasKeyPressed(Keyboard.LEFT)  -> if (Direction.LEFT in state.canChangeDirection) Direction.LEFT else dir
                        InputHandler.wasKeyPressed(Keyboard.RIGHT) -> if (Direction.RIGHT in state.canChangeDirection) Direction.RIGHT else dir
                        else                                       -> dir
                    }
                }
            }
        }
    }
//endregion

private val WorldBuilder.MoveSystem
    get() = system {
        onFixedUpdate { world, f ->
            if (!world.running) return@onFixedUpdate
            val map = world.firstComponent<MapGrid>() ?: return@onFixedUpdate
            world.query<SnakeHead, Position, Direction>().each { entity ->
                val head = entity.get<SnakeHead>()!!
                val pos = entity.get<Position>()!!
                val dir = entity.get<Direction>()!!
                val oldHeadPos = pos.copy()

                pos += dir.velocity

                entity.get<SnakeState>()?.let { it.canChangeDirection = dir.canChangeDirection }

                if (!map.inBounds(pos)) {
                    if (map.cycle) {
                        map.resetOnCycle(pos)
                    } else {
                        world.gameOver = true
                        world.running = false
                        world.emit(Unit, Channel("game_over"))
                        return@onFixedUpdate
                    }
                }

                if (head.tails.isNotEmpty()) {
                    if (head.tails.size > 1 && head.tails[0] == head.tails[1].get<Position>()) {
                        val first = head.tails.first()
                        first.get<Position>()?.let {
                            it.x = oldHeadPos.x
                            it.y = oldHeadPos.y
                        }
                    } else {
                        val last = head.tails.removeLast()
                        head.tails.addFirst(last)
                        last.get<Position>()?.let {
                            it.x = oldHeadPos.x
                            it.y = oldHeadPos.y
                        }
                    }
                }

            }
        }
    }

private val WorldBuilder.CollisionSystem
    get() = system {
        onFixedUpdate { world, f ->
            if (!world.running) return@onFixedUpdate
            val collisions = world.query<Collider, Position>().asSequence().map {
                it to it.get<Position>()!!
            }

            world.query<SnakeHead, Position>().each { entity ->
                val pos = entity.get<Position>()!!
                collisions.forEach { (collisionsEntity, position) ->
                    if (collisionsEntity.id != entity.id && pos == position) {
                        world.gameOver = true
                        world.running = false
                        world.emit(Unit, Channel("game_over"))
                        return@onFixedUpdate
                    }
                }
            }

        }
    }

private val WorldBuilder.EatSystem
    get() = system {
        onFixedUpdate { world, f ->
            if (!world.running) return@onFixedUpdate
            val foods = world.query<Food, Position>().asSequence().map {
                it to it.get<Position>()!!
            }

            world.query<SnakeHead, Position>().each { entity ->
                val head = entity.get<SnakeHead>()!!
                val pos = entity.get<Position>()!!
                foods.forEach { (food, position) ->
                    if (food.id != entity.id && pos == position) {
                        food.remove<Food>()
                        food + SnakeTail
                        food + Collider
                        head.tails.addFirst(food)

                        world.createEntity {
                            val map = world.firstComponent<MapGrid>() ?: return@createEntity
                            +Food
                            +Position.random(world.allComponents<Position>(), map)
                        }
                        entity.get<SnakeState>()?.let {
                            it.scores += 1
                            world.emit(it)
                        }
                    }
                }

            }
        }
    }


internal class DrawScopeSupplier(val scope: DrawScope) : Component

private val WorldBuilder.RenderSystem
    get() = system {
        onUpdate { world, f ->
            world.firstComponent<DrawScopeSupplier>()?.scope?.apply {
                val map = world.firstComponent<MapGrid>() ?: return@apply
                val setting = world.firstComponent<RenderSetting>() ?: return@apply

                val canvasSize = this.size

                val mapRect = Rect(Offset.Zero, canvasSize)
                val gridUnitSize = Size(canvasSize.width / map.width, canvasSize.height / map.height)

                val lineWith = setting.gridLineWidth

                val snakeHeads = world.query<SnakeHead, Position>().asSequence().map { it.get<Position>()!!.mapRenderRect(gridUnitSize) }
                val snakeTails = world.query<SnakeTail, Position>().asSequence().map { it.get<Position>()!!.mapRenderRect(gridUnitSize) }

                //BG
                drawRect(setting.bgColor, mapRect.topLeft, mapRect.size)
                //Grid
                drawRect(setting.borderColor, mapRect.topLeft, mapRect.size, style = Stroke(width = lineWith * 2))
                //Snake
                snakeTails.forEach {
                    drawRect(setting.snakePartColor, it.topLeft, it.size)
                }
                snakeHeads.forEach {
                    drawRect(setting.snakeHeadColor, it.topLeft, it.size)
                }
                //Food
                world.query<Food, Position>().each {
                    val rect = it.get<Position>()!!.mapRenderRect(gridUnitSize)
                    drawRect(setting.foodColor, rect.topLeft, rect.size)
                }
                //Wall
                world.query<Wall, Position>().each {
                    val rect = it.get<Position>()!!.mapRenderRect(gridUnitSize)
                    drawRect(setting.wallColor, rect.topLeft, rect.size)
                }
                //Grid
                val boldLineWidth = lineWith * 2f
                (1..map.width).forEach { mx ->
                    val x = mapRect.left + mx * gridUnitSize.width
                    drawLine(
                        setting.gridColor,
                        Offset(x, mapRect.top),
                        Offset(x, mapRect.bottom),
                        strokeWidth = if (mx % 10 == 0) boldLineWidth else lineWith
                    )
                }
                (1..map.height).forEach { my ->
                    val y = mapRect.top + my * gridUnitSize.height
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

//endregion


