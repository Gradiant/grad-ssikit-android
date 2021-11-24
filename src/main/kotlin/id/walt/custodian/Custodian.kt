package id.walt.custodian

import id.walt.crypto.Key
import id.walt.crypto.KeyAlgorithm
import id.walt.servicematrix.ServiceProvider
import id.walt.services.WaltIdService
import id.walt.services.context.ContextManager
import id.walt.services.key.KeyService
import id.walt.services.vc.JsonLdCredentialService
import id.walt.services.vc.JwtCredentialService
import id.walt.vclib.VcLibManager
import id.walt.vclib.model.VerifiableCredential
//ANDROID PORT
//import mu.KotlinLogging

//private val log = KotlinLogging.logger {}
//ANDROID PORT

abstract class Custodian : WaltIdService() {
    override val implementation get() = serviceImplementation<Custodian>()

    open fun generateKey(keyAlgorithm: KeyAlgorithm): Key = implementation.generateKey(keyAlgorithm)
    open fun getKey(alias: String): Key = implementation.getKey(alias)
    open fun listKeys(): List<Key> = implementation.listKeys()
    open fun storeKey(key: Key): Unit = implementation.storeKey(key)
    open fun deleteKey(id: String): Unit = implementation.deleteKey(id)

    open fun getCredential(id: String): VerifiableCredential? = implementation.getCredential(id)
    open fun listCredentials(): List<VerifiableCredential> = implementation.listCredentials()
    open fun listCredentialIds(): List<String> = implementation.listCredentialIds()
    open fun storeCredential(alias: String, vc: VerifiableCredential): Unit = implementation.storeCredential(alias, vc)
    open fun deleteCredential(alias: String): Boolean = implementation.deleteCredential(alias)

    open fun createPresentation(
        vcs: List<String>, holderDid: String, verifierDid: String? = null, domain: String? = null, challenge: String? = null
    ): String = implementation.createPresentation(vcs, holderDid, verifierDid, domain, challenge)

    companion object : ServiceProvider {
        override fun getService() = object : Custodian() {}
    }
}

open class WaltIdCustodian : Custodian() {
    private val VC_GROUP = "custodian"
    private val keyService = KeyService.getService()
    private val jwtCredentialService = JwtCredentialService.getService()
    private val jsonLdCredentialService = JsonLdCredentialService.getService()

    override fun generateKey(keyAlgorithm: KeyAlgorithm): Key = ContextManager.keyStore.load(keyService.generate(keyAlgorithm).id)
    override fun getKey(alias: String): Key = ContextManager.keyStore.load(alias)
    override fun listKeys(): List<Key> = ContextManager.keyStore.listKeys()
    override fun storeKey(key: Key) = ContextManager.keyStore.store(key)
    override fun deleteKey(id: String) = ContextManager.keyStore.delete(id)

    override fun getCredential(id: String) = ContextManager.vcStore.getCredential(id, VC_GROUP)
    override fun listCredentials(): List<VerifiableCredential> = ContextManager.vcStore.listCredentials(VC_GROUP)
    override fun listCredentialIds(): List<String> = ContextManager.vcStore.listCredentialIds(VC_GROUP)
    override fun storeCredential(alias: String, vc: VerifiableCredential) = ContextManager.vcStore.storeCredential(alias, vc, VC_GROUP)
    override fun deleteCredential(alias: String) = ContextManager.vcStore.deleteCredential(alias, VC_GROUP)

    override fun createPresentation(
        vcs: List<String>, holderDid: String, verifierDid: String?, domain: String?, challenge: String?
    ): String = when {
        vcs.stream().allMatch { VcLibManager.isJWT(it) } -> jwtCredentialService.present(vcs, holderDid, verifierDid, challenge)
        vcs.stream().noneMatch { VcLibManager.isJWT(it) } -> jsonLdCredentialService.present(vcs, holderDid, domain, challenge)
        else -> throw IllegalStateException("All verifiable credentials must be of the same proof type.")
    }
}
