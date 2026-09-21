package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import moe.forpleuvoir.compose_minecraft.platform.ui.LocalShadowLight
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.ui.sokitsu.draw.sokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuPixelScale
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ProvideContentColorTextStyle
import moe.forpleuvoir.ibukigourd.ui.util.PressedKeysState
import moe.forpleuvoir.ibukigourd.ui.util.rememberPressedKeys
import moe.forpleuvoir.ibukigourd.util.mc
import kotlin.math.abs
import kotlin.math.roundToInt


/**
 * 页签行相对面板的位置。
 *
 * 决定用哪一套页签素材（[TabStripMeta.topTabSprite] / [TabStripMeta.bottomTabSprite]）：
 * 素材只在**贴面板的那一侧**开口，只有配对正确，选中页签的 2px 外扩带才落在面板上。
 */
enum class TabStripPlacement {
    Top, Bottom
}


/**
 * 最近一次测量出的可见窗口（页签下标区间）。
 *
 * 由测量趟写入**普通字段**（非快照状态）—— 窗口是"这一帧画了什么"的事实，不需要触发重组；
 * 点击处理与 [LaunchedEffect] 读它判断"目标页签当前是否可见"。
 */
internal class TabStripWindow {

    var start = 0
    var end = -1

    /** 上次测量到的页签个数：变化时说明页签集换了，窗口不能沿用旧起点。 */
    var count = -1

    /** 固定窗口是在哪个页签集（页签个数）下记下的；与 [count] 不一致即失效。 */
    var pinCount = -1

    /**
     * 最近一次翻页的方向决定的整条对齐：`true` = 右对齐（贴可用区右边），`false` = 左对齐。
     *
     * 只在"两侧都还有隐藏页签"时起作用 —— 起点为 0 时强制左对齐、末位露出时强制右对齐。
     */
    var alignRight = false

    /**
     * 是否处于"手动翻页"状态（点箭头或点边缘页签翻过页）。
     *
     * 手动翻页期间窗口可以不含选中项（翻页不该被选中拉回去）；一旦外部改选中、或页签集变化，
     * 复位为 false，让窗口重新对齐到选中项。
     */
    var manual = false

    /** 是否已经有过一次有效测量（空页签列表时为 false）。 */
    val measured: Boolean get() = end >= 0

    fun contains(index: Int): Boolean = index in start..end
}

/**
 * 页签条 + 面板。
 *
 * 视觉：页签与面板**连体** —— 选中页签的九宫格贴面板一侧 border 为负，多出的那条外扩带给压在面板上、
 * 盖掉面板顶边与其内高光；未选中页签同一条带是透明的，
 * 于是面板边框在它下方照常可见（两套素材分别为 `top_*` / `bottom_*`，见 [TabStripPlacement]）。
 *
 * 溢出策略（面板宽度有上界、页签数量未知）：
 * - 全部页签的自然宽放得下 → 不出现箭头，直接铺开；
 * - 放不下 → 中间区域**贪心填充到第一个放不下的为止**，后面的页签整体隐藏；
 * - **箭头只在该侧确实还有隐藏页签时出现**：左箭头看窗口起点是否在 0（起点即判据），
 *   右箭头看"不留右槽时窗口能否走到末位"。窗口起点固定，缩窄区域只会让它更短，
 *   因此两个判据与最终摆放自洽、无需迭代，不会出现"箭头出现 → 页签变少 → 箭头消失"的抖动；
 *   箭头隐藏时它占的宽度立刻还给页签；
 * - **箭头点击只平移窗口、不改选中**（与浏览器页签条一致）；
 * - **按住 Ctrl 点箭头 = 直接跳到该侧末端**：左箭头把第一个页签带回视野（窗口起点归 0），
 *   右箭头则从末端往回填出末位窗口（末位页签对齐行尾，不是把它单独挪到最左边）；
 *   同样只动窗口、不改选中，滑动与淡入照旧；
 * - **点页签不动窗口**：点窗口中间的页签只换高亮，窗口与页签位置都不变
 *   （点击时把当前窗口整体固定下来，宽度变化导致它放不下才自动失效）；
 * - **点窗口边缘的页签会连窗口一起挪一格**：点到首个页签且左侧还有隐藏页签 → 左移一格把它露出来；
 *   点到末个页签且右侧还有隐藏页签 → 右翻。动作与点同侧箭头完全一致（含滑动与淡入）；
 *   面板过窄、挪完会让点击的页签自己看不见时不动窗口；
 * - **翻页保证"新页签真的露出来"**：右翻若一格腾出的宽度塞不下下一个页签，会按需多翻几格
 *   （滑动距离取两个窗口的实际摆放差），不会出现"挪了一点、右边什么都没有"；
 * - **窗口对起点单调**：起点 +1 → 窗口只会向右推进（不会回到原位），翻页与新页签露出因此必定成立；
 *   末页右侧留下的空白由"整条右对齐"吸收（见下一条）；
 *   只有**外部**改选中（调用方直接改 `selected`）且目标在窗口之外，才把窗口起点移到该页签；
 * - 窗口平移时整条页签做左右滑动，**进入窗口的那个页签**做透明渐入；动画只在绘制期读取，
 *   不触发重组与重新测量；
 * - 选中页签自身比可用区还宽时按可用宽钳制（标签省略），不会因为一条超长名字把整条 strip 清空。
 *
 * 宽度契约：调用方给出宽度上界（`fillMaxWidth` 的父容器或显式 `width`），面板铺满该上界，
 * 页签行再左右各让出 [TabStripMeta.rowPadding]（页签与箭头都排在这段留白之内，
 * 于是页签不会压到面板边框、端部也不会顶到整块边缘）。宽度无界时退化为"全部铺开"。
 *
 * 绘制分三趟（顺序由 [TabStripLayout] 的**摆放顺序**决定，与组合顺序无关）：
 * 1. 页签与箭头的**阴影层**：贴面板一侧的投影要落在面板所处的平面上；
 * 2. 面板本体（它的阴影由精灵插件先于本体提交）；
 * 3. 页签与箭头的**本体**：最后画，选中页签那 2px 外扩带才能盖在面板之上。
 * 阴影层是整块轮廓（面积与本体相同），既不能随本体同趟画（会盖住本体），
 * 也不能画在本体之后（会把本体压暗），故必须拆成独立的一趟。
 *
 * 尺寸口径（两条来源，单位都是 dp）：
 * - **素材自己表达的**（页签盒高、箭头边长）从精灵自身算：素材画布像素 × [LocalSokitsuPixelScale]；
 * - **素材表达不了的**（间隙、行内边距、页签内边距、选中页签多出的高度）登记在 [TabStripMeta]，直接吃。
 * 调用点拿到的都是 Dp，只有测量趟需要像素时才 `.roundToPx()`。
 *
 * @param selectedTab 当前选中的页签序号（与各 [TabStripTab] 的 `selected` 保持一致）
 * @param modifier 作用于整块（页签行 + 面板）的修饰
 * @param placement 页签行相对面板的位置（上/下各一套素材）
 * @param colors 配色（每个精灵只取一个 tone 层底色，描边与投影由素材图层负责）
 * @param sprites 页签条精灵，默认按 [placement] 从主题 meta 解析
 * @param tabs 页签槽：按顺序写若干 [TabStripTab]，**写下的顺序即下标**、子项个数即页签个数
 * @param panel 面板内容
 */
@Composable
fun TabStrip(
    selectedTab: Int,
    modifier: Modifier = Modifier,
    placement: TabStripPlacement = TabStripPlacement.Top,
    colors: TabStripColors = TabStripDefaults.colors(),
    sprites: TabStripSprites = TabStripDefaults.sprites(placement),
    tabs: @Composable () -> Unit,
    panel: @Composable () -> Unit = {},
) {
    // 手动滚动起点：只有箭头点击会写入。
    // 选中项变化**不**无条件复位 —— 点页签时选中的一定是可见页签，窗口必须原地不动；
    // 只有外部改选中（调用方直接改 selectedTab）才把窗口起点移到目标页签。
    var scrollStart by remember { mutableIntStateOf(selectedTab.coerceAtLeast(0)) }
    // 点页签时固定下来的窗口（首..末）。它优先于 scrollStart，使得点页签后页面原地不动；
    // 面板宽度变化导致它放不下时自动失效，回落到下面的常规计算。
    var pinnedWindow by remember { mutableStateOf<IntRange?>(null) }
    // 点击页签时窗口已经按点击行为处理过的下标：由此产生的选中变化不再走下面的兜底，
    // 否则"点边缘页签翻页"会被立刻回拉（点击的页签翻页后可能本来就不在窗口里）
    var clickedTab by remember { mutableIntStateOf(-1) }
    val window = remember { TabStripWindow() }
    val slide = remember { TabStripSlide() }
    val scope = rememberCoroutineScope()
    // 样式与帧内几何（子项下标计数、点击回调）都挂在 state 上发给 [TabStripTab]；
    // slide / window 是跨帧存在的，按引用带进去，换主题不会把它们重置。
    val state = remember(placement, colors, sprites) {
        TabStripState(placement, colors, sprites, slide, window)
    }

    // 选中项变成"当前窗口之外"的页签时（外部改选中，例如调用方直接改 selectedTab），
    // 以它作为窗口首位把它带回视野。点页签不会走到这里 —— 它的窗口行为已在点击处处理
    // （[clickedTab] 标记），且翻页后点击的页签可能本来就不在窗口里，不能再回拉。
    LaunchedEffect(selectedTab) {
        if (selectedTab == clickedTab) {
            clickedTab = -1
        } else if (window.measured && !window.contains(selectedTab)) {
            pinnedWindow = null
            window.manual = false
            scrollStart = selectedTab
        }
    }

    CompositionLocalProvider(LocalTabStripState provides state) {
        TabStripLayout(
            selectedTab = selectedTab,
            tabs = tabs,
            // 点窗口中间的页签：把当前窗口整体固定下来，页面原地不动
            onKeepWindow = { start, end ->
                pinnedWindow = start..end
                window.pinCount = window.count
                window.manual = false
                scrollStart = start
            },
            // 标记"这次选中变化来自点页签"，由点击处理调用（事件期，非测量期）
            onTabClicked = { index -> clickedTab = index },
            scrollStart = scrollStart,
            onScroll = { newStart, distance, enteringIndex ->
                pinnedWindow = null
                window.pinCount = -1
                window.manual = true
                scrollStart = newStart
                slide.distance = distance
                slide.enteringIndex = enteringIndex
                scope.launch {
                    slide.progress.snapTo(0f)
                    slide.progress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(TabStripDefaults.SlideDurationMillis),
                    )
                }
            },
            state = state,
            pinnedWindow = pinnedWindow,
            panel = panel,
            modifier = modifier.fillMaxWidth(),
        )
    }
}

/**
 * 翻页动画状态：整条页签的位移、以及"进入窗口的那个页签"的下标。
 *
 * 只在 `graphicsLayer` 的 lambda 里读取 —— 读取发生在绘制期，动画逐帧推进不会引起重组或重新测量。
 */
@Stable
internal class TabStripSlide {
    /**
     * 动画进度：0 = 刚触发（起始位移 / 进入页签透明），1 = 结束。
     *
     * 用 [Animatable] 而不是挂起式 `animate(...)`，并在**组合期**读 `.value` —— 与仓库其它动画
     * （`ResetButton` 的旋转、`ToastContainer` 的倒计时）同款；挂起式那套把进度写进只在绘制期读的
     * 状态里，在本渲染栈下推进不到，会表现为"内容被平移一格且新页签始终透明"。
     */
    val progress = Animatable(1f)

    /** 本次平移的起始位移（带符号：正数表示页签自右侧进入）。 */
    var distance: Float = 0f

    /** 进入窗口的页签下标；-1 表示不指定（淡入不生效）。 */
    var enteringIndex: Int = -1
}

/**
 * [TabStrip] 发给 [TabStripTab] 的运行时状态。
 *
 * 样式（placement / colors / sprites）随主题重建；[slide] 与 [window] 跨帧存在、按引用带进来，
 * 换主题不会把它们重置。
 *
 * 子项带一个 [Modifier.layoutId] 稳定标签，[TabStripLayout] 每帧测量时按组合顺序把"标签 → 下标"
 * 登记进 [TabStripState] —— 于是"在 `tabs` 槽里写下的顺序"就是页签下标，与摆放顺序天然一致，
 * 且页签增删之后不会错位或重复。
 * [onTabClick] 依赖本帧测量结果，由测量趟写入、点击时读取（与 [window] 同一种用法）。
 */
@Stable
internal class TabStripState(
    val placement: TabStripPlacement,
    val colors: TabStripColors,
    val sprites: TabStripSprites,
    val slide: TabStripSlide,
    val window: TabStripWindow,
) {

    /**
     * 子项标签（[Modifier.layoutId]）→ 绝对下标，每帧测量重写。
     *
     * 不用"首次组合时自增取号"：那个计数器在页签个数变化后会错位/重复（已存在的子项沿用旧号、
     * 新子项从复位后的 0 起算），点击时便可能把中间页签当成边缘页签、或反过来。
     * 这里改成测量趟按组合顺序登记，点击/淡入时按标签查，增删之后始终对得上。
     */
    private val indicesByTag = HashMap<Any, Int>()

    /** 按组合顺序登记下标（[TabStripLayout] 每帧测量调用）。 */
    fun bindIndices(tags: List<Any?>) {
        indicesByTag.clear()
        tags.forEachIndexed { k, tag ->
            if (tag != null) indicesByTag[tag] = k
        }
    }

    /** 取本项下标；标签未登记（钳制趟、或尚未测量）时回落 [fallback]。 */
    fun indexOf(tag: Any, fallback: Int = 0): Int = indicesByTag[tag] ?: fallback

    /** 点击页签时的窗口行为，由测量趟按本帧几何写好。 */
    var onTabClick: (Int) -> Unit = {}
}

/** [TabStripTab] 读取的运行时状态：只能由 [TabStrip] 提供，单独使用直接报错。 */
private val LocalTabStripState = staticCompositionLocalOf<TabStripState?> { null }

/** 钳制趟的显式下标：该趟只为量出窗口首个页签，不参与取号（也不改任何计数器）。 */
private val LocalTabStripIndexOverride = staticCompositionLocalOf<Int?> { null }

/**
 * 整块（页签行 + 面板）的测量与摆放。
 *
 * 测量在 [SubcomposeLayout] 内一次完成 —— 页签自然宽、溢出判定、窗口计算、箭头槽位回收
 * 都在同一个测量趟里定下来，因此箭头出现与否不会反过来改变页签的可用宽度。
 *
 * **摆放顺序 = 绘制顺序**：页签与箭头的阴影 → 面板 → 页签本体 → 箭头本体。层级只由这里的
 * `placeRelative` 次序决定，与组合先后无关；阴影必须先于面板、本体必须后于面板（见 [TabStrip]）。
 *
 * 页签在行内的纵向位置：高度 = 内容 + 内边距（没有高度尺寸），行高取可见页签里最高的那个；
 * **贴面板那条边一律与面板边齐平** —— 行在面板上方时页签贴行底、在下方时贴行顶。
 * 选中页签多出的高度是它自己的底部内边距，于是那点高度差总是朝远离面板的一侧长出去，
 * 这里不需要任何方向判断。
 * 于是两类页签贴面板的那条边、以及面板顶边，三者落在同一条线上；
 * 差别只在贴面板一侧那 2px 外扩带：选中页签有内容（压过面板顶边），未选中页签是透明的。
 */
@Composable
private fun TabStripLayout(
    selectedTab: Int,
    tabs: @Composable () -> Unit,
    onKeepWindow: (start: Int, end: Int) -> Unit,
    onTabClicked: (Int) -> Unit,
    scrollStart: Int,
    onScroll: (newStart: Int, distance: Float, enteringIndex: Int) -> Unit,
    state: TabStripState,
    pinnedWindow: IntRange?,
    panel: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val placement = state.placement
    val colors = state.colors
    val sprites = state.sprites
    val slide = state.slide
    val window = state.window
    // 翻页动画进度：组合期读取（逐帧重组），与仓库其它动画同款；只影响绘制位移与淡入，不影响测量
    val slideProgress = slide.progress.value

    // 裁剪框 = 整块的矩形 + 阴影外溢量：
    // 面板与页签的 shadow 图层会向光源反方向溢出（面板四周那圈投影就是这么露出来的），
    // 留出这点余量才不会被切掉；同时它把翻页滑动时临时跑到窗口外的页签挡在条内
    // （进入窗口的页签会先滑进来再淡入，不裁剪就会画到邻居上去）。
    val light = LocalShadowLight.current
    val clipBleedPx = (maxOf(abs(light.x), abs(light.y)) * LocalSokitsuPixelScale.current)
        .roundToInt().coerceAtLeast(1)

    // meta 的设计像素尺寸先折算成 dp（@Composable 读法只能在组合期，测量趟里再落到像素）
    val tabGap = TabStripDefaults.tabGap
    val arrowSize = TabStripDefaults.arrowSize(sprites)
    val rowPadding = TabStripDefaults.rowPadding
    // 箭头点击的修饰键（按住 Ctrl 跳末端）：只在点击时读 [PressedKeysState.keys]，
    // 组合期不读它，因此按任意键都不会让整条 strip 重组
    val pressedKeys = rememberPressedKeys()

    SubcomposeLayout(
        modifier.drawWithContent {
            clipRect(
                left = -clipBleedPx.toFloat(),
                top = -clipBleedPx.toFloat(),
                right = size.width + clipBleedPx,
                bottom = size.height + clipBleedPx,
            ) { this@drawWithContent.drawContent() }
        },
    ) { constraints ->
        val gapPx = tabGap.roundToPx()
        val arrowPx = arrowSize.roundToPx()
        val rowPaddingPx = rowPadding.roundToPx()
        val slotPx = arrowPx + gapPx

        // 子项下标按**组合顺序**分配（[TabStripTab] 自己在组合期取号），所以每趟先复位；
        // 点击回调依赖窗口计算结果（本趟测量之后才知道），算完再写回 state。
        val tabMeasurables = subcompose(TabStripSlot.Tabs) {
            tabs()
        }
        val natural = tabMeasurables.map { it.measure(Constraints()) }
        val count = natural.size
        // 选中值按**本帧量到的页签数**钳制：换页签集时子组合可能晚一帧才跟上，
        // 这期间调用方的下标会越界（历史崩溃点 `natural[selected]`）。
        val selected = selectedTab.coerceIn(0, (count - 1).coerceAtLeast(0))
        // 页签集换了（个数变化）→ 复位手动翻页状态，窗口需要重新对齐到选中项
        if (window.count != count) {
            window.count = count
            window.manual = false
        }
        // 登记"子项标签 → 下标"：正式趟按顺序组合全部页签，第 k 个子项就是第 k 个页签。
        // 点击的窗口行为与翻页淡入据此取号，必须每帧重写（页签增删后不能沿用旧号）。
        state.bindIndices(tabMeasurables.map { it.layoutId })

        fun spanWidth(from: Int, to: Int): Int {
            var width = 0
            for (i in from..to) {
                if (i > from) width += gapPx
                width += natural[i].width
            }
            return width
        }

        // 整块宽度：面板铺满它；页签行再左右各缩进 [TabStripMeta.rowPadding]，
        // 页签与箭头都在缩进后的区域内，不会压到面板边框上
        val stripWidth = if (constraints.hasBoundedWidth) {
            constraints.maxWidth
        } else if (count > 0) {
            spanWidth(0, count - 1) + rowPaddingPx * 2
        } else {
            rowPaddingPx * 2
        }
        val available = (stripWidth - rowPaddingPx * 2).coerceAtLeast(0)

        val fillStart = scrollStart.coerceIn(0, (count - 1).coerceAtLeast(0))

        /** 从 [from] 起向后贪心填充；一个都放不下时返回 `from to from`（由调用方钳制到可用宽）。 */
        fun fillFrom(from: Int, region: Int): Pair<Int, Int> {
            var end = -1
            var acc = 0
            for (i in from until count) {
                val need = natural[i].width + if (i == from) 0 else gapPx
                if (acc + need <= region) {
                    acc += need
                    end = i
                } else {
                    break
                }
            }
            return if (end < from) from to from else from to end
        }

        val overflow = count > 0 && spanWidth(0, count - 1) > available

        /**
         * 给定窗口起点，算出该起点实际会采用的 `(中间区域宽, 窗口首, 窗口末)`。
         *
         * 摆放与"翻页后窗口会变成什么样"共用这一份实现，因此点击边缘页签时的可见性判断
         * 与真正摆放必然一致。窗口起点**原样等于 [from]**（窗口对起点单调），判据与结果自洽、无需迭代：
         * - 左箭头：起点不在 0 → 左边确实还有隐藏页签（起点本身就是判据）；
         * - 右箭头：不留右槽时窗口仍走不到末位 → 右边确实还有隐藏页签（起点固定，收窄区域只会更短）。
         */
        fun resolveWindow(from: Int): Triple<Int, Int, Int> {
            if (count == 0) return Triple(available, 0, -1)
            if (!overflow) return Triple(available, 0, count - 1)
            val fill = from.coerceIn(0, count - 1)
            val regionWithoutRight = (available - (if (fill > 0) slotPx else 0)).coerceAtLeast(0)
            val right = fillFrom(fill, regionWithoutRight).second < count - 1
            val slotRegion = (regionWithoutRight - (if (right) slotPx else 0)).coerceAtLeast(0)
            val filled = fillFrom(fill, slotRegion)
            // 起点必须原样保留：窗口对起点单调（起点 +1 → 窗口只会向右推进），
            // 翻页与滑动位移都建立在这个前提上。末页右侧留白由整条右对齐（rowShiftPx）解决，
            // 不能在这里把起点往左拉 —— 那会让 `起点+1` 算出同一个窗口，翻页直接失效。
            return Triple(slotRegion, filled.first, filled.second)
        }

        /**
         * 显示用窗口：**非手动翻页期间保证选中项在窗口内**（外部改选中、页签集变化后靠它回到视野）。
         *
         * 与 [resolveWindow] 的唯一区别是"选中项不在窗口里时改成以它为中心回填"，用同一套槽位/区域算术；
         * 手动翻页期间直接走 [resolveWindow]（翻页结果不被选中拉回去）。
         */
        fun displayWindow(from: Int): Triple<Int, Int, Int> {
            val resolved = resolveWindow(from)
            if (window.manual || count == 0) return resolved
            if (selected in resolved.second..resolved.third) return resolved
            val regionWithoutRight = (available - (if (selected > 0) slotPx else 0)).coerceAtLeast(0)
            val right = fillFrom(selected, regionWithoutRight).second < count - 1
            val slotRegion = (regionWithoutRight - (if (right) slotPx else 0)).coerceAtLeast(0)
            var start = selected
            var end = selected
            var acc = natural[selected].width
            var i = selected - 1
            while (i >= 0 && acc + gapPx + natural[i].width <= slotRegion) {
                acc += gapPx + natural[i].width
                start = i
                i--
            }
            var j = selected + 1
            while (j < count && acc + gapPx + natural[j].width <= slotRegion) {
                acc += gapPx + natural[j].width
                end = j
                j++
            }
            return Triple(slotRegion, start, end)
        }

        // 点页签固定下来的窗口：在"按它自己的箭头配置算出的区域"里仍放得下就原样沿用
        // 固定窗口只在**它产生时的页签集**内有效：换了页签集（增删）后必须失效，
        // 否则显示会被钉在旧的窄窗口上 —— 新页签要等一次翻页（点击清掉固定窗口）才出现。
        val pinnedRange = pinnedWindow?.takeIf {
            window.pinCount == count && it.first in 0 until count && it.last in it.first until count
        }
        var pinnedRegion = -1
        if (pinnedRange != null) {
            val arrowRegion = (available -
                    (if (pinnedRange.first > 0) slotPx else 0) -
                    (if (pinnedRange.last < count - 1) slotPx else 0)).coerceAtLeast(0)
            // 单个页签允许被钳制到可用宽；多页签时首个可钳制、其余必须完整放下（与常规摆放一致）
            val fits = pinnedRange.first == pinnedRange.last ||
                    natural[pinnedRange.first].width.coerceAtMost(arrowRegion) + gapPx +
                    spanWidth(pinnedRange.first + 1, pinnedRange.last) <= arrowRegion
            if (fits) pinnedRegion = arrowRegion
        }

        val region: Int
        val start: Int
        val end: Int
        if (pinnedRange != null && pinnedRegion >= 0) {
            region = pinnedRegion
            start = pinnedRange.first
            end = pinnedRange.last
        } else {
            val resolved = displayWindow(fillStart)
            region = resolved.first
            start = resolved.second
            end = resolved.third
        }
        val leftArrow = start > 0
        val rightArrow = end < count - 1
        val originX = rowPaddingPx + if (leftArrow) slotPx.coerceAtMost(available) else 0

        // 本帧相对上一帧的翻页方向：起点变大 = 往右翻，变小 = 往左翻（用于决定整条对齐）
        when {
            start > window.start -> window.alignRight = true
            start < window.start -> window.alignRight = false
        }

        // 整条对齐：两侧都有隐藏时看最近一次翻页方向；只有一侧有隐藏时由该侧决定
        // （起点 0 → 左对齐；末位露出 → 右对齐，否则那一侧会空出一截）
        val alignRight = when {
            start <= 0 -> false
            end >= count - 1 -> true
            else -> window.alignRight
        }

        // 记录本帧窗口，供点击处理与选中变化判定"目标是否可见"
        window.start = start
        window.end = end

        /**
         * 左翻一格的起点：窗口起点即填充起点，因此退一格必然把左边隐藏的那个页签露出来。
         */
        fun leftTarget(): Int = (start - 1).coerceAtLeast(0)

        /**
         * 右翻一格的起点：**必须保证右侧隐藏的那个页签真的进得来**。
         *
         * 只退一格是不够的 —— 若离场页签比入场页签窄，腾出的宽度可能塞不下新页签，结果是
         * "内容挪了一点、右边什么也没露出来"。这里按需多退几格，直到窗口末端越过 `end`。
         */
        fun rightTarget(): Int {
            if (end >= count - 1) return start
            val nextHidden = end + 1
            var target = start + 1
            while (target < nextHidden && resolveWindow(target).third < nextHidden) target++
            return target
        }

        /**
         * 末位窗口的起点：从末尾**往前退**，直到"再退一格就容不下末位页签"为止。
         *
         * 于是窗口从末端对齐（可用区填满、末位页签可见），而不是把末位页签单独挪到最左边。
         * 判据与 [resolveWindow] 同源，因此退出来的窗口与真正摆放一致。
         */
        fun lastTarget(): Int {
            if (count == 0) return 0
            var target = count - 1
            while (target > 0 && resolveWindow(target - 1).third >= count - 1) target--
            return target
        }

        /** 窗口内页签实际占用的总宽（含间隙，窗口首个页签可能被钳制）。 */
        fun placedSpan(region: Int, windowStart: Int, windowEnd: Int): Int {
            var used = 0
            for (i in windowStart..windowEnd) {
                if (i > windowStart) used += gapPx
                used += if (i == windowStart) natural[i].width.coerceAtMost(region) else natural[i].width
            }
            return used
        }

        /**
         * 页签在窗口内的摆放 x（与摆放循环同一算法：含窗口首个页签被钳制、以及整条右对齐的位移）。
         */
        fun placeX(region: Int, windowStart: Int, windowEnd: Int, index: Int, alignRight: Boolean): Int {
            val origin = if (windowStart > 0) slotPx.coerceAtMost(available) else 0
            var x = origin
            for (i in windowStart until index) {
                val width = if (i == windowStart) natural[i].width.coerceAtMost(region) else natural[i].width
                x += width + gapPx
            }
            // 与摆放同一条规则：右对齐时位移里必须一并算上那段空白
            val shift = if (alignRight) {
                (region - placedSpan(region, windowStart, windowEnd)).coerceAtLeast(0)
            } else {
                0
            }
            return x + shift
        }

        /**
         * 整条页签的起始位移（旧位置 − 新位置），用于翻页滑动动画。
         *
         * 取两个窗口**共同页签**的实际摆放差，于是窗口向左扩展、两侧箭头槽位进出、
         * 首个页签被钳制等情形都自动算进位移，动画与最终摆放严格一致。
         */
        fun slideOffset(target: Int): Float {
            val newWindow = resolveWindow(target)
            val newStart = newWindow.second
            val newEnd = newWindow.third
            // 目标窗口的对齐：两侧都有隐藏时看"往目标方向翻"的结果，否则由该侧强制
            val newAlignRight = when {
                newStart <= 0 -> false
                newEnd >= count - 1 -> true
                else -> newStart > start
            }
            val ref = maxOf(start, newStart)
            if (ref <= minOf(end, newEnd)) {
                val oldX = placeX(region, start, end, ref, alignRight)
                val newX = placeX(newWindow.first, newStart, newEnd, ref, newAlignRight)
                return (oldX - newX).toFloat()
            }
            // 两个窗口没有交集（一次翻过好几格）：按两侧槽位差与累计宽度给出起始位移
            val slotOrigin = slotPx.coerceAtMost(available)
            val oldOrigin = if (start > 0) slotOrigin else 0
            val newOrigin = if (newStart > 0) slotOrigin else 0
            var accumulated = 0
            if (newStart > start) {
                for (i in start until newStart) accumulated += natural[i].width + gapPx
            } else {
                for (i in newStart until start) accumulated -= natural[i].width + gapPx
            }
            return (oldOrigin - newOrigin + accumulated).toFloat()
        }

        /** 左翻一格（把左边隐藏的那个露出来），带滑动与淡入。 */
        fun scrollLeft() {
            val target = leftTarget()
            if (target == start) return
            onScroll(target, slideOffset(target), resolveWindow(target).second)
        }

        /** 右翻一格（把右边隐藏的那个露出来），带滑动与淡入。 */
        fun scrollRight() {
            val target = rightTarget()
            if (target == start) return
            onScroll(target, slideOffset(target), resolveWindow(target).third)
        }

        /** 直接跳到首位窗口（第一个页签对齐到行首），带滑动与淡入。 */
        fun jumpToFirst() {
            if (start == 0) return
            onScroll(0, slideOffset(0), resolveWindow(0).second)
        }

        /** 直接跳到末位窗口（末位页签对齐到行尾），带滑动与淡入。 */
        fun jumpToLast() {
            val target = lastTarget()
            if (target == start) return
            onScroll(target, slideOffset(target), resolveWindow(target).third)
        }

        // 点页签的窗口行为：写回 state，由 [TabStripTab] 在自己的点击里先调它、再调调用方的回调
        state.onTabClick = { index ->
            // 先标记本次选中变化来自点页签，避免翻页结果被"外部改选中"的兜底逻辑回拉
            onTabClicked(index)
            // 点到窗口边缘的页签 → 与同侧箭头一致地翻页（该侧有隐藏页签就一定会翻）
            val atStart = index == start && start > 0
            val atEnd = index == end && end < count - 1
            val afterLeft = if (atStart) resolveWindow(leftTarget()) else null
            val afterRight = if (atEnd) resolveWindow(rightTarget()) else null
            // 优先选"翻完点击的页签仍完整可见"的那一侧；两边都留不住它时（面板过窄）照样翻
            val keepsLeft = afterLeft != null && index in afterLeft.second..afterLeft.third
            val keepsRight = afterRight != null && index in afterRight.second..afterRight.third
            when {
                atStart && keepsLeft -> scrollLeft()
                atEnd && keepsRight  -> scrollRight()
                atStart              -> scrollLeft()
                atEnd                -> scrollRight()
                // 其余情况：窗口原地不动
                else                 -> onKeepWindow(start, end)
            }
        }

        // 窗口首个页签比可用区还宽时按可用宽钳制（内容自己负责省略），避免整条 strip 空掉。
        // ⚠️ 同一个 Measurable 不能量两次（Compose 会直接抛 IllegalStateException），
        // 所以只能把整个 tabs 槽再组合一趟；该趟由 CompositionLocal 直接给下标。
        // 取不到子项（组合还没跟上）时返回 null，回落到未钳制的页签。
        val clampedFirst = if (count > 0 && natural[start].width > region) {
            subcompose(TabStripSlot.ClampedFirst) {
                // 钳制趟只为量出窗口首个页签：直接给它下标，不占格子、也不改任何计数器
                CompositionLocalProvider(LocalTabStripIndexOverride provides start) { tabs() }
            }.firstOrNull()?.measure(Constraints.fixedWidth(region))
        } else {
            null
        }

        // 本帧可见页签：先量出位置与各自高度，行高取其中最高的那个（高度由内容 + 内边距撑出来）
        val placed = mutableListOf<Pair<Placeable, Int>>()
        var x = originX
        for (i in start..end) {
            val placeable = if (i == start) clampedFirst ?: natural[i] else natural[i]
            placed += placeable to x
            x += placeable.width + gapPx
        }
        val rowHeightPx = placed.maxOfOrNull { it.first.height } ?: 0
        val rowShiftPx = if (alignRight) {
            (region - placed.sumOf { it.first.width } - gapPx * (placed.size - 1).coerceAtLeast(0))
                .coerceAtLeast(0)
        } else {
            0
        }

        // 贴面板那条边与面板边齐平：行在面板上方 → 页签贴行底；行在下方 → 贴行顶
        val visible = placed.mapIndexed { offset, (placeable, tabX) ->
            val index = start + offset
            TabStripTabPlacement(
                index = index,
                selected = index == selected,
                placeable = placeable,
                x = tabX + rowShiftPx,
                y = if (placement == TabStripPlacement.Top) rowHeightPx - placeable.height else 0,
            )
        }

        // 左右各一个箭头槽位：贴该侧摆好、行内纵向居中
        val arrowSlots = buildList {
            if (leftArrow) add(true to sprites.arrowLeft)
            if (rightArrow) add(false to sprites.arrowRight)
        }
        val arrows = subcompose(TabStripSlot.Arrows) {
            arrowSlots.forEach { (left, sprite) ->
                TabStripArrow(sprite = sprite, colors = colors) {
                    // 按住 Ctrl：直接跳到该侧末端（左 → 首位对齐，右 → 末位对齐）
                    val jump = pressedKeys.ctrlPressed
                    when {
                        left && jump -> jumpToFirst()
                        left         -> scrollLeft()
                        jump         -> jumpToLast()
                        else         -> scrollRight()
                    }
                }
            }
        }.mapIndexedNotNull { i, measurable ->
            // 子组合可能还没跟上本帧的槽位列表（差一帧）：只按交集配对，多出来的直接丢弃
            val (left, sprite) = arrowSlots.getOrNull(i) ?: return@mapIndexedNotNull null
            ArrowPlacement(
                sprite = sprite,
                placeable = measurable.measure(Constraints.fixed(arrowPx, arrowPx)),
                x = rowPaddingPx + if (left) 0 else (available - arrowPx).coerceAtLeast(0),
                y = (rowHeightPx - arrowPx) / 2,
            )
        }

        // 面板：宽取可用宽（页签行与它同宽），高由内容撑开；
        // 高度上限扣掉页签行占走的份额 —— 与"页签行在 Column 里排在面板之前"的约束一致，
        // 面板里用 fillMaxHeight / 滚动列表的调用方才能拿到有界的可用高。
        val panelMaxHeight = if (constraints.hasBoundedHeight) {
            (constraints.maxHeight - rowHeightPx).coerceAtLeast(0)
        } else {
            Constraints.Infinity
        }
        val panelPlaceable = subcompose(TabStripSlot.Panel) {
            Box(Modifier.fillMaxWidth().sokitsuSprite(sprites.panel, colors.panel, colors.outline)) { panel() }
        }.first().measure(
            Constraints(minWidth = stripWidth, maxWidth = stripWidth, maxHeight = panelMaxHeight),
        )
        // 页签行所占区域的顶边：面板在上方（Bottom）时它从面板底边开始
        val rowTop = if (placement == TabStripPlacement.Top) 0 else panelPlaceable.height

        // 阴影趟：页签与箭头各一条「只含 shadow 图层」的绘制命令。
        // 页签阴影带同一套翻页位移与淡入（否则本体滑动、影子留在原地）。
        val shadowItems = visible.map { tab ->
            ShadowItem(
                index = tab.index,
                sprite = sprites.tabFor(tab.selected),
                tone = colors.toneFor(tab.selected),
                x = tab.x,
                y = tab.y,
                width = tab.placeable.width,
                height = tab.placeable.height,
            )
        } + arrows.map { arrow ->
            ShadowItem(
                index = -1,
                sprite = arrow.sprite,
                tone = colors.arrow,
                x = arrow.x,
                y = arrow.y,
                width = arrowPx,
                height = arrowPx,
            )
        }
        val shadows = subcompose(TabStripSlot.Shadows) {
            shadowItems.forEach { item ->
                Box(
                    Modifier
                        .graphicsLayer {
                            if (item.index < 0) return@graphicsLayer
                            translationX = slide.distance * (1f - slideProgress)
                            alpha = if (item.index == slide.enteringIndex) slideProgress else 1f
                        }
                        .sokitsuSprite(TabStripDefaults.shadowOnly(item.sprite), item.tone, colors.outline),
                )
            }
        }.mapIndexedNotNull { i, measurable ->
            // 同上：子组合与本帧条目列表按交集配对，避免"孩子比本地列表多"时越界
            val item = shadowItems.getOrNull(i) ?: return@mapIndexedNotNull null
            measurable.measure(Constraints.fixed(item.width, item.height)) to item
        }

        // 绘制顺序 = 摆放顺序：① 阴影 → ② 面板 → ③ 页签本体 → ④ 箭头本体
        layout(stripWidth, rowHeightPx + panelPlaceable.height) {
            shadows.forEach { (placeable, item) ->
                placeable.placeRelative(item.x, rowTop + item.y)
            }
            panelPlaceable.placeRelative(0, if (placement == TabStripPlacement.Top) rowHeightPx else 0)
            visible.forEach { tab -> tab.placeable.placeRelative(tab.x, rowTop + tab.y) }
            arrows.forEach { arrow -> arrow.placeable.placeRelative(arrow.x, rowTop + arrow.y) }
        }
    }
}

/** 本帧一个可见页签的摆放几何（[TabStripTabPlacement.selected] 决定用哪套素材与色调）。 */
private class TabStripTabPlacement(
    val index: Int,
    val selected: Boolean,
    val placeable: Placeable,
    val x: Int,
    val y: Int,
)

/** 本帧一个溢出箭头的摆放几何（阴影趟与本体趟共用；尺寸统一取 [TabStripSprites.arrowBoxSize]）。 */
private class ArrowPlacement(
    val sprite: SokitsuSprite,
    val placeable: Placeable,
    val x: Int,
    val y: Int,
)

/**
 * 阴影趟的一项：精灵 + 色调 + 目标矩形。
 *
 * 阴影图层的颜色槽位恒为 `shadow`（固定黑），色调只在"取哪个色"上被忽略；
 * 带上它是为了让阴影与本体共用同一套解析路径。
 *
 * @param index 对应页签序号，用于翻页动画（位移 + 进入窗口那一项的透明渐入）；-1 = 不参与动画
 */
private class ShadowItem(
    val index: Int,
    val sprite: SokitsuSprite,
    val tone: Color,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

/** [TabStripLayout] 的子组合槽位标识（槽位与绘制顺序无关，绘制顺序由摆放次序决定）。 */
private enum class TabStripSlot { Shadows, Panel, Tabs, ClampedFirst, Arrows }

/**
 * 左右 Ctrl 任一边按住。
 *
 * 按键状态取自 [PressedKeysState]（由键盘 / 鼠标事件维护，见 `ui/util/InputState.kt`），
 * 与曲线编辑器的 Alt / Shift / Ctrl 判定同一套 —— 不查 `InputHandler`。
 */
private val PressedKeysState.ctrlPressed: Boolean
    get() = Keyboard.LEFT_CONTROL in keys || Keyboard.RIGHT_CONTROL in keys

/**
 * 单个页签：[TabStrip] 的 `tabs` 槽里按顺序写它，**写下的顺序即页签下标**。
 *
 * 只负责"这一个页签"的样子与交互；窗口怎么分配、溢出时露出哪几个，由 [TabStrip] 的测量趟决定。
 *
 * 阴影层由 [TabStripLayout] 的阴影趟单独绘制（先于面板，见 [TabStrip]），
 * 这里只提交非阴影图层（[TabStripDefaults.bodyOnly]）。
 *
 * 高度 = 内容 + 内边距，**没有任何高度尺寸**；选中页签多出的
 * [TabStripMeta.selectedExtraHeight] 加在**贴面板那一侧**的内边距上（行在面板上方 → 底边，
 * 在下方 → 顶边）—— 那条边被对齐钉住，这点内边距就把内容朝远离面板一侧顶开、页签也朝那侧长出去。
 * 另一处差别在贴面板一侧那条负 border 外扩带上：选中页签有内容（压过面板边框）、
 * 未选中是透明的，因此面板边框会在未选中页签下方露出来。
 *
 * 悬停时把 tone 向 [TabStripColors.tabHighlight] 提亮、按下时向描边色压暗来表达交互状态
 * ——素材只有选中 / 未选中两态。
 *
 * 翻页动画由 [TabStrip] 驱动：整条页签一起位移，只有"进入窗口的那个页签"做透明渐入；
 * 两者都在 `graphicsLayer` 里读取，动画逐帧推进不引起重组。
 *
 * [content] 会在页签内**居中**、并按 [TabStripMeta.tabPaddingHorizontal]/[TabStripMeta.tabPaddingVertical]
 * 内缩，同时替换成对应的内容色（选中 / 未选中不同）——里面直接写 `Text` 即可；
 * 页签被钳到很窄时内容自己负责省略（比如 `maxLines = 1, overflow = Ellipsis`）。
 *
 * @param selected 是否选中（选中态用另一张精灵与另一档色调）
 * @param onClick 点击回调；[TabStrip] 会在这之前先处理"点窗口边缘页签 → 窗口挪一格"
 * @param modifier 作用于页签盒子的修饰
 * @param content 页签内容
 */
@Composable
fun TabStripTab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val state = LocalTabStripState.current
        ?: error("TabStripTab 只能作为 TabStrip 的 tabs 槽的子项使用")
    // 下标 = 在 tabs 槽里写下的顺序。这里只准备一个稳定标签，真正的下标由测量趟按组合顺序登记
    // （见 [TabStripState.bindIndices]），点击/淡入时现查 —— 页签增删后不会用到旧号。
    // 钳制趟（只为量窗口首个页签）由 CompositionLocal 直接给下标，不查表。
    val indexOverride = LocalTabStripIndexOverride.current
    val indexTag = remember { Any() }
    val index: () -> Int = { indexOverride ?: state.indexOf(indexTag) }
    val slide = state.slide
    val colors = state.colors
    val sprites = state.sprites
    val placement = state.placement
    // 翻页动画进度：组合期读取（逐帧重组），与仓库其它动画同款
    val progress = slide.progress.value

    val base = colors.toneFor(selected)
    val contentColor = if (selected) colors.contentSelected else colors.content

    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val pressed by interactionSource.collectIsPressedAsState()
    val tone = when {
        pressed -> lerp(base, colors.outline, TabStripDefaults.PressedBlend)
        hovered -> lerp(base, colors.tabHighlight, TabStripDefaults.HoverBlend)
        else    -> base
    }

    val hoverIcon = FlatButtonDefaults.LocalHoverIcon.current
    val pressSound = FlatButtonDefaults.LocalPressSound.current

    // 页签的高矮完全由内容 + 内边距决定（没有别的高度尺寸）；
    // 选中页签多出的那点量加在**贴面板那一侧**的内边距上 —— 那条边被对齐钉住，
    // 这点内边距于是把内容朝远离面板的一侧顶开、页签也朝那一侧长出去。
    // 行在面板上方时贴面板的是底边、在下方时是顶边，两侧不一样，不能照抄。
    val padHorizontal = TabStripDefaults.tabPaddingHorizontal
    val padVertical = TabStripDefaults.tabPaddingVertical
    val extra = if (selected) TabStripDefaults.selectedExtraHeight else 0.dp
    val contentPadding = PaddingValues(
        start = padHorizontal,
        end = padHorizontal,
        top = padVertical + if (placement == TabStripPlacement.Bottom) extra else 0.dp,
        bottom = padVertical + if (placement == TabStripPlacement.Top) extra else 0.dp,
    )

    Box(
        Modifier
            .layoutId(indexTag)
            .then(modifier)
            .pointerHoverIcon(hoverIcon)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = {
                    pressSound?.let { mc.soundManager.play(it) }
                    state.onTabClick(index())
                    onClick()
                },
            )
            .graphicsLayer {
                // 绘制期读取：动画不触发重组与重新测量
                translationX = slide.distance * (1f - progress)
                alpha = if (index() == slide.enteringIndex) progress else 1f
            }
            .sokitsuSprite(
                sprite = TabStripDefaults.bodyOnly(sprites.tabFor(selected)),
                color = tone,
                outlineColor = colors.outline,
            ),
        contentAlignment = Alignment.Center,
    ) {
        ProvideContentColorTextStyle(contentColor = contentColor, textStyle = LocalTextStyle.current) {
            Box(Modifier.padding(contentPadding)) { content() }
        }
    }
}

/**
 * 溢出箭头本体。
 *
 * 尺寸由调用方按素材原尺寸给出（[TabStripLayout] 用固定约束测量），故节点自身不带尺寸修饰；
 * 朝向由精灵本身表达（左右各一张图）。阴影层同 [TabStripTab]，由阴影趟单独绘制。
 *
 * 该侧没有隐藏页签时整块不组合，因此不存在禁用态占位。
 */
@Composable
private fun TabStripArrow(
    sprite: SokitsuSprite,
    colors: TabStripColors,
    onClick: () -> Unit,
) {
    val hoverIcon = FlatButtonDefaults.LocalHoverIcon.current
    val pressSound = FlatButtonDefaults.LocalPressSound.current

    // 与页签同一套反馈：悬停提亮、按下压暗（素材只有一张图，只能靠染色表达）
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val pressed by interactionSource.collectIsPressedAsState()
    val tone = when {
        pressed -> lerp(colors.arrow, colors.outline, TabStripDefaults.PressedBlend)
        hovered -> lerp(colors.arrow, Color.White, TabStripDefaults.HighlightBlend)
        else    -> colors.arrow
    }

    Box(
        Modifier
            .pointerHoverIcon(hoverIcon)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = {
                    pressSound?.let { mc.soundManager.play(it) }
                    onClick()
                },
            )
            .sokitsuSprite(TabStripDefaults.bodyOnly(sprite), tone, colors.outline),
    )
}
