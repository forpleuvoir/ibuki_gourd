package moe.forpleuvoir.ibukigourd

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.compose_minecraft.ComposeWarmup
import moe.forpleuvoir.compose_minecraft.platform.render.MinecraftRenderPlugins
import moe.forpleuvoir.compose_minecraft.platform.screen.ComposeScreenDefaults
import moe.forpleuvoir.compose_minecraft.platform.screen.ScreenCrash
import moe.forpleuvoir.ibukigourd.api.ClientResourceReloaderListener
import moe.forpleuvoir.ibukigourd.config.ClientModConfigHandler
import moe.forpleuvoir.ibukigourd.event.events.client.ClientLifecycleEvent
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.mod.IbukiGourdModScreen
import moe.forpleuvoir.ibukigourd.mod.command.IbukiGourdCommand
import moe.forpleuvoir.ibukigourd.mod.config.IGConfig
import moe.forpleuvoir.ibukigourd.mod.ibukiGourdModScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.draw.SokitsuBubbleSpritePlugin
import moe.forpleuvoir.ibukigourd.ui.sokitsu.draw.SokitsuSpritePlugin
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuAtlasManager
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMetaLoader
import moe.forpleuvoir.ibukigourd.ui.sokitsu.toast.ToastHandler
import moe.forpleuvoir.ibukigourd.ui.sokitsu.toast.ToastOverlay
import moe.forpleuvoir.ibukigourd.ui.sokitsu.toast.ToastStrategy
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.ibukigourd.util.mc

object IbukiGourdClient {

    private val logger = logger()

    /** 崩溃提示的标识:同一次会话内反复崩溃只保留最新一条。 */
    private const val SCREEN_CRASH_TAG = "compose_screen_crash"

    /** 崩溃提示里异常消息的宽度上限(消息按此换行,最多三行)。 */
    private val crashDetailMaxWidth = 320.dp

    private val inits = listOf(
        ClientModConfigHandler,
        IbukiGourdCommand
    )

    private val clientResourceReloaderListener = listOf(
        SokitsuAtlasManager,
        SokitsuThemeMetaLoader
    )

    fun addClientResourceReloaderListener(listener: (ClientResourceReloaderListener) -> Unit) {
        clientResourceReloaderListener.forEach(listener)
    }


    fun init() {
        val initPhase = "${IbukiGourd.MOD_ID}:init"
        inits.forEach { it.init() }
        ClientLifecycleEvent.Starting.register(initPhase) {
            it.schedule {
                ComposeWarmup.warmup(::IbukiGourdModScreen)
            }
        }
        ClientModConfigHandler.register(IGConfig)
        ToastOverlay.install()
        // 界面崩溃恢复:补错误日志与提示
        ComposeScreenDefaults.onScreenCrash = ::reportScreenCrash
        MinecraftRenderPlugins.register(SokitsuSpritePlugin)
        MinecraftRenderPlugins.register(SokitsuBubbleSpritePlugin)
    }

    /**
     * 界面崩溃上报:记一条错误日志(含堆栈),并弹一条提示说明异常类型与阶段 / 消息。
     *
     * 由平台在游戏主线程回调,此时崩溃屏的场景已拆除、屏即将关闭。
     */
    private fun reportScreenCrash(crash: ScreenCrash) {
        val cause = crash.cause
        logger.error("Compose screen crashed during ${crash.phase}: ${cause.message}", cause)
        val detail = buildString {
            append(crash.phase)
            cause.message?.let { append(" · ").append(it) }
        }
        ToastHandler.showContent(strategy = ToastStrategy.Tagged.Replace(SCREEN_CRASH_TAG)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(IGLang.Misc.screenCrash(cause.javaClass.simpleName))
                Text(
                    detail,
                    modifier = Modifier.widthIn(max = crashDetailMaxWidth),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }

}