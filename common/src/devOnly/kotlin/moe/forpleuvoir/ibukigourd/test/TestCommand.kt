package moe.forpleuvoir.ibukigourd.test

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import moe.forpleuvoir.ibukigourd.command.clientSource
import moe.forpleuvoir.ibukigourd.command.dsl.registerCommand
import moe.forpleuvoir.ibukigourd.config.exportTranslateKeys
import moe.forpleuvoir.ibukigourd.event.events.client.ClientCommandRegistrationEvent
import moe.forpleuvoir.ibukigourd.lang.TranslationRecorder
import moe.forpleuvoir.ibukigourd.mod.config.IGConfig
import moe.forpleuvoir.ibukigourd.text.Texts
import moe.forpleuvoir.nebula.common.api.Initializable
import net.minecraft.commands.SharedSuggestionProvider
import kotlin.io.path.Path

object TestCommand : Initializable {

    override fun init() {
        ClientCommandRegistrationEvent.register {
            testCommand()
        }
    }

    context(context: CommandDispatcher<out SharedSuggestionProvider>)
    fun testCommand() = registerCommand("igtest") {
        "config_keys" {
            execute {
                val recorder = TranslationRecorder(false, keepExisting = true)
                recorder.categorizer = { "config" }
                recorder.addFilter { true }
                IGConfig.exportTranslateKeys().forEach {
                    recorder.record(it)
                }
                recorder.dump(Path("../../../common/src/devOnly/lang"))
            }
        }
        execute {
            clientSource.sendFeedback(Texts.literal("直接执行了 test"))
        }
        "sub1" {
            execute {
                println("执行了子节点 sub1")
            }
            "又是一层" {
                execute {
                    println("执行了子节点 sub1 又是一层")
                }
                "又又是一层" {
                    execute {
                        println("执行了子节点 sub1 又是一层 又又是一层")
                    }
                    argument("arg1", StringArgumentType.string()) {
                        suggests("arg1", "arg2")
                        execute {
                            val arg = StringArgumentType.getString(this, "arg1")
                            println("执行了子节点 sub1 又是一层 又又是一层 参数节点:$arg")
                        }
                    }
                }
            }
        }
        argument("arg1", StringArgumentType.string()) {
            suggests("arg1", "arg2")
            execute {
                val arg = StringArgumentType.getString(this, "arg1")
                println("执行了参数节点:$arg")
            }
        }

    }
}