# Walt.ID SSI Kit

The Walt.ID SSI Kit is a holistic SSI solution, with primarily focus on the European EBSI/ESSIF ecosystem.

The core services are in the scope of:
 - **Key Management** generation, import/export
 - **Decentralized Identifier (DID)** operations (register, update, deactivate)
 - **Verifiable Credential (VC)** operations (issue, present, verify)
 - **ESSIF/EBSI** related Use Cases (onboarding, VC exchange, etc.)

The ESSIF/EBSI functions are in the scope of:
 - **Onboarding ESSIF/EBSI** onboarding a natural person/legal entity including the DID creation and registration
 - **Enable Trusted Issuer** process for entitling a leagal entity to become a Trusted Issuer in the ESSIF ecosystem.
 - **Credential Issuance** protocols and data formats for issuing W3C credentials from an Trusted Issuer to a natural person.
 - **Credential Verification** verification facilities in order to determine the validity of a W3C verifiable credential aligned with ESSIF/EBSI standards

The library is written in **Kotlin/Java based library** and can be directly integrated as Maven/Gradle dependency. Alternatively the library or the additional **Docker container** can be run as RESTful webservice.

The **CLI tool** conveniently allows running all included functions manually. 

## Documentation

The documentation is hosted at: https://docs.walt.id/ssikit/

Direct links for using the SSI Kit are:

- Quick Start (running the SSI Kit with Docker or with **ssikit.sh**): https://docs.walt.id/ssikit/ssikit-usage.html#quick-start
- Building the SSI Kit with Gradle or with Docker: https://docs.walt.id/ssikit/ssikit-usage.html#build
- CLI Tool: https://docs.walt.id/ssikit/ssikit-usage.html#cli
- APIs: https://docs.walt.id/ssikit/ssikit-usage.html#apis
- Configuration: https://docs.walt.id/ssikit/ssikit-usage.html#configuration

## Examples

This project demonstrates how to integrate & use the SSI Kit in any Kotlin/Java app: https://github.com/walt-id/waltid-ssikit-examples

## License

The SSI Kit by walt.id is Open Source software released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).

# Android Port

## Changes

1. WaltIdJsonLdCredentialService.kt, WaltIdJwtCredentialService.kt -> kotlin.streams.toList must be imported.

2. Folder libs created -> ServiceMatrix jar and Vclib jar added to folder.

3. Signatory service commented in file src/test/resources/service-matrix.properties and ./service-matrix.properties. The function "fromConfiguration" gives the following error: "Could not detect parser for file extension '.conf'".

4. AuditorCommandTest, CustodianPresentTest, SignatoryServiceTest, VcVerifyCommandTest, VcIssueCommandTest, VcTemplatesCommandTest commented due to signatory service commented in point 3. 

5. HKVStoreService commented in ./service-matrix.properties. The function "fromConfiguration" gives the following error: "Could not detect parser for file extension '.conf'".

6. HKVStoreTest commented due to HKVStoreService commented in point 5.

7. KeyStoreServiceTest.kt -> size of ed25519 private key is 83, not 48 as indicated in the test. No idea why this changed.

8. build.gradle.kts -> fatJar task has some duplicity issues. Fixed after added "EXCLUDE" as duplicate strategy".

9. WaltIdServices.kt -> Android cannot execute "Runtime.version.feature()" to get Java Runtime Version. Instead, it was replaced with "System.getProperty("java.version")" which is allowed in Android.

10. WaltIdServices.kt -> Vanilla BouncyCastleProvider cannot be executed in Android. Instead, it was replaced by spongycastle.

## Android Application Requirements

1. Place the jars of waltid-ssikit, waltid-vclib, waltid-servicematrix in app/libs. Add those libraries to the project with the following line "implementation fileTree(include: ['*.jar'], dir: './libs')" inside the build.gradle of the project.

2. Android cannot resolve kotlin-reflect.kclasses, so this dependency line must be placed in build.gradle: "implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.21"" 

3. Android cannot resolve mu.KotlinLogging, so this dependency line must be placed in build.gradle: "implementation("io.github.microutils:kotlin-logging-jvm:2.0.11")"

4. Android cannot resolve spongycastle, so this 2 dependency lines must be placed in build.gradle: "implementation("com.madgag.spongycastle:prov:1.54.0.0")" and "implementation("com.madgag.spongycastle:pkix:1.54.0.0")"

