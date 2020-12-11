import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform") version "1.4.21"
    id("kotlinx.benchmark") version "0.2.0-dev-20"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.4.21"
}

repositories {
    maven(url = "https://dl.bintray.com/kotlin/kotlinx")
}

group = "me.root"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm {}
    js {
        nodejs()
    }

    sourceSets() {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
                implementation("org.jetbrains.kotlinx:kotlinx.benchmark.runtime:0.2.0-dev-20")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
            }
        }
    }
}
/*dependencies {
    testImplementation(kotlin("test-junit"))
}*/
tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}