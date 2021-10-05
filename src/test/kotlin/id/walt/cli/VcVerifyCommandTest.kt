package id.walt.cli

import com.github.ajalt.clikt.core.PrintHelpMessage
import id.walt.auditor.PolicyRegistry
import id.walt.custodian.CustodianService
import id.walt.model.DidMethod
import id.walt.servicematrix.ServiceMatrix
import id.walt.services.did.DidService
import id.walt.services.vc.JsonLdCredentialService
import id.walt.services.vc.JwtCredentialService
import id.walt.signatory.ProofConfig
import id.walt.signatory.ProofType
import id.walt.signatory.Signatory
import id.walt.test.RESOURCES_PATH
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import java.io.File
//ANDROID PORT
import java.io.FileInputStream
//ANDROID PORT

class VcVerifyCommandTest : StringSpec({

    //ANDROID PORT
    ServiceMatrix(FileInputStream(File("$RESOURCES_PATH/service-matrix.properties")))
    //ANDROID PORT

    "vc verify --help" {
        val e = shouldThrow<PrintHelpMessage> {
            VerifyVcCommand().parse(listOf("--help"))
        }
        val message = e.command.getFormattedHelp()
        message shouldContain "Verify VC or VP"
    }

    "vc policies" {
        ListVerificationPoliciesCommand().parse(listOf())
    }

    //ANDROID PORT
    /*
    "vc verify -p SignaturePolicy path/to/vp.json" {
        val did = DidService.create(DidMethod.key)
        val vcStr = Signatory.getService().issue(
            "VerifiableDiploma", ProofConfig(issuerDid = did, subjectDid = did, issuerVerificationMethod = "Ed25519Signature2018")
        )
        val vpStr = JsonLdCredentialService.getService().present(listOf(vcStr), did, "https://api.preprod.ebsi.eu", "d04442d3-661f-411e-a80f-42f19f594c9d")
        val vpFile = File.createTempFile("vpr", ".json")
        try {
            vpFile.writeText(vpStr)
            VerifyVcCommand().parse(listOf("-p", PolicyRegistry.defaultPolicyId, vpFile.absolutePath))
        } finally {
            vpFile.delete()
        }
    }

    "vc verify -p SignaturePolicy path/to/vp.jwt" {
        val did = DidService.create(DidMethod.key)
        val vcJwt = Signatory.getService().issue(
            "VerifiableDiploma", ProofConfig(issuerDid = did, subjectDid = did, issuerVerificationMethod = "Ed25519Signature2018", proofType = ProofType.JWT)
        )
        val vpJwt = CustodianService.getService().createPresentation(listOf(vcJwt), did, did, null ,"abcd")
        val vpFile = File.createTempFile("vpr", ".jwt")
        try {
            vpFile.writeText(vpJwt)
            VerifyVcCommand().parse(listOf("-p", PolicyRegistry.defaultPolicyId, vpFile.absolutePath))
        } finally {
            vpFile.delete()
        }
    }*/
    //ANDROID PORT
})
