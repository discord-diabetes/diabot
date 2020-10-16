package com.dongtronic.diabot.data.migration

import com.dongtronic.diabot.util.logger
import reactor.core.publisher.Flux

class MigrationManager {
    private val logger = logger()
    // lazy init to prevent creating all of the MongoDB connections when this class is initialised
    private val migrators by lazy {
        listOf(
                AdminChannelMigrator(),
                NightscoutMigrator(),
                NSChannelMigrator(),
                ProjectsMigrator(),
                RewardMigrator(),
                RewardOptOutMigrator(),
                UsernamesMigrator()
        )
    }

    /**
     * Checks if migration is enabled and starts the data migration process if so.
     */
    fun migrateIfNecessary() {
        val migrate = System.getenv()["REDIS_MONGO_MIGRATE"]?.toBoolean() ?: false

        if (migrate) {
            logger.info("Beginning migration")
            // block this thread until a completion signal is sent
            migrateAll().blockLast()
            logger.info("Migration finished")
        }
    }

    /**
     * Starts all the migrators.
     *
     * @return The result of each migrator
     */
    // erroneous warning
    @Suppress("ReactiveStreamsUnusedPublisher")
    fun migrateAll(): Flux<Long> {
        var migration = Flux.empty<Long>()
        migrators.forEachIndexed { i: Int, migrator: Migrator ->
            val toMigrate = migrator.checkAndMigrate().doOnNext {
                logger.info("Migrated $it object(s) from ${migrator::class.simpleName}")
            }

            migration = if (i == 0) {
                toMigrate
            } else {
                migration.thenMany(toMigrate)
            }
        }

        return migration
    }
}