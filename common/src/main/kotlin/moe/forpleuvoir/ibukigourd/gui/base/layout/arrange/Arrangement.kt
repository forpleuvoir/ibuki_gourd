package moe.forpleuvoir.ibukigourd.gui.base.layout.arrange

object Arrangement {

    interface Linear {
        val spacing: Float
            get() = 0f

        fun arrange(space: Float, sizes: List<Float>): List<Float>

        fun totalSize(sizes: List<Float>): Float =
            sizes.sum() + sizes.lastIndex * spacing
    }

    fun interface Horizontal : Linear

    fun interface Vertical : Linear

    fun interface HorizontalOrVertical : Horizontal, Vertical {
        override val spacing: Float
            get() = 0f
    }

    val Left: Horizontal = SpacedAligned(0f, Alignment.Left::align)

    val Right: Horizontal = SpacedAligned(0f, Alignment.Right::align)

    val Top: Vertical = SpacedAligned(0f, Alignment.Top::align)

    val Bottom: Vertical = SpacedAligned(0f, Alignment.Bottom::align)

    val Center: HorizontalOrVertical = SpacedAligned(0f) { space, size -> (space - size) / 2 }

    /**
     * A#A#A
     */
    val SpaceBetween = HorizontalOrVertical { space, sizes ->
        val unitSpace = ((space - sizes.sum()) / (sizes.lastIndex)).coerceAtLeast(0f)
        var offset = 0f
        sizes.map { s ->
            val position = offset
            offset += s
            offset += unitSpace
            position
        }
    }

    /**
     * #A##A##A#
     */
    val SpaceAround = HorizontalOrVertical { space, sizes ->
        val unitSpace = ((space - sizes.sum()) / (sizes.size * 2)).coerceAtLeast(0f)
        var offset = 0f
        sizes.map { s ->
            offset += unitSpace
            val position = offset
            offset += s
            offset += unitSpace
            position
        }
    }

    /**
     * #A#A#A#
     */
    val SpaceEvenly = HorizontalOrVertical { space, sizes ->
        val unitSpace = ((space - sizes.sum()) / (sizes.size + 1)).coerceAtLeast(0f)
        var offset = 0f
        sizes.map { s ->
            offset += unitSpace
            val position = offset
            offset += s
            position
        }
    }

    fun spacedBy(space: Float): HorizontalOrVertical =
        SpacedAligned(space) { s, size ->
            Alignment.Left.align(s, size)
        }

    fun spacedBy(space: Float, alignment: Alignment.Horizontal): Horizontal =
        SpacedAligned(space, alignment::align)

    fun spacedBy(space: Float, alignment: Alignment.Vertical): Vertical =
        SpacedAligned(space, alignment::align)

    fun aligned(alignment: Alignment.Horizontal): Horizontal =
        SpacedAligned(0f, alignment::align)

    fun aligned(alignment: Alignment.Vertical): Vertical =
        SpacedAligned(0f, alignment::align)

    open class SpacedAligned(override val spacing: Float, val alignment: (space: Float, size: Float) -> Float) : HorizontalOrVertical {
        override fun arrange(space: Float, sizes: List<Float>): List<Float> {
            if (sizes.isEmpty()) return emptyList()
            //子元素所占总空间
            val childrenSpace = sizes.sum() + (sizes.lastIndex * this.spacing)
            //总偏移
            val groupOffset = alignment.invoke(space, childrenSpace)
            return sizes.runningFold(groupOffset) { offset, size ->
                offset + size + spacing
            }.dropLast(1)
        }
    }

}






