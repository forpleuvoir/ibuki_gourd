package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.util.codec.dp
import moe.forpleuvoir.ibukigourd.util.codec.dpSize
import moe.forpleuvoir.ibukigourd.util.codec.padding
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.codec.Codec

data class RadioButtonMeta(
    /** 按钮最小尺寸。 */
    val minSize: DpSize,
    /** 内容内边距 */
    val padding: PaddingValues,
    /** 组内相邻按钮的间距（dp），0 = 无缝拼接。 */
    val spacing: Dp,
    /**
     * 左边的按钮的纹理
     */
    val leftSprite: UiStateIdentifier,
    /**
     * 中间的按钮的纹理
     */
    val centerSprite: UiStateIdentifier,
    /**
     * 右边的按钮的纹理
     */
    val rightSprite: UiStateIdentifier,
    /**
     * 如果只有一个按钮时使用的纹理
     */
    val singleSprite: UiStateIdentifier,
) {

    companion object : Codec<RadioButtonMeta> {

        val default = RadioButtonMeta(
            minSize = DpSize(56.dp, 56.dp),
            padding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            spacing = 3.dp,
            leftSprite = UiStateIdentifier(
                normal = identifier("ui/radio_button/left/normal"),
                pressed = identifier("ui/radio_button/left/pressed"),
                focused = identifier("ui/radio_button/left/focused"),
                disabled = identifier("ui/radio_button/left/disabled")
            ),
            centerSprite = UiStateIdentifier(
                normal = identifier("ui/radio_button/center/normal"),
                pressed = identifier("ui/radio_button/center/pressed"),
                focused = identifier("ui/radio_button/center/focused"),
                disabled = identifier("ui/radio_button/center/disabled")
            ),
            rightSprite = UiStateIdentifier(
                normal = identifier("ui/radio_button/right/normal"),
                pressed = identifier("ui/radio_button/right/pressed"),
                focused = identifier("ui/radio_button/right/focused"),
                disabled = identifier("ui/radio_button/right/disabled")
            ),
            singleSprite = UiStateIdentifier(
                normal = identifier("ui/button/normal"),
                pressed = identifier("ui/button/pressed"),
                focused = identifier("ui/button/focused"),
                disabled = identifier("ui/button/disabled")
            )
        )

        private val codec = Codec.create<RadioButtonMeta>()
            .field(RadioButtonMeta::minSize).default(default.minSize).codec(Codec.dpSize(1.dp..512.dp, 1.dp..512.dp))
            .field(RadioButtonMeta::padding).default(default.padding).codec(Codec.padding(0.dp..128.dp))
            .field(RadioButtonMeta::spacing).default(default.spacing).codec(Codec.dp(0.dp..128.dp))
            .field(RadioButtonMeta::leftSprite).default(default.leftSprite).codec(UiStateIdentifier)
            .field(RadioButtonMeta::centerSprite).default(default.centerSprite).codec(UiStateIdentifier)
            .field(RadioButtonMeta::rightSprite).default(default.rightSprite).codec(UiStateIdentifier)
            .field(RadioButtonMeta::singleSprite).default(default.singleSprite).codec(UiStateIdentifier)
            .build(::RadioButtonMeta)

        override fun serialization(target: RadioButtonMeta): SerializeElement = codec.serialization(target)

        override fun deserialization(data: SerializeElement): Result<RadioButtonMeta> = codec.deserialization(data)
    }
}

/**
 * 主题 meta 的按钮段：缺失/解码失败回落 [RadioButtonMeta] 内置默认。
 */
val SokitsuThemeMeta.radioButton: RadioButtonMeta
    get() = decodeComponent("radio_button", RadioButtonMeta, RadioButtonMeta.default)
