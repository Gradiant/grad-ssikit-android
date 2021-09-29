package id.walt.services.vc

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import id.walt.services.jwt.JwtService
import id.walt.signatory.ProofConfig
import id.walt.signatory.ProofType
import id.walt.vclib.Helpers.encode
import id.walt.vclib.Helpers.toCredential
import id.walt.vclib.Helpers.toMap
import id.walt.vclib.VcLibManager
import id.walt.vclib.model.VerifiableCredential
import id.walt.vclib.vclist.VerifiablePresentation
import info.weboftrust.ldsignatures.LdProof
//ANDROID PORT
import mu.KotlinLogging
//ANDROID PORT
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
//ANDROID PORT
import kotlin.streams.toList

//private val log = KotlinLogging.logger {}
//ANDROID PORT

private const val JWT_VC_CLAIM = "vc"
private const val JWT_VP_CLAIM = "vp"

open class WaltIdJwtCredentialService : JwtCredentialService() {

    private val jwtService = JwtService.getService()

    override fun sign(jsonCred: String, config: ProofConfig): String {
        //ANDROID PORT
        //log.debug { "Signing JWT object with config: $config" }
        //ANDROID PORT

        val issuerDid = config.issuerDid
        val issueDate = config.issueDate ?: Date()
        val validDate = config.validDate ?: Date()
        val jwtClaimsSet = JWTClaimsSet.Builder()
            .jwtID(config.id)
            .issuer(issuerDid)
            .issueTime(issueDate)
            .notBeforeTime(validDate)
            .expirationTime(config.expirationDate)

        when(val crd = jsonCred.toCredential()) {
            is VerifiablePresentation -> jwtClaimsSet
                .audience(config.verifierDid!!)
                .claim("nonce", config.nonce!!)
                .claim(JWT_VP_CLAIM, crd.toMap())
            else -> jwtClaimsSet
                .subject(config.subjectDid)
                .claim(JWT_VC_CLAIM, crd.toMap())
        }

        val payload = jwtClaimsSet.build().toString()
        //ANDROID PORT
        //log.debug { "Signing: $payload" }
        //ANDROID PORT

        return jwtService.sign(issuerDid, payload)
    }

    override fun verifyVc(issuerDid: String, vc: String): Boolean {
        //ANDROID PORT
        //log.debug { "Verifying vc: $vc with issuerDid: $issuerDid" }
        //ANDROID PORT
        return SignedJWT.parse(vc).header.keyID == issuerDid && verifyVc(vc)
    }

    override fun addProof(credMap: Map<String, String>, ldProof: LdProof): String =
        TODO("Not implemented yet.")

    override fun verify(vcOrVp: String): VerificationResult =
        when (VcLibManager.getVerifiableCredential(vcOrVp)) {
            is VerifiablePresentation -> VerificationResult(verifyVp(vcOrVp), VerificationType.VERIFIABLE_PRESENTATION)
            else -> VerificationResult(verifyVc(vcOrVp), VerificationType.VERIFIABLE_CREDENTIAL)
        }

    override fun verifyVc(vc: String): Boolean {
        //ANDROID PORT
        //log.debug { "Verifying vc: $vc" }
        //ANDROID PORT
        return JwtService.getService().verify(vc)
    }

    override fun verifyVp(vp: String): Boolean =
        verifyVc(vp)

    override fun present(vcs: List<String>, holderDid: String, verifierDid: String, challenge: String): String {
        //ANDROID PORT
        //log.debug { "Creating a presentation for VCs:\n$vcs" }
        //ANDROID PORT

        val id = "urn:uuid:${UUID.randomUUID()}"
        val config = ProofConfig(issuerDid = holderDid, verifierDid = verifierDid, proofType = ProofType.JWT, nonce = challenge, id = id)
        val vpReqStr = VerifiablePresentation(verifiableCredential = vcs.map { it.toCredential() }).encode()

        //ANDROID PORT
        //log.trace { "VP request: $vpReqStr" }
        //log.trace { "Proof config: $$config" }
        //ANDROID PORT

        val vp = sign(vpReqStr, config)

        //ANDROID PORT
        //log.debug { "VP created:$vp" }
        //ANDROID PORT
        return vp
    }

    override fun listVCs(): List<String> =
        Files.walk(Path.of("data/vc/created"))
            .filter { Files.isRegularFile(it) }
            .filter { it.toString().endsWith(".json") }
            .map { it.fileName.toString() }.toList()

    override fun defaultVcTemplate(): VerifiableCredential =
        TODO("Not implemented yet.")
}
