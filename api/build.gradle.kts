import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

val versions = mapOf(
    "personApiVersion" to "1.0.0-SNAPSHOT",
    "keycloakAdminClientVersion" to "22.0.3",
    "springdocOpenapiStarterWebfluxUiVersion" to "2.5.0",
    "mapstructVersion" to "1.5.5.Final",
    "javaxAnnotationApiVersion" to "1.3.2",
    "javaxValidationApiVersion" to "2.0.0.Final",
    "javaxServletApiVersion" to "2.5",
    "logbackClassicVersion" to "1.5.18",
    "nettyResolverVersion" to "4.1.121.Final:osx-aarch_64",
    "feignMicrometerVersion" to "13.6",
    "testContainersVersion" to "1.19.3"
)

plugins {
    java
    idea
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.13.0"
}

group = "net.proselyte"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.0.0")
        mavenBom("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:2.15.0")
    }
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

dependencies {
    implementation("net.proselyte:person-api:${versions["personApiVersion"]}")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.keycloak:keycloak-admin-client:${versions["keycloakAdminClientVersion"]}")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:${versions["springdocOpenapiStarterWebfluxUiVersion"]}")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("io.github.openfeign:feign-micrometer:${versions["feignMicrometerVersion"]}")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    compileOnly("org.projectlombok:lombok")
    compileOnly("org.mapstruct:mapstruct:${versions["mapstructVersion"]}")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor:${versions["mapstructVersion"]}")


    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.testcontainers:testcontainers:${versions["testContainersVersion"]}")
    testImplementation("org.testcontainers:postgresql:${versions["testContainersVersion"]}")
    testImplementation("org.testcontainers:junit-jupiter:${versions["testContainersVersion"]}")
    testImplementation("org.wiremock.integrations.testcontainers:wiremock-testcontainers-module:1.0-alpha-15")

    implementation("ch.qos.logback:logback-classic:${versions["logbackClassicVersion"]}")

    implementation("javax.validation:validation-api:${versions["javaxValidationApiVersion"]}")
    implementation("javax.annotation:javax.annotation-api:${versions["javaxAnnotationApiVersion"]}")

    implementation("io.opentelemetry:opentelemetry-exporter-otlp")
    implementation("io.micrometer:micrometer-observation")
    implementation("io.micrometer:micrometer-tracing")
    implementation("io.micrometer:micrometer-tracing-bridge-otel")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    implementation("io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter")

    implementation("io.netty:netty-resolver-dns-native-macos:${versions["nettyResolverVersion"]}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

/*
──────────────────────────────────────────────────────
============== Api generation =========
──────────────────────────────────────────────────────
*/

val openApiDir = file("$rootDir/openapi")
val foundSpecifications = openApiDir.listFiles { f -> f.extension in listOf("yaml", "yml") } ?: emptyArray()
logger.lifecycle("Found ${foundSpecifications.size} specifications: " + foundSpecifications.joinToString { it.name })

val outRoot = layout.buildDirectory.dir("generated")

val generateTasks = foundSpecifications.map { specFile ->
    val name = specFile.nameWithoutExtension
    val taskName = "generate" + name
        .split(Regex("[^A-Za-z0-9]"))
        .filter { it.isNotBlank() }
        .joinToString("") { it.replaceFirstChar(Char::uppercase) }

    tasks.register<GenerateTask>(taskName) {
        generatorName.set("spring")
        inputSpec.set(specFile.absolutePath)
        // важно: String, один общий outDir
        outputDir.set(outRoot.get().asFile.absolutePath)

        val base = "net.proselyte.${name.substringBefore('-').lowercase()}"
        configOptions.set(
            mapOf(
                "library" to "spring-cloud",
                "skipDefaultInterface" to "true",
                "useBeanValidation" to "true",
                "openApiNullable" to "false",
                "useFeignClientUrl" to "true",
                "useTags" to "true",
                "apiPackage" to "$base.api",
                "modelPackage" to "$base.dto",
                "configPackage" to "$base.config"
            )
        )
        doFirst {
            logger.lifecycle("$taskName: starting generation from ${specFile.name}")
        }
    }
}

val generateAllOpenApi = tasks.register("generateAllOpenApi") {
    dependsOn(generateTasks)
    doLast { logger.lifecycle("generateAllOpenApi: all specifications have been generated") }
}

configure<SourceSetContainer> {
    named(SourceSet.MAIN_SOURCE_SET_NAME) {
        java.srcDir(outRoot.map { it.dir("src/main/java") })
    }
}

tasks.named("compileJava") {
    dependsOn(generateAllOpenApi)
}

idea {
    module {
        val genDir = outRoot.map { it.dir("src/main/java").asFile }.get()
        generatedSourceDirs = generatedSourceDirs + setOf(genDir)
        sourceDirs = sourceDirs + setOf(genDir)
    }
}

/*
──────────────────────────────────────────────────────
============== Resolve NEXUS credentials =============
──────────────────────────────────────────────────────
*/

file(".env").takeIf { it.exists() }?.readLines()?.forEach {
    val (k, v) = it.split("=", limit = 2)
    System.setProperty(k.trim(), v.trim())
    logger.lifecycle("${k.trim()}=${v.trim()}")
}

val nexusUrl = System.getenv("NEXUS_URL") ?: System.getProperty("NEXUS_URL")
val nexusUser = System.getenv("NEXUS_USERNAME") ?: System.getProperty("NEXUS_USERNAME")
val nexusPassword = System.getenv("NEXUS_PASSWORD") ?: System.getProperty("NEXUS_PASSWORD")

if (nexusUrl.isNullOrBlank() || nexusUser.isNullOrBlank() || nexusPassword.isNullOrBlank()) {
    throw GradleException(
        "NEXUS_URL or NEXUS_USER or NEXUS_PASSWORD not set. " +
                "Please create a .env file with these properties or set environment variables."
    )
}

repositories {
    mavenCentral()
    maven {
        url = uri(nexusUrl)
        isAllowInsecureProtocol = true
        credentials {
            username = nexusUser
            password = nexusPassword
        }
    }
}