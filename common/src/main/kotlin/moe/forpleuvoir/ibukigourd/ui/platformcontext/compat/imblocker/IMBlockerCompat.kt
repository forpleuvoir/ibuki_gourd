package moe.forpleuvoir.ibukigourd.ui.platformcontext.compat.imblocker

import androidx.compose.ui.platform.PlatformTextInputMethodRequest
import moe.forpleuvoir.ibukigourd.platform.PLATFORM
import moe.forpleuvoir.ibukigourd.util.logger

object IMBlockerCompat {

    private val logger = logger()

    private const val IMPLEMENTATION_CLASS =
        "moe.forpleuvoir.ibukigourd.ui.platformcontext.compat.imblocker.IMBlockerCompatImpl"

    private val implementation: Implementation? by lazy {
        if (!PLATFORM.isModLoaded("imblocker")) {
            return@lazy null
        }

        runCatching {
            val clazz = Class.forName(
                IMPLEMENTATION_CLASS,
                true,
                IMBlockerCompat::class.java.classLoader,
            )

            val method = clazz.getDeclaredMethod(
                "requestTextInputFocus",
                PlatformTextInputMethodRequest::class.java,
            )

            Implementation { request ->
                method.invoke(null, request) as IMBlockerFocusSession
            }
        }.onFailure {
            logger.error("Failed to initialize IMBlocker compatibility", it)
        }.getOrNull()
    }

    fun requestTextInputFocus(
        request: PlatformTextInputMethodRequest,
    ): IMBlockerFocusSession? {
        return runCatching {
            implementation?.requestTextInputFocus(request)
        }.onFailure {
            logger.error("Failed to register Compose text input with IMBlocker", it)
        }.getOrNull()
    }

    private fun interface Implementation {

        fun requestTextInputFocus(
            request: PlatformTextInputMethodRequest,
        ): IMBlockerFocusSession
    }
}