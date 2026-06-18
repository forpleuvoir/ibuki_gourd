package moe.forpleuvoir.ibukigourd.lang

import net.minecraft.locale.Language
import java.nio.file.Files
import java.nio.file.Path

class TranslationRecorder(private val onlyMissing: Boolean = false) {

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
                    strings.joinToString(",\n") { "  \"$it\": \"\"" }
                        .let { "{\n$it\n}" }
                }
                path.resolve("$category.json").toFile().writeText(content)
            }
    }

}