package id.walt.services

import com.google.crypto.tink.config.TinkConfig
import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.hikari.HikariDataSourceDecoder
import com.sksamuel.hoplite.yaml.YamlParser
import com.zaxxer.hikari.HikariDataSource
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import mu.KotlinLogging
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.config.LoggerConfig
import org.bouncycastle.jce.provider.BouncyCastleProvider
import id.walt.Values
//ANDROID PORT
import id.walt.servicematrix.utils.AndroidUtils.getAndroidDataDir
//ANDROID PORT
import java.io.File
import java.nio.file.Files
//ANDROID PORT
import kotlin.io.path.Path
//ANDROID PORT
import java.security.Security

enum class CryptoProvider { SUN, TINK, CUSTOM }

data class WaltIdConfig(
    val hikariDataSource: HikariDataSource = HikariDataSource()
)

object WaltIdServices {

    //ANDROID PORT
    var androidDataDir = getAndroidDataDir()
    var dataDir = "$androidDataDir/data"
    var keyDir = "$dataDir/key/"
    var ebsiDir = "$dataDir/ebsi/"
    //ANDROID PORT

    val httpLogging = false
    val log = KotlinLogging.logger {}

    val http = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        if (httpLogging) {
            install(Logging) {
                logger = Logger.SIMPLE
                level = LogLevel.HEADERS
            }
        }
    }

    init {
        val javaVersion = System.getProperty("java.runtime.version")
        println("Walt.ID SSI-Kit ${Values.version} (running on Java $javaVersion)")

        //ANDROID PORT
        val version = Integer.parseInt(System.getProperty("java.version"))
        if (version < 11) {
            log.error { "Java version 11+ is required!" }
        }
        //ANDROID PORT

        // BC is required for
        // - secp256k1 curve
        //ANDROID PORT
        Security.removeProvider("BC")
        Security.addProvider(BouncyCastleProvider())
        //ANDROID PORT

        TinkConfig.register()

        createDirStructure()

        println()
    }

    fun loadHikariDataSource(): HikariDataSource {
        val conf = loadConfig()
        return conf.hikariDataSource
    }

    fun createDirStructure() {
        log.debug { "Creating dir-structure at: $dataDir" }
        //ANDROID PORT
        Files.createDirectories(Path(keyDir))
        Files.createDirectories(Path("$dataDir/did/created"))
        Files.createDirectories(Path("$dataDir/did/resolved"))
        Files.createDirectories(Path("$dataDir/vc/templates"))
        Files.createDirectories(Path("$dataDir/vc/created"))
        Files.createDirectories(Path("$dataDir/vc/presented"))
        Files.createDirectories(Path(ebsiDir))
        //ANDROID PORT
    }

    fun loadConfig() = ConfigLoader.Builder()
        .addFileExtensionMapping("yaml", YamlParser())
        .addSource(PropertySource.file(File("walt.yaml"), optional = true))
        .addSource(PropertySource.resource("/walt-default.yaml"))
        .addDecoder(HikariDataSourceDecoder())
        .build()
        .loadConfigOrThrow<WaltIdConfig>()

    fun setLogLevel(level: Level) {
        val ctx: LoggerContext = LogManager.getContext(false) as LoggerContext
        val logConfig: LoggerConfig = ctx.configuration.getLoggerConfig("id.walt")//(LogManager.ROOT_LOGGER_NAME)
        logConfig.level = level
        ctx.updateLoggers()
        log.debug { "Set log-level to $level" }
    }

}
