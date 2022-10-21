package com.dongtronic.diabot.data.migration

import com.dongtronic.diabot.util.MongoDB
import com.dongtronic.diabot.util.logger
import com.github.cloudyrock.mongock.driver.mongodb.sync.v4.driver.MongoSync4Driver
import com.github.cloudyrock.standalone.MongockStandalone
import com.mongodb.client.MongoClients

class MigrationManager {
    private val logger = logger()

    /**
     * Starts the data migration process
     */
    fun initialize() {
        val mongoClient = MongoClients.create(MongoDB.connectionURI)
        val driver = MongoSync4Driver.withDefaultLock(mongoClient, MongoDB.defaultDatabase)
        driver.disableTransaction()
        driver.changeLogRepositoryName = "mongock-changelog"
        driver.lockRepositoryName = "mongock-lock"
        MongockStandalone.builder()
                .setDriver(driver)
                .addChangeLogsScanPackage("com.dongtronic.diabot.data.migration")
                .buildRunner()
                .execute()
    }

    companion object {
        /**
         * Check if Redis -> Mongo migration is enabled and that a Redis URL is set in the environment variables.
         *
         * @return If Redis migration is possible
         */
        fun canRedisMigrate(): Boolean {
            return System.getenv()["REDIS_MONGO_MIGRATE"]?.toBoolean() == true && System.getenv().containsKey("REDIS_URL")
        }
    }
}
