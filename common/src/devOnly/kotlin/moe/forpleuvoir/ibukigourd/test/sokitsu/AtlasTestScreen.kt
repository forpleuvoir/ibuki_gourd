package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.compose_minecraft.platform.ui.draw.minecraftTexture
import moe.forpleuvoir.ibukigourd.test.CenterBox
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuAtlasManager
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuPixelScale


/**
 * Atlas 调试屏：把整张 UI 图集按原始像素尺寸 1:1 渲染出来。
 *
 * 用于直接检查图集内容——各槽位合并结果、区域位置、padding 隔离带是否正确。
 * 图集纹理已由 [SokitsuAtlasManager] 注册进 TextureManager，直接用
 * [minecraftTexture] 按 [SokitsuAtlasTexture.width] × [SokitsuAtlasTexture.height] 渲染。
 */
fun AtlasTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        CenterBox {
            val atlasId = SokitsuAtlasManager.UI_ATLAS_ID
            val atlas = SokitsuAtlasManager.atlasTexture(atlasId)
            if (atlas == null || atlas.width <= 0) {
                Box(Modifier.size(200.dp, 40.dp))
            } else {
                val pixelScale = LocalSokitsuPixelScale.current
                Box(
                    Modifier
                        .size(atlas.width.dp * pixelScale, atlas.height.dp * pixelScale)
                        .minecraftTexture(atlasId)
                )
            }
        }
    }
}
