package id.walt.custodian

import id.walt.custodian.CustodianKeyTestUtils.standardKeyTests
import id.walt.servicematrix.ServiceMatrix
import id.walt.servicematrix.ServiceRegistry
import id.walt.servicematrix.utils.AndroidUtils
import id.walt.services.keystore.KeyStoreService
import id.walt.services.keystore.SqlKeyStoreService
import id.walt.test.RESOURCES_PATH
import io.kotest.core.spec.style.StringSpec
//ANDROID PORT
import java.io.File
import java.io.FileInputStream
/*

class CustodianSqlKeyTests : StringSpec({

    //ANDROID PORT
    AndroidUtils.setAndroidDataDir(System.getProperty("user.dir"))
    ServiceMatrix(FileInputStream(File("$RESOURCES_PATH/service-matrix.properties")))
    //ANDROID PORT
    val custodian = Custodian.getService()

    ServiceRegistry.registerService<KeyStoreService>(SqlKeyStoreService())
    standardKeyTests(custodian)
})*/
//ANDROID PORT
