package moe.forpleuvoir.ibukigourd.gui.util

enum class Direction {
    Top,
    Bottom,
    Left,
    Right;

    companion object {

        val clockwiseFromLeft = listOf(Left, Top, Right, Bottom)
        val clockwiseFromTop = listOf(Top, Right, Bottom, Left)
        val clockwiseFromRight = listOf(Right, Bottom, Left, Top)
        val clockwiseFromBottom = listOf(Bottom, Left, Top, Right)

        val counterClockwiseFromLeft = listOf(Left, Bottom, Right, Top)
        val counterClockwiseFromTop = listOf(Top, Left, Bottom, Right)
        val counterClockwiseFromRight = listOf(Right, Top, Left, Bottom)
        val counterClockwiseFromBottom = listOf(Bottom, Right, Top, Left)

        val leftRightTopBottom = listOf(Left, Right, Top, Bottom)
        val rightLeftTopBottom = listOf(Right, Left, Top, Bottom)
        val topBottomLeftRight = listOf(Top, Bottom, Left, Right)
        val bottomTopLeftRight = listOf(Bottom, Top, Left, Right)
        val leftRightBottomTop = listOf(Left, Right, Bottom, Top)
        val rightLeftBottomTop = listOf(Right, Left, Bottom, Top)
        val topBottomRightLeft = listOf(Top, Bottom, Right, Left)
        val bottomTopRightLeft = listOf(Bottom, Top, Right, Left)

    }

}