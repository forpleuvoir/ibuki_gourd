package moe.forpleuvoir.ibukigourd.gui.base.element

/**
 * GUI层级
 * 每一个`层`都应该是单例对象
 * 由Screen管理,一般元素使用默认`层`.
 * 处理顺序由Screen管理,在Screen中维护一个`层`顺序列表,记录`层`处理顺序.
 * 每一个元素都有一个所在`层`的属性,将元素添加到Screen中时,将元素所在`层`添加到Screen中时,会按照`层`分类添加到Map<Layer,List<Element>>中
 */
interface Layer {

    companion object {

        val default = DefaultLayer

        val pop = PopLayer

    }


}

/**
 * 默认层
 */
object DefaultLayer : Layer

/**
 * 弹出层
 */
object PopLayer : Layer