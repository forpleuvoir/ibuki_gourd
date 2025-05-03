package moe.forpleuvoir.ibukigourd.config

import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.text.Translatable
import moe.forpleuvoir.ibukigourd.text.copyToText
import moe.forpleuvoir.nebula.config.ConfigSerializable
import moe.forpleuvoir.nebula.config.fold

fun ConfigSerializable.translationKey(
    prefix: String = this.configManager().let { if (it is ModConfigManager) "${it.modMetadata.id}." else "" }
): String = fold(prefix) { acc, c ->
    acc + (if (c.parentContainer != null) "." else "") + c.key
}

private const val TRANSLATE_TEXT_KYE = "#translate_text"

private const val COMMENT_KYE = "#comment"

fun ConfigSerializable.translateTextWithParent(level: Int = 1, connector: String): Text {
    var count = 0
    val path = mutableListOf<ConfigSerializable>()
    var currentNode: ConfigSerializable? = this
    while (currentNode != null && count <= level) {
        path.add(currentNode)
        currentNode = currentNode.parentContainer
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

val ConfigSerializable.translateText: Text
    get() = runCatching {
        (getUserData(TRANSLATE_TEXT_KYE) as Text).copyToText()
    }.getOrElse {
        val text = Translatable(translationKey())
        setUserData(TRANSLATE_TEXT_KYE, text)
        text.copyToText()
    }


val ConfigSerializable.comment: Text
    get() = runCatching {
        (getUserData(COMMENT_KYE) as Text).copyToText()
    }.getOrElse {
        val text = Translatable(translationKey() + ".comment", translateText.plainText)
        setUserData(COMMENT_KYE, text)
        text.copyToText()
    }