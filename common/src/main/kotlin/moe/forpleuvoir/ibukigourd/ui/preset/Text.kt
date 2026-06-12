package moe.forpleuvoir.ibukigourd.ui.preset

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import it.unimi.dsi.fastutil.booleans.BooleanConsumer
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.text.flat
import moe.forpleuvoir.ibukigourd.ui.toast.ToastHandler
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.truncate
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.ConfirmLinkScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.commands.Commands
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.util.Util
import java.net.URI

@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun Text(
    component: Component,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    autoSize: TextAutoSize? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    noinline onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
) {
    androidx.compose.material3.Text(
        text = component.toAnnotatedString(),
        modifier = modifier,
        color = color,
        autoSize = autoSize,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        inlineContent = inlineContent,
        onTextLayout = onTextLayout,
        style = style
    )
}

fun Component.toAnnotatedString(): AnnotatedString = buildAnnotatedString {
    flat().forEach { c ->
        val s = c.style
        if (s != Style.EMPTY) {
            // 收集样式
            val color = s.color?.let { Color(it.value).copy(alpha = 1f) } ?: Color.Unspecified
            val fontWeight = if (s.bold == true) FontWeight.Bold else null
            val fontStyle = if (s.italic == true) FontStyle.Italic else null
            val textDecoration = buildList {
                if (s.strikethrough == true) add(TextDecoration.LineThrough)
                if (s.underlined == true) add(TextDecoration.Underline)
            }.takeIf { it.isNotEmpty() }?.let { TextDecoration.combine(it) }
            val shadow = s.shadowColor?.let { Shadow(Color(it).copy(alpha = 1f), Offset(2f, 2f)) }

            // 点击事件（ClickableText 需要）
            s.clickEvent?.let { event ->
                val link = LinkAnnotation.Clickable(event.action().serializedName) {
                    when (event) {
                        is ClickEvent.OpenFile        -> Util.getPlatform().openFile(event.file())
                        is ClickEvent.OpenUrl         -> clickUrlAction(uri = event.uri())
                        is ClickEvent.RunCommand      -> mc.player?.connection?.sendUnattendedCommand(Commands.trimOptionalPrefix(event.command), mc.screen)
                        is ClickEvent.CopyToClipboard -> {
                            mc.keyboardHandler.clipboard = event.value
                            ToastHandler.showContent { Text(IGLang.Misc.copySuccess(event.value.truncate(10))) }
                        }
                    }
                }
                pushLink(link)
            }
            // 应用样式并追加文字
            withStyle(
                SpanStyle(
                    color = color,
                    fontWeight = fontWeight,
                    fontStyle = fontStyle,
                    textDecoration = textDecoration,
                    shadow = shadow
                )
            ) {
                append(c.string)
            }
            if (s.clickEvent != null) pop()
        } else {
            append(c.string)
        }
    }
}

private fun clickUrlAction(minecraft: Minecraft = mc, screen: Screen? = mc.screen, uri: URI): Boolean {
    if (!minecraft.options.chatLinks().get()) {
        return false
    } else {
        if (minecraft.options.chatLinksPrompt().get()) {
            minecraft.setScreen(ConfirmLinkScreen({ result: Boolean ->
                if (result) {
                    Util.getPlatform().openUri(uri)
                }
                minecraft.setScreen(screen)
            }, uri.toString(), false))
        } else {
            Util.getPlatform().openUri(uri)
        }

        return true
    }
}
