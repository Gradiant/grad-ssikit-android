package id.walt.services.vcstore


import id.walt.vclib.model.VerifiableCredential
import id.walt.vclib.model.toCredential
import java.io.File
//ANDROID PORT
import id.walt.servicematrix.utils.AndroidUtils
//ANDROID PORT
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

open class FileSystemVcStoreService : VcStoreService() {

    //ANDROID PORT
    val store = File("${AndroidUtils.getDataRoot()}/credential-store").apply { mkdir() }
    //ANDROID PORT

    private fun getGroupDir(group: String) = File(store.absolutePath, group).apply { mkdirs() }
    private fun getFileById(id: String, group: String) = File(getGroupDir(group),"${URLEncoder.encode(id, StandardCharsets.UTF_8)}.cred")
    private fun loadFileString(id: String, group: String) = getFileById(id, group).let {
        when(it.exists()) {
            true -> it.readText()
            false -> null
        }
    }

    override fun getCredential(id: String, group: String): VerifiableCredential? = loadFileString(id, group)?.let { it.toCredential() }

    override fun listCredentials(group: String): List<VerifiableCredential> =
        listCredentialIds(group).map { getCredential(it, group)!! }

    override fun listCredentialIds(group: String): List<String> = getGroupDir(group).listFiles()!!.map { it.nameWithoutExtension }

    override fun storeCredential(alias: String, vc: VerifiableCredential, group: String) =
        getFileById(
            alias, group
        ).writeText(vc.encode())

    override fun deleteCredential(alias: String, group: String) = getFileById(alias, group).delete()
}
