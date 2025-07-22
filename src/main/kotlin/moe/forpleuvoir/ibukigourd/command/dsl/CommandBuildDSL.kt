package moe.forpleuvoir.ibukigourd.command.dsl

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.minecraft.command.CommandSource
import java.util.stream.Stream


@DslMarker
annotation class CommandDslMark

@CommandDslMark
open class ArgumentScope<S, T : ArgumentBuilder<S, T>>(
    val argumentBuilder: T,
) {

    fun literal(name: String, scope: ArgumentScope<S, LiteralArgumentBuilder<S>>.() -> Unit) {
        argumentBuilder.then(ArgumentScope(LiteralArgumentBuilder.literal<S>(name)).apply(scope).argumentBuilder)
    }

    operator fun String.invoke(scope: ArgumentScope<S, LiteralArgumentBuilder<S>>.() -> Unit) {
        argumentBuilder.then(ArgumentScope(LiteralArgumentBuilder.literal<S>(this)).apply(scope).argumentBuilder)
    }

    fun <A> argument(name: String, type: ArgumentType<A>, scope: RequiredArgumentScope<S, A>.() -> Unit) {
        argumentBuilder.then(RequiredArgumentScope(RequiredArgumentBuilder.argument<S, A>(name, type)).apply(scope).argumentBuilder)
    }

    fun requires(requirement: S.() -> Boolean) {
        argumentBuilder.requires(requirement)
    }

    fun execute(action: CommandContext<S>.() -> Unit) {
        argumentBuilder.executes { action(it);1 }
    }

    fun executes(action: CommandContext<S>.() -> Int) {
        argumentBuilder.executes { action(it) }
    }


}


class RequiredArgumentScope<S, T>(
    argumentBuilder: RequiredArgumentBuilder<S, T>,
) : ArgumentScope<S, RequiredArgumentBuilder<S, T>>(argumentBuilder) {

    fun suggests(provider: SuggestionProvider<S>) {
        argumentBuilder.suggests(provider)
    }

    fun suggests(vararg candidates: String) {
        suggests { _, builder -> CommandSource.suggestMatching(candidates, builder) }
    }

    fun suggests(candidates: Stream<String>) {
        suggests { _, builder -> CommandSource.suggestMatching(candidates, builder) }
    }

    @JvmName("suggestsStream")
    fun suggests(candidates: () -> Stream<String>) {
        suggests { ctx, builder -> CommandSource.suggestMatching(candidates(), builder) }
    }

    fun suggests(candidates: Iterable<String>) {
        suggests { _, builder -> CommandSource.suggestMatching(candidates, builder) }
    }

    @JvmName("suggestsIterable")
    fun suggests(candidates: () -> Iterable<String>) {
        suggests { ctx, builder -> CommandSource.suggestMatching(candidates(), builder) }
    }

}

@CommandDslMark
fun <S> Command(name: String, scope: ArgumentScope<S, LiteralArgumentBuilder<S>>.() -> Unit): LiteralArgumentBuilder<S> {
    return ArgumentScope(LiteralArgumentBuilder.literal<S>(name))
        .apply(scope)
        .argumentBuilder
}


@CommandDslMark
fun <S> CommandDispatcher<S>.registerCommand(name: String, scope: ArgumentScope<S, LiteralArgumentBuilder<S>>.() -> Unit): CommandDispatcher<S> {
    this.register(Command(name, scope))
    return this
}
