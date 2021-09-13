package id.walt.services.vc

import id.walt.servicematrix.ServiceProvider
import id.walt.servicematrix.ServiceRegistry
import id.walt.vclib.model.VerifiableCredential
import info.weboftrust.ldsignatures.LdProof
import id.walt.services.WaltIdService
import id.walt.services.essif.EssifServer.nonce
import id.walt.services.essif.TrustedIssuerClient.domain
import id.walt.signatory.ProofConfig


abstract class JwtCredentialService : WaltIdService() {
    override val implementation get() = ServiceRegistry.getService<JwtCredentialService>()

    open fun sign(
        jsonCred: String,
        config: ProofConfig
    ): String = implementation.sign(jsonCred, config)

    open fun verify(vcOrVp: String): VerificationResult = implementation.verify(vcOrVp)
    open fun verifyVc(issuerDid: String, vc: String): Boolean = implementation.verifyVc(issuerDid, vc)
    open fun verifyVc(vc: String): Boolean = implementation.verifyVc(vc)
    open fun verifyVp(vp: String): Boolean = implementation.verifyVp(vp)

    open fun present(vc: String): String =
        implementation.present(vc)

    open fun listVCs(): List<String> = implementation.listVCs()

    open fun defaultVcTemplate(): VerifiableCredential = implementation.defaultVcTemplate()

    open fun addProof(credMap: Map<String, String>, ldProof: LdProof): String =
        implementation.addProof(credMap, ldProof)

    companion object : ServiceProvider {
        override fun getService() = object : JwtCredentialService() {}
    }
}
