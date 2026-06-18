package moe.forpleuvoir.ibukigourd.config

import moe.forpleuvoir.ibukigourd.text.*
import moe.forpleuvoir.nebula.config.ConfigManager
import moe.forpleuvoir.nebula.config.ConfigNode
import moe.forpleuvoir.nebula.config.flat
import moe.forpleuvoir.nebula.config.path
import net.minecraft.locale.Language

fun ConfigNode.translationKey(
    prefix: String = this.root.let {
        if (it is ModConfigManager) "${it.modId}.${if (it.name == "config") it.name else "config.${it.name}"}" else it?.name ?: ""
    }
): String = if (path.isNotEmpty()) "$prefix.$path" else prefix

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
        val text = Translatable(translationKey(), this.name)
        setMetadata(TRANSLATE_TEXT_KYE, text)
        text.copy()
    }
    set(value) {
        setMetadata(TRANSLATE_TEXT_KYE, value)
    }


var ConfigNode.translateComment: MutableText
    get() = runCatching {
        (getMetadata(COMMENT_KYE) as MutableText).copy()
    }.getOrElse {
        val text = Translatable(translationKey() + ".comment", this.name)
        setMetadata(COMMENT_KYE, text)
        text.copy()
    }
    set(value) {
        setMetadata(COMMENT_KYE, value)
    }


fun ConfigNode.matchWithTranslate(regex: Regex): Boolean =
    this.matched(regex)
            || regex.containsMatchIn(translationKey())
            || regex.containsMatchIn(translateText.plainText)
            || regex.containsMatchIn(translateComment.plainText)


fun ConfigManager.exportTranslateKeys(onlyMissing: Boolean = false, withComment: Boolean = true): List<String> {
    return buildList {
        flat.forEach {
            val key = it.translationKey()
            val comment = it.translationKey() + ".comment"
            if (onlyMissing) {
                if (!Language.getInstance().has(key))
                    add(key)
                if (withComment && !Language.getInstance().has(comment))
                    add(comment)
            } else {
                add(key)
                if (withComment) add(comment)
            }
        }
    }
}