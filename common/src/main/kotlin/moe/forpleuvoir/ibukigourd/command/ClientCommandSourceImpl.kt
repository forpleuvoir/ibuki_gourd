package moe.forpleuvoir.ibukigourd.command

import com.mojang.brigadier.suggestion.SuggestionsBuilder
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.text.Texts
import moe.forpleuvoir.ibukigourd.text.withColor
import moe.forpleuvoir.ibukigourd.util.chatMessage
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.player.LocalPlayer
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.core.HolderLookup
import net.minecraft.network.chat.Component

class ClientCommandSourceImpl(
    private val source: SharedSuggestionProvider,
    override val client: Minecraft
) : ClientCommandSource, SharedSuggestionProvider by source {

    override fun sendFeedback(message: Text) {
        client.chatMessage(message)
    }

    override fun sendError(message: Text) {
        sendFeedback(Texts.empty().append(message).withColor(Colors.RED))
    }

    override val sender: LocalPlayer
        get() = client.player!!

    override val level: ClientLevel
        get() = client.level!!

    override fun getCustomTabSugggestions(): Collection<String> {
        return source.customTabSugggestions
    }

    override fun getSelectedEntities(): Collection<String> {
        return source.selectedEntities
    }

    override fun getRelevantCoordinates(): Collection<SharedSuggestionProvider.TextCoordinates> {
        return source.relevantCoordinates
    }

    override fun getAbsoluteCoordinates(): Collection<SharedSuggestionProvider.TextCoordinates> {
        return source.absoluteCoordinates
    }

    override fun suggestRegistryElements(
        lookup: HolderLookup<*>,
        type: SharedSuggestionProvider.ElementSuggestionType,
        builder: SuggestionsBuilder
    ) {
        source.suggestRegistryElements(lookup, type, builder)
    }
}