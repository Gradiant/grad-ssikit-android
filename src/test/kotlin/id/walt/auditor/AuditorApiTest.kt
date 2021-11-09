package id.walt.auditor

import com.beust.klaxon.Klaxon
import id.walt.model.DidMethod
import id.walt.servicematrix.ServiceMatrix
//ANDROID PORT
import id.walt.servicematrix.utils.AndroidUtils
//ANDROID PORT
import id.walt.services.did.DidService
import id.walt.signatory.ProofConfig
import id.walt.signatory.Signatory
import id.walt.test.RESOURCES_PATH
import id.walt.test.readVerifiableCredential
import io.github.rybalkinsd.kohttp.dsl.httpPost
import io.github.rybalkinsd.kohttp.ext.asString
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
//ANDROID PORT
import java.io.File
import java.io.FileInputStream
//ANDROID PORT
import java.time.LocalDateTime

class AuditorApiTest : AnnotationSpec() {

    init {
        //ANDROID PORT
        AndroidUtils.setAndroidDataDir(System.getProperty("user.dir"))
        ServiceMatrix(FileInputStream(File("$RESOURCES_PATH/service-matrix.properties")))
        //ANDROID PORT
    }

    val Auditor_HOST = "localhost"
    val Auditor_API_PORT = 7001
    val Auditor_API_URL = "http://$Auditor_HOST:$Auditor_API_PORT"

    val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        expectSuccess = false
    }

    @BeforeClass
    fun startServer() {
        AuditorRestAPI.start(Auditor_API_PORT)
    }

    @AfterClass
    fun teardown() {
        AuditorRestAPI.stop()
    }

    @Test
    fun testHealth() = runBlocking {
        val response: HttpResponse = client.get("$Auditor_API_URL/health")
        response.readText() shouldBe "OK"
    }

    @Test
    fun testListVerificationPolicies() = runBlocking {
        val policies = client.get<List<VerificationPolicyMetadata>>("$Auditor_API_URL/v1/policies") {
            contentType(ContentType.Application.Json)
        }

        policies shouldContain VerificationPolicyMetadata("Verify by signature", "SignaturePolicy")
        policies shouldContain VerificationPolicyMetadata("Verify by JSON schema", "JsonSchemaPolicy")
    }

    private fun postAndVerify(vcToVerify: String, policyList: String = "SignaturePolicy") {
        val verificationResultJson = httpPost {
            host = Auditor_HOST
            port = Auditor_API_PORT
            path = "/v1/verify"
            param {
                "policyList" to policyList
            }
            body {
                json(vcToVerify)
            }
        }.asString()!!

        println(verificationResultJson)

        @Test
        fun testVerifiableDiploma() {
            postAndVerify(readVerifiableCredential("VerifiableDiploma"))
    }

        @Test
        fun testVerifiableId() {
            postAndVerify(readVerifiableCredential("VerifiableId"))
        }

        @Test
        fun testDeqarCredential() {
            postAndVerify(readVerifiableCredential("DeqarCredential"))
        }

        @Test
        fun testGaiaxCredential() {
            postAndVerify(readVerifiableCredential("GaiaxCredential"), "JsonSchemaPolicy")
        }

        @Test
        fun testPermanentResidentCardCredential() {
            postAndVerify(readVerifiableCredential("PermanentResidentCard"))
        }

    @Test
    //ANDROID PORT
    @Ignore //Android Lazy Sodium
    //ANDROID PORT
    fun testCredentialVerification() = runBlocking {
        val signatory = Signatory.getService()
        val did = DidService.create(DidMethod.key)

        val vcToVerify = signatory.issue(
            "VerifiableId", ProofConfig(
                subjectDid = did,
                issuerDid = did,
                issueDate = LocalDateTime.of(2020, 11, 3, 0, 0)
            )
        )

        postAndVerify(vcToVerify, "SignaturePolicy")
    }

    @Test
    fun testVerifiableAuthorizationCredential() {
        postAndVerify(readVerifiableCredential("VerifiableAuthorization"), "SignaturePolicy,JsonSchemaPolicy,TrustedSubjectDidPolicy,TrustedIssuerDidPolicy")
    }

    @Test
    fun testVerifiableAttestationCredential() {
        postAndVerify(readVerifiableCredential("VerifiableAttestation"))
    }

        val vr = Klaxon().parse<VerificationResult>(verificationResultJson)!!
        vr.overallStatus shouldBe true
    }
}
