package moe.forpleuvoir.ibukigourd.event.events.client

import com.mojang.brigadier.CommandDispatcher
import moe.forpleuvoir.nebula.event.Event
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.SharedSuggestionProvider

/**
 * 客户端命令注册事件
 *
 * 用于在客户端注册命令时触发，以提供指令的注册能力。通过此事件可以获取命令注册相关的调度器和注册访问对象。
 *
 * @property dispatcher 命令调度器，提供了客户端指令的注册与处理功能。
 * @property buildContext 注册访问对象，允许访问注册表以支持命令的相关注册过程。
 */
class ClientCommandRegisterEvent(
    @JvmField val dispatcher: CommandDispatcher<out SharedSuggestionProvider>,
    @JvmField val buildContext: CommandBuildContext,
) : Event