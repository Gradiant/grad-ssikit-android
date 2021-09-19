package id.walt.services.vc

import id.walt.crypto.KeyAlgorithm
import id.walt.model.DidMethod
import id.walt.servicematrix.ServiceMatrix
import id.walt.services.did.DidService
import id.walt.services.jwt.JwtService
import id.walt.services.key.KeyService
import id.walt.signatory.ProofConfig
import id.walt.test.RESOURCES_PATH
import id.walt.vclib.Helpers.encode
import id.walt.vclib.vclist.Europass
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.*
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.maps.shouldContainKey
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class WaltIdJwtCredentialServiceTest : AnnotationSpec() {

    init {
        ServiceMatrix("$RESOURCES_PATH/service-matrix.properties")
    }

    private val credentialService = JwtCredentialService.getService()
    private val jwtService = JwtService.getService()
    private val keyId = KeyService.getService().generate(KeyAlgorithm.ECDSA_Secp256k1)

    private val id = "education#higherEducation#51e42fda-cb0a-4333-b6a6-35cb147e1a88"
    private val issuerDid = DidService.create(DidMethod.ebsi, keyId.id)
    private val subjectDid = "did:ebsi:22AhtW7XMssv7es4YcQTdV2MCM3c8b1VsiBfi5weHsjcCY9o"
    private val issueDate = Date()
    private val expirationDate = Date(Date().time + (1000 * 60 * 60 * 24))
    private val validFrom = "2021-05-31T12:29:47Z"

    @AfterAll
    fun tearDown() {
        // Only required if file-key store is used
//        val didFilename = "${issuerDid.replace(":", "-")}.json"
//        Files.delete(Path.of("data", "did", "created", didFilename))
        KeyService.getService().delete(keyId.id)
    }

    @Test
    fun testSignedVcAttributes() {
        val credential = credentialService.sign(
            Europass(validFrom = validFrom).encode(),
            ProofConfig(
                id = id,
                issuerDid = issuerDid,
                subjectDid = subjectDid,
                issueDate = issueDate,
                expirationDate = expirationDate
            )
        )

        val claims = jwtService.parseClaims(credential)!!
        claims["jti"] shouldBe id
        claims["iss"] shouldBe issuerDid
        claims["sub"] shouldBe subjectDid
        (claims["iat"] as Date).toInstant().epochSecond shouldBe issueDate.toInstant().epochSecond
        (claims["nbf"] as Date).toInstant().epochSecond shouldBe issueDate.toInstant().epochSecond
        (claims["exp"] as Date).toInstant().epochSecond shouldBe expirationDate.toInstant().epochSecond
        claims shouldContainKey "vc"
        claims["vc"].let {
            it as Map<*, *>
            it.keys.size shouldBe 3
            it.keys.forEach { listOf("@context", "type", "validFrom") shouldContain it }
            it["@context"] shouldBe listOf("https://www.w3.org/2018/credentials/v1")
            it["type"] shouldBe listOf("VerifiableCredential", "VerifiableAttestation", "Europass")
            it["validFrom"] shouldBe validFrom
        }
    }

    @Test
    fun testOptionalConfigsAreNull() {
        val claims = jwtService.parseClaims(
            credentialService.sign(Europass().encode(), ProofConfig(issuerDid = issuerDid))
        )!!
        claims["jti"] shouldBe null
        claims["iss"] shouldBe issuerDid
        claims["sub"] shouldBe null
        claims["iat"] shouldNotBe null
        claims["nbf"] shouldNotBe null
        claims["exp"] shouldBe null
        claims shouldContainKey "vc"
    }


    @Test
    fun testVerifyVc() = credentialService.verifyVc(
        credentialService.sign(Europass().encode(), ProofConfig(issuerDid = issuerDid))
    ) shouldBe true

    @Test
    fun testVerifyVcWithIssuerDid() = credentialService.verifyVc(
        issuerDid,
        credentialService.sign(Europass().encode(), ProofConfig(issuerDid = issuerDid))
    ) shouldBe true

    @Test
    fun testVerifyVcWithWrongIssuerDid() = credentialService.verifyVc(
        "wrong",
        credentialService.sign(Europass().encode(), ProofConfig(issuerDid = issuerDid)
        )
    ) shouldBe false
}
