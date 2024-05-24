package moe.forpleuvoir.ibukigourd.text

import moe.forpleuvoir.ibukigourd.text.style.StyleScope
import net.minecraft.text.MutableText
import net.minecraft.text.Style

@TextDslMark
open class TextScope {
    private val textChain: MutableList<MutableText> = ArrayList()

    val text: MutableText
        get() {
            return textChain[0]
        }

    fun newLine() {
        literal("\n")
    }

    fun literal(scope: LiteralScope.() -> Unit) {
        textChain.add(LiteralScope().apply(scope).text)
    }

    fun literal(content: Any, scope: LiteralScope.() -> Unit = {}) {
        textChain.add(LiteralScope().apply {
            context(content.toString())
            scope.invoke(this)
        }.text)
    }

    fun literal(content: Any) {
        textChain.add(Literal(content.toString()))
    }

    fun translatable(key: String, fallback: String?, vararg params: Any, scope: TranslatableScope.() -> Unit = {}) {
        textChain.add(TranslatableScope().apply {
            key { key }
            fallback { fallback }
            params(*params)
            scope.invoke(this)
        }.text)
    }

    fun translatable(key: String, vararg params: Any, scope: TranslatableScope.() -> Unit = {}) {
        textChain.add(TranslatableScope().apply {
            key { key }
            params(*params)
            scope.invoke(this)
        }.text)
    }

    fun translatable(scope: TranslatableScope.() -> Unit) {
        textChain.add(TranslatableScope().apply(scope).text)
    }

}


@TextDslMark
class LiteralScope {

    val text: MutableText
        get() {
            check(::content.isInitialized) { "Content is not initialized" }
            return Literal(content).setStyle(style)
        }

    private lateinit var content: String

    private var style: Style = Style.EMPTY

    fun context(content: () -> Any) {
        this.content = content().toString()
    }

    fun context(content: String) {
        this.content = content
    }

    fun style(style: StyleScope.() -> Unit) {
        this.style = StyleScope().apply(style).style
    }

}

@TextDslMark
class TranslatableScope {

    val text: MutableText
        get() {
            check(key != null) { "Key is not initialized" }
            return Translatable(key!!, fallback, *params ?: emptyArray()).setStyle(style)
        }

    private var key: String? = null

    private var fallback: String? = null

    private var params: Array<out Any>? = null

    private var style: Style = Style.EMPTY

    fun key(key: () -> Any) {
        this.key = key().toString()
    }

    fun params(vararg params: Any) {
        this.params = params
    }

    fun fallback(fallback: () -> String?) {
        this.fallback = fallback().toString()
    }

    fun style(style: StyleScope.() -> Unit) {
        this.style = StyleScope().apply(style).style
    }

}

fun Text(content: TextScope.() -> Unit): MutableText {
    return TextScope().apply(content).text
}
