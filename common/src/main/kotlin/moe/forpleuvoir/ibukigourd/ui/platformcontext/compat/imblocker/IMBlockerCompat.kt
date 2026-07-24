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

            val requestTextInputFocusMethod =
                clazz.getDeclaredMethod(
                    "requestTextInputFocus",
                    PlatformTextInputMethodRequest::class.java,
                )

            val updateCaretPositionMethod =
                clazz.getDeclaredMethod(
                    "updateCaretPosition",
                )

            object : Implementation {

                override fun requestTextInputFocus(
                    request: PlatformTextInputMethodRequest,
                ): IMBlockerFocusSession {
                    return requestTextInputFocusMethod.invoke(
                        null,
                        request,
                    ) as IMBlockerFocusSession
                }

                override fun updateCaretPosition() {
                    updateCaretPositionMethod.invoke(null)
                }
            }
        }.onFailure {
            logger.error("Failed to initialize IMBlocker compatibility", it)
        }.getOrNull()
    }

    fun updateCaretPosition() {
        runCatching {
            implementation?.updateCaretPosition()
        }.onFailure {
            logger.error(
                "Failed to update IMBlocker composition position",
                it,
            )
        }
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

    private interface Implementation {

        fun requestTextInputFocus(
            request: PlatformTextInputMethodRequest,
        ): IMBlockerFocusSession

        fun updateCaretPosition()
    }
}