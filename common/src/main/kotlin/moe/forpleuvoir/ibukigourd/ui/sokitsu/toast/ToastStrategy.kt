package moe.forpleuvoir.ibukigourd.ui.sokitsu.toast

/**
 * 新提示与已有提示的冲突处置策略。
 *
 * 无 tag 三种：
 * - [ReplaceAll]：清空队列并把所有活动提示置为过期，然后展示新提示（默认，语义 = "后来的完全接管"）；
 * - [Refresh]：若已有活动提示则**重置其时长为新时长**并累加刷新计数（倒计时条重播），不新建条目；
 * - [Enqueue]：排队等待，活动数量不足 [ToastHandler.maxVisible] 时才出队展示。
 *
 * [Tagged] 三种：先按 [tag] 在活动列表 / 队列中定位同标识提示，命中后按子类语义处理，
 * 未命中则递归回落 [fallback] 策略（默认 [ReplaceAll]）：
 * - [Tagged.Refresh]：命中的活动提示重置时长并累加刷新计数（队列中的同标识条目被整体替换）；
 * - [Tagged.Replace]：命中的活动提示置为过期后新建条目，队列中的同标识条目被丢弃；
 * - [Tagged.Drop]：只要活动列表或队列中已有同标识提示就**整条丢弃**，不做任何展示。
 *
 * 典型用途：反复触发的同一类提示（如按住键连续切换状态）用 `Tagged.Refresh` 原地刷新，
 * 避免堆叠成一串。
 */
sealed class ToastStrategy {

    /** 清空队列、过期全部活动提示，随后展示新提示。 */
    data object ReplaceAll : ToastStrategy()

    /** 原地刷新首个活动提示的时长（无活动提示时新建）。 */
    data object Refresh : ToastStrategy()

    /** 入队，等有位置时再展示。 */
    data object Enqueue : ToastStrategy()

    /** 带标识的策略族；未命中标识时回落 [fallback]。 */
    sealed class Tagged(
        val tag: String,
        val fallback: ToastStrategy = ReplaceAll
    ) : ToastStrategy() {

        /** 同标识提示存在则刷新，否则回落。 */
        class Refresh(tag: String, fallback: ToastStrategy = ReplaceAll) : Tagged(tag, fallback)

        /** 同标识提示存在则替换，否则回落。 */
        class Replace(tag: String, fallback: ToastStrategy = ReplaceAll) : Tagged(tag, fallback)

        /** 同标识提示存在则什么都不做。 */
        class Drop(tag: String, fallback: ToastStrategy = ReplaceAll) : Tagged(tag, fallback)

    }

}
