package io.github.lucaargolo.fabricvision.utils

import io.github.lucaargolo.fabricvision.FabricVision
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object ModLogger {

    private val logger: Logger = LogManager.getLogger(FabricVision.MOD_NAME)

    fun debug(message: String) {
        logger.debug("[${FabricVision.MOD_NAME}] $message")
    }

    fun info(message: String) {
        logger.info("[${FabricVision.MOD_NAME}] $message")
    }

    fun warn(message: String) {
        logger.warn("[${FabricVision.MOD_NAME}] $message")
    }

    fun warn(message: String, exception: Exception) {
        logger.warn("[${FabricVision.MOD_NAME}] $message", exception)
    }

    fun error(message: String) {
        logger.error("[${FabricVision.MOD_NAME}] $message")
    }

    fun error(message: String, exception: Exception) {
        logger.error("[${FabricVision.MOD_NAME}] $message", exception)
    }

}