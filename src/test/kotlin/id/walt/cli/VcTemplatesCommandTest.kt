package id.walt.cli

import id.walt.servicematrix.ServiceMatrix
import io.kotest.core.spec.style.StringSpec
import id.walt.cli.*
import id.walt.test.RESOURCES_PATH
//ANDROID PORT
import java.io.File
import java.io.FileInputStream
//ANDROID PORT


class VcTemplatesCommandTest : StringSpec({

    //ANDROID PORT
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
