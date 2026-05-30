package moe.forpleuvoir.ibukigourd.ui.platformcontext

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.NativeClipboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.Minecraft
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable


private val awtClipboard = java.awt.datatransfer.Clipboard(IbukiGourd.MOD_ID)

/**
 * 弹出层可能需要手动提供此对象
 * ```kotlin
 *  CompositionLocalProvider(LocalClipboard provides MinecraftClipboard) {
 *      //TODO
 *  }
 *
 * ```
 */
object MinecraftClipboard : Clipboard {
    override val nativeClipboard: NativeClipboard = awtClipboard

    @OptIn(ExperimentalComposeUiApi::class)
    override suspend fun getClipEntry(): ClipEntry {
        val text = mc.keyboardHandler.clipboard
        return ClipEntry(StringSelection(text))
    }

    fun setClipboardText(text: String) {
        mc.keyboardHandler.clipboard = text
        awtClipboard.setContents(StringSelection(text), null)
    }

    @OptIn(ExperimentalComposeUiApi::class)
    override suspend fun setClipEntry(clipEntry: ClipEntry?) {
        val transferable = clipEntry?.nativeClipEntry as? Transferable
        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                val text = withContext(Dispatchers.IO) {
                    transferable.getTransferData(DataFlavor.stringFlavor)
                } as? String
                if (text != null) {
                    mc.keyboardHandler.clipboard = text
                    awtClipboard.setContents(StringSelection(text), null)
                }
            } catch (_: Exception) {
            }
        }
    }
}