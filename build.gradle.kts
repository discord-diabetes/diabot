import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("java")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
    alias(libs.plugins.detekt)
    alias(libs.plugins.release)
    alias(libs.plugins.versions)
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

val reportMerge by tasks.registering(io.gitlab.arturbosch.detekt.report.ReportMergeTask::class) {
    output.set(rootProject.layout.buildDirectory.file("reports/detekt/merge.sarif")) // or "reports/detekt/merge.sarif"
}

allprojects {
    group = "com.dongtronic.diabot"

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

    kotlin {
        jvmToolchain(23)
        compilerOptions {
            javaParameters.set(true)
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        systemProperty("junit.jupiter.testinstance.lifecycle.default", "per_class")
    }

    detekt {
        buildUponDefaultConfig = true // preconfigure defaults
        allRules = false // activate all available (even unstable) rules.
        config.setFrom("$rootDir/config/detekt/detekt.yml")
        // point to your custom config defining rules to run, overwriting default behavior
        baseline = file("config/detekt/baseline.xml")
        autoCorrect = true

        source.setFrom(
            "src/main/java",
            "src/test/java",
        )
    }

    tasks.withType<io.gitlab.arturbosch.detekt.Detekt> {
        reports {
            html.required.set(true)
            html.outputLocation.set(file("build/reports/detekt.html"))

            txt.required.set(true)
            txt.outputLocation.set(file("build/reports/detekt.txt"))

            sarif.required.set(true)
            sarif.outputLocation.set(file("build/reports/detekt.sarif"))

            md.required.set(false)
            xml.required.set(false)
        }

        finalizedBy(reportMerge)

        reportMerge {
            input.from(sarifReportFile)
        }
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
    }
}
