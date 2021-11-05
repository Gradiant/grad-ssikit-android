package id.walt.signatory

import com.beust.klaxon.Klaxon
import id.walt.model.DidMethod
import id.walt.servicematrix.ServiceMatrix
import id.walt.servicematrix.utils.AndroidUtils
import id.walt.services.did.DidService
import id.walt.signatory.rest.IssueCredentialRequest
import id.walt.signatory.rest.SignatoryRestAPI
import id.walt.test.RESOURCES_PATH
import id.walt.vclib.Helpers.encode
import id.walt.vclib.vclist.Europass
import io.github.rybalkinsd.kohttp.dsl.httpPost
import io.github.rybalkinsd.kohttp.ext.asString
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
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

class SignatoryApiTest : AnnotationSpec() {

    init {
        //ANDROID PORT
        AndroidUtils.setAndroidDataDir(System.getProperty("user.dir"))
        ServiceMatrix(FileInputStream(File("$RESOURCES_PATH/service-matrix.properties")))
        //ANDROID PORT
    }

    val SIGNATORY_API_PORT = 7001
    val SIGNATORY_API_URL = "http://localhost:$SIGNATORY_API_PORT"

    val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        expectSuccess = false
    }

    @BeforeClass
    fun startServer() {
        SignatoryRestAPI.start(SIGNATORY_API_PORT)
    }

    @AfterClass
    fun teardown() {
        SignatoryRestAPI.stop()
    }

    @Test
    fun testHealth() = runBlocking {
        val response: HttpResponse = client.get("$SIGNATORY_API_URL/health")
        response.readText() shouldBe "OK"
    }

    @Test
    fun testListVcTemplates() = runBlocking {
        val templates = client.get<List<String>>("$SIGNATORY_API_URL/v1/templates") {
            contentType(ContentType.Application.Json)
        }

        templates shouldContain "Europass"
        templates shouldContain "VerifiablePresentation"
    }

    @Test
    fun testGetVcDefaultTemplate() = runBlocking {
        val europassJson = client.get<String>("$SIGNATORY_API_URL/v1/templates/Europass") {
            contentType(ContentType.Application.Json)
        }

        europassJson shouldEqualJson Europass.template!!.invoke().encode()
    }

    @Test
    //ANDROID PORT
    @Ignore //Android Lazy Sodium
    //ANDROID PORT
    fun testCredentialIssuance() = runBlocking {
        val did = DidService.create(DidMethod.key)

        val vc = httpPost {
            host = SIGNATORY_API_URL.drop(SIGNATORY_API_URL.indexOf("/") + 2).split(":").first()
            port = SIGNATORY_API_PORT
            path = "/v1/credentials/issue"

            body {
                json(
                    Klaxon().toJsonString(
                        IssueCredentialRequest(
                            "VerifiableDiploma",
                            ProofConfig(
                                issuerDid = did,
                                subjectDid = did,
                                issuerVerificationMethod = "Ed25519Signature2018",
                                proofType = ProofType.LD_PROOF
                            )
                        )
                    )
                )
            }
        }.asString()

        println(vc)
        vc shouldContain did
    }
}
