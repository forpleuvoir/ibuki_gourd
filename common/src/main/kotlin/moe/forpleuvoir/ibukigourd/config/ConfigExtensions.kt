package moe.forpleuvoir.ibukigourd.config

import moe.forpleuvoir.ibukigourd.text.*
import moe.forpleuvoir.nebula.config.ConfigGroup
import moe.forpleuvoir.nebula.config.ConfigManager
import moe.forpleuvoir.nebula.config.ConfigNode
import moe.forpleuvoir.nebula.config.flat
import moe.forpleuvoir.nebula.config.path
import net.minecraft.locale.Language

object ConfigTranslationDefaults {

    const val TRANSLATE_KEY_KEY = "#translate_key"

    const val TRANSLATE_TEXT_KYE = "#translate_text"

    const val COMMENT_KYE_KEY = "#translate_comment_key"

    const val COMMENT_KYE = "#translate_comment_text"
}

var ConfigNode.translationKey: String
    get() = runCatching {
        getMetadata(ConfigTranslationDefaults.TRANSLATE_KEY_KEY) as String
    }.getOrElse {
        val prefix = this.root.let {
            if (it is ModConfigManager) "${it.modId}.${if (it.name == "config") it.name else "config.${it.name}"}" else it?.name ?: ""
        }
        val result = if (path.isNotEmpty()) "${if (prefix.isNotEmpty()) "$prefix." else ""}$path" else prefix
        setMetadata(ConfigTranslationDefaults.TRANSLATE_KEY_KEY, result)
        result
    }
    set(value) {
        setMetadata(ConfigTranslationDefaults.TRANSLATE_KEY_KEY, value)
    }

var ConfigNode.translateCommentKey: String
    get() = runCatching {
        getMetadata(ConfigTranslationDefaults.COMMENT_KYE_KEY) as String
    }.getOrElse {
        val text = "$translationKey.comment"
        setMetadata(ConfigTranslationDefaults.COMMENT_KYE_KEY, text)
        text
    }
    set(value) {
        setMetadata(ConfigTranslationDefaults.COMMENT_KYE_KEY, value)
    }

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
        (getMetadata(ConfigTranslationDefaults.TRANSLATE_TEXT_KYE) as MutableText).copy()
    }.getOrElse {
        val text = Translatable(translationKey, this.name)
        setMetadata(ConfigTranslationDefaults.TRANSLATE_TEXT_KYE, text)
        text.copy()
    }
    set(value) {
        setMetadata(ConfigTranslationDefaults.TRANSLATE_TEXT_KYE, value)
    }


var ConfigNode.translateComment: MutableText
    get() = runCatching {
        (getMetadata(ConfigTranslationDefaults.COMMENT_KYE) as MutableText).copy()
    }.getOrElse {
        val text = Translatable(translateCommentKey, this.name)
        setMetadata(ConfigTranslationDefaults.COMMENT_KYE, text)
        text.copy()
    }
    set(value) {
        setMetadata(ConfigTranslationDefaults.COMMENT_KYE, value)
    }


fun ConfigNode.matchWithTranslate(regex: Regex): Boolean =
    this.matched(regex)
            || regex.containsMatchIn(translationKey)
            || regex.containsMatchIn(translateText.plainText)
            || regex.containsMatchIn(translateComment.plainText)


fun ConfigGroup.exportTranslateKeys(onlyMissing: Boolean = false, withComment: Boolean = true): List<String> {
    return buildList {
        flat.forEach {
            val key = it.translationKey
            val comment = it.translateCommentKey
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