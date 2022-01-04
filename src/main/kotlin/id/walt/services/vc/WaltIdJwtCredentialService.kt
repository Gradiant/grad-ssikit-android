package id.walt.services.vc

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import id.walt.services.jwt.JwtService
import id.walt.signatory.ProofConfig
import id.walt.signatory.ProofType
import id.walt.vclib.model.toCredential
import id.walt.vclib.credentials.VerifiablePresentation
import id.walt.vclib.model.VerifiableCredential
import info.weboftrust.ldsignatures.LdProof
import mu.KotlinLogging
import net.pwall.json.schema.JSONSchema
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
//ANDROID PORT
import kotlin.streams.toList
//ANDROID PORT

private val log = KotlinLogging.logger {}

private const val JWT_VC_CLAIM = "vc"
private const val JWT_VP_CLAIM = "vp"

open class WaltIdJwtCredentialService : JwtCredentialService() {

    private val jwtService = JwtService.getService()

    override fun sign(jsonCred: String, config: ProofConfig): String {
        log.debug { "Signing JWT object with config: $config" }

        val issuerDid = config.issuerDid
        val issueDate = config.issueDate ?: Instant.now()
        val validDate = config.validDate ?: Instant.now()
        val jwtClaimsSet = JWTClaimsSet.Builder()
            .jwtID(config.credentialId)
            .issuer(issuerDid)
            .issueTime(Date.from(issueDate))
            .notBeforeTime(Date.from(validDate))

        if (config.expirationDate != null)
            jwtClaimsSet.expirationTime(Date.from(config.expirationDate))

        config.verifierDid?.let { jwtClaimsSet.audience(config.verifierDid) }
        config.nonce?.let { jwtClaimsSet.claim("nonce", config.nonce) }

        when (val crd = jsonCred.toCredential()) {
            is VerifiablePresentation -> jwtClaimsSet
                .claim(JWT_VP_CLAIM, crd.toMap())
            else -> jwtClaimsSet
                .subject(config.subjectDid)
                .claim(JWT_VC_CLAIM, crd.toMap())
        }

        val payload = jwtClaimsSet.build().toString()
        log.debug { "Signing: $payload" }

        return jwtService.sign(issuerDid, payload)
    }

    override fun verifyVc(issuerDid: String, vc: String): Boolean {
        log.debug { "Verifying vc: $vc with issuerDid: $issuerDid" }
        return SignedJWT.parse(vc).header.keyID == issuerDid && verifyVc(vc)
    }

    override fun addProof(credMap: Map<String, String>, ldProof: LdProof): String =
        TODO("Not implemented yet.")

    override fun verify(vcOrVp: String): VerificationResult =
        when (VerifiableCredential.fromString(vcOrVp)) {
            is VerifiablePresentation -> VerificationResult(verifyVp(vcOrVp), VerificationType.VERIFIABLE_PRESENTATION)
            else -> VerificationResult(verifyVc(vcOrVp), VerificationType.VERIFIABLE_CREDENTIAL)
        }

    override fun verifyVc(vc: String): Boolean {
        log.debug { "Verifying vc: $vc" }
        return JwtService.getService().verify(vc)
    }

    override fun verifyVp(vp: String): Boolean =
        verifyVc(vp)

    override fun present(vcs: List<String>, holderDid: String, verifierDid: String?, challenge: String?): String {
        log.debug { "Creating a presentation for VCs:\n$vcs" }

        val id = "urn:uuid:${UUID.randomUUID()}"
        val config = ProofConfig(
            issuerDid = holderDid,
            verifierDid = verifierDid,
            proofType = ProofType.JWT,
            nonce = challenge,
            credentialId = id
        )
        val vpReqStr = VerifiablePresentation(verifiableCredential = vcs.map { it.toCredential() }).encode()

        log.trace { "VP request: $vpReqStr" }
        log.trace { "Proof config: $$config" }

        val vp = sign(vpReqStr, config)

        log.debug { "VP created:$vp" }
        return vp
    }

    override fun listVCs(): List<String> =
        Files.walk(Path.of("data/vc/created"))
            .filter { Files.isRegularFile(it) }
            .filter { it.toString().endsWith(".json") }
            .map { it.fileName.toString() }.toList()

    override fun defaultVcTemplate(): VerifiableCredential =
        TODO("Not implemented yet.")

    override fun validateSchema(vc: VerifiableCredential, schema: String): Boolean = TODO("Not implemented yet.")

    override fun validateSchemaTsr(vc: String) = try {
        vc.toCredential().let {
            if (it is VerifiablePresentation) return true

            val credentialSchema = it.credentialSchema ?: return true
            val schema = JSONSchema.parse(URL(credentialSchema.id).readText())
            return schema.validateBasic(it.json!!).valid
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
