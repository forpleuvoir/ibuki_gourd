package moe.forpleuvoir.ibukigourd.gui.base.render.arrange

sealed interface Arrangement {

    /**
     * 排列子元素
     * @param parentPosition Float 父元素位置
     * @param parentSize Float 父元素大小
     * @param children List<Float> 子元素的大小集合
     * @return List<Float> 子元素的位置
     */
    fun arrange(parentPosition: Float, parentSize: Float, children: List<Float>): List<Float>

    data object SpaceBetween : Arrangement {
        override fun arrange(parentPosition: Float, parentSize: Float, children: List<Float>): List<Float> {
            val unitSpace = ((parentSize - children.sum()) / (children.lastIndex)).coerceAtLeast(0f)
            var offset = 0f
            return children.map { space ->
                val position = parentPosition + offset
                offset += space
                offset += unitSpace
                position
            }
        }
    }

    data object SpaceAround : Arrangement {
        override fun arrange(parentPosition: Float, parentSize: Float, children: List<Float>): List<Float> {
            val unitSpace = ((parentSize - children.sum()) / (children.size * 2)).coerceAtLeast(0f)
            var offset = 0f
            return children.map { space ->
                offset += unitSpace
                val position = parentPosition + offset
                offset += space
                offset += unitSpace
                position
            }
        }
    }

    data object SpaceEvenly : Arrangement {
        override fun arrange(parentPosition: Float, parentSize: Float, children: List<Float>): List<Float> {
            val unitSpace = ((parentSize - children.sum()) / (children.size + 1)).coerceAtLeast(0f)
            var offset = 0f
            return children.map { space ->
                offset += unitSpace
                val position = parentPosition + offset
                offset += space
                position
            }
        }
    }

    data object Start : Arrangement {
        override fun arrange(parentPosition: Float, parentSize: Float, children: List<Float>): List<Float> {
            var offset = 0f
            return children.map { space ->
                val position = parentPosition + offset
                offset += space
                position
            }
        }
    }

    data object Center : Arrangement {
        override fun arrange(parentPosition: Float, parentSize: Float, children: List<Float>): List<Float> {
            val start = (parentPosition + (parentSize / 2f)) - children.sum() / 2f
            var offset = 0f
            return children.map { space ->
                val position = start + offset
                offset += space
                position
            }
        }
    }

    data object End : Arrangement {
        override fun arrange(parentPosition: Float, parentSize: Float, children: List<Float>): List<Float> {
            val start = (parentPosition + parentSize) - children.sum()
            var offset = 0f
            return children.map { space ->
                val vec = start + offset
                offset += space
                vec
            }
        }
    }

    companion object {

        val values: List<Arrangement> = listOf(SpaceBetween, SpaceAround, SpaceEvenly, Start, Center, End)

    }


}

