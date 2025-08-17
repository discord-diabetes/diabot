// Mongock v5 deprecated ChangeLog and ChangeSet, but recommends keeping existing uses the same.
@file:Suppress("DEPRECATION")

package com.dongtronic.diabot.data.migration.redis

import com.dongtronic.diabot.data.migration.MigrationManager
import com.dongtronic.diabot.data.mongodb.RewardsDAO
import com.dongtronic.diabot.data.mongodb.RewardsDTO
import com.dongtronic.diabot.util.logger
import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import redis.clients.jedis.Jedis

@ChangeLog(order = "005")
class RewardMigrator {
    private val mongo = RewardsDAO.instance
    private val redis by lazy { com.dongtronic.diabot.data.redis.RewardDAO.getInstance() }
    private val jedis by lazy { Jedis(System.getenv("REDIS_URL")) }
    private val logger = logger()

    fun needsMigration(): Boolean {
        if (!MigrationManager.canRedisMigrate()) return false

        return mongo.rewards.countDocuments().toMono().map {
            return@map it == 0L || getAllRewards().size.toLong() > it
        }.block()!!
    }

    @ChangeSet(order = "001", id = "redisRewards", author = "Garlic")
    fun migrate() {
        if (!needsMigration()) return

        getAllRewards().toFlux()
            .flatMap { mongo.importReward(it) }
            .map { it.wasAcknowledged() }
            .onErrorContinue { t, u ->
                logger.warn("Could not import reward: $u", t)
            }
            .filter { it }
            .blockLast()!!
    }

    /**
     * Gets a list of all the rewards in Redis
     */
    private fun getAllRewards(): List<RewardsDTO> {
        return jedis.keys("*:simplerewards")
            // grab the guild ID only (before `:`)
            .map { it.substringBefore(":") }
            .toSet()
            .flatMap { guildId ->
                val guildRewards = redis.getSimpleRewards(guildId)!!.toList()
                buildRewards(guildId, guildRewards)
            }
    }

    /**
     * Converts a guild's rewards into a list of [RewardsDTO] objects.
     *
     * @param guildId The guild ID these rewards belong to
     * @param rewards A list of `requiredRole:rewardRole` strings
     * @return List of [RewardsDTO] objects belonging to a guild
     */
    private fun buildRewards(guildId: String, rewards: List<String>): List<RewardsDTO> {
        val mutableRewardMap = mutableMapOf<String, List<String>>()

        rewards.forEach {
            val required = it.substringBefore(":")
            val reward = it.substringAfter(":")

            // build a list of rewards for each required role
            mutableRewardMap.merge(required, listOf(reward)) { old: List<String>, new: List<String> ->
                // append reward role if the required role has several rewards
                old.plus(new)
            }
        }

        // convert all the rewards to RewardsDTO
        return mutableRewardMap.map { RewardsDTO(guildId, it.key, it.value) }
    }
}
