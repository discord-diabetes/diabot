dependencies {
    // JDA
    implementation(libs.jda.ktx)
    implementation(libs.jda.chewtils)
    implementation(libs.jda.commands)
    implementation(libs.jda) {
        exclude(module = "opus-java")
    }

    // Logging
    implementation(libs.logback.classic)

    // Parsing
    implementation(libs.jsoup)

    // Apache Commons
    implementation(libs.commons.lang3)
    implementation(libs.commons.configuration2)

    // Networking
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.jackson)
    implementation(libs.retrofit.reactor)

    // Database
    implementation(libs.jedis)
    implementation(libs.mongodb.reactivestreams)
    implementation(libs.kmongo.async)

    // Database Migrations
    implementation(libs.mongock.standalone)
    implementation(libs.mongock.driver)
    implementation(libs.kmongo)

    // Cache
    implementation(libs.caffeine)

    // Misc
    implementation(project(":utilities"))
    implementation(project(":nightscout-api"))
    implementation(project(":bg-graph"))
    implementation(libs.reactor.core)
    implementation(libs.reactor.kotlin.extensions)
}
