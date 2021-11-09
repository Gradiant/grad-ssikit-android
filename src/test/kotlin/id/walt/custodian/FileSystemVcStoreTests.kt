package id.walt.custodian

import id.walt.servicematrix.ServiceMatrix
import id.walt.servicematrix.ServiceRegistry
//ANDROID PORT
import id.walt.servicematrix.utils.AndroidUtils
//ANDROID PORT
import id.walt.services.vcstore.FileSystemVcStoreService
import id.walt.services.vcstore.VcStoreService
import id.walt.test.RESOURCES_PATH
import id.walt.vclib.vclist.Europass
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
//ANDROID PORT
import java.io.File
import java.io.FileInputStream
//ANDROID PORT

class FileSystemVcStoreTests : StringSpec({

    //ANDROID PORT
    AndroidUtils.setAndroidDataDir(System.getProperty("user.dir"))
    AndroidUtils.setDataRoot(System.getProperty("user.dir") + "/data")
    ServiceMatrix(FileInputStream(File("$RESOURCES_PATH/service-matrix.properties")))
    //ANDROID PORT
    val custodian = Custodian.getService()
    ServiceRegistry.registerService<VcStoreService>(FileSystemVcStoreService())

    val vc = Europass.template!!.invoke()

    "1: Store credential" {
        custodian.storeCredential("my-test-europass", vc)

        custodian.listCredentialIds().size shouldBeGreaterThanOrEqual 1
    }

    "2: List credential ids" {
        custodian.listCredentialIds().forEach {
            println(it)
        }
    }

    "3: Retrieve credential" {
        val retrievedVc = custodian.getCredential("my-test-europass")
        println(retrievedVc)
        retrievedVc shouldNotBe null
        println((retrievedVc!! as Europass).credentialSchema!!.id)
    }

    "3: List credentials" {
        custodian.listCredentials().contains(vc) shouldBe true
    }

    "4: Delete credentials" {
        custodian.listCredentialIds().forEach {
            custodian.deleteCredential(it)
        }

        custodian.listCredentialIds().forEach {
            println("! Remaining credential id: $it")
        }

        custodian.listCredentialIds().size shouldBe 0
        custodian.listCredentials().size shouldBe 0
    }
})
