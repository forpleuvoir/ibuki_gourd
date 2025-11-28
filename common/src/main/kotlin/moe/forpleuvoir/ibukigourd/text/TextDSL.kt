package moe.forpleuvoir.ibukigourd.text

import moe.forpleuvoir.ibukigourd.text.style.StyleScope
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style

@TextDslMark
open class TextScope {

    private lateinit var content: MutableComponent

    val isInitialized get() = ::content.isInitialized

    val text: MutableComponent
        get() {
            check(isInitialized) { "Content is not initialized" }
            return content
        }

    fun append(text: MutableComponent) {
        if (isInitialized) {
            content.append(text)
        } else {
            content = text
        }
    }

    fun newLine() {
        literal("\n")
    }

    fun literal(scope: LiteralScope.() -> Unit) {
        append(LiteralScope().apply(scope).text)
    }

    fun literal(content: Any, scope: LiteralScope.() -> Unit = {}) {
        val a = LiteralScope().apply {
            content(content.toString())
            scope.invoke(this)
        }
        append(a.text)
    }

    fun literal(content: Any) {
        append(Literal(content.toString()))
    }

    fun translatable(key: String, fallback: String?, vararg params: Any, scope: TranslatableScope.() -> Unit = {}) {
        append(TranslatableScope().apply {
            key { key }
            fallback { fallback }
            params(*params)
            scope.invoke(this)
        }.text)
    }

    fun translatable(key: String, vararg params: Any, scope: TranslatableScope.() -> Unit = {}) {
        append(TranslatableScope().apply {
            key { key }
            params(*params)
            scope.invoke(this)
        }.text)
    }

    fun translatable(scope: TranslatableScope.() -> Unit) {
        append(TranslatableScope().apply(scope).text)
    }

}


@TextDslMark
class LiteralScope {

    val text: MutableComponent
        get() {
            check(::_content.isInitialized) { "Content is not initialized" }
            return Literal(_content).setStyle(style)
        }

    private lateinit var _content: String

    private var style: Style = Style.EMPTY

    fun content(content: () -> Any) {
        this._content = content().toString()
    }

    fun content(content: String) {
        this._content = content
    }

    fun style(style: StyleScope.() -> Unit) {
        this.style = StyleScope(this.style).apply(style).asStyle
    }

}

@TextDslMark
class TranslatableScope {

    val text: MutableComponent
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
        this.style = StyleScope(this.style).apply(style).asStyle
    }

}

fun buildText(content: TextScope.() -> Unit): MutableComponent {
    return TextScope().apply(content).text
}
