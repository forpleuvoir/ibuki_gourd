package moe.forpleuvoir.ibukigourd.text

import moe.forpleuvoir.ibukigourd.text.style.*
import net.minecraft.item.ItemStack
import net.minecraft.text.MutableText
import net.minecraft.text.Style

open class TextScope {
    private val textChain: MutableList<MutableText> = ArrayList()

    val text: MutableText
        get() {
            return textChain[0]
        }

    fun line(content: LineScope.() -> Unit) {
        LineScope()
            .apply(content)
            .text
            .let {
                if (textChain.isNotEmpty()) {
                    literal("\n")
                }
                textChain.add(it)
                literal("\n")
            }
    }

    fun literal(content: LiteralScope.() -> Unit) {
        textChain.add(LiteralScope().apply(content).text)
    }

    fun literal(content: Any) {
        textChain.add(Literal(content.toString()))
    }

    fun translate(content: TranslateScope.() -> Unit) {
        textChain.add(TranslateScope().apply(content).text)
    }

}

class LineScope : TextScope()

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

class TranslateScope {

    val text: MutableText
        get() {
            check(key != null) { "Key is not initialized" }
            return Trans(key!!, fallback, *params ?: emptyArray()).setStyle(style)
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

    fun fallback(fallback: () -> Any) {
        this.fallback = fallback().toString()
    }

    fun style(style: StyleScope.() -> Unit) {
        this.style = StyleScope().apply(style).style
    }

}

fun Text(content: TextScope.() -> Unit): MutableText {
    return TextScope().apply(content).text
}

//TODO("待删除")
fun usage() {
    Text { literal { context { "Hello" } } }
    Text {
        line { literal { context { "Hello" } } }
        translate {
            key { "key" }
            params("param1", "param2")
            style {
                color(0x00FF00)
                bold()
                italic()
                click<RunCommand>("/test")
            }
        }
    }

    Literal("Hello").style {
        color(0x00FF00)
    }

}