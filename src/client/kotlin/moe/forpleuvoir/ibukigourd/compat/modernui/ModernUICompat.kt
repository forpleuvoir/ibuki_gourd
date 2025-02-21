package moe.forpleuvoir.ibukigourd.compat.modernui

import icyllis.modernui.mc.ModernUIMod
import moe.forpleuvoir.ibukigourd.event.events.client.ClientLifecycleEvent
import moe.forpleuvoir.ibukigourd.util.loader
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.nebula.event.EventPriority
import moe.forpleuvoir.nebula.event.EventSubscriber
import moe.forpleuvoir.nebula.event.Subscriber

@EventSubscriber
object ModernUICompat {

    private val log = loader.logger()

    var isTextEngineEnabled = false
        private set

    @Subscriber(priority = EventPriority.MONITOR)
    fun init(event: ClientLifecycleEvent.ClientStartedEvent) {
        runCatching {
            isTextEngineEnabled = ModernUIMod.isTextEngineEnabled()
            log.info("modernui is loaded")
        }
    }

    inline fun textEngineEnabled(consumer: () -> Unit) {
        if (isTextEngineEnabled) consumer()
    }

    inline fun <T> textEngineEnabled(consumer: () -> T, orElse: () -> T): T {
        return if (isTextEngineEnabled) consumer() else orElse()
    }

    fun <T> textEngineEnabled(consumer: T, orElse: T): T {
        return if (isTextEngineEnabled) consumer else orElse
    }

}