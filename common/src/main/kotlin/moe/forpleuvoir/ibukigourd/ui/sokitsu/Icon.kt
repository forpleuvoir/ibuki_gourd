package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.draw.sokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuAtlasManager
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalContentColor
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuPixelScale
import moe.forpleuvoir.ibukigourd.util.identifier
import net.minecraft.resources.Identifier

/**
 * 精灵的**逻辑尺寸**（dp，即布局尺寸）= 源像素尺寸 ÷ 素材密度；
 * 空容器（缺失/未加载）为 `0×0`。
 *
 * 位图元素按逻辑尺寸布局，再由 [moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuPixelScale]
 * 换算到屏幕像素（1 逻辑像素 = N×N 屏幕像素）。
 */
val SokitsuSprite.logicalSize: DpSize get() = DpSize(logicalWidth.dp, logicalHeight.dp)

/**
 * Sokitsu 图标集：`texture/sokitsu/icon/<id>.aseprite` 对应的图集精灵（图集 id 见 [ATLAS_ID]）。
 *
 * 每个属性按需向图集查询（`SokitsuAtlasManager.sprite`），**未加载/未命中返回空容器**
 * （[SokitsuSprite.isEmpty]，`logicalSize` 为 0）→ [Icon] 整块不渲染，不会抛异常；
 * 资源重载后自动取到新图集，无需重启。
 *
 * 图标素材约定：单层、`level=tone` + `tint=Mask`（纯色剪影）—— 这样图标颜色完全由调用点的
 * `tint` 决定，能跟随内容色与状态修正（见 [FlatButton]）。
 *
 * 图标**没有统一的默认尺寸**：布局尺寸 = 素材逻辑尺寸 × [Icon] 的 `scale`（默认取当前像素放大倍率），
 * 所以要画多大就画多大；需要精确尺寸时传 `size`，需要放大/缩小时改 `scale`（整数）。
 */
object Icons {

    /**
     * 图标图集 id：定义文件 `sokitsu_atlas/icon.json`，扫描目录 `texture/sokitsu/icon/`
     * （图集按 `texture/sokitsu/<atlasId>/` 取源文件，纹理 id 为去掉该前缀后的路径）。
     */
    val ATLAS_ID: Identifier = identifier("icon")

    private fun of(id: String): SokitsuSprite = SokitsuAtlasManager.sprite(
        ATLAS_ID,
        identifier("icon/$id"),
    )

    val Add get() = of("add")
    val ArrowDown get() = of("arrow_down")
    val ArrowLeft get() = of("arrow_left")
    val ArrowRight get() = of("arrow_right")
    val ArrowUp get() = of("arrow_up")
    val Back get() = of("back")
    val Close get() = of("close")
    val Compress get() = of("compress")
    val Copy get() = of("copy")
    val Cut get() = of("cut")
    val DarkMode get() = of("dark_mode")
    val Delete get() = of("delete")
    val Down get() = of("down")
    val DragHandle get() = of("drag_handle")
    val Edit get() = of("edit")
    val Expand get() = of("expand")
    val Export get() = of("export")
    val Filter get() = of("filter")
    val Forward get() = of("forward")
    val Help get() = of("help")
    val Import get() = of("import")
    val Keyboard get() = of("keyboard")
    val LightMode get() = of("light_mode")
    val Lock get() = of("lock")
    val Menu get() = of("menu")
    val MenuBook get() = of("menu_book")
    val Palette get() = of("palette")
    val Paste get() = of("paste")
    val Reset get() = of("reset")
    val Save get() = of("save")
    val Search get() = of("search")
    val SelectAll get() = of("select_all")
    val Setting get() = of("setting")
    val SyncAlt get() = of("sync_alt")
    val Unlock get() = of("unlock")
    val Up get() = of("up")

    /** 全部图标 id，顺序即 [all] / [byId] 的遍历顺序。 */
    private val IDS = listOf(
        "add",
        "arrow_down", "arrow_left", "arrow_right", "arrow_up",
        "back",
        "close", "compress", "copy", "cut",
        "dark_mode", "delete", "down", "drag_handle",
        "edit", "expand", "export",
        "filter", "forward",
        "help",
        "import",
        "keyboard",
        "light_mode", "lock",
        "menu", "menu_book",
        "palette", "paste",
        "reset",
        "save", "search", "select_all", "setting", "sync_alt",
        "unlock", "up",
    )

    /**
     * 全部图标（顺序同 [IDS]），供图标总览屏、素材完整性校验遍历。
     *
     * 新增图标时记得同步：一个 `val` 属性 + [IDS] 里的一项。
     */
    val all: List<SokitsuSprite> get() = IDS.map(::of)

    /** `id → 精灵`（顺序同 [IDS]），需要显示图标名时用（如总览屏）。 */
    val byId: Map<String, SokitsuSprite> get() = IDS.associateWith(::of)
}

/**
 * 图标：把 [Icons] 里的精灵按 **素材尺寸 × 倍率** 画出来。
 *
 * - 尺寸 = [SokitsuSprite.logicalSize] × [scale]，[scale] 缺省取 [LocalSokitsuPixelScale]
 *   （当前像素放大倍率，缺省 3）—— 即"一个素材像素对应 N 个逻辑像素"，
 *   图标大小随像素缩放设置联动；需要固定尺寸时传 [size] 直接接管；
 * - 倍率必须是**整数**（配合整数像素缩放，非整数倍会让纹素大小不均、糊边）；
 * - [tint] 缺省取 [LocalContentColor]（即"当前内容色"）—— 放进 [FlatButton] 等按钮里会自动
 *   拿到按钮内容色，连 `contentBlend` / `disabledBlend` 的状态修正一起吃到；
 * - 精灵为空（缺素材）时布局尺寸为 0，等于什么都没画。
 *
 * @param scale 尺寸倍率（素材逻辑尺寸 → 布局尺寸），默认跟随 [LocalSokitsuPixelScale]
 * @param size 显式尺寸；非 null 时无视 [scale]
 */
@Composable
fun Icon(
    icon: SokitsuSprite,
    modifier: Modifier = Modifier,
    scale: Int = 2,
    size: DpSize? = null,
    tint: Color = Color.Unspecified,
) {
    val resolvedSize = size ?: DpSize(icon.logicalWidth.dp * scale, icon.logicalHeight.dp * scale)
    Box(
        modifier = modifier
            .size(resolvedSize)
            .sokitsuSprite(icon, tint.takeOrElse { LocalContentColor.current })
    )
}
