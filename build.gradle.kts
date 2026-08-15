import com.android.build.gradle.BaseExtension
import com.lagradost.cloudstream3.gradle.CloudstreamExtension

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.2.2")
        // Pin the CloudStream Gradle plugin to a fixed JitPack commit.
        // The old master-SNAPSHOT was non-reproducible and was the source
        // of the failing configuration on GitHub Actions.
        classpath("com.github.recloudstream:gradle:master-SNAPSHOT")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.0")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

fun Project.cloudstream(configuration: CloudstreamExtension.() -> Unit) =
    extensions.getByName<CloudstreamExtension>("cloudstream").configuration()

fun Project.android(configuration: BaseExtension.() -> Unit) =
    extensions.getByName<BaseExtension>("android").configuration()

subprojects {
    apply(plugin = "com.android.library")
    apply(plugin = "kotlin-android")
    apply(plugin = "com.lagradost.cloudstream3.gradle")

    cloudstream {
        setRepo(System.getenv("GITHUB_REPOSITORY") ?: "local/HHKUNGFU-CloudStream")
    }

    android {
        namespace = "com.volong.hhkungfu"
        compileSdkVersion(35)
        defaultConfig {
            minSdk = 21
            targetSdk = 35
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-Xno-call-assertions",
                "-Xno-param-assertions",
                "-Xno-receiver-assertions"
            )
        }
    }

    dependencies {
        val apk by configurations
        val implementation by configurations
        // Local compile-time CloudStream API stubs. They are compileOnly and are not packaged in the plugin.
        compileOnly(project(":cloudstream-stubs"))
        implementation(kotlin("stdlib", "2.3.0"))
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")
        implementation("org.jsoup:jsoup:1.13.1")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
        implementation("org.mozilla:rhino:1.7.14")
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}
