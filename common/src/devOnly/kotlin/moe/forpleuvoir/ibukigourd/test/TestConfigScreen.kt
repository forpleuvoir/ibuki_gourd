package moe.forpleuvoir.ibukigourd.test

import androidx.compose.runtime.Composable
import moe.forpleuvoir.ibukigourd.ui.configwrapper.ConfigManagerWrapper

@Composable
fun ConfigTest() {
    ConfigManagerWrapper(TestConfig)
}