package id.walt.services.essif

import com.beust.klaxon.Klaxon
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
//ANDROID PORT
//import mu.KotlinLogging
//ANDROID PORT
import id.walt.common.readEssif
import id.walt.model.AuthRequestResponse
import id.walt.services.WaltIdServices
import id.walt.services.essif.enterprisewallet.EnterpriseWalletService
import id.walt.services.essif.mock.DidRegistry
import id.walt.model.TrustedIssuer

//ANDROID PORT
//private val log = KotlinLogging.logger {}
//ANDROID PORT

object TrustedIssuerClient {

    // TODO: move to config file
    //ANDROID PORT
    var domain = "https://api.preprod.ebsi.eu"
    //val domain = "https://api.test.intebsi.xyz"

    var authorisation = "$domain/authorisation/v1"
    var onboarding = "$domain/users-onboarding/v1"
    var authentication ="$domain/authentication/v1"
    //ANDROID PORT
    val trustedIssuerUrl = "http://localhost:7001/v2/trusted-issuer"

    private val enterpriseWalletService = EnterpriseWalletService.getService()

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Used for VC exchange flows

    // Stubs
//    fun generateAuthenticationRequest(): String {
//        return EssifServer.generateAuthenticationRequest()
//    }
//
//    fun openSession(authResp: String): String {
//        return EssifServer.openSession(authResp)
//    }

    fun generateAuthenticationRequest(): String = runBlocking {
        return@runBlocking WaltIdServices.http.post<String>("$trustedIssuerUrl/generateAuthenticationRequest") {
            contentType(ContentType.Application.Json)
            headers {
                append(HttpHeaders.Accept, "application/json")
            }
        }
    }


    fun openSession(authResp: String): String = runBlocking {
        return@runBlocking WaltIdServices.http.post<String>("$trustedIssuerUrl/openSession") {
            contentType(ContentType.Application.Json)
            headers {
                append(HttpHeaders.Accept, "application/json")
            }
            body = authResp
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Used for registering DID EBSI

    fun authenticationRequests(): AuthRequestResponse = runBlocking {
        return@runBlocking WaltIdServices.http.post<AuthRequestResponse>("$onboarding/authentication-requests") {
            contentType(ContentType.Application.Json)
            headers {
                append(HttpHeaders.Accept, "application/json")
            }
            body = mapOf("scope" to "ebsi users onboarding")
        }
    }

    fun authenticationResponse(idToken: String, bearerToken: String): String = runBlocking {
        return@runBlocking WaltIdServices.http.post<String>("$onboarding/authentication-responses") {
            contentType(ContentType.Application.Json)
            headers {
                append(HttpHeaders.Accept, "application/json")
                append(HttpHeaders.Authorization, "Bearer $bearerToken")
            }
            body = mapOf("id_token" to idToken)
        }
    }

    fun siopSession(idToken: String): String = runBlocking {
        return@runBlocking WaltIdServices.http.post<String>("$authorisation/siop-sessions") {
            contentType(ContentType.Application.Json)
            headers {
                append(HttpHeaders.Accept, "application/json")
            }
            body = mapOf("id_token" to idToken)
        }
    }

    fun siopSessionBearer(idToken: String, bearerToken: String): String = runBlocking {
        return@runBlocking WaltIdServices.http.post<String>("$authorisation/siop-sessions") {
            contentType(ContentType.Application.Json)
            headers {
                append(HttpHeaders.Accept, "application/json")
                append(HttpHeaders.Authorization, "Bearer $bearerToken")
            }
            body = mapOf("id_token" to idToken)
        }
    }

    //ANDROID PORT
    fun postAuthenticationRequests(): AuthRequestResponse = runBlocking {
        return@runBlocking WaltIdServices.http.post<AuthRequestResponse>("$authentication/authentication-requests") {
            contentType(ContentType.Application.Json)
            headers {
                append(HttpHeaders.Accept, "application/json")
            }
            body = mapOf("scope" to "ebsi users onboarding")
        }
    }

    fun postAuthenticationResponse(idToken: String, bearerToken: String): String = runBlocking {
        return@runBlocking WaltIdServices.http.post<String>("$authentication/authentication-responses") {
            contentType(ContentType.Application.Json)
            headers {
                append(HttpHeaders.Accept, "application/json")
                append(HttpHeaders.Authorization, "Bearer $bearerToken")
            }
            body = mapOf("id_token" to idToken)
        }
    }
    //ANDROID PORT

    // GET /issuers/{did}
    // returns trusted issuer record
    fun getIssuerRaw(did: String): String = runBlocking {
        //ANDROID PORT
        //log.debug { "Getting trusted issuer with DID $did" }
        //ANDROID PORT

        val trustedIssuer: String = WaltIdServices.http.get("https://api.preprod.ebsi.eu/trusted-issuers-registry/v2/issuers/$did")

        //ANDROID PORT
        //log.debug { trustedIssuer }
        //ANDROID PORT

        return@runBlocking trustedIssuer
    }

    fun getIssuer(did: String): TrustedIssuer = runBlocking {
        //ANDROID PORT
        //log.debug { "Getting trusted issuer with DID $did" }
        //ANDROID PORT

        val trustedIssuer: String = WaltIdServices.http.get("https://api.preprod.ebsi.eu/trusted-issuers-registry/v2/issuers/$did")

        //ANDROID PORT
        //log.debug { trustedIssuer }
        //ANDROID PORT

        return@runBlocking Klaxon().parse<TrustedIssuer>(trustedIssuer)!!
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //TODO: the methods below are stubbed - to be considered

    // POST /onboards
    // returns DID ownership
    fun onboards(): String {
        println("6. [Eos] Request DID ownership")
        return readEssif("onboarding-onboards-resp")
    }

    fun signedChallenge(signedChallenge: String): String {

        val header = readEssif("onboarding-onboards-callback-req-header")
        val body = readEssif("onboarding-onboards-callback-req-body")

        //ANDROID PORT
        //log.debug { "header: $header" }
        //log.debug { "body: $body" }
        //ANDROID PORT

        println("8. [Eos] Validate DID Document")
        println("9. [Eos] GET /identifiers/{did}")
        DidRegistry.get("did")
        println("10. [Eos] 404 Not found")
        println("11. [Eos] Generate Verifiable Authorization")
        val verifiableAuthorization = readEssif("onboarding-onboards-callback-resp")
        return verifiableAuthorization
    }

    fun requestVerifiableCredential(): String {
        println("4. [Eos] Request V.ID")
        return enterpriseWalletService.generateDidAuthRequest()
    }

    fun requestCredentialUri(): String {
        println("2 [Eos] Request Credential (QR, URI, ...)")
        return "new session - QR/URI"
    }

    fun didOwnershipResponse(didOwnershipResp: String): String {
        println("8. [Eos] Response DID ownership")
        //ANDROID PORT
        //log.debug { "didOwnershipResp: $didOwnershipResp" }
        //ANDROID PORT

        // TODO: move following call to:
        //EnterpriseWalletService.validateDidAuthResponse(didOwnershipResp)

        println("9. [Eos] Validate DID ownership")
        val didOwnershipRespHeader = readEssif("onboarding-did-ownership-resp-header")

        //ANDROID PORT
        //log.debug { "didOwnershipRespHeader: $didOwnershipRespHeader" }
        //ANDROID PORT
        val didOwnershipRespBody = readEssif("onboarding-did-ownership-resp-body")
        //ANDROID PORT
        //log.debug { "didOwnershipRespBody: $didOwnershipRespBody" }
        //ANDROID PORT
        val vIdRequestOkResp = readEssif("onboarding-vid-req-ok")

        return vIdRequestOkResp
    }

    fun getCredential(id: String): String {
        println("12. [Eos] [GET]/credentials")
        return readEssif("onboarding-vid")
    }

    fun getCredentials(isUserAuthenticated: Boolean): String {
        return if (isUserAuthenticated) {
            readEssif("vc-issuance-auth-req")
        } else {
            println("2. [Eos] [GET]/credentials")
            enterpriseWalletService.generateDidAuthRequest()
            println("4. [Eos] 200 <DID-Auth Req>")
            println("5. [Eos] Generate QR, URI")
            // TODO: Trigger job for [GET] /sessions/{id}
            val str = enterpriseWalletService.getSession("sessionID")
            readEssif("vc-issuance-auth-req")
        }

    }

    //ANDROID PORT
    fun setTrustedIssuerDomain(domain: String) {
        this.domain = domain
        this.authorisation = "${TrustedIssuerClient.domain}/authorisation/v1"
        this.onboarding = "${TrustedIssuerClient.domain}/users-onboarding/v1"
        this.authentication ="${TrustedIssuerClient.domain}/authentication/v1"
    }
    //ANDROID PORT
}
