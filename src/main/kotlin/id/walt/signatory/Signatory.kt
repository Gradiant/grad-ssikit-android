package id.walt.signatory

import com.beust.klaxon.Json
import id.walt.servicematrix.BaseService
import id.walt.servicematrix.ServiceConfiguration
import id.walt.servicematrix.ServiceProvider
import id.walt.services.context.WaltContext
import id.walt.services.vc.JsonLdCredentialService
import id.walt.services.vc.JwtCredentialService
import id.walt.vclib.Helpers.encode
import id.walt.vclib.Helpers.toCredential
import id.walt.vclib.model.VerifiableCredential
import id.walt.vclib.templates.VcTemplateManager
import java.time.LocalDateTime
import java.util.*

// JWT are using the IANA types of signatures: alg=EdDSA oder ES256 oder ES256K oder RS256
//enum class ProofType {
//    JWT_EdDSA,
//    JWT_ES256,
//    JWT_ES256K,
//    LD_PROOF_Ed25519Signature2018,
//    LD_PROOF_EcdsaSecp256k1Signature2019
//}

// Assuming the detailed algorithm will be derived from the issuer key algorithm
enum class ProofType {
    JWT,
    LD_PROOF
}

data class ProofConfig(
    val issuerDid: String,
    @Json(serializeNull = false) val subjectDid: String? = null,
    @Json(serializeNull = false) val verifierDid: String? = null,
    @Json(serializeNull = false) val issuerVerificationMethod: String? = null, // DID URL that defines key ID; if null the issuers' default key is used
    val proofType: ProofType = ProofType.LD_PROOF,
    @Json(serializeNull = false) val domain: String? = null,
    @Json(serializeNull = false) val nonce: String? = null,
    @Json(serializeNull = false) val proofPurpose: String? = null,
    @Json(serializeNull = false) val credentialId: String? = null,
    @Json(serializeNull = false) val issueDate: LocalDateTime? = null, // issue date from json-input or current system time if null
    @Json(serializeNull = false) val validDate: LocalDateTime? = null, // valid date from json-input or current system time if null
    @Json(serializeNull = false) val expirationDate: LocalDateTime? = null,
    @Json(serializeNull = false) val dataProviderIdentifier: String? = null // may be used for mapping data-sets from a custom data-provider
)

data class SignatoryConfig(
    val proofConfig: ProofConfig
) : ServiceConfiguration

interface ISignatory {

    fun issue(templateId: String, config: ProofConfig): String

}

abstract class Signatory : BaseService(), ISignatory {
    override val implementation: Signatory get() = serviceImplementation()

    companion object : ServiceProvider {
        override fun getService() = object : Signatory() {}
    }

    override fun issue(templateId: String, config: ProofConfig): String = implementation.issue(templateId, config)
    open fun listTemplates(): List<String> = implementation.listTemplates()
    open fun loadTemplate(templateId: String): VerifiableCredential = implementation.loadTemplate(templateId)
}
//ANDROID PORT
class WaltSignatory() : Signatory() {
//ANDROID PORT
    private val VC_GROUP = "signatory"
    //ANDROID PORT
    //override val configuration: SignatoryConfig = fromConfiguration(configurationPath)
    //ANDROID PORT

    override fun issue(templateId: String, config: ProofConfig): String {

        // TODO: load proof-conf from signatory.conf and optionally substitute values on request basis
        val vcTemplate = VcTemplateManager.loadTemplate(templateId)

        val configDP = when (config.credentialId.isNullOrBlank()) {
            true -> ProofConfig(
                issuerDid = config.issuerDid,
                subjectDid = config.subjectDid,
                null,
                issuerVerificationMethod = config.issuerVerificationMethod,
                proofType = config.proofType,
                domain = config.domain,
                nonce = config.nonce,
                proofPurpose = config.proofPurpose,
                config.credentialId ?: "identity#${templateId}#${UUID.randomUUID()}",
                issueDate = config.issueDate ?: LocalDateTime.now(),
                validDate = config.validDate ?: LocalDateTime.MAX,
                expirationDate = config.expirationDate,
                dataProviderIdentifier = config.dataProviderIdentifier
            )
            else -> config
        }

        val dataProvider = DataProviderRegistry.getProvider(vcTemplate::class) // vclib.getUniqueId(vcTemplate)
        val vcRequest = dataProvider.populate(vcTemplate, configDP)

        val signedVc = when (config.proofType) {
            ProofType.LD_PROOF -> JsonLdCredentialService.getService().sign(vcRequest.encode(), config)
            ProofType.JWT -> JwtCredentialService.getService().sign(vcRequest.encode(), config)
        }
        WaltContext.vcStore.storeCredential(configDP.credentialId!!, signedVc.toCredential(), VC_GROUP)
        return signedVc
    }

    override fun listTemplates(): List<String> = VcTemplateManager.getTemplateList()

    override fun loadTemplate(templateId: String): VerifiableCredential = VcTemplateManager.loadTemplate(templateId)

}
