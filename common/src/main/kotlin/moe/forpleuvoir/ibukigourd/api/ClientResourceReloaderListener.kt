package moe.forpleuvoir.ibukigourd.api

import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.PreparableReloadListener

interface ClientResourceReloaderListener : PreparableReloadListener {

    val identifier: Identifier

}