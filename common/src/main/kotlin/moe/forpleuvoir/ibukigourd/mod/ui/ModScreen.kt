package moe.forpleuvoir.ibukigourd.mod.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.icon.default.DarkMode
import moe.forpleuvoir.ibukigourd.ui.icon.default.LightMode
import moe.forpleuvoir.ibukigourd.ui.icon.default.Menu
import moe.forpleuvoir.ibukigourd.ui.icon.filled.DarkMode
import moe.forpleuvoir.ibukigourd.ui.icon.filled.LightMode

/**
 * 抽屉导航项：[label] / [icon] 为可自由组合的 Composable，选中时主区域渲染 [content]。
 */
class DrawerItem(
    val label: @Composable () -> Unit,
    val icon: @Composable () -> Unit,
    val content: @Composable () -> Unit,
)

/**
 * 通用模组界面：顶栏 + 可滑出抽屉 + 主内容区。
 *
 * 抽屉顶部和底部为可选 slot（[header] / [footer]），由调用方自行填充
 * （如模组身份卡片、主题切换等），保持本组件与具体模组解耦。
 *
 * @param title 顶栏标题
 * @param items 抽屉导航项，第一项默认选中
 * @param header 抽屉顶部内容
 * @param footer 抽屉底部内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModScreen(
    title: @Composable () -> Unit,
    items: List<DrawerItem>,
    header: @Composable (() -> Unit)? = null,
    footer: @Composable (() -> Unit)? = null,
) {
    require(items.isNotEmpty()) { "items must not be empty" }

    var drawerOpen by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(0) }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { drawerOpen = !drawerOpen }) {
                            Icon(Icons.Menu, null)
                        }
                    },
                    title = {
                        ProvideTextStyle(MaterialTheme.typography.titleLarge, title)
                    },
                )
            }
        ) { innerPadding ->
            Column(Modifier.padding(innerPadding).padding(horizontal = 16.dp)) {
                items[selectedIndex].content()
            }
        }

        // 遮罩
        AnimatedVisibility(
            visible = drawerOpen,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { drawerOpen = false }
                    )
            )
        }

        // 抽屉
        AnimatedVisibility(
            visible = drawerOpen,
            enter = slideInHorizontally { -it },
            exit = slideOutHorizontally { -it },
        ) {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(16.dp))
                    header?.invoke()

                    if (header != null) Spacer(Modifier.height(16.dp))
                    items.forEachIndexed { index, item ->
                        NavigationDrawerItem(
                            label = {
                                ProvideTextStyle(MaterialTheme.typography.labelLarge, item.label)
                            },
                            selected = index == selectedIndex,
                            icon = item.icon,
                            onClick = {
                                selectedIndex = index
                                drawerOpen = false
                            }
                        )
                    }

                    if (footer != null) {
                        Spacer(Modifier.weight(1f))
                        HorizontalDivider(Modifier.padding(vertical = 6.dp))
                        footer()
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

/**
 * 抽屉顶部模组身份卡片：左侧标识 + 主标题 / 副标题，作为视觉锚点填补顶部空白。
 *
 * [monogram] 为完全自由的 slot（文本 monogram、纹理 logo 等均可），
 * 组件不对其施加任何背景或形状，由调用方自行决定外观。
 * [name] / [subtitle] 均提供默认 typography，调用方传入的 Text 若未显式指定 style 则采用默认值。
 *
 * @param monogram 左侧标识内容（如纹理 logo、文字缩写）
 * @param name 主标题
 * @param subtitle 副标题（如 mod id）
 */
@Composable
fun DrawerHeader(
    monogram: @Composable () -> Unit,
    name: @Composable () -> Unit,
    subtitle: @Composable () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        monogram()
        Column {
            ProvideTextStyle(MaterialTheme.typography.titleMedium, name)
            val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
            CompositionLocalProvider(LocalContentColor provides onSurfaceVariant) {
                ProvideTextStyle(MaterialTheme.typography.labelSmall, subtitle)
            }
        }
    }
}

/**
 * 底部主题切换：浅色 / 深色。一个 primary 高亮块在两栏之间滑动，
 * 配合图标颜色与形态过渡。
 *
 * @param isLight 当前是否浅色模式
 * @param onToggle 传入目标是否浅色
 */
@Composable
fun ThemeSwitcher(
    isLight: Boolean,
    onToggle: (isLight: Boolean) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        val gap = 4.dp
        // maxWidth 已是 padding 之后的内容宽度，每栏宽度 = (内容宽 - 间距) / 2，需与 Row 中 weight(1f) 子项一致
        val half = (maxWidth - gap) / 2

        // 选中项的滑块位移：深色在左(offset 0)，浅色在右(offset half + gap)
        val indicatorOffset by animateDpAsState(
            targetValue = if (isLight) half + gap else 0.dp,
            animationSpec = tween(durationMillis = 200),
            label = "themeIndicatorOffset"
        )

        // 高亮滑块（置于底层，图标在其上）
        Box(
            modifier = Modifier
                .offset { IntOffset(indicatorOffset.roundToPx(), 0) }
                .width(half)
                .height(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primary)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            ThemeOption(
                modifier = Modifier.weight(1f),
                activeIcon = Icons.Filled.DarkMode,
                inactiveIcon = Icons.DarkMode,
                selected = !isLight,
                onClick = { onToggle(false) }
            )
            ThemeOption(
                modifier = Modifier.weight(1f),
                activeIcon = Icons.Filled.LightMode,
                inactiveIcon = Icons.LightMode,
                selected = isLight,
                onClick = { onToggle(true) }
            )
        }
    }
}

@Composable
private fun ThemeOption(
    modifier: Modifier = Modifier,
    activeIcon: ImageVector,
    inactiveIcon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    // 图标颜色随选中态平滑过渡
    val tint by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 200),
        label = "themeOptionTint"
    )
    Box(
        modifier = modifier
            .height(32.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(
            targetState = selected,
            animationSpec = tween(durationMillis = 200),
            label = "themeOptionIcon"
        ) { active ->
            Icon(
                if (active) activeIcon else inactiveIcon,
                null,
                tint = tint
            )
        }
    }
}
