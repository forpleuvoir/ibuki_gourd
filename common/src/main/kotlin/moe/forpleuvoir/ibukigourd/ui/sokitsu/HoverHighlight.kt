package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import moe.forpleuvoir.ibukigourd.ui.sokitsu.draw.sokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuAtlasManager
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeToken
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolve
import moe.forpleuvoir.ibukigourd.util.identifier
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * 悬停高亮的缺省值：一笔圆角平涂素材 + 主题主色 + 两成浓度（见 [hoverHighlight]）。
 */
object HoverHighlightDefaults {

    /** 高亮块精灵：铺满所在节点的圆角平涂素材（源素材只有切角，没有描边）。 */
    val sprite: SokitsuSprite
        get() = SokitsuAtlasManager.sprite(
            SokitsuAtlasManager.UI_ATLAS_ID,
            identifier("ui/surface/flat_1x_round"),
        )

    /** 高亮块的染色槽位。 */
    val Tone: ColorSchemeToken = ColorSchemeToken.Primary

    /**
     * 高亮块浓度：状态层要看得见、又不能盖掉底色，取主色的两成。
     *
     * 亮色主题下它压暗底色、暗色主题下它提亮底色，两套主题共用这一个值。
     */
    const val Alpha: Float = 0.2f

    /** 悬停淡入 / 淡出时长。 */
    val Animation: Duration = 120.milliseconds
}

/**
 * 悬停高亮：交互源被悬停时，在**内容之下**铺一层半透明高亮块。
 *
 * 高亮块经 [Modifier.sokitsuSprite] 绘制 —— 精灵先提交、内容后画（见该修饰器的 `draw`），
 * 因此不需要另起一个背景节点；淡入淡出走**染色色的 alpha**：图层 alpha（`Modifier.alpha` /
 * `graphicsLayer`）会把该节点的内容一起淡掉，而这一层只该淡化自己。
 *
 * 悬停状态直接读 [interactionSource]（[collectIsHoveredAsState]），所以多处共用一个交互源时
 * 会一起亮；某个节点不想高亮就不挂这个修饰器。
 *
 * 高亮块铺满所挂节点的范围，节点多大、高亮就多大 —— 想让"整组 / 整行"一起亮，就把它挂在
 * 包住整组 / 整行的那个容器上。
 *
 * @param interactionSource 悬停状态来源；与 `hoverable` / `clickable` 用的那个共用即可
 * @param sprite 高亮块精灵
 * @param color 高亮块染色；未指定时按 [HoverHighlightDefaults.Tone] 解析
 * @param alpha 高亮块浓度（淡入完成后它是最终不透明度）
 * @param animation 淡入 / 淡出时长
 */
@Composable
fun Modifier.hoverHighlight(
    interactionSource: MutableInteractionSource,
    sprite: SokitsuSprite = HoverHighlightDefaults.sprite,
    color: Color = Color.Unspecified,
    alpha: Float = HoverHighlightDefaults.Alpha,
    animation: Duration = HoverHighlightDefaults.Animation,
): Modifier {
    val hovered by interactionSource.collectIsHoveredAsState()
    val progress by animateFloatAsState(
        targetValue = if (hovered) 1f else 0f,
        animationSpec = tween(animation.inWholeMilliseconds.toInt()),
        label = "hoverHighlight",
    )
    val tone = color.resolve(HoverHighlightDefaults.Tone)
    return sokitsuSprite(sprite, tone.copy(alpha = tone.alpha * alpha * progress))
}
