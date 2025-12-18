package moe.forpleuvoir.ibukigourd.util

 /**
 * 对 [Sequence] 进行分页操作
 *
 * @param T 序列元素类型
 * @param page 页码，从 1 开始计数
 * @param pageSize 每页大小，必须大于 0
 * @return 分页后的 [Sequence]，如果页码超出范围则返回空序列
 * @throws IllegalArgumentException 当 [page] 小于 1 或 [pageSize] 小于等于 0 时抛出
 *
 **/
fun <T> Sequence<T>.page(page: Int, pageSize: Int): Sequence<T> {
    require(page >= 1) { "Page number must be greater than or equal to 1, got $page" }
    require(pageSize > 0) { "Page size must be greater than 0, got $pageSize" }
    return this.drop(pageSize * (page - 1)).take(pageSize)
}