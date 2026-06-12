package moe.forpleuvoir.ibukigourd.ui.platformcontext

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.NativeClipboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.nebula.common.util.ioLaunch
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable

/**
 * 弹出层可能需要手动提供此对象
 * ```kotlin
 *  CompositionTextContextProvider {
 *      //TODO
 *  }
 *
 * ```
 */
object MinecraftClipboard : Clipboard {

    private val awtClipboard = java.awt.datatransfer.Clipboard(IbukiGourd.MOD_ID)

    override val nativeClipboard: NativeClipboard = awtClipboard

    @OptIn(ExperimentalComposeUiApi::class)
    override suspend fun getClipEntry(): ClipEntry {
        val text = mc.keyboardHandler.clipboard
        return ClipEntry(StringSelection(text))
    }

    fun setClipboardText(text: String) {
        mc.keyboardHandler.clipboard = text
        ioLaunch { awtClipboard.setContents(StringSelection(text), null) }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    override suspend fun setClipEntry(clipEntry: ClipEntry?) {
        val transferable = clipEntry?.nativeClipEntry as? Transferable
        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            ioLaunch {
                runCatching {
                    val text = transferable.getTransferData(DataFlavor.stringFlavor) as? String
                    if (text != null) {
                        awtClipboard.setContents(StringSelection(text), null)
                        mc.keyboardHandler.clipboard = text
                    }
                }.onFailure { cause ->
                    cause.printStackTrace()
                }
            }
        }
    }
}