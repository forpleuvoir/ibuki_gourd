package moe.forpleuvoir.ibukigourd.gui.base.layout.arrange

sealed interface Orientation {

    data object Vertical : Orientation

    data object Horizontal : Orientation

}

inline fun <R> Orientation.peek(vertical: (Orientation.Vertical) -> R, horizontal: (Orientation.Horizontal) -> R): R {
    return when (this) {
        is Orientation.Vertical   -> vertical(this)
        is Orientation.Horizontal -> horizontal(this)
    }
}

fun <R> Orientation.peek(vertical: R, horizontal: R): R {
    return when (this) {
        is Orientation.Vertical   -> vertical
        is Orientation.Horizontal -> horizontal
    }
}