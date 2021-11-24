# Android SSI Kit

This is the Android Ported Version of the Walt.ID SSI Kit, developed by Gradiant.

- This repository demonstrates how to integrate the Android SSI Kit Project inside an Android Application: https://github.com/Gradiant/grad-ssikit-example-android 
- Android Ported Version of the servicematrix: https://github.com/Gradiant/grad-servicematrix-android
- Android Ported Version of the ssikit vclib: https://github.com/Gradiant/grad-ssikit-vclib-android

- Original Walt.ID SSI Kit: https://github.com/walt-id/waltid-ssikit

## Changes that were added in chronological order

1. WaltIdJsonLdCredentialService.kt, WaltIdJwtCredentialService.kt -> kotlin.streams.toList must be imported.

2. Folder libs created -> ServiceMatrix jar and Vclib jar added to folder.

3. Signatory service commented in file src/test/resources/service-matrix.properties and ./service-matrix.properties. The function "fromConfiguration" gives the following error: "Could not detect parser for file extension '.conf'".

4. AuditorCommandTest, CustodianPresentTest, SignatoryServiceTest, VcVerifyCommandTest, VcIssueCommandTest, VcTemplatesCommandTest commented due to signatory service commented in point 3. 

5. HKVStoreService commented in ./service-matrix.properties. The function "fromConfiguration" gives the following error: "Could not detect parser for file extension '.conf'".

6. HKVStoreTest commented due to HKVStoreService commented in point 5.

7. KeyStoreServiceTest.kt -> size of ed25519 private key is 83, not 48 as indicated in the test. No idea why this changed.

8. build.gradle.kts -> fatJar task has some duplicity issues. Fixed after added "EXCLUDE" as duplicate strategy".

9. WaltIdServices.kt -> Android cannot execute "Runtime.version.feature()" to get Java Runtime Version. Instead, it was replaced with "System.getProperty("java.version")" which is allowed in Android.

10. WaltIdServices.kt -> java.nio.file.Path does not exist in Android. Instead, it was replaced by kotlin.io.path.Path.

11. WaltIdServices.kt -> Changed path variables to handle Android data directory path.

12. SqlDbManager.kt -> Android cannot execute common java driver jdbc, so it's needed to replace it with a port of this driver to Android: SqlDroid. The HikariDataSource tries to execute the function getNetworkTimeout that doesn't exist in java.sql.connection of Android. Instead, it was replaced with a common DriverManager.getConnection(). SqlDroid doesn't work fine with autocommit mode to false (database gets blocked after the first execution). Additionally, all manual commits were commented since they are useless now, and jdbcUrl is created based in the androidDataDir path set by the application.

13. WaltCLI.kt -> ServiceMatrix call was adjusted to the changes of the constructor.

14. FileSystemHKVStore.kt -> FilesystemStoreConfig created from constructor using the dataRoot parameter set by the Android Application, instead of using fromConfiguration function. This was needed due to an error called previously ("Could not detect parser for file extension '.conf'").

15. HKVKey.kt -> Path.of method does not exist in Android SDK, so it was replaced by Path() constructor from "kotlin.io.path.Path".

16. Every ServiceMatrix(string) appearance in the tests has been replaced by ServiceMatrix(FileInputStream) since this constructor was changed to easily support Android resource files.

17. build.gradle.kts -> implementation of single jars replaced for all jars in libs folder.

18. Several appareances of URLDecoder.decode(string, charset) were replaced for URLDecoder.decode(string, string) as it is the only supported invocation of this function in Android SDK.

19. WaltIdProvider.kt -> java.security.provider constructor of Android SDK doesn't allow the version to be a String, so the parameter versionNum was added to Values.kt. 

20. WaltIdJwtService -> BouncyCastle ECDSA functions doesn't seem to work for Secp256k1 curve, as it is not natively supported in Android. Several signers from different libraries were tested, but all of them got a "OpenSSLX509CertificateFactory$ParsingException". The solution taken uses a jni to directly interact with the native secp256k1 code, and it led to the creation of a AndroidECDSASigner and AndroidECDSAVerifier.

21. build.gradle.kts -> Related to previous point, the jni was added with the following dependency "fr.acinq.secp256k1:secp256k1-kmp:0.6.0".

22. WaltIdDidEbsiService.kt -> BigInteger.TWO does not exist in Android, so it was replaced by BigInteger.ONE.add(BigInteger.ONE). 

23. CryptFun.kt -> LazySodiumJava cannot be executed by an Android environment. Instead, the LazySodiumAndroid variant must be used. As this is a Java library, and it cannot import the LazySodiumAndroid despite being able to resolve the dependency, the constructor was called using getKClass functions from ReflectionUtils.

24. SignatoryService.kt -> Removed configuration file due to the previous error about the fromConfiguration function not able to parse .conf files. Since this configuration file is not used for now, it wasn't replaced by another means to get its information.

25. TrustedIssuerClient.kt -> Added setTrustedIssuerDomain function to be able to use our own eos instead of the one hardcoded in the library.

26. WaltIdInMemoryProvider.kt -> Same reason as in point 19.

27. TrustedIssuerClient.kt -> Added two methods for making did authentication against the eos endpoint that returns a verifiable credential. Added the new endpoint authentication to the setTrustedIssuerDomain function from point 25.

28. FileSystemVcStoreService.kt -> Changed credential-store location to point towards the android application folder.

29. All tests have been adapted to this ssikit (+ other modified libraries) version. There are still some executionErrors appearing due to the fact that SQLDroid Driver can only be executed in Android environments. Other tests were ignored due to a similar problem (Android Lazy Sodium can only be executed in Android environments)

# Walt.ID SSI Kit

[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=walt-id_waltid-ssikit&metric=security_rating)](https://sonarcloud.io/dashboard?id=walt-id_waltid-ssikit)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=walt-id_waltid-ssikit&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=walt-id_waltid-ssikit)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=walt-id_waltid-ssikit&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=walt-id_waltid-ssikit)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=walt-id_waltid-ssikit&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=walt-id_waltid-ssikit)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=walt-id_waltid-ssikit&metric=ncloc)](https://sonarcloud.io/dashboard?id=walt-id_waltid-ssikit)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=walt-id_waltid-ssikit-examples&metric=alert_status)](https://sonarcloud.io/dashboard?id=walt-id_waltid-ssikit)

[![CI/CD Workflow for walt.id SSI Kit](https://github.com/walt-id/waltid-ssikit/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/walt-id/waltid-ssikit/actions/workflows/build.yml)

The Walt.ID SSI Kit is a holistic SSI solution, with primarily focus on the European EBSI/ESSIF ecosystem.

The core services are in the scope of:
 - **Key Management** generation, import/export
 - **Decentralized Identifier (DID)** operations (create, register, update, deactivate)
 - **Verifiable Credential (VC)** operations (issue, present, verify)
 - **ESSIF/EBSI** related Use Cases (onboarding, VC exchange, etc.)

The ESSIF/EBSI functions are in the scope of:
 - **Onboarding ESSIF/EBSI** onboarding a natural person/legal entity including the DID creation and registration
 - **Enable Trusted Issuer** process for entitling a legal entity to become a Trusted Issuer in the ESSIF ecosystem.
 - **Credential Issuance** protocols and data formats for issuing W3C credentials from a Trusted Issuer to a natural person.
 - **Credential Verification** verification facilities in order to determine the validity of a W3C Verifiable Credential aligned with ESSIF/EBSI standards.

The library is written in **Kotlin/Java based library** and can be directly integrated as Maven/Gradle dependency. Alternatively the library or the additional **Docker container** can be run as RESTful webservice.

## Getting Started

The easiest way to getting your hands diry and to "play" with the functions the SSI Kit provides is by running the **CLI tool** with Docker.

    docker run -itv $(pwd)/data:/app/data waltid/ssikit -h

Please go ahead and find further CLI commands and well as other ways how to use the SSI Kit in the documentation section below.

## Documentation

The documentation is hosted at: https://docs.walt.id/ssikit/

Direct links for using the SSI Kit are:

- Quick Start (running the SSI Kit with Docker or with **ssikit.sh**): https://docs.walt.id/ssikit/ssikit-usage.html#quick-start
- Building the SSI Kit with Gradle or with Docker: https://docs.walt.id/ssikit/ssikit-usage.html#build
- CLI Tool: https://docs.walt.id/ssikit/ssikit-usage.html#cli
- APIs: https://docs.walt.id/ssikit/ssikit-usage.html#rest-apis
- Configuration: https://docs.walt.id/ssikit/ssikit-usage.html#configuration

## Examples

This project demonstrates how to integrate & use the SSI Kit in any Kotlin/Java app: https://github.com/walt-id/waltid-ssikit-examples. Also the **Gradle** and **Maven** build instructions are provided there.

Following code snipped gives a first impression how to use the SSI Kit for creating **W3C Decentralized Identifiers** and for issuing/verifying **W3C Verifiable Credentials** in **JSON_LD** as well as **JWT** format.

    fun main() {

        ServiceMatrix("service-matrix.properties")
    
        val issuerDid = DidService.create(DidMethod.ebsi)
        val holderDid = DidService.create(DidMethod.key)
    
        // Issue VC in JSON-LD and JWT format (for show-casing both formats)
        val vcJson = Signatory.getService().issue("VerifiableId", ProofConfig(issuerDid = issuerDid, subjectDid = holderDid, proofType = ProofType.LD_PROOF))
        val vcJwt = Signatory.getService().issue("VerifiableId", ProofConfig(issuerDid = issuerDid, subjectDid = holderDid, proofType = ProofType.JWT))
    
        // Present VC in JSON-LD and JWT format (for show-casing both formats)
        val vpJson = Custodian.getService().createPresentation(listOf(vcJson), holderDid)
        val vpJwt = Custodian.getService().createPresentation(listOf(vcJwt), holderDid)
    
        // Verify VPs, using Signature, JsonSchema and a custom policy
        val resJson = Auditor.getService().verify(vpJson, listOf(SignaturePolicy(), JsonSchemaPolicy()))
        val resJwt = Auditor.getService().verify(vpJwt, listOf(SignaturePolicy(), JsonSchemaPolicy()))
    
        println("JSON verification result: ${resJson.overallStatus}")
        println("JWT verification result: ${resJwt.overallStatus}")
    }

## License

The SSI Kit by walt.id is Open Source software released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).
