package moe.forpleuvoir.ibukigourd.test

import com.mojang.serialization.JsonOps
import moe.forpleuvoir.ibukigourd.IbukiGourd.log
import moe.forpleuvoir.ibukigourd.util.mc
import net.minecraft.item.ItemStack

fun TestScreen2() {
    runCatching {
        mc.player?.handItems?.firstOrNull()?.let { item ->
            val ser = ItemStack.UNCOUNTED_CODEC.encodeStart(JsonOps.INSTANCE, item).resultOrPartial {
                log.info(it)
            }
            ser.get().let {
                log.info(it.toString())
            }
        }
    }.onFailure {
        log.error(it)
    }
}