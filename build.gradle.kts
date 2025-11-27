import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.kotlin.kapt") version "1.9.25"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.25"
    id("com.gradleup.shadow") version "8.3.9"
    id("io.micronaut.application") version "4.5.3"
    id("io.micronaut.aot") version "4.5.3"
}

group = "no.nav.hm"
version = properties["version"] ?: "local-build"

val kotlinVersion = project.properties["kotlinVersion"]
val poiVersion = "5.5.0"
val jvmTarget = "17"
val kotestVersion = "5.9.1"
val micrometerRegistryPrometheusVersion = "1.16.0"
val grunndataDtoVersion = "202511170912"
val hmLeaderElectionVersion = "202506021230"
val log4jVersion = "2.25.2"
val mockkVersion = "1.14.2"

dependencies {
    kapt("io.micronaut.data:micronaut-data-processor")
    kapt("io.micronaut:micronaut-http-validation")
    kapt("io.micronaut.openapi:micronaut-openapi")
    kapt("io.micronaut.serde:micronaut-serde-processor")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("io.micronaut:micronaut-jackson-databind")

    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive")
    implementation("org.apache.poi:poi:$poiVersion")
    implementation("org.apache.poi:poi-ooxml:$poiVersion")
    compileOnly("io.micronaut:micronaut-http-client")
    compileOnly("io.micronaut.openapi:micronaut-openapi-annotations")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("org.yaml:snakeyaml")

    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")

    implementation("io.micronaut.micrometer:micronaut-micrometer-core")
    implementation("io.micronaut.micrometer:micronaut-micrometer-registry-prometheus")
    implementation("io.micronaut:micronaut-management")

    implementation("no.nav.hm.grunndata:hm-grunndata-rapid-dto:$grunndataDtoVersion")

    implementation("com.github.navikt:hm-micronaut-leaderelection:$hmLeaderElectionVersion")

    testImplementation("io.micronaut:micronaut-http-client")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")


}


application {
    mainClass = "no.nav.hm.finnhjelpemiddelbff.Application"
}

java {
    sourceCompatibility = JavaVersion.toVersion(jvmTarget)
    targetCompatibility = JavaVersion.toVersion(jvmTarget)
    withSourcesJar()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = jvmTarget
}

tasks.named<KotlinCompile>("compileTestKotlin") {
    kotlinOptions.jvmTarget = jvmTarget
}

tasks.named<ShadowJar>("shadowJar") {
    isZip64 = true
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("no.nav.hm.finnhjelpemiddelbff.*")
    }
    aot {
        // Please review carefully the optimizations enabled below
        // Check https://micronaut-projects.github.io/micronaut-aot/latest/guide/ for more details
        optimizeServiceLoading = false
        convertYamlToJava = false
        precomputeOperations = true
        cacheEnvironment = true
        optimizeClassLoading = true
        deduceEnvironment = true
        optimizeNetty = true
        replaceLogbackXml = true
    }
}

sourceSets {
    main {
        java {
            srcDirs("src/main/kotlin")
        }
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
}
