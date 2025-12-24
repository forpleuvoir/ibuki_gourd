package moe.forpleuvoir.ibukigourd.text

import moe.forpleuvoir.ibukigourd.text.style.StyleBuilder
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style

@TextDslMark
open class TextBuilder {

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

    fun literal(scope: LiteralBuilder.() -> Unit) {
        append(LiteralBuilder().apply(scope).text)
    }

    fun literal(content: Any, scope: LiteralBuilder.() -> Unit = {}) {
        val a = LiteralBuilder().apply {
            content(content.toString())
            scope.invoke(this)
        }
        append(a.text)
    }

    fun literal(content: Any) {
        append(Literal(content.toString()))
    }

    fun translatable(key: String, fallback: String?, vararg params: Any, scope: TranslatableBuilder.() -> Unit = {}) {
        append(TranslatableBuilder().apply {
            key { key }
            fallback { fallback }
            params(*params)
            scope.invoke(this)
        }.text)
    }

    fun translatable(key: String, vararg params: Any, scope: TranslatableBuilder.() -> Unit = {}) {
        append(TranslatableBuilder().apply {
            key { key }
            params(*params)
            scope.invoke(this)
        }.text)
    }

    fun translatable(scope: TranslatableBuilder.() -> Unit) {
        append(TranslatableBuilder().apply(scope).text)
    }

}


@TextDslMark
class LiteralBuilder {

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

    fun style(style: StyleBuilder.() -> Unit) {
        this.style = StyleBuilder(this.style).apply(style).asStyle
    }

}

@TextDslMark
class TranslatableBuilder {

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

    fun style(style: StyleBuilder.() -> Unit) {
        this.style = StyleBuilder(this.style).apply(style).asStyle
    }

}

fun buildText(content: TextBuilder.() -> Unit): MutableComponent {
    return TextBuilder().apply(content).text
}
