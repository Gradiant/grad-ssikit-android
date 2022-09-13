import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.serialization") version "1.6.10"
    id("com.github.kkdad.dependency-license-report") version "1.16.6"
    id("org.owasp.dependencycheck") version "6.5.3"
    application
    `maven-publish`
}

group = "id.walt"
version = "1.11.0-SNAPSHOT"

repositories {
    mavenCentral()
    //jcenter()
    maven("https://jitpack.io")
    maven("https://repo.danubetech.com/repository/maven-public/")
    maven("https://maven.walt.id/repository/waltid/")
    maven("https://maven.walt.id/repository/waltid-ssi-kit/")
    mavenLocal()
}

dependencies {
    //ANDROID PORT
    //VC & Service-Matrix Ported Versions
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    //ANDROID PORT
    // Crypto
    api("com.google.crypto.tink:tink:1.6.1")
    api("info.weboftrust:ld-signatures-java:0.5-SNAPSHOT")
    api("decentralized-identity:jsonld-common-java:0.2.0")
    implementation("com.goterl:lazysodium-java:5.1.1")
    implementation("com.github.multiformats:java-multibase:v1.1.0")
    implementation("com.microsoft.azure:azure-keyvault:1.2.6")
    implementation("com.microsoft.azure:azure-client-authentication:1.7.14")
    implementation("com.nimbusds:nimbus-jose-jwt:9.23")
    implementation("com.nimbusds:oauth2-oidc-sdk:9.37.2")

    implementation("org.bouncycastle:bcprov-jdk15to18:1.71")
    implementation("org.bouncycastle:bcpkix-jdk15to18:1.71")


    // Ethereum
    implementation("org.web3j:core:5.0.0")
    implementation("org.web3j:crypto:5.0.0")

    implementation("com.google.guava:guava:31.1-jre")

    // VC
    //ANDROID PORT
    //implementation("id.walt:waltid-ssikit-vclib:1.22.0")
    //ANDROID PORT

    // JSON
    implementation("org.json:json:20220320")
    implementation("com.beust:klaxon:5.6")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.3")
    implementation("io.ktor:ktor-client-jackson:2.0.2")
    implementation("io.ktor:ktor-client-content-negotiation:2.0.2")
    implementation("com.jayway.jsonpath:json-path:2.7.0")

    //implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")

    // DB
    implementation("org.xerial:sqlite-jdbc:3.36.0.3")
    implementation("com.zaxxer:HikariCP:5.0.1")

    // CLI
    implementation("com.github.ajalt.clikt:clikt-jvm:3.4.2")
    implementation("com.github.ajalt.clikt:clikt:3.4.2")

    // Misc
    implementation("commons-io:commons-io:2.11.0")
    implementation("io.minio:minio:8.4.1")

    // HTTP
    implementation("io.ktor:ktor-client-core:2.0.2")
    implementation("io.ktor:ktor-client-cio:2.0.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.0.2")
    implementation("io.ktor:ktor-client-logging:2.0.2")
    implementation("io.github.rybalkinsd:kohttp:0.12.0")

    // REST
    implementation("io.javalin:javalin:4.6.4")
    implementation("io.javalin:javalin-openapi:4.6.4")
    implementation("io.javalin:javalin-testtools:4.5.0")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.0-alpha6")
    implementation("org.slf4j:slf4j-simple:2.0.0-alpha6")

    implementation("io.github.microutils:kotlin-logging-jvm:2.1.23")

    // Config
    implementation("com.sksamuel.hoplite:hoplite-core:2.1.5")
    implementation("com.sksamuel.hoplite:hoplite-yaml:2.1.5")
    implementation("com.sksamuel.hoplite:hoplite-hikaricp:2.1.5")

    // Service-Matrix
    //ANDROID PORT
    //implementation("id.walt.servicematrix:WaltID-ServiceMatrix:1.1.1")
    //ANDROID PORT

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.21")

    // Testing
    //testImplementation(kotlin("test-junit"))
    testImplementation("io.mockk:mockk:1.12.4")

    testImplementation("io.kotest:kotest-runner-junit5:5.3.0")
    testImplementation("io.kotest:kotest-assertions-core:5.3.0")
    testImplementation("io.kotest:kotest-assertions-json:5.3.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

/*
tasks.withType<Test> {
    useJUnitPlatform()
}
 */

java {
    toolchain {
        //ANDROID PORT
        languageVersion.set(JavaLanguageVersion.of(11))
        //ANDROID PORT
    }
}

tasks.withType<KotlinCompile> {
    //ANDROID PORT
    kotlinOptions.jvmTarget = "11"
    //ANDROID PORT
}

tasks.named<CreateStartScripts>("startScripts") {
    doLast {
        windowsScript.writeText(
            windowsScript.readText().replace(Regex("set CLASSPATH=.*"), "set CLASSPATH=%APP_HOME%\\\\lib\\\\*")
        )
    }
}

val fatJar = task("fatJar", type = Jar::class) {
    group = "build"
    //ANDROID PORT
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    //ANDROID PORT

    archiveBaseName.set("${project.name}-with-dependencies")

    manifest {
        attributes["Implementation-Title"] = "Gradle Jar Bundling"
        attributes["Implementation-Version"] = archiveVersion.get()
        attributes["Main-Class"] = "id.walt.MainCliKt"
    }

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

application {
    mainClass.set("id.walt.MainCliKt")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            pom {
                name.set("walt.id SSI Kit")
                description.set("Kotlin/Java library for SSI core services, with primary focus on European EBSI/ESSIF ecosystem.")
                url.set("https://walt.id")
            }
            from(components["java"])
        }
    }

    repositories {
        maven {
            url = uri("https://maven.walt.id/repository/waltid-ssi-kit/")
            val usernameFile = File("secret_maven_username.txt")
            val passwordFile = File("secret_maven_password.txt")
            val secretMavenUsername = System.getenv()["MAVEN_USERNAME"] ?: if (usernameFile.isFile) { usernameFile.readLines()[0] } else { "" }
            println("Deploy username length: ${secretMavenUsername.length}")
            val secretMavenPassword = System.getenv()["MAVEN_PASSWORD"] ?: if (passwordFile.isFile) { passwordFile.readLines()[0] } else { "" }

            if (secretMavenPassword.isBlank()) {
                println("WARNING: Password is blank!")
            }

            credentials {
                username = secretMavenUsername
                password = secretMavenPassword
            }
        }
    }
}

licenseReport {
    renderers = arrayOf<com.github.jk1.license.render.ReportRenderer>(
        com.github.jk1.license.render.InventoryHtmlReportRenderer(
            "report.html",
            "Backend"
        )
    )
    filters =
        arrayOf<com.github.jk1.license.filter.DependencyFilter>(com.github.jk1.license.filter.LicenseBundleNormalizer())
}

