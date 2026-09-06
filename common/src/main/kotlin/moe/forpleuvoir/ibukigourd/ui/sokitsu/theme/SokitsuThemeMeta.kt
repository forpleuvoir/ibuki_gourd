package moe.forpleuvoir.ibukigourd.ui.sokitsu.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuAtlasManager
import moe.forpleuvoir.ibukigourd.util.codec.ibukigourdIdentifier
import moe.forpleuvoir.ibukigourd.util.codec.offset
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.ibukigourd.util.toComposeColor
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.codec.*
import net.minecraft.resources.Identifier
import java.util.concurrent.ConcurrentHashMap
import moe.forpleuvoir.nebula.common.color.Color as NebulaColor

/**
 * 单个 [ColorTone]（五档色阶家族）的 meta 定义。
 *
 * [base] 是唯一必填的档位，[outline] 可选——缺失档位由 [ColorTone.fromBase] 按像素风规则推导
 * （shadow 恒为 50% 纯黑、dark/highlight 按 HSV 明度偏移），主题包作者只关心 base 即可上手。
 * 颜色取值支持 hex 字符串（`#8647B3`）与 ARGB int（nebula [Codec.color] 现成解码）。
 */
data class ColorToneMeta(
    val base: NebulaColor?,
    val outline: NebulaColor?,
) {

    /** base 缺失时返回 null（该槽位回落主题内置默认）。 */
    fun toColorTone(): ColorTone? {
        val base = base ?: return null
        return ColorTone.fromBase(base = base.toComposeColor(), outline = outline?.toComposeColor())
    }

    companion object : Codec<ColorToneMeta> {

        val default = ColorToneMeta(
            base = null,
            outline = null,
        )

        private val codec = Codec.create<ColorToneMeta>()
            .field(ColorToneMeta::base).default(default.base).codec(Codec.color.nullable())
            .field(ColorToneMeta::outline).default(default.outline).codec(Codec.color.nullable())
            .build(::ColorToneMeta)

        override fun serialization(target: ColorToneMeta): SerializeElement = codec.serialization(target)

        override fun deserialization(data: SerializeElement): Result<ColorToneMeta> = codec.deserialization(data)
    }
}

/**
 * `assets/<ns>/sokitsu_meta.json` 的文件解析形态——**仅资源重载时由 [SokitsuThemeMetaLoader]
 * 解析并深合并**，合并结果写入全局单例 [SokitsuThemeMeta]。
 *
 * JSON 键均为 snake_case（nebula Codec 字段名自动转换）：
 * ```jsonc
 * {
 *   pixel_scale: 3
 *   light: {
 *     primary: { base: "#8647B3" }          // 只写 base，其余档自动推导
 *     surface: { base: "#DDD3E6", outline: "#6B5E7A" }
 *   }
 *   dark: { ... }
 *   ui_meta: {                              // 组件级 meta，各组件自行定义结构
 *     button: { min_width: 56, min_height: 56, padding_horizontal: 18, padding_vertical: 12 }
 *     switch: { track_width: 90, track_height: 48, thumb_size: 48 }
 *   }
 * }
 * ```
 *
 * 字段单位约定：除颜色外所有数值均为 **dp**（逻辑像素），消费端转 `.dp`；
 * 与屏幕像素的换算由 [pixelScale] 决定。
 */
class SokitsuThemeMetaFile(
    val pixelScale: Int,
    val shadowLight: Offset,
    val uiAtlas: Identifier,
    val light: Map<String, ColorToneMeta?>,
    val dark: Map<String, ColorToneMeta?>,
    val uiMeta: Map<String, SerializeElement>,
) {

    companion object : Codec<SokitsuThemeMetaFile> {

        val default = SokitsuThemeMetaFile(
            pixelScale = 3,
            shadowLight = Offset(-1f, -1f),
            uiAtlas = identifier("ui"),
            light = emptyMap(),
            dark = emptyMap(),
            uiMeta = emptyMap(),
        )

        private val codec = Codec.create<SokitsuThemeMetaFile>()
            .field(SokitsuThemeMetaFile::pixelScale).default(default.pixelScale).codec(Codec.int(1..10))
            .field(SokitsuThemeMetaFile::shadowLight).default(default.shadowLight).codec(Codec.offset)
            .field(SokitsuThemeMetaFile::uiAtlas).default(default.uiAtlas).codec(Codec.ibukigourdIdentifier)
            .field(SokitsuThemeMetaFile::light).default(default.light).codec(Codec.map(ColorToneMeta.nullable()))
            .field(SokitsuThemeMetaFile::dark).default(default.dark).codec(Codec.map(ColorToneMeta.nullable()))
            .field(SokitsuThemeMetaFile::uiMeta).default(default.uiMeta).codec(Codec.map(SerializeElementCodec))
            .build({ pixelScale, shadowLight, uiAtlas, light, dark, uiMeta ->
                SokitsuThemeMetaFile(pixelScale, shadowLight, uiAtlas, light, dark, uiMeta)
            })

        override fun serialization(target: SokitsuThemeMetaFile): SerializeElement = codec.serialization(target)

        override fun deserialization(data: SerializeElement): Result<SokitsuThemeMetaFile> = codec.deserialization(data)
    }
}

private val SerializeElementCodec: Codec<SerializeElement> = object : Codec<SerializeElement> {
    override fun serialization(target: SerializeElement): SerializeElement = target
    override fun deserialization(data: SerializeElement): Result<SerializeElement> = Result.success(data)
}

/**
 * 全局唯一的主题 meta 运行时单例：**运行期只有一个变量**，
 * 仅在资源重载时由 [SokitsuThemeMetaLoader] 重新读取 `sokitsu_meta.json`
 * 并经 [reload] 整体刷新，没有任何其他写路径。
 *
 * 字段均为 snapshot state（[mutableStateOf]）——组合内读取自动响应热重载；
 * `ui_meta` 保持原始 [SerializeElement]，各组件经扩展属性按需解码并缓存具体对象
 * （如 [moe.forpleuvoir.ibukigourd.ui.sokitsu.SokitsuThemeMeta.button]），重载时缓存清空。
 *
 * 职责边界：
 * - 颜色 → [light] / [dark]（槽位定义，缺槽回落 [lightColorScheme] / [darkColorScheme] 内置默认）；
 * - 数值/尺寸 → `ui_meta` 各组件段（**单位均为 dp**，消费端转 `.dp`）；
 * - `pixel_scale` → 全局像素放大倍率（喂 [LocalSokitsuPixelScale]）；
 * - 字体 → 仍归 [Typography] / [LocalTypography]，不进 meta。
 */
object SokitsuThemeMeta {

    private val logger = logger()

    /** 像素放大倍率：1 逻辑像素 → N×N 屏幕像素块（见 [LocalSokitsuPixelScale]）。 */
    var pixelScale: Int by mutableStateOf(SokitsuThemeMetaFile.default.pixelScale)
        internal set

    var shadowLight: Offset by mutableStateOf(SokitsuThemeMetaFile.default.shadowLight)
        internal set

    /** 组件纹理所在的通用 UI 图集 id（默认 `ibukigourd:ui`，见 [moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuAtlasManager]），资源包可覆盖。 */
    var uiAtlas: Identifier by mutableStateOf(SokitsuThemeMetaFile.default.uiAtlas)
        internal set

    /** 亮色方案的槽位定义；null 槽位回落 [lightColorScheme] 内置默认。 */
    var light: Map<String, ColorToneMeta?> by mutableStateOf(SokitsuThemeMetaFile.default.light)
        internal set

    /** 暗色方案的槽位定义；null 槽位回落 [darkColorScheme] 内置默认。 */
    var dark: Map<String, ColorToneMeta?> by mutableStateOf(SokitsuThemeMetaFile.default.dark)
        internal set

    /** 组件级原始 meta 表（键 = 组件名，值 = 该组件自定义结构的 JSON 子树）。 */
    var uiMeta: Map<String, SerializeElement> by mutableStateOf(SokitsuThemeMetaFile.default.uiMeta)
        internal set

    private val decoded = ConcurrentHashMap<String, Any>()

    /**
     * 解码组件 meta 并缓存：缓存键即组件键（一个键对应唯一组件/唯一解码类型）。
     *
     * 返回具体对象而非 [Result]；解码失败回落 [default] 并记警告，不抛异常打断渲染，
     * 调用点无需二次拆包。
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> decodeComponent(key: String, codec: Codec<T>, default: T): T =
        decoded.getOrPut(key) {
            uiMeta[key]?.deserialization(codec)?.onFailure { logger.warn("SokitsuThemeMeta[$key] Failed to decode, falling back to defaults: ${it.message}") }
                ?.getOrDefault(default)
                ?: default
        } as T

    /**
     * 构建运行时 [ColorScheme]：以内置工厂（[lightColorScheme] / [darkColorScheme]）为底，
     * meta 中给出的槽位逐个覆盖（[ColorScheme.copy] 逐槽回落，任何槽位都不会缺）。
     *
     * [ThemeType.Unknown] 按浅色处理（[ThemeType.isLight]）。
     */
    fun colorScheme(theme: ThemeType): ColorScheme {
        val isLight = theme.isLight
        val defaults = if (isLight) lightColorScheme() else darkColorScheme()
        val section = if (isLight) light else dark
        fun tone(name: String): ColorTone? = section[name]?.toColorTone()
        return defaults.copy(
            background = tone("background") ?: defaults.background,
            surface = tone("surface") ?: defaults.surface,
            primary = tone("primary") ?: defaults.primary,
            secondary = tone("secondary") ?: defaults.secondary,
            error = tone("error") ?: defaults.error,
            onBackground = tone("on_background") ?: defaults.onBackground,
            onSurface = tone("on_surface") ?: defaults.onSurface,
            onPrimary = tone("on_primary") ?: defaults.onPrimary,
            primaryContainer = tone("primary_container") ?: defaults.primaryContainer,
            onPrimaryContainer = tone("on_primary_container") ?: defaults.onPrimaryContainer,
            onSecondary = tone("on_secondary") ?: defaults.onSecondary,
            onError = tone("on_error") ?: defaults.onError,
            surfaceVariant = tone("surface_variant") ?: defaults.surfaceVariant,
            onSurfaceVariant = tone("on_surface_variant") ?: defaults.onSurfaceVariant,
            isLight = isLight,
        )
    }

    /** 资源重载入口（[SokitsuThemeMetaLoader] 专调用）：整体刷新并清空解码缓存。 */
    internal fun reload(file: SokitsuThemeMetaFile) {
        pixelScale = file.pixelScale
        uiAtlas = file.uiAtlas
        light = file.light
        dark = file.dark
        uiMeta = file.uiMeta
        decoded.clear()
    }
}

fun SokitsuThemeMeta.uiSprite(textureId: Identifier) = SokitsuAtlasManager.sprite(uiAtlas, textureId)