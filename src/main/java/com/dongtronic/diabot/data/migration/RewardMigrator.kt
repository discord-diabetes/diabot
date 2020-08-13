package com.dongtronic.diabot.data.migration

import com.dongtronic.diabot.data.mongodb.RewardsDAO
import com.dongtronic.diabot.data.mongodb.RewardsDTO
import com.dongtronic.diabot.util.logger
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import redis.clients.jedis.Jedis

class RewardMigrator : Migrator {
    private val redis = com.dongtronic.diabot.data.redis.RewardDAO.getInstance()
    private val mongo = RewardsDAO.instance
    private val jedis: Jedis = Jedis(System.getenv("REDIS_URL"))
    private val logger = logger()

    override fun needsMigration(): Mono<Boolean> {
        return mongo.rewards.countDocuments().toMono().map {
            return@map it == 0L || getAllRewards().size.toLong() > it
        }
    }

    override fun migrate(): Flux<Long> {
        return getAllRewards().toFlux()
                .flatMap { mongo.importReward(it) }
                .map { it.wasAcknowledged() }
                .onErrorContinue { t, u ->
                    logger.warn("Could not import reward: $u", t)
                }
                .filter { it }
                .count()
                .toFlux()
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

        // convert all of the rewards to RewardsDTO
        return mutableRewardMap.map { RewardsDTO(guildId, it.key, it.value) }
    }
}