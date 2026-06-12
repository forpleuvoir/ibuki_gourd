package moe.forpleuvoir.ibukigourd.ui.preset

import androidx.compose.foundation.Image
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.skia.LocalSkiaSurface
import moe.forpleuvoir.ibukigourd.ui.util.render.SkiaItemRenderHelper
import moe.forpleuvoir.ibukigourd.ui.util.toComposeColor
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.textRenderer
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import kotlin.time.Duration.Companion.milliseconds

val LocalItemIconVanillaSize = staticCompositionLocalOf {
    DpSize(42.5.dp, 42.5.dp)
}

val LocalItemIconVanillaPadding = staticCompositionLocalOf {
    PaddingValues(4.dp)
}

/**
 * 使用原版方法渲染物品
 * 优点是速度快,效果最好,缺点是无法设置 z index,会导致永远在`Compose`场景之上,不建议在有Popup层时使用,或者专用于Popup层会比较好
 */
@Composable
fun ItemIconVanilla(
    item: ItemStack,
    modifier: Modifier = Modifier,
    size: DpSize = LocalItemIconVanillaSize.current,
    padding: PaddingValues = LocalItemIconVanillaPadding.current,
    showTooltip: Boolean = true,
    showCount: Boolean = false,
) {
    val surface = LocalSkiaSurface.current
    var offset by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var count by remember { mutableStateOf(item.count) }
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    LaunchedEffect(item) {
        count = item.count
        while (isActive) {
            withFrameNanos {
                val coords = offset ?: return@withFrameNanos
                surface.postRender {
                    if (!coords.isAttached) return@postRender
                    val guiScale = mc.window.guiScale.toFloat()
                    val density = 1f / guiScale

                    val width = coords.size.width * density
                    val height = coords.size.height * density

                    val clipLeft = coords.boundsInWindow().left * density
                    val clipTop = coords.boundsInWindow().top * density
                    val clipWidth = coords.boundsInWindow().width * density
                    val clipHeight = coords.boundsInWindow().height * density

                    val xScale = width / 16f
                    val yScale = height / 16f

                    val spacerX = coords.positionInWindow().x * density
                    val spacerY = coords.positionInWindow().y * density

                    val x = spacerX / xScale
                    val y = spacerY / yScale

                    val xi = x.toInt()
                    val yi = y.toInt()

                    //原版在 guiScale != 0 的情况下裁剪并不精准
                    enableScissor(clipLeft.toInt() - 1, clipTop.toInt() - 1, (clipLeft + clipWidth).toInt() + 2, (clipTop + clipHeight).toInt() + 2)

                    pose().pushMatrix()
                    pose().scale(xScale, yScale)
                    pose().translate(x - xi, y - yi)


                    fakeItem(item, xi, yi)

                    if (showCount && count > 1) {
                        val count = count.toString()
                        text(textRenderer, count, xi + 17 - textRenderer.width(count), yi + 9, -1, true)
                    }

                    disableScissor()
                    pose().popMatrix()

                    if (showTooltip && hovered) {
                        val mouseX = (mc.mouseHandler.xpos() * density).toInt()
                        val mouseY = (mc.mouseHandler.ypos() * density).toInt()
                        setTooltipForNextFrame(mc.font, item, mouseX, mouseY)
                    }
                }
            }
        }
    }
    Spacer(
        modifier
            .padding(padding)
            .size(size)
            .hoverable(interactionSource)
            .onGloballyPositioned { offset = it }
    )
}

/**
 * 使用烘焙的纹理渲染物品
 * 优点是与Compose场景兼容性更好,缺点是无法实时更新,没有原版的物品动画
 */
@Composable
fun ItemIcon(
    item: ItemStack,
    modifier: Modifier = Modifier,
    imageSize: IntSize = IntSize(64, 64),
    showTooltip: Boolean = true,
    showCount: Boolean = false,
    countModifier: Modifier = Modifier,
    countAlignment: Alignment = BiasAlignment(0.75f, 0.85f),
    countStyle: TextStyle = TextStyle(
        color = Colors.WHITE.toComposeColor,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        shadow = Shadow(Colors.BLACK.alpha(0.5).toComposeColor, Offset(3f, 3f), blurRadius = 1f)
    )
) {
    val surface = LocalSkiaSurface.current
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var count by remember { mutableStateOf(item.count) }
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    LaunchedEffect(item) {
        delay(1.milliseconds)
        mc.execute {
            bitmap = SkiaItemRenderHelper.renderItemToBufferedImage(item, imageSize.width, imageSize.height)
        }
        count = item.count
        while (isActive) {
            withFrameNanos {
                if (showTooltip && hovered) {
                    surface.postRender {
                        val guiScale = mc.window.guiScale.toFloat()
                        val density = 1f / guiScale
                        val mouseX = (mc.mouseHandler.xpos() * density).toInt()
                        val mouseY = (mc.mouseHandler.ypos() * density).toInt()
                        setTooltipForNextFrame(mc.font, item, mouseX, mouseY)
                    }
                }
            }
        }
    }

    bitmap?.let {
        Box(modifier = modifier.defaultMinSize(32.dp, 32.dp)) {
            Image(
                bitmap = it,
                contentDescription = item.itemName.plainText,
                modifier = Modifier.matchParentSize()
                    .hoverable(interactionSource),
                filterQuality = if (item.item is BlockItem) FilterQuality.Medium else FilterQuality.None,
                contentScale = ContentScale.Crop
            )
            if (showCount) {
                Text(
                    text = count.toString(),
                    modifier = Modifier
                        .align(countAlignment)
                        .then(countModifier),
                    style = countStyle
                )
            }
        }
    }
}
