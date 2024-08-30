package moe.forpleuvoir.ibukigourd.gui.base.layout.util

@JvmInline
value class FillMode private constructor(val code: Int) {

    companion object {

        val None = FillMode(0)

        /**
         * 表示填充父组件的模式。
         * 此常量用于指定组件应当填充其父组件的整个空间。
         */
        val MatchParent = FillMode(1)

        /**
         * 表示填充兄弟组件的模式。
         * 此常量用于指定组件应当填充至其他最大尺寸的兄弟组件空间
         */
        val MatchSibling = FillMode(2)

    }


}