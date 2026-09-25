package moe.forpleuvoir.ibukigourd.ui

import moe.forpleuvoir.compose_minecraft.platform.screen.ComposeScreen
import moe.forpleuvoir.ibukigourd.util.mc

/**
 * Compose 屏幕的判定与渲染开关：供原版补丁（mixin）在不依赖界面内部结构的情况下查询。
 *
 * 仅回答"当前屏幕是不是 Compose 屏幕"这类问题，不持有任何界面状态。
 */
object ComposeScreenHelper {

    /**
     * 打开 Compose 屏幕时是否解除原版"界面内 60 帧"限制；由配置项 `screen.unlimit_framerate` 写入。
     *
     * 缓存在这里而不是每次读配置：查询发生在每帧的帧率上限计算里，配置读取走的是委托链。
     */
    @JvmStatic
    var unlimitFramerate: Boolean = true

    /** 当前最上层屏幕是否是 [ComposeScreen]。 */
    @JvmStatic
    fun isComposeScreen(): Boolean = mc.gui.screen() is ComposeScreen

    /**
     * 是否需要解除帧数限制：开关打开且当前开着 Compose 屏幕。
     *
     * 无世界（标题界面 / 世界加载中）且开着界面时，原版把帧数压到 60；像素动画在这类界面上最明显，
     * 所以只在这种情况下放行，其余档位（窗口最小化 / 挂机）仍走原版。
     */
    @JvmStatic
    fun shouldUnlimitFramerate(): Boolean = unlimitFramerate && isComposeScreen()
}
