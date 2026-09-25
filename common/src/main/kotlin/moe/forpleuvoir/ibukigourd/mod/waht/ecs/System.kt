package moe.forpleuvoir.ibukigourd.mod.waht.ecs

/**
 * 系统:每帧/每固定步长对世界进行逻辑更新。
 *
 * - [fixedUpdate] 在固定步长下调用(适合物理积分等需要稳定时间步的逻辑);
 * - [update] 每帧调用一次,参数 [dt] 为实际帧间隔(可能波动)。
 *
 * 两者执行顺序见 [World.update]。两类回调都在 `World.update(dt)` 单次调用内完成。
 */
interface System {

    /** 被加入世界时调用一次。 */
    fun onAdded(world: World) {}

    /** 被移除时调用一次。 */
    fun onRemoved(world: World) {}

    /** 每帧更新。[dt] 为距上一帧的实际秒数。 */
    fun update(world: World, dt: Float) {}

    /** 固定步长更新。[dt] 为 [World.fixedStep](固定值)。 */
    fun fixedUpdate(world: World, dt: Float) {}
}

/**
 * 以函数式方式定义的 [System]。
 *
 * 适合简单逻辑,避免为每个小系统单独写一个类。
 */
class FunctionalSystem(
    val onInit: (World) -> Unit = {},
    val onUpdate: (World, Float) -> Unit = { _, _ -> },
    val onFixedUpdate: (World, Float) -> Unit = { _, _ -> },
) : System {
    override fun onAdded(world: World) = onInit(world)
    override fun update(world: World, dt: Float) = onUpdate(world, dt)
    override fun fixedUpdate(world: World, dt: Float) = onFixedUpdate(world, dt)
}

/**
 * 系统 DSL 作用域,在 [World.system] 的 lambda 接收者中使用。
 */
@EcsDsl
class SystemScope @PublishedApi internal constructor() {

    internal var init: ((World) -> Unit)? = null
    internal var update: ((World, Float) -> Unit)? = null
    internal var fixedUpdate: ((World, Float) -> Unit)? = null

    /** 注册加入世界时的回调(等价 [System.onAdded])。 */
    fun onInit(block: (World) -> Unit) {
        init = block
    }

    /** 注册每帧回调(等价 [System.update])。 */
    fun onUpdate(block: (World, Float) -> Unit) {
        update = block
    }

    /** 注册固定步长回调(等价 [System.fixedUpdate])。 */
    fun onFixedUpdate(block: (World, Float) -> Unit) {
        fixedUpdate = block
    }
}
