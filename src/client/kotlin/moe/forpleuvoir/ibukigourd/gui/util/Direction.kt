package moe.forpleuvoir.ibukigourd.gui.util

enum class Direction {
    Top,
    Right,
    Bottom,
    Left;

    companion object {

        val clockwiseFromLeft = listOf(Left, Top, Right, Bottom)
        val clockwiseFromTop = listOf(Top, Right, Bottom, Left)
        val clockwiseFromRight = listOf(Right, Bottom, Left, Top)
        val clockwiseFromBottom = listOf(Bottom, Left, Top, Right)

        val counterClockwiseFromLeft = listOf(Left, Bottom, Right, Top)
        val counterClockwiseFromTop = listOf(Top, Left, Bottom, Right)
        val counterClockwiseFromRight = listOf(Right, Top, Left, Bottom)
        val counterClockwiseFromBottom = listOf(Bottom, Right, Top, Left)
    }

}