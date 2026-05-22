package moe.forpleuvoir.ibukigourd.event.events.server

import com.mojang.brigadier.CommandDispatcher
import moe.forpleuvoir.nebula.event.Event
import moe.forpleuvoir.nebula.event.EventFactory
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider

/**
 * 服务端命令注册事件
 * - `dispatcher` [CommandDispatcher]<[CommandSourceStack]>
 * - `buildContext` [CommandBuildContext]
 * - `selection` [Commands.CommandSelection]
 */
@JvmField
val ServerCommandRegistrationEvent =
    EventFactory.create<context(CommandDispatcher<CommandSourceStack>, CommandBuildContext, Commands.CommandSelection)() -> Unit>(emptyInvoker = {}) { listener ->
        { dispatcher, buildContext, selection -> listener.forEach { it.invoke(dispatcher, buildContext, selection) } }
    }

