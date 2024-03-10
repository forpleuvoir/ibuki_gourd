package moe.forpleuvoir.ibukigourd.gui.render

import moe.forpleuvoir.ibukigourd.gui.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.render.math.plus
import org.joml.Vector2fc
import java.util.*

class ScissorStack {

    private val stack: Deque<Box> = ArrayDeque()

    private val offsetStack: Deque<Vector2fc> = ArrayDeque()

    fun rest() {
        stack.clear()
        offsetStack.clear()
    }

    fun push(box: Box): Box {
        val rect: Box? = stack.peekLast()
        if (rect != null) {
            val rect2 = box intersectWith rect
            stack.addLast(rect2)
            return rect2
        }
        stack.addLast(box)
        return box
    }

    fun pop(): Box? {
        stack.removeLast()
        return stack.peekLast()
    }

    fun peek(): Box? {
        val peek = stack.peekLast() ?: return null
        peekOffset()?.let {
            val pos = peek.position + it
            return Box(pos, peek)
        }
        return peek
    }

    fun pushOffset(offset: Vector2fc): Vector2fc {
        val o: Vector2fc? = offsetStack.peekLast()
        if (o != null) {
            val o2 = offset + o
            offsetStack.addLast(o2)
            return o2
        }
        offsetStack.addLast(offset)
        return offset
    }

    fun popOffset(): Vector2fc? {
        offsetStack.removeLast()
        return offsetStack.peekLast()
    }

    fun peekOffset(): Vector2fc? {
        return offsetStack.peekLast()
    }

}