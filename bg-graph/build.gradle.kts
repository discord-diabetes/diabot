dependencies {
    // Graph Generation
    api(project(":nightscout-api"))
    api(libs.xchart)

    // Misc
    implementation(project(":utilities"))
    implementation(libs.reactor.core)
    implementation(libs.reactor.kotlin.extensions)
}
