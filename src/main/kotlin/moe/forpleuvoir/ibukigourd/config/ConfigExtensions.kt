package moe.forpleuvoir.ibukigourd.config

import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.text.Translatable
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.fold

fun Config<*, *>.translationKey(
    prefix: String = this.configManager().let { if (it is ModConfigManager) "${it.modMetadata.id}." else "" }
): String = fold(prefix) { acc, c ->
    acc + (if (c.parentContainer != null) "." else "") + c.key
}


val Config<*, *>.translateText: Text
    get() = Translatable(translationKey())

val Config<*, *>.descriptionText: Text
    get() = Translatable(translationKey() + ".description")
