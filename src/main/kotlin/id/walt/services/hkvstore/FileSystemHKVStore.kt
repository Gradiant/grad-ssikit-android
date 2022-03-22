package id.walt.services.hkvstore

import id.walt.servicematrix.ServiceConfiguration
import io.ipfs.multibase.binary.Base32
import io.ktor.util.*
import mu.KotlinLogging
import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.nio.file.Path
//ANDROID PORT
import kotlin.io.path.Path
import id.walt.servicematrix.utils.AndroidUtils.getDataRoot
//ANDROID PORT
import java.util.*
import kotlin.io.path.*


data class FilesystemStoreConfig(
    val dataRoot: String
) : ServiceConfiguration {
    //ANDROID PORT
    val dataDirectory: Path = Path(dataRoot)
    //ANDROID PORT
}

//ANDROID PORT
class FileSystemHKVStore() : HKVStoreService() {

    override val configuration: FilesystemStoreConfig = FilesystemStoreConfig(getDataRoot())
//ANDROID PORT
    private val logger = KotlinLogging.logger("FileSystemHKVStore")

    private val mappingFilePath = lazy {
        configuration.dataDirectory.resolve("hash-mappings.properties").apply {
            if (notExists()) Properties().store(this.bufferedWriter(), hashMappingDesc)
        }
    }
    private val mappingProperties = lazy {
        if (mappingFilePath.value.notExists()) Properties().store(mappingFilePath.value.bufferedWriter(), hashMappingDesc)

        Properties().apply { load(mappingFilePath.value.bufferedReader()) }
    }

    private fun storeMappings() = mappingProperties.value.store(mappingFilePath.value.bufferedWriter(), hashMappingDesc)

    private fun storeHashMapping(keyName: String, hashMapping: String) {
        logger.debug { "Mapping \"$keyName\" to \"$hashMapping\"" }
        mappingProperties.value[hashMapping] = keyName
        storeMappings()
    }

    private fun retrieveHashMapping(hashMapping: String): String = mappingProperties.value[hashMapping] as String

    private fun hashIfNeeded(path: Path): File {
        if (path.name.length > MAX_KEY_SIZE) {
            val hashedFileNameBytes = DigestUtils.sha3_512(path.nameWithoutExtension)
            val hashedFileName = Base32().encodeToString(hashedFileNameBytes).replace("=", "").replace("+", "")

            //val ext = extension

            val newName = hashedFileName //+ (ext.ifBlank { "" })

            val newPath = path.parent.resolve(newName)

            storeHashMapping(path.nameWithoutExtension, newName)

            logger.debug { "File mapping is hashed: Path was \"${path.absolutePathString()}\", new path is $newPath" }
            return dataDirCombinePath(dataDirRelativePath(newPath))
        }
        return path.toFile()
    }

    private fun getFinalPath(keyPath: Path): File = hashIfNeeded(dataDirCombinePath(keyPath).toPath())

    override fun put(key: HKVKey, value: ByteArray) {
        getFinalPath(key.toPath()).apply {
            parentFile.mkdirs()
            writeBytes(value)
        }
    }

    override fun getAsByteArray(key: HKVKey): ByteArray? = getFinalPath(key.toPath()).run {
        return when (exists()) {
            true -> readBytes()
            else -> null
        }
    }

    override fun listChildKeys(parent: HKVKey, recursive: Boolean): Set<HKVKey> =
        getFinalPath(parent.toPath()).listFiles().let { pathFileList ->
            when (recursive) {
                false -> pathFileList?.filter { it.isFile }?.map {
                    var mapping = it.toPath()
                    if (mapping.name.length > MAX_KEY_SIZE) {
                        mapping = mapping.parent.resolve(retrieveHashMapping(mapping.name))
                    }

                    HKVKey.fromPath(dataDirRelativePath(mapping))
                }?.toSet()
                true -> pathFileList?.flatMap {
                    var mapping = it.toPath()
                    if (mapping.name.length > MAX_KEY_SIZE) {
                        mapping = mapping.parent.resolve(retrieveHashMapping(mapping.name))
                    }

                    HKVKey.fromPath(dataDirRelativePath(mapping)).let { currentPath ->
                        when {
                            it.isFile -> setOf(currentPath)
                            else -> listChildKeys(currentPath, true)
                        }
                    }
                }?.toSet()
            } ?: emptySet()
        }

    override fun delete(key: HKVKey, recursive: Boolean): Boolean =
        getFinalPath(key.toPath()).run { if (recursive) deleteRecursively() else delete() }

    private fun dataDirRelativePath(file: File) = configuration.dataDirectory.relativize(file.toPath())
    private fun dataDirRelativePath(path: Path) = configuration.dataDirectory.relativize(path)
    private fun dataDirCombinePath(key: Path) = configuration.dataDirectory.combineSafe(key)

    companion object {
        private const val MAX_KEY_SIZE = 100
        private const val hashMappingDesc = "FileSystemHKVStore hash mappings properties"
    }
}
