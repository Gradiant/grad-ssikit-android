package id.walt.services.essif

import id.walt.servicematrix.ServiceMatrix
import id.walt.servicematrix.utils.AndroidUtils
import id.walt.test.RESOURCES_PATH
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
//ANDROID PORT
import java.io.File
import java.io.FileInputStream
//ANDROID PORT

class TrustedIssuerClientTest : AnnotationSpec() {
    companion object {
        private const val VALID_DID = "did:ebsi:224AEY73SGS1gpTvbt5TNTTPdNj8GU6NAq2AVBFmasQbntCt"
    }

    init {
        println("Running ServiceMatrix")
        //ANDROID PORT
        AndroidUtils.setAndroidDataDir(System.getProperty("user.dir"))
        ServiceMatrix(FileInputStream(File("$RESOURCES_PATH/service-matrix.properties")))
        //ANDROID PORT
        println("Done running the ServiceMatrix")
    }

    @Test
    @Ignore // TODO: ESSIF backend issue
    fun testGetIssuerValid() {
        println("GETTING VALID ISSUER")
        val trustedIssuer = TrustedIssuerClient.getIssuer(VALID_DID)
        trustedIssuer.did shouldBe VALID_DID
        trustedIssuer.attributes[0].hash shouldBe "14f2d3c3320f65b6fd9413608e4c17f831e3c595ad61222ec12f899752348718"
        trustedIssuer.attributes[0].body shouldBe "eyJAY29udGV4dCI6Imh0dHBzOi8vZWJzaS5ldSIsInR5cGUiOiJhdHRyaWJ1dGUiLCJuYW1lIjoiaXNzdWVyIiwiZGF0YSI6IjVkNTBiM2ZhMThkZGUzMmIzODRkOGM2ZDA5Njg2OWRlIn0="
    }
}
