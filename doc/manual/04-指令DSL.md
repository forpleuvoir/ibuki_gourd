# 04 指令 DSL

这章解决：**用 Kotlin DSL 注册 Brigadier 指令**（比裸 `LiteralArgumentBuilder` 少写一半代码）。

## 1. 快速开始

注册时机由事件提供（客户端 / 服务端各一个）。事件 lambda 的上下文里已经带着
`CommandDispatcher`，所以直接调 `registerCommand` 即可：

```kotlin
import moe.forpleuvoir.ibukigourd.command.dsl.registerCommand
import moe.forpleuvoir.ibukigourd.event.events.client.ClientCommandRegistrationEvent

fun onInit() {
    ClientCommandRegistrationEvent.register {
        registerCommand("yourCommand") {
            literal("hello") {
                execute { clientSource.sendFeedback(Literal("hi!")) }
            }
        }
    }
}
```

服务端同理用 `ServerCommandRegistrationEvent`（上下文还多一个 `Commands.CommandSelection`）：

```kotlin
ServerCommandRegistrationEvent.register {
    registerCommand("yourCommand") {
        executes { source.sendSuccess({ Literal("ok") }, false); 1 }
    }
}
```

## 2. 关键 API

### 入口（`command/dsl/CommandBuildDSL.kt`）

| API | 签名 | 说明 |
|---|---|---|
| `registerCommand` | `context(CommandDispatcher<S>) fun <S> registerCommand(name: String, scope: ArgumentScope<S, LiteralArgumentBuilder<S>>.() -> Unit): CommandDispatcher<S>` | 在"已有 dispatcher 上下文"里注册（事件 lambda 内直接用） |
| `registerCommand` | `fun <S> CommandDispatcher<S>.registerCommand(name, scope): CommandDispatcher<S>` | 扩展版，手里有 dispatcher 时用 |
| `createCommand` | `fun <S> createCommand(name, scope): LiteralArgumentBuilder<S>` | 只构造、不注册（自己攒大树时用） |

### 参数作用域

| 作用域 | API | 说明 |
|---|---|---|
| `ArgumentScope<S, T>` | `literal(name) { }` | 追加字面量子节点 |
| | `"name" { }` | 上面那个的语法糖（`operator fun String.invoke`） |
| | `argument(name, type: ArgumentType<A>) { }` | 追加参数节点，body 是 `RequiredArgumentScope` |
| | `requires { this: S.() -> Boolean }` | 权限 / 条件判定 |
| | `execute { }` | 挂执行体（返回 1） |
| | `executes { ...: Int }` | 挂执行体并自己给返回值（失败返回 0） |
| `RequiredArgumentScope<S, A>` | `suggests(provider)` / `suggests(vararg String)` / `suggests(Stream<String>)` / `suggests { Stream<String> }` | 补全建议 |

### 客户端指令的便捷读取（`command/ClientCommandSource.kt`）

```kotlin
import moe.forpleuvoir.ibukigourd.command.clientSource

literal("whereami") {
    execute {
        val player = clientSource.sender       // 当前玩家（客户端指令专用）
        val level = clientSource.level
        clientSource.sendFeedback(Literal("here"))
    }
}
```

`CommandContext<out SharedSuggestionProvider>.clientSource` 是扩展属性，等价于 `ClientCommandSourceImpl(source, mc)`。

## 3. 常见用法

### 3.1 参数 + 建议

```kotlin
argument("name", StringArgumentType.word()) {
    suggests("alice", "bob")
    execute { clientSource.sendFeedback(Literal("you said: ${StringArgumentType.getString(this, "name")}")) }
}
```

### 3.2 嵌套子命令与条件

```kotlin
registerCommand("yourmod") {
    requires { hasPermission(2) }
    literal("reload") {
        executes { source.sendSuccess({ Literal("reloaded") }, true); 1 }
    }
    "debug" {                          // 等价于 literal("debug")
        execute { /* ... */ }
    }
}
```

### 3.3 注册在哪

| 位置 | 用什么 |
|---|---|
| 客户端（含 devOnly 测试） | `ClientCommandRegistrationEvent.register { ... }`（Fabric / NeoForge 侧由加载器的 `RegisterClientCommandsEvent` 触发） |
| 服务端 | `ServerCommandRegistrationEvent.register { ... }`（由 `RegisterCommandsEvent` 触发） |
| 只想拿 dispatcher 自己注册 | 上面两个事件的 launcher 形态：`ClientCommandRegistrationEvent()(dispatcher, buildContext)`（仓库里加载器适配层就是这么调的） |

## 4. 注意事项

- **不在事件里注册就直接用 `createCommand`**：指令必须在加载器给的那一次注册窗口里挂上去，事后加不会生效。
- **`execute { }` 固定返回 1**（成功），要在失败时返回 0 用 `executes { ... }`。
- **`ArgumentScope` 带 `@CommandDslMark`**：不能在 lambda 里嵌套使用别的 `ArgumentScope` 作用域（避免 `then` 挂错父节点）。
- **客户端指令的参数用 `CommandContext<out SharedSuggestionProvider>`**，`clientSource` 才可用；服务端用 `CommandContext<CommandSourceStack>`，此时没有 `clientSource`。
- **`require`s 的接收者是 source 类型**：客户端是 `SharedSuggestionProvider`，服务端是 `CommandSourceStack`。

## 5. 相关源码

- `command/dsl/CommandBuildDSL.kt`（DSL 全部实现）
- `command/ClientCommandSource.kt`、`ClientCommandSourceImpl.kt`
- `event/events/client/ClientCommandRegistrationEvent.kt`、`event/events/server/ServerCommandRegistrationEvent.kt`
- 加载器接线：`fabric/src/main/kotlin/.../FabricIbukiGourd(Client).kt`、
  `neoforge/src/main/kotlin/.../neoforgeevent/CommandRegistry.kt`
- 活例子：`common/src/devOnly/kotlin/.../test/TestCommand.kt`（`igtest` 指令）

下一页：[05 事件总线](05-事件总线.md) →
