plugins {
    application
    kotlin("jvm") version "1.9.23"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("maven-publish")
}

group = "cc.suffro"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.jetbrains.space/data2viz/p/maven/public")
    }
}

buildscript {
    repositories {
        maven {
            setUrl("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("org.openjfx:javafx-plugin:0.1.0")
    }
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.graphics", "javafx.media")
}

application {
    mainClass.set("cc.suffro.bpmanalyzer.Main")
}

object Version {
    const val ASSERTK = "0.25"
    const val COROUTINES = "1.7.0-Beta"
    const val JDBC = "3.45.1.0"
    const val JUNIT = "5.10.1"
    const val KOIN = "3.5.3"
    const val KOIN_TEST = "3.5.3"
    const val KOTLIN_MATH = "1.0"
    const val KOTLINX_CLI = "0.3.6"
    const val LOGBACK = "1.2.3"
    const val LOGGING = "3.0.2"
    const val SLF4J = "2.0.3"
    const val VIZ = "0.9.1"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(kotlin("test-junit5"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Version.COROUTINES}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:${Version.COROUTINES}")

    implementation("org.xerial:sqlite-jdbc:${Version.JDBC}")

    api("io.insert-koin:koin-core:${Version.KOIN}")
    implementation("io.insert-koin:koin-test:${Version.KOIN_TEST}") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-test-junit")
    }

    implementation("org.jetbrains.kotlinx:kotlinx-cli:${Version.KOTLINX_CLI}")

    implementation("org.kotlinmath:complex-numbers:${Version.KOTLIN_MATH}")

    implementation("io.github.microutils:kotlin-logging-jvm:${Version.LOGGING}")

    implementation("org.slf4j:slf4j-simple:${Version.SLF4J}")

    implementation("io.data2viz.d2v:d2v-axis:${Version.VIZ}")
    implementation("io.data2viz.d2v:d2v-color:${Version.VIZ}")
    implementation("io.data2viz.d2v:d2v-delaunay:${Version.VIZ}")
    implementation("io.data2viz.d2v:d2v-dsv:${Version.VIZ}")
    implementation("io.data2viz.d2v:d2v-ease:${Version.VIZ}")
    implementation("io.data2viz.d2v:d2v-force:${Version.VIZ}")
    implementation("io.data2viz.d2v:d2v-format:${Version.VIZ}")
    implementation("io.data2viz.d2v:d2v-scale:${Version.VIZ}")
    implementation("io.data2viz.d2v:d2v-shape:${Version.VIZ}")
    implementation("io.data2viz.d2v:d2v-viz:${Version.VIZ}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${Version.JUNIT}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${Version.JUNIT}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Version.JUNIT}")

    runtimeOnly("ch.qos.logback:logback-classic:${Version.LOGBACK}")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    withType<Test> {
        useJUnitPlatform()
        minHeapSize = "512m"
        maxHeapSize = "4096m"
    }
}

publishing {
    publications {
        create<MavenPublication>("kymatik") {
            from(components["java"])

            group = "cc.suffro"
            artifactId = "kymatik"
            version = "0.1.0"

            pom {
                name.set("BPM Analyzer")
                description.set("A Kotlin library for audio analysis: FFT, pitch shifting and accurate BPM detection for audio files.")
                url.set("https://example.com/my-library")

                licenses {
                    license {
                        name.set("The MIT License")
                    }
                }

                developers {
                    developer {
                        id.set("xsoophx")
                        name.set("Sophia Köhler")
                        email.set("ccsophia.koehler@gmail.com")
                    }
                }

                scm {
                    // url.set("https://example.com/my-library.git")
                }
            }
        }
    }

    repositories {
        mavenLocal()
    }
}

kotlin {
    sourceSets {
        main {
            kotlin.srcDirs("src/main/kotlin")
        }
        test {
            kotlin.srcDirs("src/test/kotlin")
        }
    }
}
