package id.walt.cli

import id.walt.servicematrix.ServiceMatrix
//ANDROID PORT
import id.walt.servicematrix.utils.AndroidUtils
//ANDROID PORT
import id.walt.test.RESOURCES_PATH
//ANDROID PORT
import java.io.File
import java.io.FileInputStream
//ANDROID PORT
import io.kotest.core.spec.style.StringSpec


class VcTemplatesCommandTest : StringSpec({

    //ANDROID PORT
    AndroidUtils.setAndroidDataDir(System.getProperty("user.dir"))
    ServiceMatrix(FileInputStream(File("$RESOURCES_PATH/service-matrix.properties")))

    /*"vc templates list" {
        VcTemplatesListCommand().parse(listOf())
    }*/
    //ANDROID PORT

    "vc templates export" {
        // TODO WALT0508
        //VcTemplatesExportCommand().parse(listOf("Europass"))
    }
})
