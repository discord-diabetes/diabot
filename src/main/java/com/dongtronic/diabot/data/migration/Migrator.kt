package com.dongtronic.diabot.data.migration

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface Migrator {
    /**
     * Calls `needsMigration` and proceeds with calling `migrate` if it returns true.
     *
     * @return The number of data objects migrated. This will be empty if `needsMigration` was false
     */
    fun checkAndMigrate(): Flux<Long> {
        return needsMigration().flatMapMany { needsMigrating ->
            if (needsMigrating) {
                migrate()
            } else {
                Flux.empty()
            }
        }
    }

    /**
     * Checks if any data needs to be migrated.
     *
     * @return Whether there is data which needs to be migrated or not
     */
    fun needsMigration(): Mono<Boolean>

    /**
     * Attempts to migrate data.
     *
     * @return The number of data objects migrated
     */
    fun migrate(): Flux<Long>
}