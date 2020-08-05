package com.dongtronic.diabot.data.migration

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface Migrator {
    fun checkAndMigrate(): Flux<Long>

    fun needsMigration(): Mono<Boolean>

    fun migrate(): Flux<Long>
}