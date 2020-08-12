package com.dongtronic.diabot.data.migration

import com.dongtronic.diabot.data.mongodb.RewardOptOutsDTO
import com.dongtronic.diabot.data.mongodb.RewardsDAO
import com.dongtronic.diabot.util.logger
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import redis.clients.jedis.Jedis

class RewardOptOutMigrator : Migrator {
    private val redis = com.dongtronic.diabot.data.redis.RewardDAO.getInstance()
    private val mongo = RewardsDAO.instance
    private val jedis: Jedis = Jedis(System.getenv("REDIS_URL"))
    private val logger = logger()

    override fun checkAndMigrate(): Flux<Long> {
        return needsMigration().flatMapMany { needsMigrating ->
            if (needsMigrating) {
                migrate()
            } else {
                Flux.empty()
            }
        }
    }

    override fun needsMigration(): Mono<Boolean> {
        return mongo.optOuts.countDocuments().toMono().map {
            return@map it == 0L || getAllRewardOptOuts().size.toLong() > it
        }
    }

    override fun migrate(): Flux<Long> {
        return getAllRewardOptOuts().toFlux()
                .flatMap { mongo.importOptOuts(it) }
                .map { it.insertedId }
                .onErrorContinue { t, u ->
                    logger.warn("Could not import reward opt-outs: $u", t)
                }
                .filter { it != null }
                .count()
                .toFlux()
    }

    fun getAllKeys(): Set<String> {
        return jedis.keys("*:rewardoptouts") ?: emptySet()
    }

    fun getAllRewardOptOuts(): List<RewardOptOutsDTO> {
        return getAllKeys()
                .map { it.substringBefore(":") }
                .toSet()
                .map {
                    RewardOptOutsDTO(it, redis.getOptOuts(it)!!)
                }
    }
}