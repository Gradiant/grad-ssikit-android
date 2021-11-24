package id.walt.services.did

import com.beust.klaxon.Klaxon
import id.walt.common.prettyPrint
import id.walt.crypto.KeyAlgorithm
import id.walt.crypto.decodeBase58
//ANDROID PORT
import id.walt.model.ContextConverter
//ANDROID PORT
import id.walt.model.DidMethod
import id.walt.model.DidUrl
import id.walt.servicematrix.ServiceMatrix
//ANDROID PORT
import id.walt.servicematrix.utils.AndroidUtils
//ANDROID PORT
import id.walt.services.key.KeyService
import id.walt.test.RESOURCES_PATH
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldBeOneOf
import io.kotest.matchers.shouldBe
import java.io.File
//ANDROID PORT
import java.io.FileInputStream
//ANDROID PORT

class DidServiceTest : AnnotationSpec() {

    @Before
    fun setup() {
        //ANDROID PORT
        AndroidUtils.setAndroidDataDir(System.getProperty("user.dir"))
        ServiceMatrix(FileInputStream(File("$RESOURCES_PATH/service-matrix.properties")))
        //ANDROID PORT
    }

    private val keyService = KeyService.getService()

    fun readExampleDid(fileName: String) =
        File("$RESOURCES_PATH/dids/${fileName}.json").readText(Charsets.UTF_8)

    val ds = DidService

    @Test
    fun parseDidUrlTest() {

        val didUrl = DidUrl("method", "identifier", "key1")
        "did:method:identifier#key1" shouldBe didUrl.url

        val obj: DidUrl = DidUrl.from(didUrl.url)
        didUrl shouldBe obj
    }

    @Test
    //ANDROID PORT
    @Ignore //Android Lazy Sodium
    //ANDROID PORT
    fun createResolveDidKeyTest() {

        // Create
        val did = ds.create(DidMethod.key)
        val didUrl = DidUrl.from(did)
        did shouldBe didUrl.did
        "key" shouldBe didUrl.method
        print(did)

        // Resolve
        val resolvedDid = ds.resolve(did)
        //ANDROID PORT
        val encoded = Klaxon().converter(ContextConverter()).toJsonString(resolvedDid)
        //ANDROID PORT
        println(encoded)
    }

    @Test
    fun createResolveDidWebTest() {

        // Create
        val did = ds.create(DidMethod.web)
        val didUrl = DidUrl.from(did)
        did shouldBe didUrl.did
        "web" shouldBe didUrl.method
        print(did)

        // Resolve
        val resolvedDid = ds.resolve(did)
        //ANDROID PORT
        val encoded = Klaxon().converter(ContextConverter()).toJsonString(resolvedDid)
        //ANDROID PORT
        println(encoded)
    }

    @Test
    fun createDidEbsiV2Identifier() {
        val didUrl = DidUrl.generateDidEbsiV2DidUrl()
        val did = didUrl.did
        did.substring(0, 9) shouldBe  "did:ebsi:"
        didUrl.identifier.length shouldBeOneOf listOf(23, 24)
        didUrl.identifier[0] shouldBe 'z'
        didUrl.identifier.substring(1).decodeBase58()[0] shouldBe 0x01
        didUrl.method shouldBe "ebsi"
    }

    @Test
    fun createDidEbsiTest() {

        // Create
        val keyId = keyService.generate(KeyAlgorithm.ECDSA_Secp256k1)
        val did = ds.create(DidMethod.ebsi, keyId.id)

        // Load
        val resolvedDid = ds.loadDidEbsi(did)
        //ANDROID PORT
        val encoded = Klaxon().converter(ContextConverter()).toJsonString(resolvedDid)
        //ANDROID PORT
        println(encoded)

        // Update
        resolvedDid.assertionMethod = listOf(resolvedDid.verificationMethod!!.get(0).id)
        ds.updateDidEbsi(resolvedDid)
        //ANDROID PORT
        val encodedUpd = Klaxon().converter(ContextConverter()).toJsonString(resolvedDid)
        //ANDROID PORT
        println(encodedUpd)
    }

    @Test
    @Ignore // TODO: ESSIF backend issue
    fun resolveDidEbsiTest() {
        //ANDROID PORT
        val did = "did:ebsi:zcGvqgZTHCtkjgtcKRL7H8k"
        //ANDROID PORT
        val didDoc = DidService.resolveDidEbsi(did)
        //ANDROID PORT
        val encDidEbsi = Klaxon().converter(ContextConverter()).toJsonString(didDoc)
        //ANDROID PORT
        println(encDidEbsi)
    }

    @Test
    @Ignore // TODO: ESSIF backend issue
    fun resolveDidEbsiRawTest() {
        val did = "did:ebsi:22S7TBCJxzPS2Vv1UniBSdzFD2ZDFjZeYvQuFQWSeAQN5nTG"
        val didDoc = DidService.resolveDidEbsiRaw(did)
        println(didDoc.prettyPrint())
    }

    @Test
    //ANDROID PORT
    @Ignore //Android Lazy Sodium
    //ANDROID PORT
    fun listDidsTest() {

        ds.create(DidMethod.key)

        val dids = ds.listDids()

        dids.isNotEmpty() shouldBe true

        dids.forEach { s -> s shouldBe DidUrl.from(s).did }
    }

}
