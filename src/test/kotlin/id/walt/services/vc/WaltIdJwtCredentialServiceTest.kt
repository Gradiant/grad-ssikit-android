package id.walt.services.vc

import id.walt.crypto.KeyAlgorithm
import id.walt.model.DidMethod
import id.walt.servicematrix.ServiceMatrix
//ANDROID PORT
import id.walt.servicematrix.utils.AndroidUtils
//ANDROID PORT
import id.walt.services.did.DidService
import id.walt.services.jwt.JwtService
import id.walt.services.key.KeyService
import id.walt.signatory.DataProviderRegistry
import id.walt.signatory.ProofConfig
import id.walt.signatory.ProofType
import id.walt.signatory.Signatory
import id.walt.test.DummySignatoryDataProvider
import id.walt.test.RESOURCES_PATH
import id.walt.vclib.Helpers.encode
import id.walt.vclib.credentials.Europass
import id.walt.vclib.credentials.VerifiableId
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.maps.shouldContainKey
//ANDROID PORT
import java.io.File
import java.io.FileInputStream
//ANDROID PORT
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class WaltIdJwtCredentialServiceTest : AnnotationSpec() {

    init {
        //ANDROID PORT
        AndroidUtils.setAndroidDataDir(System.getProperty("user.dir"))
        ServiceMatrix(FileInputStream(File("$RESOURCES_PATH/service-matrix.properties")))
        //ANDROID PORT
    }

    private val credentialService = JwtCredentialService.getService()
    private val jwtService = JwtService.getService()
    private val keyId = KeyService.getService().generate(KeyAlgorithm.ECDSA_Secp256k1)

    private val id = "education#higherEducation#51e42fda-cb0a-4333-b6a6-35cb147e1a88"
    private val issuerDid = DidService.create(DidMethod.ebsi, keyId.id)
    private val subjectDid = "did:ebsi:22AhtW7XMssv7es4YcQTdV2MCM3c8b1VsiBfi5weHsjcCY9o"
    private val issueDate = LocalDateTime.now()
    private val validDate = issueDate.minusDays(1)
    private val expirationDate = issueDate.plusDays(1)

    @AfterAll
    fun tearDown() {
        // Only required if file-key store is used
//        val didFilename = "${issuerDid.replace(":", "-")}.json"
//        Files.delete(Path.of("data", "did", "created", didFilename))
        KeyService.getService().delete(keyId.id)
    }

    @Test
    //ANDROID PORT
    @Ignore //Android Secp256k1 JNI
    //ANDROID PORT
    fun testSignedVcAttributes() {
        val credential = credentialService.sign(
            Europass().encode(),
            ProofConfig(
                credentialId = id,
                issuerDid = issuerDid,
                subjectDid = subjectDid,
                issueDate = issueDate,
                validDate = validDate,
                expirationDate = expirationDate
            )
        )

        val claims = jwtService.parseClaims(credential)!!
        claims["jti"] shouldBe id
        claims["iss"] shouldBe issuerDid
        claims["sub"] shouldBe subjectDid
        (claims["iat"] as Date).toInstant().epochSecond shouldBe issueDate.toInstant(ZoneOffset.UTC).epochSecond
        (claims["nbf"] as Date).toInstant().epochSecond shouldBe validDate.toInstant(ZoneOffset.UTC).epochSecond
        (claims["exp"] as Date).toInstant().epochSecond shouldBe expirationDate.toInstant(ZoneOffset.UTC).epochSecond
        claims shouldContainKey "vc"
        claims["vc"].let {
            it as Map<*, *>
            it.keys.size shouldBe 2
            it.keys.forEach { listOf("@context", "type") shouldContain it }
            it["@context"] shouldBe listOf("https://www.w3.org/2018/credentials/v1")
            it["type"] shouldBe listOf("VerifiableCredential", "VerifiableAttestation", "Europass")
        }
    }

    @Test
    //ANDROID PORT
    @Ignore //Android Secp256k1 JNI
    //ANDROID PORT
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
    //ANDROID PORT
    @Ignore //Android Secp256k1 JNI
    //ANDROID PORT
    fun testVerifyVc() = credentialService.verifyVc(
        credentialService.sign(Europass().encode(), ProofConfig(issuerDid = issuerDid))
    ) shouldBe true

    @Test
    //ANDROID PORT
    @Ignore //Android Secp256k1 JNI
    //ANDROID PORT
    fun testVerifyVcWithIssuerDid() = credentialService.verifyVc(
        issuerDid,
        credentialService.sign(Europass().encode(), ProofConfig(issuerDid = issuerDid))
    ) shouldBe true

    @Test
    //ANDROID PORT
    @Ignore //Android Secp256k1 JNI
    //ANDROID PORT
    fun testVerifyVcWithWrongIssuerDid() = credentialService.verifyVc(
        "wrong",
        credentialService.sign(Europass().encode(), ProofConfig(issuerDid = issuerDid)
        )
    ) shouldBe false

    @Test
    fun testValidateSchema() {
        // Required at the moment because EBSI did not upgrade V_ID schema with necessary changes.
        DataProviderRegistry.register(VerifiableId::class, DummySignatoryDataProvider())

        val noSchemaVc = VerifiableId().encode()
        val validVc = Signatory.getService().issue("VerifiableId", ProofConfig(issuerDid = issuerDid, subjectDid = issuerDid, proofType = ProofType.JWT))
        val invalidDataVc = Signatory.getService().issue("VerifiableId", ProofConfig(issuerDid = issuerDid, proofType = ProofType.JWT))
        val notParsableVc = ""

        credentialService.validateSchemaTsr(noSchemaVc) shouldBe true
        credentialService.validateSchemaTsr(validVc) shouldBe true
        credentialService.validateSchemaTsr(invalidDataVc) shouldBe false
        credentialService.validateSchemaTsr(notParsableVc) shouldBe false
    }
}
