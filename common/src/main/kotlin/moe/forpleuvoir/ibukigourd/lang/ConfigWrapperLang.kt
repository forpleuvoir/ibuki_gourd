package moe.forpleuvoir.ibukigourd.lang

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.text.MutableText
import moe.forpleuvoir.ibukigourd.text.Translatable

@Suppress("NOTHING_TO_INLINE")
object ConfigWrapperLang {

    @PublishedApi
    internal inline fun lang(key: String, vararg args: Any): MutableText =
        Translatable("${IbukiGourd.MOD_ID}.config_wrapper.$key", args = args)


    val pairFirst get() = lang("pair.first")

    val pairSecond get() = lang("pair.second")

    val mapKey get() = lang("map.key")

    val mapValue get() = lang("map.value")

    fun keyExists(key: Any) = lang("key_exists", key)

    val move get() = lang("move")

    fun listConfigWrapperText(count: Int) = lang("list.text", count)

    fun mapConfigWrapperText(count: Int) = lang("map.text", count)

    /** 图集缓存：已使用 */
    val cacheUsed get() = lang("cache.used")

    /** 图集缓存：上限 */
    val cacheLimit get() = lang("cache.limit")

    /** 图集缓存：MB 单位 */
    val cacheMb get() = lang("cache.mb")

}
