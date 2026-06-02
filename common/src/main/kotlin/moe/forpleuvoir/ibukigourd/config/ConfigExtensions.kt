package moe.forpleuvoir.ibukigourd.config

import moe.forpleuvoir.ibukigourd.text.*
import moe.forpleuvoir.nebula.config.ConfigNode
import moe.forpleuvoir.nebula.config.path

fun ConfigNode.translationKey(
    prefix: String = this.root.let { if (it is ModConfigManager) it.modId else "" }
): String = if(path.isNotEmpty()) "$prefix.$path" else prefix

const val TRANSLATE_TEXT_KYE = "#translate_text"

const val COMMENT_KYE = "#comment"

fun ConfigNode.translateTextWithParent(level: Int = 1, connector: String): MutableText {
    var count = 0
    val path = mutableListOf<ConfigNode>()
    var currentNode: ConfigNode? = this
    while (currentNode != null && count <= level) {
        path.add(currentNode)
        currentNode = currentNode.parent
        count++
    }
    val first = Literal("")
    path.reversed().forEachIndexed { index, c ->
        if (index == 0) {
            first.append(c.translateText)
        } else {
            first.appendLiteral(connector).append(c.translateText)
        }
    }
    return first
}

var ConfigNode.translateText: MutableText
    get() = runCatching {
        (getMetadata(TRANSLATE_TEXT_KYE) as MutableText).copy()
    }.getOrElse {
        val text = Translatable(translationKey())
        setMetadata(TRANSLATE_TEXT_KYE, text)
        text.copy()
    }
    set(value) {
        setMetadata(TRANSLATE_TEXT_KYE, value)
    }


var ConfigNode.comment: MutableText
    get() = runCatching {
        (getMetadata(COMMENT_KYE) as MutableText).copy()
    }.getOrElse {
        val text = Translatable(translationKey() + ".comment", translateText.plainText)
        setMetadata(COMMENT_KYE, text)
        text.copy()
    }
    set(value) {
        setMetadata(COMMENT_KYE, value)
    }