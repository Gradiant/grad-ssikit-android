package id.walt.auditor

import id.walt.model.TrustedIssuer
import id.walt.services.did.DidService
import id.walt.services.essif.TrustedIssuerClient
import id.walt.services.vc.JsonLdCredentialService
import id.walt.services.vc.JwtCredentialService
import id.walt.services.vc.VcUtils
import id.walt.vclib.Helpers.toCredential
import id.walt.vclib.model.VerifiableCredential
import id.walt.vclib.vclist.VerifiablePresentation
import kotlinx.serialization.Serializable
//ANDROID PORT
//import mu.KotlinLogging
//ANDROID PORT

// the following validation policies can be applied
// - SIGNATURE
// - JSON_SCHEMA
// - TRUSTED_ISSUER_DID
// - TRUSTED_SUBJECT_DID
// - REVOCATION_STATUS
// - ISSUANCE_DATA_AFTER
// - EXPIRATION_DATE_BEFORE
// - REVOCATION_STATUS
// - SECURE_CRYPTO
// - HOLDER_BINDING (only for VPs)

//ANDROID PORT
//val log = KotlinLogging.logger {}
//ANDROID PORT

@Serializable
data class VerificationPolicyMetadata(val description: String, val id: String)

interface VerificationPolicy {
    val id: String
        get() = this.javaClass.simpleName
    val description: String
    fun verify(vc: VerifiableCredential): Boolean
}

class SignaturePolicy : VerificationPolicy {
    private val jsonLdCredentialService = JsonLdCredentialService.getService()
    private val jwtCredentialService = JwtCredentialService.getService()
    override val description: String = "Verify by signature"

    override fun verify(vc: VerifiableCredential): Boolean {
        return try {
            //ANDROID PORT
            //log.debug { "is jwt: ${vc.jwt != null}" }
            //ANDROID PORT

            DidService.importKey(VcUtils.getIssuer(vc)) && when (vc.jwt) {
                null -> jsonLdCredentialService.verify(vc.json!!).verified
                else -> jwtCredentialService.verify(vc.jwt!!).verified
            }
        } catch (e: Exception) {
            //ANDROID PORT
            //log.error(e.localizedMessage)
            //ANDROID PORT
            false
        }
    }
}

class JsonSchemaPolicy : VerificationPolicy { // Schema already validated by json-ld?
    override val description: String = "Verify by JSON schema"
    override fun verify(vc: VerifiableCredential) = true // TODO validate policy
}

class TrustedIssuerDidPolicy : VerificationPolicy {
    override val description: String = "Verify by trusted issuer did"
    override fun verify(vc: VerifiableCredential): Boolean {

        return when (vc) {
            is VerifiablePresentation -> true
            else -> DidService.loadOrResolveAnyDid(VcUtils.getIssuer(vc)) != null
        }
    }
}

class TrustedIssuerRegistryPolicy : VerificationPolicy {
    override val description: String = "Verify by trusted EBSI Trusted Issuer Registry record"
    override fun verify(vc: VerifiableCredential): Boolean {

        // VPs are not considered
        if (vc is VerifiablePresentation) {
            return true
        }

        val issuerDid = VcUtils.getIssuer(vc)

        val resolvedIssuerDid =
            DidService.loadOrResolveAnyDid(issuerDid) ?: throw Exception("Could not resolve issuer DID $issuerDid")

        if (resolvedIssuerDid.id != issuerDid) {
            //ANDROID PORT
            //log.debug { "Resolved DID ${resolvedIssuerDid.id} does not match the issuer DID $issuerDid" }
            //ANDROID PORT
            return false
        }

        val tirRecord = try {
            TrustedIssuerClient.getIssuer(issuerDid)
        } catch (e: Exception) {
            throw Exception("Could not resolve issuer TIR record of $issuerDid", e)
        }

        return validTrustedIssuerRecord(tirRecord)

    }

    private fun validTrustedIssuerRecord(tirRecord: TrustedIssuer): Boolean {
        var issuerRecordValid = true

        if (tirRecord.attributes[0].body != "eyJAY29udGV4dCI6Imh0dHBzOi8vZWJzaS5ldSIsInR5cGUiOiJhdHRyaWJ1dGUiLCJuYW1lIjoiaXNzdWVyIiwiZGF0YSI6IjVkNTBiM2ZhMThkZGUzMmIzODRkOGM2ZDA5Njg2OWRlIn0=") {
            issuerRecordValid = false
            //ANDROID PORT
            //log.debug { "Body of TIR record ${tirRecord} not valid." }
            //ANDROID PORT
        }

        if (tirRecord.attributes[0].hash != "14f2d3c3320f65b6fd9413608e4c17f831e3c595ad61222ec12f899752348718") {
            issuerRecordValid = false
            //ANDROID PORT
            //log.debug { "Body of TIR record ${tirRecord} not valid." }
            //ANDROID PORT
        }
        return issuerRecordValid
    }
}

class TrustedSubjectDidPolicy : VerificationPolicy {
    override val description: String = "Verify by trusted subject did"
    override fun verify(vc: VerifiableCredential): Boolean {

        //TODO complete PoC implementation
        return when (vc) {
            is VerifiablePresentation -> true
            else -> DidService.loadOrResolveAnyDid(VcUtils.getHolder(vc)) != null
        }
    }
}

object PolicyRegistry {
    private val policies = LinkedHashMap<String, VerificationPolicy>()
    val defaultPolicyId: String

    fun register(policy: VerificationPolicy) = policies.put(policy.id, policy)
    fun getPolicy(id: String) = policies[id]!!
    fun contains(id: String) = policies.containsKey(id)
    fun listPolicies() = policies.values

    init {
        val sigPol = SignaturePolicy()
        defaultPolicyId = sigPol.id
        register(sigPol)
        register(JsonSchemaPolicy())
        register(TrustedSubjectDidPolicy())
        register(TrustedIssuerDidPolicy())
        register(TrustedIssuerRegistryPolicy())
    }
}

data class VerificationResult(
    val overallStatus: Boolean = false,
    val policyResults: Map<String, Boolean>
) {
    override fun toString() =
        "VerificationResult(overallStatus=$overallStatus, policyResults={${policyResults.entries.joinToString { it.key + "=" + it.value }}})"
}

interface IAuditor {

    fun verify(vcJson: String, policies: List<VerificationPolicy>): VerificationResult

//    fun verifyVc(vc: String, config: AuditorConfig) = VerificationStatus(true)
//    fun verifyVp(vp: String, config: AuditorConfig) = VerificationStatus(true)
}

object Auditor : IAuditor {

    private fun allAccepted(policyResults: Map<String, Boolean>) = policyResults.values.all { it }

    override fun verify(vcJson: String, policies: List<VerificationPolicy>): VerificationResult {
        val vc = vcJson.toCredential()
        val policyResults = policies
            .associateBy(keySelector = VerificationPolicy::id) { policy ->
                //ANDROID PORT
                //log.debug { "Verifying vc with ${policy.id}..." }
                //ANDROID PORT
                policy.verify(vc) && when (vc) {
                    is VerifiablePresentation -> vc.verifiableCredential.all { cred ->
                        //ANDROID PORT
                        //log.debug { "Verifying ${cred.type.last()} in VP with ${policy.id}..." }
                        //ANDROID PORT
                        policy.verify(cred)
                        }
                    else -> true
                }
            }

        return VerificationResult(allAccepted(policyResults), policyResults)
    }
}
