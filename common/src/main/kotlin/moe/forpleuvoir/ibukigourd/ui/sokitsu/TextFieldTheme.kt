package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeToken
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.util.codec.ibukigourdIdentifier
import moe.forpleuvoir.ibukigourd.util.codec.dpSize
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.codec.Codec
import net.minecraft.resources.Identifier

/**
 * 输入框的主题接入声明：token 映射（"什么颜色"），与 [ButtonTokens] / [SliderTokens] 同构。
 */
object TextFieldTokens {

    /** 容器（凹槽背景）精灵的染色色板，与滑条轨道/开关关闭态轨道同源。 */
    val Container = ColorSchemeToken.SurfaceVariant

    /** 容器上文本的颜色（取色板的 [ColorTone.base]）。 */
    val Content = ColorSchemeToken.OnSurfaceVariant

    /** 禁用态容器基准色（会被 [DisabledContainerOpacity] 压透明）。 */
    val DisabledContainer = ColorSchemeToken.SurfaceVariant

    /** 禁用态文本基准色（会被 [DisabledContentOpacity] 压透明）。 */
    val DisabledContent = ColorSchemeToken.OnSurfaceVariant

    /** 光标颜色：主色。 */
    val Cursor = ColorSchemeToken.Primary

    /** 错误态描边：错误色。 */
    val ErrorOutline = ColorSchemeToken.Error

    /** 禁用态容器不透明度。 */
    const val DisabledContainerOpacity = 0.38f

    /** 禁用态文本不透明度，沿用 Material3 `DisabledLabelTextOpacity`。 */
    const val DisabledContentOpacity = 0.38f
}

/**
 * 输入框的 meta：**单位均为 dp**（逻辑像素，消费端转 `.dp`）。
 *
 * ```jsonc
 * ui_meta: {
 *   text_field: {
 *     min_width: 120, min_height: 56,
 *     background_sprite: "ui/text_field/background"
 *   }
 * }
 * ```
 *
 * [minSize] 的高度须与纹理比例匹配：素材为 16×16 源像素，高度取 `16 × pixelScale`
 * （默认 pixelScale = 3 → 48）才能一个源像素对应一个 `pixelScale` 方块。
 * 内容内边距不走 meta：对齐 M3 OutlinedTextField 的 `contentPadding` 参数，见
 * [TextFieldDefaults.contentPadding]。
 */
data class TextFieldMeta(
    /** 输入框最小尺寸。 */
    val minSize: DpSize,
    /** 容器（凹槽背景）纹理。 */
    val backgroundSprite: Identifier,
) {

    companion object : Codec<TextFieldMeta> {

        val default = TextFieldMeta(
            // 高度与 contentPadding(垂直 10×2) 同步增加:内容区恒为 36dp
            minSize = DpSize(120.dp, 56.dp),
            backgroundSprite = identifier("ui/text_field/background"),
        )

        private val codec = Codec.create<TextFieldMeta>()
            .field(TextFieldMeta::minSize).default(default.minSize)
            .codec(Codec.dpSize(16.dp..1024.dp, 16.dp..512.dp))
            .field(TextFieldMeta::backgroundSprite).default(default.backgroundSprite)
            .codec(Codec.ibukigourdIdentifier)
            .build(::TextFieldMeta)

        override fun serialization(target: TextFieldMeta): SerializeElement = codec.serialization(target)

        override fun deserialization(data: SerializeElement): Result<TextFieldMeta> = codec.deserialization(data)
    }
}

/**
 * 主题 meta 的输入框段：缺失/解码失败回落 [TextFieldMeta] 内置默认。
 */
val SokitsuThemeMeta.textField: TextFieldMeta
    get() = decodeComponent("text_field", TextFieldMeta, TextFieldMeta.default)
