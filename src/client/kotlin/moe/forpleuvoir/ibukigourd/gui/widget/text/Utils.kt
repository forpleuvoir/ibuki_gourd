package moe.forpleuvoir.ibukigourd.gui.widget.text

val Char.isPunct: Boolean
    get() {
        val ub = Character.UnicodeBlock.of(this)
        val cnPunct = ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS
                || ub == Character.UnicodeBlock.VERTICAL_FORMS
                || ub == Character.UnicodeBlock.MISCELLANEOUS_SYMBOLS // 添加其他常用符号的Unicode块
                || ub == Character.UnicodeBlock.MISCELLANEOUS_SYMBOLS_AND_ARROWS
        val arr = arrayOf('-', '.', ',', '?', ':', '_', '(', ')', '{', '}', '[', ']', '/', '\\', '=')
        return cnPunct || this in arr || this.code == 183 || this.code == 903 || this.code == 1757 || this.code == 1758
                || (!Character.isLetterOrDigit(this) && !Character.isWhitespace(this))
    }