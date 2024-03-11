package moe.forpleuvoir.ibukigourd.text.style

import net.minecraft.text.ClickEvent
import net.minecraft.text.Style

inline fun <reified T : ClickEventAction> Style.click(value: String):Style {
    val event = when (T::class) {
        OpenUrl::class         -> ClickEvent(ClickEvent.Action.OPEN_URL, value)
        OpenFile::class        -> ClickEvent(ClickEvent.Action.OPEN_FILE, value)
        RunCommand::class      -> ClickEvent(ClickEvent.Action.RUN_COMMAND, value)
        SuggestCommand::class  -> ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, value)
        ChangePage::class      -> ClickEvent(ClickEvent.Action.CHANGE_PAGE, value)
        CopyToClipboard::class -> ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, value)
        else                   ->  throw IllegalArgumentException("Unknown click event action: ${T::class.java.simpleName}")
    }
    this.withClickEvent(event)
    return this
}

sealed interface ClickEventAction

data object OpenUrl : ClickEventAction

data object OpenFile : ClickEventAction

data object RunCommand : ClickEventAction

data object SuggestCommand : ClickEventAction

data object ChangePage : ClickEventAction

data object CopyToClipboard : ClickEventAction