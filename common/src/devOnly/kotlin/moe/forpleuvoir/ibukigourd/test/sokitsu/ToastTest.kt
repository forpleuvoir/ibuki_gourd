package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.compose_minecraft.platform.screen.ComposeScreen
import moe.forpleuvoir.ibukigourd.test.CenterBox
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Button
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.toast.ToastAnimation
import moe.forpleuvoir.ibukigourd.ui.sokitsu.toast.ToastHandler
import moe.forpleuvoir.ibukigourd.ui.sokitsu.toast.ToastStrategy
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 提示（Toast）测试屏：覆盖策略、时长、外观与"跨屏幕常驻"。
 *
 * 验证点：
 * 1. 默认外观 —— 面板复用气泡提示素材，底部倒计时条随剩余时长收缩；
 * 2. `duration = ZERO` —— 不自动消失，且不绘制倒计时条（只能被 [ToastHandler.dismissAll] 或策略替换清掉）；
 * 3. [ToastStrategy.Refresh] —— 反复点击只有一条，倒计时条每次重播（不堆叠）；
 * 4. [ToastStrategy.Tagged.Refresh] —— 同 tag 原地刷新，不同 tag 各自独立；
 * 5. [ToastStrategy.Enqueue] —— 配合配置 `max_visible > 1` 才能看到排队出队；
 * 6. 自定义内容 —— `show` 不套面板容器，外观完全由内容决定（含图标 + 文字横向排版）；
 * 7. 自定义动画 —— 从左侧滑入、向上滑出，验证 [ToastAnimation] 整条覆盖；
 * 8. 常驻 —— 关闭本屏（回游戏 HUD）后提示仍在，因为它挂在宿主自己的常驻场景上。
 *
 * 提示的配色取自**全局** Sokitsu 主题（资源包 meta），不跟随本测试屏的主题开关 ——
 * 宿主场景与屏幕场景相互独立，两者主题互不影响。
 */
@Composable
fun ToastTestContent() {
    var lastAction by remember { mutableStateOf("（还没操作）") }
    var taggedA by remember { mutableStateOf(0) }
    var taggedB by remember { mutableStateOf(0) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("最近操作：$lastAction")
        Text("提示画在原版 GUI 与 Compose 屏幕之上；关闭本屏后仍可见")

        // 1. 默认外观
        Button({
            ToastHandler.showContent { Text("这是一条默认提示") }
            lastAction = "1. 默认（2s + 倒计时条）"
        }) { Text("1. 默认提示") }

        // 2. 不自动消失
        Button({
            ToastHandler.showContent(duration = Duration.ZERO) { Text("不自动消失（无倒计时条）") }
            lastAction = "2. duration = ZERO"
        }) { Text("2. 不自动消失") }

        // 3. Refresh：反复点只有一条，倒计时重播
        Button({
            ToastHandler.showContent(strategy = ToastStrategy.Refresh) { Text("Refresh：倒计时重播") }
            lastAction = "3. Refresh（重复点击应只有一条）"
        }) { Text("3. 刷新（Refresh）") }

        // 4. Tagged.Refresh：两个 tag 各自独立刷新
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button({
                taggedA++
                ToastHandler.showContent(strategy = ToastStrategy.Tagged.Refresh("demo:a")) {
                    Text("标签 A 第 $taggedA 次")
                }
                lastAction = "4. 标签 A 刷新（第 $taggedA 次）"
            }) { Text("4. 标签 A") }
            Button({
                taggedB++
                ToastHandler.showContent(strategy = ToastStrategy.Tagged.Refresh("demo:b")) {
                    Text("标签 B 第 $taggedB 次")
                }
                lastAction = "4. 标签 B 刷新（第 $taggedB 次）"
            }) { Text("4. 标签 B") }
        }

        // 5. 队列：同屏上限由配置 max_visible 决定，超出的排队
        Button({
            repeat(3) { index ->
                ToastHandler.showContent(
                    duration = 1.5.seconds,
                    strategy = ToastStrategy.Enqueue,
                ) { Text("排队第 ${index + 1} 条") }
            }
            lastAction = "5. 连续入队 3 条（上限见配置 max_visible）"
        }) { Text("5. 入队 3 条（Enqueue）") }

        // 6. 自定义内容：show 不套面板，外观自定
        Button({
            ToastHandler.show(duration = 3.seconds) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Copy)
                    Text("自定义内容：图标 + 文字")
                }
            }
            lastAction = "6. 自定义内容（无面板容器）"
        }) { Text("6. 自定义内容") }

        // 7. 自定义动画：整条覆盖进出场
        Button({
            ToastHandler.show(
                duration = 2.seconds,
                animation = ToastAnimation(
                    enter = fadeIn(tween(700)) + slideInHorizontally(tween(700)) { -it },
                    exit = fadeOut(tween(450)) + slideOutVertically(tween(450)) { -it },
                ),
            ) { Text("从左侧滑入，向上滑出") }
            lastAction = "7. 自定义动画（700ms / 450ms）"
        }) { Text("7. 自定义动画") }

        // 8. 常驻验证：关屏后提示仍应可见
        Button({
            ToastHandler.showContent(duration = 5.seconds) { Text("关屏后我还在") }
            lastAction = "8. 已关屏并弹出提示"
            ComposeScreen.closeCurrent()
        }) { Text("8. 关屏后弹提示（常驻验证）") }

        Button({
            ToastHandler.dismissAll()
            lastAction = "9. 已清空"
        }) { Text("9. 全部关闭") }
    }
}

fun ToastTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        CenterBox {
            ToastTestContent()
        }
    }
}
