package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.event.events.client.ClientCommandRegisterEvent
import moe.forpleuvoir.nebula.event.EventSubscriber
import moe.forpleuvoir.nebula.event.Subscriber

@EventSubscriber
object TestCommand {

    @Subscriber
    fun register(event: ClientCommandRegisterEvent) {
        println("注册指令...")
//        event.dispatcher.testCommand()
    }

//    fun CommandDispatcher<FabricClientCommandSource>.testCommand() = registerCommand("igtest") {
//        requires {
//            this.player.hasPermissions(2)
//        }
//        execute {
//            println("直接执行了 test")
//        }
//        "sub1" {
//            execute {
//                println("执行了子节点 sub1")
//            }
//            "又是一层" {
//                execute {
//                    println("执行了子节点 sub1 又是一层")
//                }
//                "又又是一层" {
//                    execute {
//                        println("执行了子节点 sub1 又是一层 又又是一层")
//                    }
//                    argument("arg1", StringArgumentType.string()) {
//                        suggests("arg1", "arg2")
//                        execute {
//                            val arg = StringArgumentType.getString(this, "arg1")
//                            println("执行了子节点 sub1 又是一层 又又是一层 参数节点:$arg")
//                        }
//                    }
//                }
//            }
//        }
//        argument("arg1", StringArgumentType.string()) {
//            suggests("arg1", "arg2")
//            execute {
//                val arg = StringArgumentType.getString(this, "arg1")
//                println("执行了参数节点:$arg")
//            }
//        }
//
//    }
}