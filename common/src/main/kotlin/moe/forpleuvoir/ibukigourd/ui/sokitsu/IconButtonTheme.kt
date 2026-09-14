package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.util.codec.dpSize
import moe.forpleuvoir.ibukigourd.util.codec.padding
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.codec.Codec

/**
 * 图标按钮的 meta：**单位均为 dp**（逻辑像素，消费端转 `.dp`）。
 *
 * ```jsonc
 * ui_meta: {
 *   icon_button: { min_width: 48, min_height: 48, padding: 6 }
 * }
 * ```
 *
 * [padding] 是**四边相等**的内边距（`PaddingValues(all = x)`，编解码时单个数值即表示四边全等），
 * 图标天然居中，四边留白理应一致；需要非等边时仍可传对象形式（`{start, top, end, bottom}`）。
 *
 * [sprite] 缺省与 [FlatButtonMeta.sprite] **指向同一批素材**（`ui/flat_button/`）：单个按钮图标
 * 不需要单独一张底图。日后若要给图标按钮单独出图，往 `ui/icon_button/` 放文件并把这里改掉即可。
 *
 * 尺寸关系：实际按钮尺寸 = `max(minSize, 图标尺寸 + padding × 2)`。图标默认尺寸 = 素材尺寸 ×
 * `pixelScale`（16px 素材 → 48 逻辑像素），故缺省下实际为 `48 + 6×2 = 60` 见方（由内容撑开）；
 * 若把图标倍率调小（如 `Icon(scale = 1)` → 16），按钮会回落到 [minSize] 48×48。
 */
data class IconButtonMeta(
    /** 按钮最小尺寸（实际尺寸由内容撑开时以内容为准）。 */
    val minSize: DpSize,
    /** 内容内边距（四边相等；缺省 6dp）。 */
    val padding: PaddingValues,
    /** 按钮的四态纹理。 */
    val sprite: UiStateIdentifier,
) {

    companion object : Codec<IconButtonMeta> {

        val default = IconButtonMeta(
            minSize = DpSize(48.dp, 48.dp),
            padding = PaddingValues(6.dp),
            sprite = UiStateIdentifier(
                normal = identifier("ui/flat_button/normal"),
                pressed = identifier("ui/flat_button/pressed"),
                focused = identifier("ui/flat_button/focused"),
                disabled = identifier("ui/flat_button/disabled"),
            )
        )

        private val codec = Codec.create<IconButtonMeta>()
            .field(IconButtonMeta::minSize).default(default.minSize).codec(Codec.dpSize(1.dp..512.dp, 1.dp..512.dp))
            .field(IconButtonMeta::padding).default(default.padding).codec(Codec.padding(0.dp..512.dp))
            .field(IconButtonMeta::sprite).default(default.sprite).codec(UiStateIdentifier)
            .build(::IconButtonMeta)

        override fun serialization(target: IconButtonMeta): SerializeElement = codec.serialization(target)

        override fun deserialization(data: SerializeElement): Result<IconButtonMeta> = codec.deserialization(data)
    }
}

/**
 * 主题 meta 的图标按钮段：缺失/解码失败回落 [IconButtonMeta] 内置默认。
 */
val SokitsuThemeMeta.iconButton: IconButtonMeta
    get() = decodeComponent("icon_button", IconButtonMeta, IconButtonMeta.default)
