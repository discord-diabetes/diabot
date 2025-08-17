import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
    alias(libs.plugins.detekt)
    alias(libs.plugins.release)
    alias(libs.plugins.versions)
}

val reportMerge by tasks.registering(io.gitlab.arturbosch.detekt.report.ReportMergeTask::class) {
    output.set(rootProject.layout.buildDirectory.file("reports/detekt/merge.sarif")) // or "reports/detekt/merge.sarif"
}

// From https://github.com/ben-manes/gradle-versions-plugin#rejectversionsif-and-componentselection
// Inverted check(to be `isStable` for clarity)
fun isStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable
}

// https://github.com/ben-manes/gradle-versions-plugin
tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        // Exclude unstable dependency versions from update check(er)
        !isStable(candidate.version) && isStable(currentVersion)
    }
}

allprojects {
    val libs = rootProject.libs
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://m2.chew.pro/releases")
    }

    dependencies {
        implementation(libs.kotlin.stdlib)
        implementation(libs.kotlin.reflect)
        implementation(libs.coroutines.core)
        implementation(libs.coroutines.reactor)

        add("detekt", libs.detekt.formatting)
        add("detekt", libs.detekt.cli)

        testImplementation(libs.junit.engine)
        testImplementation(libs.junit.params)
        testRuntimeOnly(libs.junit.platform)

        api(libs.slf4j.api)
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        systemProperty("junit.jupiter.testinstance.lifecycle.default", "per_class")
    }

    java {
        targetCompatibility = JavaVersion.VERSION_21
        sourceCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    group = "com.dongtronic.diabot"

    detekt {
        buildUponDefaultConfig = true // preconfigure defaults
        allRules = false // activate all available (even unstable) rules.
        config.setFrom("$rootDir/config/detekt/detekt.yml")
        // point to your custom config defining rules to run, overwriting default behavior
        baseline = file("config/detekt/baseline.xml")

        source.setFrom(
            "src/main/java",
            "src/test/java",
//            "bot/src/main/java",
//            "bg-graph/src/main/java",
//            "nightscout-api/src/main/java",
//            "utilities/src/main/java",
        )

        autoCorrect = true

        reports {
            html.required.set(true) // observe findings in your browser with structure and code snippets
            xml.required.set(false) // checkstyle like format mainly for integrations like Jenkins
            txt.required.set(true)
            sarif.required.set(true)
        }
    }

    reportMerge {
        input.from(tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().map { it.reports.sarif.outputLocation }) // or sarif.outputLocation
    }
}

tasks.register("stage") {
    dependsOn("clean", "shadowJar")
}

tasks.shadowJar {
    exclude("logback-test.xml")
    archiveBaseName.set("diabot")
    archiveClassifier.set("")
    archiveVersion.set("")
}

tasks.jar {
    manifest {
        attributes["Implementation-Title"] = "Diabot - a diabetes Discord bot"
        attributes["Implementation-Version"] = project.version
        attributes["Main-Class"] = "com.dongtronic.diabot.Main"
    }
}

release {
    tagTemplate = "v$version"
    git {
        requireBranch = "main"
        signTag = true
        ignoredSnapshotDependencies = listOf("pw.chew:jda-chewtils")
    }
}

dependencies {
    implementation(project(path = ":bot"))
}
