package id.walt.services.vc

import com.nimbusds.jwt.SignedJWT
import id.walt.vclib.model.VerifiableCredential
import id.walt.vclib.vclist.*
//ANDROID PORT
//import mu.KotlinLogging

//private val log = KotlinLogging.logger("SSIKIT VcUtils")
//ANDROID PORT

object VcUtils {

    fun getIssuer(vcObj: VerifiableCredential): String = when (vcObj) {
        is Europass -> vcObj.issuer!!
        is VerifiableId -> vcObj.issuer!!
        is VerifiableDiploma -> vcObj.issuer!!
        is PermanentResidentCard -> vcObj.issuer!!
        is UniversityDegree -> vcObj.issuer.id
        is VerifiableAttestation -> vcObj.issuer
        is VerifiableAuthorization -> vcObj.issuer
        is VerifiablePresentation -> when (vcObj.jwt) {
            null -> vcObj.proof!!.creator!!
            else -> SignedJWT.parse(vcObj.jwt).jwtClaimsSet.issuer
        }
        is GaiaxCredential -> vcObj.issuer
        else -> {
            //ANDROID PORT
            //log.warn { "No getIssuer for ${vcObj.type.last()}!" }
            //ANDROID PORT
            ""
        }
    }

    fun getSubject(vcObj: VerifiableCredential): String = when (vcObj) {
        is Europass -> vcObj.credentialSubject!!.id!!
        is VerifiableId -> vcObj.credentialSubject!!.id!!
        is VerifiableDiploma -> vcObj.credentialSubject!!.id!!
        is PermanentResidentCard -> vcObj.credentialSubject!!.id!!
        is UniversityDegree -> vcObj.credentialSubject.id
        is VerifiableAttestation -> vcObj.credentialSubject!!.id
        is VerifiableAuthorization -> vcObj.credentialSubject.id
        is GaiaxCredential -> vcObj.credentialSubject.id
        else -> {
            //ANDROID PORT
            //log.warn { "No getHolder for ${vcObj.type.last()}!" }
            //ANDROID PORT
            ""
        }
    }

    fun getIssuanceDate(vc: VerifiableCredential) = when (vc) {
        is Europass -> vc.issuanceDate
        is VerifiableId -> vc.issuanceDate
        is VerifiableDiploma -> vc.issuanceDate
        is UniversityDegree -> vc.issuanceDate
        is VerifiableAttestation -> vc.issuanceDate
        is VerifiableAuthorization -> vc.issuanceDate
        else -> {
            //ANDROID PORT
            //log.warn { "No getIssuanceDate for ${vc.type.last()}!" }
            //ANDROID PORT
            ""
        }
    }

    fun getValidFrom(vc: VerifiableCredential) = when (vc) {
        is Europass -> vc.validFrom
        is VerifiableId -> vc.validFrom
        is VerifiableDiploma -> vc.validFrom
        is VerifiableAttestation -> vc.validFrom
        is VerifiableAuthorization -> vc.validFrom
        else -> {
            //ANDROID PORT
            //log.warn { "No getValidFrom for ${vc.type.last()}!" }
            //ANDROID PORT
            ""
        }
    }

    fun getExpirationDate(vc: VerifiableCredential) = when (vc) {
        is Europass -> vc.expirationDate
        is VerifiableId -> vc.expirationDate
        is VerifiableDiploma -> vc.expirationDate
        is VerifiableAuthorization -> vc.expirationDate
        else -> {
            //ANDROID PORT
            //log.warn { "No getExpirationDate for ${vc.type.last()}!" }
            //ANDROID PORT
            ""
        }
    }

    fun getCredentialSchema(vc: VerifiableCredential) = when (vc) {
        is Europass -> vc.credentialSchema
        is VerifiableId -> vc.credentialSchema
        is VerifiableDiploma -> vc.credentialSchema
        is VerifiableAttestation -> vc.credentialSchema
        is VerifiableAuthorization -> vc.credentialSchema
        else -> {
            //ANDROID PORT
            //log.warn { "No getCredentialSchema for ${vc.type.last()}!" }
            //ANDROID PORT
            null
        }
    }
}
