package moe.forpleuvoir.ibukigourd.test

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import moe.forpleuvoir.ibukigourd.ui.configwrapper.ConfigManagerWrapper
import moe.forpleuvoir.ibukigourd.ui.configwrapper.ConfigUiWrapper

@Composable
fun ConfigTest() {
    ConfigManagerWrapper(TestConfig )
//    Box {
//        val scrollState = rememberScrollState()
//        Column(Modifier.verticalScroll(scrollState)) {
//            ConfigUiWrapper(TestConfig)
//        }
//        VerticalScrollbar(
//            modifier = Modifier.align(Alignment.CenterEnd),
//            adapter = rememberScrollbarAdapter(scrollState),
//        )
//    }
}