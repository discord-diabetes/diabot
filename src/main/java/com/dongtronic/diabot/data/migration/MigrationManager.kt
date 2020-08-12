package com.dongtronic.diabot.data.migration

import com.dongtronic.diabot.util.logger
import reactor.core.publisher.Flux

class MigrationManager {
    private val logger = logger()
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

    fun migrateIfNecessary() {
        val migrate = System.getenv()["REDIS_MONGO_MIGRATE"]?.toBoolean() ?: false
        if (migrate) {
            logger.info("Beginning migration")
            migrateAll().blockLast()
        }
    }

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