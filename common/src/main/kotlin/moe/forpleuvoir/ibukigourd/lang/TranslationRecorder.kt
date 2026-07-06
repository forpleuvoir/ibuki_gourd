package moe.forpleuvoir.ibukigourd.lang

import moe.forpleuvoir.nebula.serialization.json.JsonDialect
import net.minecraft.locale.Language
import java.nio.file.Files
import java.nio.file.Path

class TranslationRecorder(private val onlyMissing: Boolean = false, private val keepExisting: Boolean = false) {

    private val records: MutableSet<String> = LinkedHashSet()

    private val filters: MutableList<(String) -> Boolean> = mutableListOf()

    var categorizer: (String) -> String = { "default" }

    fun record(key: String) {
        if (onlyMissing) {
            if (Language.getInstance().has(key)) {
                records.remove(key)
                return
            }
        }
        if (filters.any { it(key) }) {
            records.add(key)
        }
    }

    fun addFilter(filter: (String) -> Boolean) {
        filters.add(filter)
    }

    fun dump(path: Path) {
        Files.createDirectories(path)
        records.groupBy(categorizer)
            .forEach { (category, strings) ->
                val content = if (strings.isEmpty()) {
                    "{}"
                } else {
                    strings.joinToString(",\n") {
                        val value = if (keepExisting)
                            Language.getInstance().getOrDefault(it, "").let { escape(it) }
                        else ""
                        "  \"$it\": \"$value\""
                    }.let { "{\n$it\n}" }
                }
                path.resolve("$category.json").toFile().writeText(content)
            }
    }

    private fun escape(s: String): String = buildString {
        for (c in s) {
            when (c) {
                '"'      -> append("\\\"")
                '\\'     -> append("\\\\")
                '\b'     -> append("\\b")
                '\u000C' -> append("\\f")
                '\n'     -> append("\\n")
                '\r'     -> append("\\r")
                '\t'     -> append("\\t")
                else     -> if (c < ' ') {
                    append("\\u%04x".format(c.code))
                } else {
                    append(c)
                }
            }
        }
    }
}