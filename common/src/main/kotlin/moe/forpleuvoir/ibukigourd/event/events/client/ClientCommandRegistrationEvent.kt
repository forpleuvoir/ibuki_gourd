package moe.forpleuvoir.ibukigourd.event.events.client

import com.mojang.brigadier.CommandDispatcher
import moe.forpleuvoir.nebula.event.EventFactory
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.SharedSuggestionProvider

/**
 * - [CommandDispatcher]<out [SharedSuggestionProvider]> 命令调度器，提供了客户端指令的注册与处理功能。
 * - [CommandBuildContext] 注册访问对象，允许访问注册表以支持命令的相关注册过程。
 */
@JvmField
val ClientCommandRegistrationEvent =
    EventFactory.create<context(CommandDispatcher<out SharedSuggestionProvider>, CommandBuildContext)() -> Unit>(emptyInvoker = {}) { listener ->
        { dispatcher, buildContext -> listener.forEach { it.invoke(dispatcher, buildContext) } }
    }
