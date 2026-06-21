package moe.forpleuvoir.ibukigourd.mod.waht.ecs

/**
 * ECS 组件标记接口。
 *
 * 推荐用 `data class` 承载纯数据,每实体同类型组件最多 1 个实例。
 *
 * 示例:
 * ```kotlin
 * data class Position(var x: Float, var y: Float) : Component
 * ```
 */
interface Component

/**
 * 限制 ECS DSL 作用域,防止嵌套 lambda 误调外层接收者。
 */
@DslMarker
annotation class EcsDsl
