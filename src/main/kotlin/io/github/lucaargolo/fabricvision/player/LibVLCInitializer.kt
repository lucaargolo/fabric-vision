package io.github.lucaargolo.fabricvision.player

import com.sun.jna.NativeLibrary
import com.sun.jna.Platform
import io.github.lucaargolo.fabricvision.utils.ModLogger
import net.fabricmc.loader.api.FabricLoader
import uk.co.caprica.vlcj.binding.support.runtime.RuntimeUtil
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery
import uk.co.caprica.vlcj.factory.discovery.strategy.BaseNativeDiscoveryStrategy
import uk.co.caprica.vlcj.factory.discovery.strategy.NativeDiscoveryStrategy
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.zip.ZipInputStream
import kotlin.io.path.pathString

object LibVLCInitializer {

    var isLoaded = false
        private set

    var isLinux = false
        private set

    fun initialize() {
        try{
            MinecraftMediaPlayerHolder.FACTORY = MediaPlayerFactory(LibVLCDiscovery(), "--quiet")
            isLoaded = true
        }catch (exception: Exception) {
            ModLogger.error("Exception while trying to load LibVLC", exception)
        }
    }

    class LibVLCDiscovery: NativeDiscovery() {

        private val strategiesField = NativeDiscovery::class.java.getDeclaredField("discoveryStrategies")
        private val findMethod = BaseNativeDiscoveryStrategy::class.java.getDeclaredMethod("find", String::class.java)
        init {
            strategiesField.isAccessible = true
            findMethod.isAccessible = true
        }

        override fun onFound(path: String, strategy: NativeDiscoveryStrategy) {
            super.onFound(path, strategy)
            ModLogger.info("VLC found in your system at $path.")
        }

        override fun onNotFound() {
            ModLogger.info("VLC is not installed in your system. Attempting to load local LibVLC.")

            val path = FabricLoader.getInstance().gameDir.pathString + File.separator + ".libvlc"
            val strategies = strategiesField.get(this) as Array<*>

            var success = false
            strategies.forEach { strategy ->
                if(strategy is BaseNativeDiscoveryStrategy) {
                    if(findMethod.invoke(strategy, path) as Boolean) {
                       success = true
                       return@forEach
                    }
                }
            }

            if(!success) {
                ModLogger.warn("Couldn't find local LibVLC. Attempting to download it.")

                val vlcVersion = "3.0.18"
                val (vlcPlatform, vlcArchitecture) = if(Platform.isWindows() && Platform.isIntel()) {
                    if (Platform.is64Bit()) {
                        "win64" to "win64"
                    }else {
                        "win32" to "win32"
                    }
                }else if(Platform.isMac() && Platform.is64Bit()){
                    if(Platform.isIntel()) {
                        "macosx" to "intel64"
                    }
                    else if(Platform.isARM()) {
                        "macosx" to "arm64"
                    }
                    else{
                        null to null
                    }
                }else {
                    null to null
                }


                if(vlcPlatform != null && vlcArchitecture != null) {
                    val vlcExtension = if(vlcPlatform.startsWith("mac")) "dmg" else "zip"
                    val downloadUrl = "https://download.videolan.org/pub/vlc/$vlcVersion/$vlcPlatform/vlc-$vlcVersion-$vlcArchitecture.$vlcExtension"

                    ModLogger.warn("Detected platform $vlcPlatform. Attempting to download from $downloadUrl.")

                    val downloadedFileName = downloadUrl.split("/").last()
                    val extractedFolderName = if(vlcPlatform.startsWith("mac")) {
                        "VLC media player" + File.separator + "VLC.app" + File.separator + "Contents" + File.separator + "MacOS"
                    }else{
                        downloadedFileName.split("-").filterNot { it.endsWith(".$vlcExtension") }.joinToString("-") { s -> s }
                    }

                    val folder = File(path)
                    folder.mkdirs()

                    val downloadedFile = File(folder, downloadedFileName)
                    ModLogger.warn("Starting download to ${downloadedFile.name}")
                    URL(downloadUrl).openStream().use { input ->
                        FileOutputStream(downloadedFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    ModLogger.info("Download finished.")

                    val extractedFolder = File(folder, extractedFolderName)
                    ModLogger.warn("Extracting file to $extractedFolderName")
                    if(vlcExtension == "zip") {
                        ZipInputStream(downloadedFile.inputStream()).use { zipInputStream ->
                            while (true) {
                                val entry = zipInputStream.nextEntry ?: break
                                val entryFile = File(folder, entry.name)
                                if (entry.isDirectory) {
                                    entryFile.mkdirs()
                                } else {
                                    entryFile.parentFile.mkdirs()
                                    entryFile.outputStream().use { output ->
                                        zipInputStream.copyTo(output)
                                    }
                                }
                                zipInputStream.closeEntry()
                            }
                        }
                    }
                    ModLogger.info("Extraction completed.")

                    ModLogger.warn("Copying extracted files to $folder.")
                    extractedFolder.walkTopDown().forEach { file ->
                        if(file.isFile && file.nameWithoutExtension == "libvlc" || file.nameWithoutExtension == "libvlccore") {
                            val destinationPath = File(folder, file.name)
                            file.copyTo(destinationPath, true)
                        }else if(file.isDirectory && file.name == "plugins"){
                            val destinationPath = File(folder, "plugins")
                            file.copyRecursively(destinationPath, true)
                        }else if(file.isDirectory && file.name == "lib") {
                            file.copyRecursively(folder, true)
                        }
                    }
                    ModLogger.info("Copying completed.")

                    ModLogger.warn("Cleaning up temporary files.")
                    downloadedFile.delete()
                    extractedFolder.deleteRecursively()
                    ModLogger.info("Cleanup completed.")
                }else{
                    if(Platform.isLinux()) {
                        ModLogger.error("Linux detected. Please manually install VLC.")
                    }else{
                        ModLogger.error("Unknown platform. Please manually install VLC.")
                    }
                }
            }else{
                ModLogger.info("Successfully found local LibVLC. Loading it.")
            }

            NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), path)
            NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcCoreLibraryName(), path)
            NativeLibrary.getInstance(RuntimeUtil.getLibVlcCoreLibraryName())
        }

    }


}