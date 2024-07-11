package moe.forpleuvoir.ibukigourd.gui.base.measure

interface IntrinsicMeasurable {

    val parentData: Any?

    fun minIntrinsicWidth(height: Float): Float


    fun maxIntrinsicWidth(height: Float): Float


    fun minIntrinsicHeight(width: Float): Float


    fun maxIntrinsicHeight(width: Float): Float

}