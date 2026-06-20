package moe.forpleuvoir.ibukigourd.ui.preset

import androidx.compose.foundation.Image
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
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
import moe.forpleuvoir.ibukigourd.render.extension.pushItem
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.skia.LocalSkiaSurface
import moe.forpleuvoir.ibukigourd.ui.util.render.SkiaItemRenderHelper
import moe.forpleuvoir.ibukigourd.ui.util.toComposeColor
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import kotlin.time.Duration.Companion.milliseconds

val LocalItemIconVanillaSize = staticCompositionLocalOf {
    DpSize(45.5.dp, 45.5.dp)
}

val LocalItemIconVanillaPadding = staticCompositionLocalOf {
    PaddingValues(4.dp)
}

/**
 * 使用原版方法渲染物品图标
 *
 * 通过调用 Minecraft 原版的物品渲染管线，在 Compose 场景中绘制物品图标。
 * 优点是速度快、渲染效果最好（保留原版动画与着色器效果）；
 * 缺点是无法设置 z index，会导致渲染内容永远在 Compose 场景之上，
 * 不建议在有 Popup 层时使用，或者专用于 Popup 层会比较好。
 *
 * 该组件内部通过 [LaunchedEffect] 持续在每帧进行原生渲染，
 * 并使用一个不可见的 [Spacer] 占据布局空间、追踪位置和悬停状态。
 *
 * @param item 要渲染的物品堆叠
 * @param modifier 应用于占位 [Spacer] 的 Compose 修饰符
 * @param size 物品图标的尺寸，默认使用 [LocalItemIconVanillaSize] 提供的值
 * @param padding 物品图标的内边距，默认使用 [LocalItemIconVanillaPadding] 提供的值
 * @param showTooltip 是否在鼠标悬停时显示物品提示信息，默认为 true
 * @param showCount 是否在物品图标右下角显示堆叠数量（仅当数量大于 1 时显示），默认为 false
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
    // 追踪组件布局坐标，供原生渲染定位使用
    var offset by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var count by remember { mutableStateOf(item.count) }
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    // item 变化时重置数量，并启动逐帧原生渲染循环
    LaunchedEffect(item) {
        count = item.count
        while (isActive) {
            withFrameNanos {
                val coords = offset ?: return@withFrameNanos
                surface.postRender {
                    if (!coords.isAttached) return@postRender
                    // 根据 GUI 缩放比将 Compose 坐标转换为游戏内像素坐标
                    val guiScale = mc.window.guiScale.toFloat()
                    val density = 1f / guiScale

                    val width = coords.size.width * density
                    val height = coords.size.height * density

                    // 计算组件在窗口中的裁剪区域
                    val clipLeft = coords.boundsInWindow().left * density
                    val clipTop = coords.boundsInWindow().top * density
                    val clipWidth = coords.boundsInWindow().width * density
                    val clipHeight = coords.boundsInWindow().height * density

                    // 基于原版物品图标 16×16 基准计算缩放比例
                    val xScale = width / 16f
                    val yScale = height / 16f

                    val spacerX = coords.positionInWindow().x * density
                    val spacerY = coords.positionInWindow().y * density

                    //原版在 guiScale != 0 的情况下裁剪并不精准
                    enableScissor(clipLeft.toInt() - 1, clipTop.toInt() - 1, (clipLeft + clipWidth).toInt() + 2, (clipTop + clipHeight).toInt() + 2)

                    pushItem(item, spacerX, spacerY, ScaleFactor(xScale, yScale), showCount = showCount)

                    disableScissor()

                    // 悬停时渲染原版物品提示框
                    if (showTooltip && hovered) {
                        val mouseX = (mc.mouseHandler.xpos() * density).toInt()
                        val mouseY = (mc.mouseHandler.ypos() * density).toInt()
                        setTooltipForNextFrame(mc.font, item, mouseX, mouseY)
                    }
                }
            }
        }
    }
    // 不可见 Spacer 占位，追踪全局位置与悬停交互
    Spacer(
        modifier
            .padding(padding)
            .size(size)
            .hoverable(interactionSource)
            .onGloballyPositioned { offset = it }
    )
}

/**
 * 使用烘焙的纹理渲染物品图标
 *
 * 通过预渲染物品纹理为位图，再以 [Image] 组件的方式在 Compose 场景中显示。
 * 优点是与 Compose 场景兼容性更好（支持 z index、裁剪等）；
 * 缺点是无法实时更新，没有原版的物品动画效果。
 *
 * @param item 要渲染的物品堆叠
 * @param modifier 应用于外层 [Box] 容器的 Compose 修饰符
 * @param imageSize 预渲染位图的像素尺寸，默认为 64×64
 * @param showTooltip 是否在鼠标悬停时显示物品提示信息，默认为 true
 * @param showCount 是否显示堆叠数量文本，默认为 false
 * @param countModifier 应用于数量 [Text] 组件的 Compose 修饰符
 * @param countAlignment 数量文本在容器中的对齐方式，默认偏右下角
 * @param countStyle 数量文本的样式，默认为白色粗体带黑色阴影
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

    // item 变化时重新烘焙纹理，并启动逐帧提示框渲染循环
    LaunchedEffect(item) {
        delay(1.milliseconds)
        mc.execute {
            bitmap = SkiaItemRenderHelper.renderItemToBufferedImage(item, imageSize.width, imageSize.height)
        }
        count = item.count
        while (isActive) {
            withFrameNanos {
                // 悬停时通过原生渲染管线绘制物品提示框
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

    // 纹理烘焙完成后以 Compose Image 渲染物品图标
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
            // 需要时在指定对齐位置叠加显示堆叠数量
            if (showCount && count > 1) {
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
