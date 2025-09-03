// Mongock v5 deprecated ChangeLog and ChangeSet, but recommends keeping existing uses the same.
@file:Suppress("DEPRECATION")

package com.dongtronic.diabot.data.migration.redis

import com.dongtronic.diabot.data.migration.MigrationManager
import com.dongtronic.diabot.data.mongodb.RewardOptOutsDTO
import com.dongtronic.diabot.data.mongodb.RewardsDAO
import com.dongtronic.diabot.util.logger
import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import redis.clients.jedis.Jedis

@ChangeLog(order = "006")
@Deprecated(level = DeprecationLevel.WARNING, message = "Support for Redis will be removed in Diabot version 2")
class RewardOptOutMigrator {
    private val mongo = RewardsDAO.instance
    private val redis by lazy { com.dongtronic.diabot.data.redis.RewardDAO.getInstance() }
    private val jedis by lazy { Jedis(System.getenv("REDIS_URL")) }
    private val logger = logger()

    fun needsMigration(): Boolean {
        if (!MigrationManager.canRedisMigrate()) return false

        return mongo.optOuts.countDocuments().toMono().map {
            return@map it == 0L || getAllRewardOptOuts().size.toLong() > it
        }.block()!!
    }

    @ChangeSet(order = "001", id = "redisRewardOptOuts", author = "Garlic")
    fun migrate() {
        if (!needsMigration()) return

        getAllRewardOptOuts().toFlux()
            .flatMap { mongo.importOptOuts(it) }
            .map { it.wasAcknowledged() }
            .onErrorContinue { t, u ->
                logger.warn("Could not import reward opt-outs: $u", t)
            }
            .filter { it }
            .blockLast()!!
    }

    /**
     * Gets all the guilds who have users opted-out for rewards
     */
    private fun getAllRewardOptOuts(): List<RewardOptOutsDTO> {
        return jedis.keys("*:rewardoptouts")
            .map { it.substringBefore(":") }
            .toSet()
            .map { RewardOptOutsDTO(it, redis.getOptOuts(it)!!) }
    }
}
