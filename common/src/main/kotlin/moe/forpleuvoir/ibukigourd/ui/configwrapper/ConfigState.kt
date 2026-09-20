package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import moe.forpleuvoir.nebula.common.api.Observable
import moe.forpleuvoir.nebula.common.api.Resettable
import moe.forpleuvoir.nebula.config.Config

/**
 * 把 [Config] 的当前值订阅成 Compose [State]，节点销毁时自动注销。
 *
 * nebula 的 `Config` 实现 `Observable`，值变更经 `observe` 推送；组件不轮询。
 */
@Composable
fun <T, C : Config<T>> C.asState(): State<T> = produceState(initialValue = getValue(), key1 = this) {
    val disposable = observe { value = it.getValue() }
    awaitDispose { disposable.dispose() }
}

/**
 * 由 [Config] 派生的 Compose [State]。
 *
 * [forceRefresh] 为 `true` 时每次变更都自增一个版本号强制重组（派生结果相等但仍需重算的场景）；
 * 否则只更新派生值，值相等时 Compose 会自行跳过重组。
 */
@Composable
fun <T, C : Config<T>, R> C.asDerivedState(forceRefresh: Boolean = false, derive: (C) -> R): State<R> {
    var version by remember(this) { mutableIntStateOf(0) }
    return produceState(initialValue = derive(this@asDerivedState), key1 = this, key2 = version) {
        val disposable = this@asDerivedState.observe {
            value = derive(this@asDerivedState)
            if (forceRefresh) version++
        }
        awaitDispose { disposable.dispose() }
    }
}

/**
 * [Resettable] 当前是否处于默认值（即"能否重置"），供重置按钮的可用态使用。
 *
 * 实现 `Observable` 的节点走订阅；否则按 [ConfigRowDefaults.ValuePollInterval] 轮询。
 */
@Composable
fun Resettable.asDefaultState(): State<Boolean> {
    if (this is Observable<*>) {
        return produceState(initialValue = isDefault(), key1 = this) {
            val disposable = this@asDefaultState.observe { value = this@asDefaultState.isDefault() }
            awaitDispose { disposable.dispose() }
        }
    }

    val interval = ConfigRowDefaults.ValuePollInterval
    return produceState(initialValue = isDefault(), key1 = this) {
        while (isActive) {
            value = this@asDefaultState.isDefault()
            delay(interval)
        }
    }
}
