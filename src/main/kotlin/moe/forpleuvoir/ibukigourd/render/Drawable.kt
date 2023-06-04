package moe.forpleuvoir.ibukigourd.render

interface Drawable {

	/**
	 * 可见
	 */
	val visible: Boolean get() = true

	/**
	 * Z轴偏移 越低越先渲染
	 */
	val renderPriority: Int get() = 0

	/**
	 * 渲染
	 */
	val render: (renderContext: RenderContext) -> Unit

	/**
	 * 渲染
	 * @param renderContext MatrixStack
	 * @param delta Float
	 */
	fun onRender(renderContext: RenderContext)

}