package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 表格的排版缺省值。
 *
 * 表格是**应用级页面的排版量**（不随主题或资源包变化），因此不设 `ui_meta` 段，
 * 需要局部调整时在调用点覆盖 [TableLayout] / [LazyTableLayout] 的对应参数即可。
 */
object TableLayoutDefaults {

    /** 行间距。 */
    val RowGap: Dp = 2.dp

    /** 列间距。 */
    val ColumnGap: Dp = 12.dp
}
