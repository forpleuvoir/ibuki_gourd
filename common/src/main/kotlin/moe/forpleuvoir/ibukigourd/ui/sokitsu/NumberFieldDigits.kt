package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.text.TextRange

/**
 * 数值输入框"按位步进"的两个纯函数：算**位权**、算**步进后的光标**。
 *
 * ## 位权（[placeExponent]）
 *
 * 指数按"同侧还有几位数字"数，位权 = `10^指数`：
 * - 整数位：它右边还剩几位**整数**数字（个位 0、十位 1、百位 2…），小数部分不算；
 * - 小数位：它是小数点后第几位，取负（十分位 -1、百分位 -2…）。
 *
 * | 文本 + 光标 | 命中位 | 指数 | 位权 |
 * |---|---|---|---|
 * | `\|35` | `3`（右边还有 1 位整数） | 1 | `10` |
 * | `3\|5` | `5`（右边没有整数位） | 0 | `1` |
 * | `\|2.0` | `2`（右边只剩小数位） | 0 | `1` |
 * | `0.\|25` | `2`（小数点后第 1 位） | -1 | `0.1` |
 * | `0.2\|5` | `5`（小数点后第 2 位） | -2 | `0.01` |
 * | `16\|` | 无 → 再下一位 | -1 | `0.1` |
 *
 * 方向键只做数值加减（`值 ± 位权`），**没有字符串进位 / 借位** —— `0` 退一位就是 `-1`，
 * `|199` 百位加一就是 `299`，越界由各字段自己的 `valueRange` 收敛。各入口再按自己的
 * 值语义换算：`Int` / `Long` 取整且至少为 1；`Percent` 内部是占比、显示 ×100，故指数减 2；
 * `Duration` 没有位权概念，传 `null` 不接管方向键。
 *
 * ## 光标（[restoreDigitCursor]）
 *
 * 步进后按**"右侧数字个数不变"**落位：`-`、`.` 不是数字、不参与计数，所以补负号不会把光标
 * 推错，进位长出新高位也自然落在正确一侧。
 *
 * | 步进前 | 右侧数字个数 | 步进后 | 光标 |
 * |---|---|---|---|
 * | `\|9` | 1 | `10` | `1\|0` |
 * | `\|90` | 2 | `100` | `1\|00` |
 * | `\|0` | 1 | `-1` | `-\|1` |
 * | `\|35` | 2 | `45` | `\|45` |
 * | `3\|5` | 1 | `36` | `3\|6` |
 */

/** 从 [index] 起向右（含自身）找最近一个数字的下标；找不到返回 `null`。 */
private fun CharSequence.nextDigitIndex(index: Int): Int? {
    for (i in index.coerceIn(0, length) until length) if (this[i] in '0'..'9') return i
    return null
}

/**
 * 光标 [cursor] 所指示的数字下标：**位置本身是数字就是它**（`|35` → `3`、`3|5` → `5`），
 * 否则往右找最近的一个（`0|.25` → `.` 右边的 `2`）。
 *
 * 光标落在**文本末尾**时右边没有数字，返回 `null` —— 由 [placeExponent] 按"再下一位"处理。
 * 注意不要退回左边找：`|---9` 这类"光标在末尾"与 `---9|` 是同一个位置，
 * 若退回左边就会把 `0|.00` 错当成个位。
 */
fun CharSequence.digitIndexAt(cursor: Int): Int? = nextDigitIndex(cursor)

/**
 * 光标 [cursor] 所指示数位的位权指数（位权 = `10^指数`），见文件头表格。
 */
fun placeExponent(text: CharSequence, cursor: Int): Int {
    val index = text.digitIndexAt(cursor) ?: return -(digitsAfterDot(text) + 1)
    val dot = text.indexOf('.')
    return if (dot < 0 || index < dot) {
        integerExponent(text, index)
    } else {
        fractionalExponent(text, index)
    }
}

/** 整数位 [index] 的指数：它右边还剩几位**整数**数字（小数部分不算）。 */
private fun integerExponent(text: CharSequence, index: Int): Int {
    var count = 0
    for (i in index + 1 until text.length) {
        if (text[i] == '.') break
        if (text[i] in '0'..'9') count++
    }
    return count
}

/** 小数位 [index] 的指数：它在小数点后第几位，取负。 */
private fun fractionalExponent(text: CharSequence, index: Int): Int {
    val dot = text.indexOf('.')
    var position = 0
    for (i in dot + 1..index) if (text[i] in '0'..'9') position++
    return -position
}

/** [text] 小数点之后的数字个数；没有小数点时为 `0`。 */
private fun digitsAfterDot(text: CharSequence): Int {
    val dot = text.indexOf('.')
    if (dot < 0) return 0
    var count = 0
    for (i in dot + 1 until text.length) if (text[i] in '0'..'9') count++
    return count
}

/**
 * 把光标落回**右侧数字个数与 [digitsToRight] 一致**的位置。
 *
 * 这是"同一个数位"的唯一稳定定义：从文本末尾往左数，数到第 `digitsToRight` 个数字为止，
 * 光标就停在那里。正负号、小数点都不是数字、不参与计数，所以"补负号"这种只插入装饰字符
 * 的变化不会把光标推错；进位长出新高位时（数字变多）也自动落在正确的一侧。
 *
 * | 步进前 | 右侧数字个数 | 步进后 | 光标 |
 * |---|---|---|---|
 * | `\|9` | 1 | `10` | `1\|0` |
 * | `\|90` | 2 | `100` | `1\|00` |
 * | `\|0` | 1 | `-1` | `-\|1` |
 * | `\|35` | 2 | `45` | `\|45` |
 * | `3\|5` | 1 | `36` | `3\|6` |
 *
 * @param digitsToRight 步进前光标右侧（含光标所在位置的字符起算）的数字个数
 */
internal fun TextFieldState.restoreDigitCursor(digitsToRight: Int) {
    val text = text.toString()
    var remaining = digitsToRight
    var i = text.length - 1
    while (i >= 0 && remaining > 0) {
        if (text[i] in '0'..'9') remaining--
        i--
    }
    // 循环退出时 i 指向"已数到的第 digitsToRight 个数字"的前一个字符，光标落在它之后
    edit { selection = TextRange((i + 1).coerceIn(0, text.length)) }
}

/** [cursor] 及其右侧的数字个数（`.``-` 不计）。 */
fun digitsRightOf(text: CharSequence, cursor: Int): Int {
    var count = 0
    for (i in cursor.coerceIn(0, text.length) until text.length) {
        if (text[i] in '0'..'9') count++
    }
    return count
}

