package moe.forpleuvoir.ibukigourd.util.text

import com.google.common.collect.ImmutableList
import net.minecraft.text.StringVisitable
import net.minecraft.text.Style
import net.minecraft.text.TranslatableTextContent
import net.minecraft.text.TranslationException

class ServerText(
	content: ServerTranslatableContents,
	list: List<net.minecraft.text.Text> = emptyList(),
	style: Style = Style.EMPTY,
) : Text(content, list, style) {

	constructor(key: String, fallback: String? = null, vararg args: Any) : this(ServerTranslatableContents(key, fallback, *args))

	constructor(key: String, fallback: String? = null) : this(key, fallback, emptyArray<Any>())

	class ServerTranslatableContents(key: String, fallback: String?, vararg args: Any) : TranslatableTextContent(key, fallback, args) {

        constructor(key: String, fallback: String? = null) : this(key, fallback, emptyArray<Any>())

		override fun updateTranslations() {
			val language = ServerLanguage
			if (language === languageCache) {
				return
			}
			languageCache = language
			val string = if (fallback != null) language.get(key, fallback!!) else language[key]
			translations = runCatching {
				val builder = ImmutableList.builder<StringVisitable>()
				forEachPart(string) { builder.add(it) }
				builder.build()
			}.getOrElse {
				if (it is TranslationException) ImmutableList.of(StringVisitable.plain(string))
				else throw it
			}
		}

	}

}