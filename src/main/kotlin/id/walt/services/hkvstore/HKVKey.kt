package id.walt.services.hkvstore

import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Path
//ANDROID PORT
import kotlin.io.path.Path
//ANDROID PORT
import java.util.*

class HKVKey (
    private val rootKey: String,
    vararg moreKeys: String
    ) {

    private val subKeys = LinkedList<String>()

    init {
        if(moreKeys.isNotEmpty())
            subKeys.addAll(moreKeys)
    }

    //ANDROID PORT
    private fun forFS(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
    //ANDROID PORT

    fun toPath(): Path {
        //ANDROID PORT
        return Path(forFS(rootKey), *subKeys.map { forFS(it) }.toTypedArray())
        //ANDROID PORT
    }

    override fun toString(): String {
        return "/${rootKey}/${subKeys.joinToString("/")}"
    }

    override fun equals(other: Any?): Boolean {
        if(other != null) {
            return other.toString() == toString()
        } else
            return false
    }

    override fun hashCode(): Int {
        return toString().hashCode()
    }

    fun startsWith(key: HKVKey): Boolean {
        return toString().startsWith(key.toString())
    }

    val name: String
        get() = when(subKeys.isNotEmpty()) {
            true -> subKeys.last
            else -> rootKey
        }

    val parent: HKVKey?
        get() = when(subKeys.isNotEmpty()) {
            true -> HKVKey(rootKey, *subKeys.subList(0, subKeys.count() - 1).toTypedArray())
            else -> null
        }

    companion object {
        private fun fromFS(value: String): String {
            return URLDecoder.decode(value, StandardCharsets.UTF_8)
        }

        fun fromPath(path: Path): HKVKey {
            return HKVKey(fromFS(path.getName(0).toString()), *path.subpath(1, path.nameCount).map { fromFS(it.toString()) }.toTypedArray())
        }

        fun fromString(path: String): HKVKey {
            val parts = path.split("/")
            val rootKey = parts[0]
            return HKVKey(rootKey, *parts.subList(1, parts.count()).toTypedArray())
        }
    }
}