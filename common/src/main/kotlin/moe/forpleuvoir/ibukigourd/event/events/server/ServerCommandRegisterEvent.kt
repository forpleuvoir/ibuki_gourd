package moe.forpleuvoir.ibukigourd.event.events.server

import com.mojang.brigadier.CommandDispatcher
import moe.forpleuvoir.nebula.event.Event
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

/**
 * 服务端命令注册事件
 * @property dispatcher CommandDispatcher<CommandSourceStack>
 * @property buildContext CommandBuildContext
 * @property selection Commands.CommandSelection
 * @constructor
 */
class ServerCommandRegisterEvent(
    @JvmField
    val dispatcher: CommandDispatcher<CommandSourceStack>,
    @JvmField
    val buildContext: CommandBuildContext,
    @JvmField
    val selection: Commands.CommandSelection,
) : Event