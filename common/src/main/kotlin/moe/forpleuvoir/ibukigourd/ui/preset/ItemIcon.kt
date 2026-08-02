package moe.forpleuvoir.ibukigourd.ui.preset

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

val LocalItemIconVanillaSize = staticCompositionLocalOf {
    DpSize(45.5.dp, 45.5.dp)
}

val LocalItemIconVanillaPadding = staticCompositionLocalOf {
    PaddingValues(4.dp)
}

val LocalInheritedAlpha = compositionLocalOf { 1f }

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
 * @param scaleOnHover 鼠标悬停时的放大倍数，默认为 1.1。设为 1 则禁用悬浮放大效果
 */
@Composable
fun ItemIconVanilla(
    item: ItemStack,
    modifier: Modifier = Modifier,
    size: DpSize = LocalItemIconVanillaSize.current,
    padding: PaddingValues = LocalItemIconVanillaPadding.current,
    showTooltip: Boolean = true,
    showCount: Boolean = false,
    countColor: moe.forpleuvoir.nebula.common.color.Color = Colors.WHITE,
    scaleOnHover: Float = 1.1f,
) {
    val surface = LocalSkiaSurface.current
    // 追踪组件布局坐标，供原生渲染定位使用
    var offset by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var count by remember { mutableIntStateOf(item.count) }
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val alpha = LocalInheritedAlpha.current
    // 悬浮放大动画：原生渲染管线不响应 Compose graphicsLayer，需把缩放因子喂进 ScaleFactor
    val scale by animateFloatAsState(
        targetValue = if (hovered) scaleOnHover else 1f,
        animationSpec = tween(150),
        label = "itemIconVanillaScale"
    )
    // item 变化时重置数量，并启动逐帧原生渲染循环
    LaunchedEffect(item, alpha) {
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

                    // 根据参数计算放大后的渲染尺寸，保持以图标中心为锚点
                    val renderW = width * scale
                    val renderH = height * scale

                    // 计算组件在窗口中的裁剪区域
                    val clipLeft = coords.boundsInWindow().left * density
                    val clipTop = coords.boundsInWindow().top * density
                    val clipWidth = coords.boundsInWindow().width * density
                    val clipHeight = coords.boundsInWindow().height * density

                    // scissor 同比放大并保持中心对齐，确保放大后的图标不被裁掉
                    val cw = clipWidth * scale
                    val ch = clipHeight * scale
                    val cl = clipLeft + (clipWidth - cw) / 2f
                    val ct = clipTop + (clipHeight - ch) / 2f

                    // 基于原版物品图标 16×16 基准计算缩放比例（叠加悬浮放大）
                    val xScale = renderW / 16f
                    val yScale = renderH / 16f

                    // 以图标中心为锚点重算定位，使放大效果从中心向外扩展
                    val spacerX = coords.positionInWindow().x * density + (width - renderW) / 2f
                    val spacerY = coords.positionInWindow().y * density + (height - renderH) / 2f

                    //原版在 guiScale != 0 的情况下裁剪并不精准
                    enableScissor(cl.toInt() - 1, ct.toInt() - 1, (cl + cw).toInt() + 2, (ct + ch).toInt() + 2)

                    pushItem(
                        item,
                        spacerX,
                        spacerY,
                        ScaleFactor(xScale, yScale),
                        color = Colors.WHITE.alpha(alpha),
                        textColor = countColor.opacity(alpha),
                        showCount = showCount
                    )

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
 * @param scaleOnHover 鼠标悬停时的放大倍数，默认为 1.1。设为 1 则禁用悬浮放大效果
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
        shadow = Shadow(Colors.BLACK.alpha(0.5f).toComposeColor, Offset(3f, 3f), blurRadius = 1f)
    ),
    scaleOnHover: Float = 1.1f,
) {
    val surface = LocalSkiaSurface.current
    var count by remember { mutableStateOf(item.count) }
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    // 悬浮放大动画：仅作用于 Image，避免影响外层布局与数量文本定位
    val scale by animateFloatAsState(
        targetValue = if (hovered) scaleOnHover else 1f,
        animationSpec = tween(150),
        label = "itemIconScale"
    )

    // item 变化时提交渲染请求至队列
    LaunchedEffect(item, imageSize) {
        count = item.count
        SkiaItemRenderHelper.requestRender(item, imageSize.width, imageSize.height)
    }

    // 悬停时通过原生渲染管线逐帧绘制物品提示框
    LaunchedEffect(item, hovered) {
        if (showTooltip && hovered) {
            while (isActive) {
                withFrameNanos {
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

    // 读取图集版本号以订阅缓存就绪事件，上传完成后自动重组
    val revision = SkiaItemRenderHelper.cacheRevision

    // 纹理烘焙完成后以 Compose Image 渲染物品图标
    val painter = remember(item, imageSize, revision) {
        SkiaItemRenderHelper.getPainter(
            item,
            imageSize.width,
            imageSize.height,
            if (item.item is BlockItem) FilterQuality.Medium else FilterQuality.None,
        )
    }
    painter?.let {
        Box(modifier = modifier.defaultMinSize(32.dp, 32.dp)) {
            Image(
                painter = it,
                contentDescription = item.itemName.plainText,
                modifier = Modifier.matchParentSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin.Center
                    }
                    .hoverable(interactionSource),
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

