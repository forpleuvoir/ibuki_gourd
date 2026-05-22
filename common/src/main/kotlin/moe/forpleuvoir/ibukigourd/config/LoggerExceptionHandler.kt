package moe.forpleuvoir.ibukigourd.config

import moe.forpleuvoir.nebula.config.ConfigNode
import moe.forpleuvoir.nebula.config.ExceptionHandler
import moe.forpleuvoir.nebula.serialization.DeserializationException
import moe.forpleuvoir.nebula.serialization.SerializationException
import org.slf4j.Logger

class LoggerExceptionHandler(val logger: Logger) : ExceptionHandler {
    override fun onSerializationException(config: ConfigNode, e: SerializationException) {
        logger.warn("Failed to encode config :${config.name}", e)
    }

    override fun onDeserializationException(config: ConfigNode, e: DeserializationException) {
        logger.warn("Failed to decode config :${config.name}", e)
    }
}