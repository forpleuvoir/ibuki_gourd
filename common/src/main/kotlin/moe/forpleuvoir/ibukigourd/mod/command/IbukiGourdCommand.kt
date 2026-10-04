package moe.forpleuvoir.ibukigourd.mod.command

import com.mojang.brigadier.CommandDispatcher
import moe.forpleuvoir.ibukigourd.command.dsl.registerCommand
import moe.forpleuvoir.ibukigourd.event.events.client.ClientCommandRegistrationEvent
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.nebula.common.api.Initializable
import net.minecraft.commands.SharedSuggestionProvider

object IbukiGourdCommand : Initializable {

    override fun init() {
        ClientCommandRegistrationEvent.register {
            inputCommand()
        }
    }

    context(context: CommandDispatcher<out SharedSuggestionProvider>)
    private fun inputCommand() = registerCommand("hs:input") {
        "clear" {
            InputHandler.releaseAll()
        }
    }
}
