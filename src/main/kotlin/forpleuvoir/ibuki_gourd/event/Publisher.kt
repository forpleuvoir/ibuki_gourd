package forpleuvoir.ibuki_gourd.event


/**
 * 事件发布者

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.event

 * 文件名 Publisher

 * 创建时间 2021/12/9 11:22

 * @author forpleuvoir

 */
interface Publisher {
	/**
	 * 发布事件
	 * @return Unit
	 */
	fun broadcast()
}