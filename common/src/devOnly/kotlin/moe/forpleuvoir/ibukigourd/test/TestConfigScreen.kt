package moe.forpleuvoir.ibukigourd.test

import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import moe.forpleuvoir.ibukigourd.ui.configwrapper.UiWrapper
import moe.forpleuvoir.ibukigourd.ui.configwrapper.uiWrapper

@Composable
fun ConfigTest() {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        UiWrapper(TestConfig)
    }
}