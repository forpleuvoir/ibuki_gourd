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


val ConfigSerializable.translateText: Text
    get() = Translatable(translationKey())

val ConfigSerializable.comment: Text
    get() = Translatable(translationKey() + ".comment")
