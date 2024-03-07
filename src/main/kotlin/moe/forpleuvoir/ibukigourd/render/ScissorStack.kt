package moe.forpleuvoir.ibukigourd.render

import moe.forpleuvoir.ibukigourd.render.base.math.plus
import moe.forpleuvoir.ibukigourd.render.shape.rectangle.Rect
import moe.forpleuvoir.ibukigourd.render.shape.rectangle.Rectangle
import org.joml.Vector3fc
import java.util.*

class ScissorStack {

    private val stack: Deque<Rectangle> = ArrayDeque()

    private val offsetStack: Deque<Vector3fc> = ArrayDeque()

    fun rest() {
        stack.clear()
        offsetStack.clear()
    }

    fun push(rectangle: Rectangle): Rectangle {
        val rect: Rectangle? = stack.peekLast()
        if (rect != null) {
            val rect2 = rectangle.intersection(rect)
            stack.addLast(rect2)
            return rect2
        }
        stack.addLast(rectangle)
        return rectangle
    }

    fun pop(): Rectangle? {
        stack.removeLast()
        return stack.peekLast()
    }

    fun peek(): Rectangle? {
        val peek = stack.peekLast() ?: return null
        peekOffset()?.let {
            val pos = peek.position + it
            return Rect(pos, peek)
        }
        return peek
    }

    fun pushOffset(offset: Vector3fc): Vector3fc {
        val o: Vector3fc? = offsetStack.peekLast()
        if (o != null) {
            val o2 = offset + o
            offsetStack.addLast(o2)
            return o2
        }
        offsetStack.addLast(offset)
        return offset
    }

    fun popOffset(): Vector3fc? {
        offsetStack.removeLast()
        return offsetStack.peekLast()
    }

    fun peekOffset(): Vector3fc? {
        return offsetStack.peekLast()
    }

}