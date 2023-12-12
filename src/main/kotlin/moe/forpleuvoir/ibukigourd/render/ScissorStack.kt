package moe.forpleuvoir.ibukigourd.render

import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.graphics.rectangle.Rectangle
import moe.forpleuvoir.ibukigourd.render.graphics.rectangle.rect
import java.util.*

class ScissorStack {

    private val stack: Deque<Rectangle<Vector3<Float>>> = ArrayDeque()

    private val offsetStack: Deque<Vector3<Float>> = ArrayDeque()

    fun rest() {
        stack.clear()
        offsetStack.clear()
    }

    fun push(rectangle: Rectangle<Vector3<Float>>): Rectangle<Vector3<Float>> {
        val rect: Rectangle<Vector3<Float>>? = stack.peekLast()
        if (rect != null) {
            val rect2 = rectangle.intersection(rect)
            stack.addLast(rect2)
            return rect2
        }
        stack.addLast(rectangle)
        return rectangle
    }

    fun pop(): Rectangle<Vector3<Float>>? {
        stack.removeLast()
        return stack.peekLast()
    }

    fun peek(): Rectangle<Vector3<Float>>? {
        val peek = stack.peekLast() ?: return null
        peekOffset()?.let {
            val pos = peek.position + it
            return rect(pos, peek)
        }
        return peek
    }

    fun pushOffset(offset: Vector3<Float>): Vector3<Float> {
        val o: Vector3<Float>? = offsetStack.peekLast()
        if (o != null) {
            val o2 = offset + o
            offsetStack.addLast(o2)
            return o2
        }
        offsetStack.addLast(offset)
        return offset
    }

    fun popOffset(): Vector3<Float>? {
        offsetStack.removeLast()
        return offsetStack.peekLast()
    }

    fun peekOffset(): Vector3<Float>? {
        return offsetStack.peekLast()
    }

}