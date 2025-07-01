package moe.forpleuvoir.ibukigourd.text.inline_style_text.modifier

import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.text.flat
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.HSVColor
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import java.text.BreakIterator
import java.util.*

object ColorModifier : TextModifier {

    override fun modifier(exp: String): ((MutableText) -> MutableText)? {
        return parseColor(exp)
    }

    private fun parseColor(exp: String): ((MutableText) -> MutableText)? {
        parseRGBColor(exp)?.let {
            return it
        }
        parseHSVColor(exp)?.let {
            return it
        }
        return null
    }

    private fun parseRGBColor(exp: String): ((MutableText) -> MutableText)? {
        //&{#FF66CC}
        if (exp.matches(Regex("#[0-9A-Fa-f]{6}")) && exp.length == 7) {
            return { text -> text.withColor(Color(exp).rgb) }
        }
        //&{#FF66CC->#FF88BB}
        if (exp.matches(Regex("#[0-9A-Fa-f]{6}->#[0-9A-Fa-f]{6}"))) {
            val (sStart, sEnd) = exp.split("->")
            val (start, end) = Color(sStart) to Color(sEnd)
            return { text ->
                gradientText(text, start, end)
            }
        }
        return null
    }

    private fun parseHSVColor(exp: String): ((MutableText) -> MutableText)? {
        //&{[360 100 20]}
        if (exp.matches(Regex("\\[((360|3[0-5][0-9]|2\\d{2}|1\\d{2}|\\d{1,2})(\\.\\d+)?) \\s*((100|[1-9]?\\d)(\\.\\d+)?) \\s*((100|[1-9]?\\d)(\\.\\d+)?)]"))) {
            val hsv = exp.substring(1, exp.length - 1).split(' ').map { it.toFloat() }
            return { text -> text.withColor(HSVColor(hsv[0], hsv[1] / 100f, hsv[2] / 100f).rgb) }
        }
        //&{[360 99.6 20]->[360 20 100]}
        if (exp.matches(Regex("\\[((360|3[0-5][0-9]|2\\d{2}|1\\d{2}|\\d{1,2})(\\.\\d+)?) \\s*((100|[1-9]?\\d)(\\.\\d+)?) \\s*((100|[1-9]?\\d)(\\.\\d+)?)]->\\[((360|3[0-5][0-9]|2\\d{2}|1\\d{2}|\\d{1,2})(\\.\\d+)?) \\s*((100|[1-9]?\\d)(\\.\\d+)?) \\s*((100|[1-9]?\\d)(\\.\\d+)?)]"))) {
            val (fStart, fEnd) = exp.split("->").map { it -> it.substring(1, it.length - 1).split(" ").map { it.toFloat() } }
            val (start, end) = HSVColor(fStart[0], fStart[1] / 100, fStart[2] / 100) to HSVColor(fEnd[0], fEnd[1] / 100, fEnd[2] / 100)
            return { text ->
                gradientText(text, start, end)
            }
        }
        return null
    }

    private fun <C : ARGBColor> gradientText(content: MutableText, start: C, end: C): MutableText {
        return Text {
            if (content.string.isEmpty()) {
                append(content)
                return@Text
            }
            val texts = splitText(content)
            start.gradient(end, texts.size).forEachIndexed { index, color ->
                append(texts[index].withColor(color.rgb))
            }
        }
    }

    private fun splitText(text: MutableText): List<MutableText> = buildList {
        text.flat().forEach {
            val style = if (it is MutableText) it.style else Style.EMPTY
            val t = it.string
            val iterator: BreakIterator = BreakIterator.getCharacterInstance(Locale.US)
            iterator.setText(t)
            var start = iterator.first()
            var end = iterator.next()

            while (end != BreakIterator.DONE) {
                val emoji = t.substring(start, end)
                add(Literal(emoji).setStyle(style))

                start = end
                end = iterator.next()
            }
        }
    }


    fun HSVColor.gradient(to: HSVColor, steps: Int): List<HSVColor> {
        return gradientHSVColor(this, to, steps)
    }

    fun ARGBColor.gradient(to: ARGBColor, steps: Int): List<ARGBColor> {
        return if (this is HSVColor && to is HSVColor)
            this.gradient(to, steps)
        else
            gradientColor(this, to, steps)
    }

    @Suppress("DuplicatedCode")
    fun gradientHSVColor(from: HSVColor, to: HSVColor, steps: Int): List<HSVColor> {
        check(steps > 0) { "steps must be greater than 0" }
        if (steps == 1) return listOf(HSVColor(from.hue, from.saturation, from.value, from.value, false))

        return buildList {
            repeat(steps) { index ->
                add(from.lerp(to, (index * (1f / (steps - 1))).coerceIn(0f, 1f)))
            }
        }
    }

    @Suppress("DuplicatedCode")
    fun gradientColor(from: ARGBColor, to: ARGBColor, steps: Int): List<ARGBColor> {
        check(steps > 0) { "steps must be greater than 0" }
        if (steps == 1) return listOf(Color(from.red, from.green, from.blue, from.alpha, false))
        return buildList {
            repeat(steps) { index ->
                add(from.lerp(to, (index * (1f / (steps - 1))).coerceIn(0f, 1f)))
            }
        }
    }

}