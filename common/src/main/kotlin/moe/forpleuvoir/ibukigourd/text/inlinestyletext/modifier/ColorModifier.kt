package moe.forpleuvoir.ibukigourd.text.inlinestyletext.modifier

import moe.forpleuvoir.ibukigourd.text.*
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.util.primitive.either
import net.minecraft.network.chat.MutableComponent
import java.text.BreakIterator
import java.util.*

object ColorModifier : TextModifier {

    override fun modify(exp: String, current: MutableComponent): MutableComponent? {
        val shouldApplyShadow = exp.startsWith("s")
        val cleanExp = if (shouldApplyShadow) exp.substring(1) else exp
        if (cleanExp == "#null" || cleanExp == "#none") {
            return if (shouldApplyShadow)
                current.style { shadowColor(null as Int?) }
            else
                current.style { color(null as Int?) }
        }

        return parseRGBColor(cleanExp, shouldApplyShadow, current)
            ?: parseHSVColor(cleanExp, shouldApplyShadow, current)
    }

    private fun singleColorModifier(color: Color, shadow: Boolean, current: MutableComponent): MutableComponent =
        shadow.either({ current.withShadowColor(color) }, { current.withColor(color) })


    private fun parseRGBColor(exp: String, shadow: Boolean, current: MutableComponent): MutableComponent? {
        return runCatching {
            singleColorModifier(Color.fromHexString(exp), shadow, current)
        }.getOrElse {
            parseRGBGradient(exp, shadow, current)
        }
    }

    private fun parseRGBGradient(exp: String, shadow: Boolean, current: MutableComponent): MutableComponent? {
        val colors = runCatching { exp.split("->").map { Color.fromHexString(it) } }.getOrNull() ?: return null

        if (colors.size < 2) return singleColorModifier(colors[0], shadow, current)

        return multiGradientText(current, colors, shadow, false)
    }

    private fun parseHSVColor(exp: String, shadow: Boolean, current: MutableComponent): MutableComponent? {
        return runCatching {
            singleColorModifier(parseHSV(exp)!!, shadow, current)
        }.getOrElse {
            parseHSVGradient(exp, shadow, current)
        }
    }

    private fun parseHSVGradient(exp: String, shadow: Boolean, current: MutableComponent): MutableComponent? {
        val colors = runCatching { exp.split("->").map { parseHSV(it)!! } }.getOrNull() ?: return null

        if (colors.size < 2) return singleColorModifier(colors[0], shadow, current)

        return multiGradientText(current, colors, shadow, true)
    }

    private val HSV_PATTERN = """\[(\d+(?:\.\d+)?)\s+(\d+(?:\.\d+)?)\s+(\d+(?:\.\d+)?)]""".toRegex()

    private fun parseHSV(exp: String): Color? {
        val (h, s, v) = HSV_PATTERN.matchEntire(exp)?.destructured ?: return null
        val hue = h.toFloatOrNull() ?: return null
        val sat = s.toFloatOrNull() ?: return null
        val value = v.toFloatOrNull() ?: return null
        return if (hue <= 360 && sat <= 100 && value <= 100) {
            Color.fromHSV(hue / 360, sat / 100, value / 100)
        } else null
    }

    private fun multiGradientText(text: MutableComponent, colors: List<Color>, shadow: Boolean, isHsv: Boolean): MutableComponent {
        val texts = splitText(text)
        if (texts.isEmpty()) return text
        val total = texts.size

        val coloredText = texts.mapIndexed { index, component ->
            val t = if (total <= 1) 0f else index.toFloat() / (total - 1)

            val segment = (t * (colors.lastIndex)).toInt().coerceIn(0, colors.size - 2)
            val localT = (t * colors.lastIndex) - segment

            val startColor = colors[segment]
            val endColor = colors[segment + 1]

            val interpolated = if (isHsv) {
                startColor.hsvLerp(endColor, localT, true)
            } else {
                startColor.lerp(endColor, localT, false)
            }

            if (shadow) {
                component.withShadowColor(interpolated)
            } else {
                component.withColor(interpolated)
            }
        }
        return Text.empty().apply {
            coloredText.forEach(this::append)
        }
    }

    private val breakIterator = ThreadLocal.withInitial {
        BreakIterator.getCharacterInstance(Locale.ROOT)
    }

    private fun splitText(text: MutableComponent): List<MutableComponent> = buildList {
        text.flat().asSequence().filterIsInstance<MutableText>().forEach {
            val content = it.plainText
            if (content.isNotBlank()) {
                val iterator = breakIterator.get()
                iterator.setText(content)
                var start = iterator.first()
                var end = iterator.next()
                while (end != BreakIterator.DONE) {
                    val cluster = content.substring(start, end)
                    if (cluster.isNotBlank()) {
                        this += Text.literal(cluster).setStyle(it.style)
                    }
                    start = end
                    end = iterator.next()
                }
            }
        }

    }
}