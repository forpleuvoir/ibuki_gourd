package moe.forpleuvoir.ibukigourd.mod.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.mod.config.IGConfig
import moe.forpleuvoir.ibukigourd.text.InlineStyleText
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.ComposeScreen
import moe.forpleuvoir.ibukigourd.ui.configwrapper.ConfigManagerWrapper
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.icon.filled.Settings
import moe.forpleuvoir.ibukigourd.ui.platformcontext.IbukiGourdTheme
import moe.forpleuvoir.ibukigourd.ui.preset.BlitTexture
import moe.forpleuvoir.ibukigourd.ui.preset.Text
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.gui.screens.Screen

fun IbukiGourdScreen(
    pauseGame: Boolean = false,
    renderParent: Boolean = false,
    parentScreen: Screen? = mc.screen,
    shouldRenderLevel: Boolean = true,
) = ComposeScreen(pauseGame, renderParent, parentScreen, shouldRenderLevel) {
    IbukiGourdScreenContent()
}

@Composable
internal fun IbukiGourdScreenContent() {
    IbukiGourdTheme {
        ModScreen(
            title = {
                Text(IbukiGourd.MOD_NAME, fontWeight = FontWeight.Bold)
            },
            items = listOf(
                DrawerItem(
                    label = { Text(InlineStyleText(IGConfig.translateText.plainText)) },
                    icon = { Icon(Icons.Filled.Settings, null) },
                ) {
                    ConfigManagerWrapper(IGConfig)
                }
            ),
            header = {
                DrawerHeader(
                    monogram = {
                        BlitTexture(
                            identifier(IbukiGourd.MOD_ID, "icon.png"),
                            Modifier.size(40.dp)
                        )
                    },
                    name = {
                        Text(IbukiGourd.MOD_NAME, fontWeight = FontWeight.Bold)
                    },
                    subtitle = {
                        Text(IbukiGourd.MOD_ID)
                    }
                )
            },
            footer = {
                ThemeSwitcher(
                    isLight = IGConfig.Gui.Theme.lightMode,
                    onToggle = { IGConfig.Gui.Theme.lightMode = it }
                )
            }
        )
    }
}

