package moe.forpleuvoir.ibukigourd.ui.toast

sealed class ToastStrategy {

    data object ReplaceAll : ToastStrategy()

    data object Refresh : ToastStrategy()

    data object Enqueue : ToastStrategy()

    sealed class Tagged(
        val tag: String,
        val fallback: ToastStrategy = ReplaceAll
    ) : ToastStrategy() {

        class Refresh(tag: String, fallback: ToastStrategy = ReplaceAll) : Tagged(tag, fallback)

        class Replace(tag: String, fallback: ToastStrategy = ReplaceAll) : Tagged(tag, fallback)

        class Drop(tag: String, fallback: ToastStrategy = ReplaceAll) : Tagged(tag, fallback)

    }

}
