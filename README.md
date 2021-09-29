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

10. WaltIdServices.kt -> java.nio.file.Path does not exist in Android. Instead, it was replaced by kotlin.io.path.Path.

11. WaltIdServices.kt -> Changed path variables to handle Android data directory path.

12. SqlDbManager.kt -> Android cannot execute common java driver jdbc, so it's needed to replace it with a port of this driver to Android: SqlDroid. The HikariDataSource tries to execute the function getNetworkTimeout that doesn't exist in java.sql.connection of Android. Instead, it was replaced with a common DriverManager.getConnection(). SqlDroid doesn't work fine with autocommit mode to false (database gets blocked after the first execution). Additionally, all manual commits were commented since they are useless now, and jdbcUrl is created based in the androidDataDir path set by the application.

13. WaltCLI.kt -> ServiceMatrix call was adjusted to the changes of the constructor.

14. FileSystemHKVStore.kt -> FilesystemStoreConfig created from constructor using the dataRoot parameter set by the Android Application, instead of using fromConfiguration function. This was needed due to an error called previously ("Could not detect parser for file extension '.conf'").

15. HKVKey.kt -> Path.of method does not exist in Android SDK, so it was replaced by Path() constructor from "kotlin.io.path.Path".

16. Every ServiceMatrix(string) appearance in the tests has been replaced by ServiceMatrix(FileInputStream) since this constructor was changed to easily support Android resource files.

17. build.gradle.kts -> implementation of single jars replaced for all jars in libs folder.

18. Several appareances of URLDecoder.decode(string, charset) were replaced for URLDecoder.decode(string, string) as it is the only supported invocation of this function in Android SDK.

## Android Application Requirements

1. Place the jars of waltid-ssikit, waltid-vclib, waltid-servicematrix in app/libs. Add those libraries to the project with the following line "implementation fileTree(include: ['*.jar'], dir: './libs')" inside the build.gradle of the project.

2. Android cannot resolve kotlin-reflect.kclasses, so this dependency line must be placed in build.gradle: "implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.21")".

3. Android cannot resolve mu.KotlinLogging, so this dependency line must be placed in build.gradle: "implementation("io.github.microutils:kotlin-logging-jvm:2.0.11")".

4. Android cannot resolve ktor-client, so this two dependency lines must be placed in build.gradle: "implementation("io.ktor:ktor-client-cio:1.6.3")" and "implementation("io.ktor:ktor-client-serialization:1.6.3")".

5. Android cannot resolve TinkConfig, so this dependency line must be placed in build.gradle: "api("com.google.crypto.tink:tink:1.6.1")".

6. WaltIdServices cannot access relative Android Data Directory Path. To solve this, it is needed to set this android path in some variable, so the next line must be placed in the MainActivity: "id.walt.common.androidDataDir = dataDir.absolutePath" (Kotlin)

7. Android cannot resolve hoplite, so this 3 dependency lines must be placed in build.gradle: "implementation("com.sksamuel.hoplite:hoplite-core:1.4.7")", "implementation("com.sksamuel.hoplite:hoplite-yaml:1.4.7")" and "implementation("com.sksamuel.hoplite:hoplite-hikaricp:1.4.7")".

8. Android cannot resolve sqldroid, so this dependency line must be placed in build.gradle: "implementation('org.sqldroid:sqldroid:1.0.3')".

9. Android cannot resolve "com.nimbusds.jose", so this dependency line must be placed in build.gradle: "implementation 'com.nimbusds:nimbus-jose-jwt:9.15.1'"

10. Android cannot resolve some libraries located in alternative repositories, so this lines must be added to the list of maven repositories: 
* maven {
    url "https://maven.walt.id/repository/danubetech"
}
* maven {
    url "https://repo.danubetech.com/repository/maven-public/"
}
* maven {
    url "https://jitpack.io"
}

11. Android cannot resolve keyFormats, so this dependency line must be placed in build.gradle:
* api("info.weboftrust:ld-signatures-java:0.5-SNAPSHOT") {
        exclude group:"net.jcip", module:"jcip-annotations"
    }
A duplicity issue appears after adding this library, so the module "jcip-annotations" must be excluded.

12. Android cannot resolve klaxon, so this dependency line must be placed in build.gradle: "implementation("com.beust:klaxon:5.5")".

13. Android needs a servicematrix.properties file placed in resources/raw. It contains the matrix of services to initialize, and must not contain conf files for the services. This resource should be obtained using the function "openRawResource" and the passed to the ServiceMatrix. To be able to initialize successfully the services, two functions need to be executed before the ServiceMatrix execution: "setAndroidDatDir" and "setDataRoot".
