package moe.forpleuvoir.ibukigourd.ui.sokitsu.toast

import moe.forpleuvoir.ibukigourd.ui.overlay.OverlayService
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuPixelScale
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuTheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.currentSokitsuColorScheme
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 提示接在[全局覆盖层][OverlayService]上的方式：注册一个常驻条目渲染 [ToastContainer]，
 * 并注册一个每帧回调推进 [ToastHandler.tick]。
 *
 * 提示于是与覆盖层上的其它内容共用同一个常驻场景与帧钩子，本身不再持有场景与渲染器；
 * [ToastHandler.isIdle]（既无活动提示也无排队提示）时条目 `present` 为 `false`，
 * 覆盖层整帧跳过组合与绘制。
 *
 * 配色取 [ToastHandler.activeScheme]，为 `null` 时跟随当前主题
 * （[currentSokitsuColorScheme]）——"提示是否跟界面走"由配置决定。覆盖层场景根
 * （`SokitsuScreenRoot`）已按窗口分辨率下发密度与像素放大倍率，条目内这层主题只为换配色，
 * 因此把 `pixelScale` 原样接回来（[LocalSokitsuPixelScale]），否则会把档位覆盖回主题缺省值。
 *
 * 由客户端入口 `IbukiGourdClient.init()` 安装一次。
 */
object ToastOverlay {

    private const val KEY = "ibukigourd:toast"

    private val installed = AtomicBoolean()

    /** 安装提示条目与帧回调（幂等）。 */
    fun install() {
        if (!installed.compareAndSet(false, true)) return
        OverlayService.register(KEY, present = { !ToastHandler.isIdle }) {
            SokitsuTheme(
                colorScheme = ToastHandler.activeScheme ?: currentSokitsuColorScheme(),
                pixelScale = LocalSokitsuPixelScale.current,
            ) {
                ToastContainer()
            }
        }
        OverlayService.registerFrameListener { ToastHandler.tick(it) }
    }
}
