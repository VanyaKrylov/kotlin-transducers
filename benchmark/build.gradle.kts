plugins {
    kotlin("multiplatform")
    id("kotlinx.benchmark") version("0.2.0-dev-20")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.apply {
                jvmTarget = "1.6"
            }
        }
    }

    js {
        nodejs()
        /*browser {
            testTask {
                useKarma {
                    usePhantomJS()
                }
            }
        }*/
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx.benchmark.runtime:0.2.0-dev-20")
                api(project(":core"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
    }
}

benchmark {
    /*configurations {
        named("main") {
            iterations = 20
        }
    }*/
    targets {
        register("jvm")
        register("js")
        register("linuxX64")
    }
}