package moe.forpleuvoir.ibukigourd.ui.preset

import androidx.compose.foundation.Image
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.skia.LocalSkiaSurface
import moe.forpleuvoir.ibukigourd.ui.util.render.SkiaItemRenderHelper
import moe.forpleuvoir.ibukigourd.ui.util.toComposeColor
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack

@Composable
fun ItemIcon(
    item: ItemStack,
    modifier: Modifier = Modifier,
    imageSize: IntSize = IntSize(64, 64),
    showTooltip: Boolean = true,
    scale: Float = 1f,
    showCount: Boolean = false,
    countModifier: Modifier = Modifier,
    countAlignment: Alignment = BiasAlignment(0.75f, 0.75f),
    countStyle: TextStyle = TextStyle(
        color = Colors.WHITE.toComposeColor,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        shadow = Shadow(Colors.BLACK.alpha(0.5).toComposeColor, Offset(3f,3f), blurRadius = 1f)
    ),
) {
    val surface = LocalSkiaSurface.current
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var count by remember { mutableStateOf(0) }
    var offset by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    LaunchedEffect(item) {
        bitmap = SkiaItemRenderHelper.renderItemToBufferedImage(item, imageSize.width, imageSize.height)
        count = item.count
        while (isActive) {
            withFrameNanos {
                if (showTooltip && hovered) {
                    val coords = offset ?: return@withFrameNanos
                    if (!coords.isAttached) return@withFrameNanos
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
        Box(modifier = modifier.fillMaxSize()) {
            Image(
                bitmap = it,
                contentDescription = item.itemName.plainText,
                modifier = Modifier.matchParentSize()
                    .scale(scale)
                    .hoverable(interactionSource)
                    .onGloballyPositioned { offset = it },
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
