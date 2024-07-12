package moe.forpleuvoir.ibukigourd.gui.base.measure

interface IntrinsicMeasurable {

    val parentData: Any?

    /**
     * 测量出最小宽度
     * @param height 父组件提供的最大高度
     */
    fun minIntrinsicWidth(height: Float): Float

    /**
     * 测量出最大宽度
     * @param height 父组件提供的最大高度
     */
    fun maxIntrinsicWidth(height: Float): Float

    /**
     * 测量出最小高度
     * @param width 父组件提供的最大宽度
     */
    fun minIntrinsicHeight(width: Float): Float

    /**
     * 测量出最大高度
     * @param width 父组件提供的最大宽度
     */
    fun maxIntrinsicHeight(width: Float): Float

}