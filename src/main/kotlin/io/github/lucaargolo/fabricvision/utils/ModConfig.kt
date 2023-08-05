package io.github.lucaargolo.fabricvision.utils

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import io.github.lucaargolo.fabricvision.FabricVision
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.io.PrintWriter
import java.nio.file.Files

class ModConfig {

    val projectorFalloutDistance = 32f

    val projectorFramebufferWidth = 1280
    val projectorFramebufferHeight = 720

    val maxPanelDepth = 200

    companion object {

        private val config: ModConfig by lazy {
            val gson = GsonBuilder().setPrettyPrinting().create()
            val configFile = File("${FabricLoader.getInstance().configDir}${File.separator}${FabricVision.MOD_ID}.json")
            var finalConfig: ModConfig
            ModLogger.info("Trying to read config file...")
            try {
                if (configFile.createNewFile()) {
                    ModLogger.info("No config file found, creating a new one...")
                    val json: String = gson.toJson(JsonParser.parseString(gson.toJson(ModConfig())))
                    PrintWriter(configFile).use { out -> out.println(json) }
                    finalConfig = ModConfig()
                    ModLogger.info("Successfully created default config file.")
                } else {
                    ModLogger.info("A config file was found, loading it..")
                    finalConfig = gson.fromJson(String(Files.readAllBytes(configFile.toPath())), ModConfig::class.java)
                    ModLogger.info("Successfully loaded config file.")
                }
            } catch (exception: Exception) {
                ModLogger.error("There was an error creating/loading the config file!", exception)
                finalConfig = ModConfig()
                ModLogger.warn("Defaulting to original config.")
            }
            finalConfig
        }

        fun getInstance() = config

    }

}