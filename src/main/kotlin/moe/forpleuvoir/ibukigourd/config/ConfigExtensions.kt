package moe.forpleuvoir.ibukigourd.config

import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.text.Translatable
import moe.forpleuvoir.nebula.config.ConfigSerializable
import moe.forpleuvoir.nebula.config.fold

fun ConfigSerializable.translationKey(
    prefix: String = this.configManager().let { if (it is ModConfigManager) "${it.modMetadata.id}." else "" }
): String = fold(prefix) { acc, c ->
    acc + (if (c.parentContainer != null) "." else "") + c.key
}

private const val TRANSLATE_TEXT_KYE = "#translate_text"

private const val COMMENT_KYE = "#comment"

val ConfigSerializable.translateText: Text
    get() =
        runCatching {
            getUserData(TRANSLATE_TEXT_KYE) as Text
        }.getOrElse {
            val text = Translatable(translationKey())
            setUserData(TRANSLATE_TEXT_KYE, text)
            text
        }


val ConfigSerializable.comment: Text
    get() = runCatching {
        getUserData(COMMENT_KYE) as Text
    }.getOrElse {
        val text = Translatable(translationKey() + ".comment")
        setUserData(COMMENT_KYE, text)
        text
    }