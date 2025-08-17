group = "com.dongtronic"

dependencies {
    // Networking
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.jackson)
    implementation(libs.retrofit.reactor)

    api(libs.kmongo.shared)

    // https://mvnrepository.com/artifact/com.fasterxml.jackson.module/jackson-module-kotlin
    api(libs.jackson.module.kotlin)

    // Misc
    implementation(project(":utilities"))
    implementation(libs.reactor.core)
    implementation(libs.reactor.kotlin.extensions)
}
