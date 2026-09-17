package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolve

/**
 * 横向分割线：一条**纯色矩形**，用于在同一列里分隔内容分组。
 *
 * 不依赖任何纹理素材——像素风里线条本身就是 1~2 像素的实心块，画成精灵反而增加
 * 图集与资源包维护成本。颜色走"调用点传参 > 组件 token（[DividerTokens.Line]）> 主题槽位"回退链。
 *
 * 宽度铺满可用空间（[fillMaxWidth]），故需放在有确定宽度的容器里（Column / Box）；
 * 放在 `Row` 这类宽度无界的父布局里会让线条把父布局撑开。
 *
 * @param thickness 线条厚度，默认 [DividerDefaults.thickness]
 * @param color 线条颜色，未指定按 [DividerTokens.Line] 解析
 */
@Composable
fun Divider(
    modifier: Modifier = Modifier,
    thickness: Dp = DividerDefaults.thickness,
    color: Color = Color.Unspecified,
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color.resolve(DividerTokens.Line)),
    )
}
