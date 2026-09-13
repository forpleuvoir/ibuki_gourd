package moe.forpleuvoir.ibukigourd.render.extension

/**
 * 气泡（tooltip / speech bubble）相对锚组件的位置：**气泡在目标的 X 侧**，而非箭头指向。
 *
 * 与 `ui/tooltip/arrow/{above,below,left,right}` 纹理文件名 1:1（文件名 = 气泡位置），故：
 * - [Above]  → 气泡在目标**上方** → 箭头在气泡**底边** → 九宫格底行中格 `(2,1)` 开缺口
 * - [Below]  → 气泡在目标**下方** → 箭头在气泡**顶边** → 九宫格顶行中格 `(0,1)` 开缺口
 * - [Left]   → 气泡在目标**左侧** → 箭头在气泡**右边** → 九宫格右列中格 `(1,2)` 开缺口
 * - [Right]  → 气泡在目标**右侧** → 箭头在气泡**左边** → 九宫格左列中格 `(1,0)` 开缺口
 *
 * 该语义与 `BubbleEdge`（描述"箭头在气泡体的边"）反义，故气泡统一以本类型表达位置。
 *
 * 用 `enum class`（非 `value class`）以保证 `when` 表达式可穷尽（多调用点的表达式 `when` 依赖此），
 * 与 compose-minecraft 的 `popup.AnchorPosition` 风格一致。
 */
enum class AnchorPosition {
    Above,
    Below,
    Left,
    Right,
}
