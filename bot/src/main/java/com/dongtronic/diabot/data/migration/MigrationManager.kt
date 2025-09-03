package com.dongtronic.diabot.data.migration

import com.dongtronic.diabot.util.MongoDB
import com.dongtronic.diabot.util.logger
import com.mongodb.client.MongoClients
import io.mongock.driver.mongodb.sync.v4.driver.MongoSync4Driver
import io.mongock.runner.standalone.MongockStandalone

class MigrationManager {
    private val logger = logger()

    /**
     * Starts the data migration process
     */
    fun initialize() {
        val mongoClient = MongoClients.create(MongoDB.connectionURI)
        val driver = MongoSync4Driver.withDefaultLock(mongoClient, MongoDB.defaultDatabase)
        driver.disableTransaction()
        driver.migrationRepositoryName = "mongock-changelog"
        driver.lockRepositoryName = "mongock-lock"
        MongockStandalone.builder()
            .setDriver(driver)
            .addMigrationScanPackage("com.dongtronic.diabot.data.migration")
            .buildRunner()
            .execute()
    }

    companion object {
        /**
         * Check if Redis -> Mongo migration is enabled and that a Redis URL is set in the environment variables.
         *
         * @return If Redis migration is possible
         */
        @Deprecated(level = DeprecationLevel.WARNING, message = "Support for Redis will be removed in Diabot version 2")
        fun canRedisMigrate(): Boolean {
            return System.getenv()["REDIS_MONGO_MIGRATE"]?.toBoolean() == true && System.getenv().containsKey("REDIS_URL")
        }
    }
}
