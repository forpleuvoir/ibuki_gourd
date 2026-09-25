package moe.forpleuvoir.ibukigourd.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mojang.blaze3d.textures.FilterMode
import moe.forpleuvoir.compose_minecraft.platform.ui.draw.minecraftTexture
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TabStrip
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TabStripColors
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TabStripDefaults
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TabStripPlacement
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TabStripSprites
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TabStripTab
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalColorScheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ProvideContentColorTextStyle
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuTheme
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier

/**
 * 模组屏幕的一页。
 *
 * @param title 页签文字；页签内单行居中，放不下时省略
 * @param content 这一页的内容
 */
@Immutable
class ModScreenTab(
    val title: Component,
    val content: @Composable () -> Unit,
)

/**
 * 模组屏幕的选中状态（当前页签下标）。
 *
 * 由调用方持有即可从外部切页（比如"打开某页"的入口、或顶栏动作里的跳转），经 [ModScreen] 的
 * `state` 传进去；不传则由屏幕自己 [rememberModScreenState]。下标越界时按页数钳制，
 * 页数变化不需要调用方自己修正。
 */
@Stable
class ModScreenState(selectedTab: Int = 0) {

    /** 当前选中的页签下标。 */
    var selectedTab: Int by mutableIntStateOf(selectedTab)
}

/**
 * 记住一份 [ModScreenState]。
 *
 * @param initialSelectedTab 初始选中的页签下标
 */
@Composable
fun rememberModScreenState(initialSelectedTab: Int = 0): ModScreenState =
    remember { ModScreenState(initialSelectedTab) }

/**
 * 模组屏幕：顶栏一行（图标槽 + 标题槽 + 尾部动作槽），下面是页签条（[TabStrip]），页签下方挂各页内容。
 *
 * 本组件只负责排版，不绑定任何具体模组：顶栏显示什么、有哪几页、每页放什么，全部由调用方给
 * （[title] / [icon] / [tabs]，需要更自由的顶栏时用 [ModScreen] 的 `header` 重载整个替换）。
 *
 * 页签条用主题的全屏面板素材铺底，页签带着它顶上那条边一起构成页签 / 面板的分界；
 * 面板里的每页内容自己决定怎么排（配置页通常把右侧"配置本体"画成一块内嵌面板）。
 *
 * 配色 / 字体 / 像素缩放由 [moe.forpleuvoir.ibukigourd.ui.sokitsu.SokitsuScreen] 铺好，
 * 直接组合本函数时必须自己套它。
 *
 * @param tabs 全部页面；顺序即页签顺序，第一页默认选中
 * @param modifier 作用于整屏
 * @param title 顶栏标题槽；内容色由顶栏下发（见 [ModScreenHeader]）
 * @param icon 顶栏图标槽，排在标题之前；模组图标可用 [ModScreenIcon] 拼
 * @param headerActions 顶栏尾部动作槽，排在标题之后、靠右
 * @param state 选中状态；需要从外部切页时传入自己持有的 [ModScreenState]
 * @param placement 页签行相对面板的位置
 * @param colors 页签条配色；缺省取 [ModScreenDefaults.colors]
 * @param sprites 页签条精灵；缺省按 [placement] 取主题的全屏面板素材
 */
@Composable
fun ModScreen(
    tabs: List<ModScreenTab>,
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit = {},
    icon: @Composable () -> Unit = {},
    headerActions: @Composable RowScope.() -> Unit = {},
    state: ModScreenState = rememberModScreenState(),
    placement: TabStripPlacement = ModScreenDefaults.Placement,
    colors: TabStripColors = ModScreenDefaults.colors(),
    sprites: TabStripSprites = TabStripDefaults.screenPanelSprites(placement),
) = ModScreen(
    tabs = tabs,
    header = { ModScreenHeader(title = title, icon = icon, actions = headerActions) },
    modifier = modifier,
    state = state,
    placement = placement,
    colors = colors,
    sprites = sprites,
)

/**
 * [ModScreen] 的自定义顶栏重载：顶栏整个由调用方给（如模组自己的工具栏），其余参数语义相同。
 *
 * @param tabs 全部页面；顺序即页签顺序，第一页默认选中
 * @param header 顶栏内容
 * @param modifier 作用于整屏
 * @param state 选中状态；需要从外部切页时传入自己持有的 [ModScreenState]
 * @param placement 页签行相对面板的位置
 * @param colors 页签条配色；缺省取 [ModScreenDefaults.colors]
 * @param sprites 页签条精灵；缺省按 [placement] 取主题的全屏面板素材
 */
@Composable
fun ModScreen(
    tabs: List<ModScreenTab>,
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    state: ModScreenState = rememberModScreenState(),
    placement: TabStripPlacement = ModScreenDefaults.Placement,
    colors: TabStripColors = ModScreenDefaults.colors(),
    sprites: TabStripSprites = TabStripDefaults.screenPanelSprites(placement),
) {
    val current = state.selectedTab.coerceIn(0, tabs.lastIndex.coerceAtLeast(0))

    Column(modifier.fillMaxSize()) {
        header()
        TabStrip(
            selectedTab = current,
            modifier = Modifier.weight(1f),
            placement = placement,
            colors = colors,
            sprites = sprites,
            tabs = {
                tabs.forEachIndexed { index, tab ->
                    TabStripTab(
                        selected = index == current,
                        onClick = { state.selectedTab = index },
                    ) {
                        Text(component = tab.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            },
        ) {
            // 面板底由页签条自己的面板素材提供
            tabs.getOrNull(current)?.content?.invoke()
        }
    }
}

/**
 * 模组屏幕顶栏：[ModScreen] 的缺省顶栏重载用它；要换掉整行时走 [ModScreen] 的 `header` 重载。
 *
 * 三个槽位依次排开：图标 → 标题 → 尾部动作（靠右）。标题槽由本组件下发内容色
 * （[titleColor]，缺省主题主色），槽里的 [Text] 不显式指定颜色即取到它；图标槽里的模组图标
 * 可用 [ModScreenIcon] 拼（原版纹理 + 可选点击）。槽位为空时不占位。
 *
 * @param title 标题槽
 * @param modifier 作用于顶栏整行
 * @param icon 图标槽，排在标题之前
 * @param actions 尾部动作槽，排在标题之后、靠右
 * @param titleColor 下发给标题槽的内容色；未指定时取主题的 primary
 */
@Composable
fun ModScreenHeader(
    title: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    titleColor: Color = Color.Unspecified,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(ModScreenDefaults.HeaderPadding),
        horizontalArrangement = Arrangement.spacedBy(ModScreenDefaults.HeaderSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        ProvideContentColorTextStyle(
            contentColor = titleColor.takeOrElse { LocalColorScheme.current.primary },
            textStyle = TextStyle(fontSize = SokitsuTheme.typography.subtitle.fontSize),
        ) {
            title()
        }
        Spacer(Modifier.weight(1f))
        actions()
    }
}

/**
 * 顶栏模组图标：原版纹理 + 可选点击，可放进 [ModScreenHeader] / [ModScreen] 的 `icon` 槽。
 *
 * 图标用 [Modifier.minecraftTexture] 直接画**原版纹理**（mod 元数据里那一个，不走 sokitsu 图集、
 * 也不需要副本），按 1 纹理像素 = 1 逻辑像素画、过滤取最近邻，缩放不糊。
 * 给 [onClick] 才可点：点它**没有任何反馈** —— 不给 indication、也不换手型光标，按下去不会亮、
 * 不出声，与"这是一张图"的观感一致；不给则整块不可交互。
 *
 * @param icon 原版纹理 id（即 `assets/<namespace>/icon.png`）
 * @param modifier 作用于图标盒子
 * @param onClick 点击回调；null 时不可交互
 */
@Composable
fun ModScreenIcon(
    icon: Identifier,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier
            .size(ModScreenDefaults.IconSize)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = onClick != null,
                onClick = { onClick?.invoke() },
            )
            // 像素素材按 1 纹理像素 : 1 逻辑像素画，过滤取最近邻，缩放不糊
            .minecraftTexture(icon, filterMode = FilterMode.NEAREST),
    )
}

/**
 * 模组屏幕的排版常量与缺省配色。
 */
object ModScreenDefaults {

    /** 顶栏内边距。 */
    val HeaderPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp)

    /** 顶栏内元素间距。 */
    val HeaderSpacing: Dp = 8.dp

    /** 顶栏模组图标边长：素材是 32×32，按 1 纹理像素 = 1 逻辑像素画。 */
    val IconSize: Dp = 64.dp

    /** 页签行相对面板的缺省位置。 */
    val Placement: TabStripPlacement = TabStripPlacement.Top

    /**
     * 模组屏幕的缺省页签条配色：主题默认（[TabStripDefaults.colors]）。
     *
     * 留作**单一入口** —— 要整体换一套配色时改这一处即可，不必逐个调用点传 `colors`；
     * 单个屏幕仍可用 [ModScreen] 的 `colors` 参数覆盖。
     */
    @Composable
    fun colors(): TabStripColors = TabStripDefaults.colors()
}
