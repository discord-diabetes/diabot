package com.dongtronic.diabot.data.migration

import com.dongtronic.diabot.data.mongodb.NameRuleDAO
import com.dongtronic.diabot.data.mongodb.NameRuleDTO
import com.dongtronic.diabot.util.logger
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import redis.clients.jedis.Jedis

class UsernamesMigrator : Migrator {
    private val redis = com.dongtronic.diabot.data.redis.AdminDAO.getInstance()
    private val mongo = NameRuleDAO.instance
    private val jedis: Jedis = Jedis(System.getenv("REDIS_URL"))
    private val logger = logger()

    override fun needsMigration(): Mono<Boolean> {
        return mongo.collection.countDocuments().toMono().map {
            return@map it == 0L || getAllGids().size.toLong() > it
        }
    }

    override fun migrate(): Flux<Long> {
        val dtos = getAllGids().mapNotNull { guildId ->
            val enforcing = redis.getUsernameEnforcementEnabled(guildId)
            val hint = redis.getUsernameHint(guildId)
            val pattern = redis.getUsernamePattern(guildId)

            if (!enforcing
                    && hint.isNullOrEmpty()
                    && pattern.isNullOrEmpty()) {
                return@mapNotNull null
            }

            NameRuleDTO(
                    guildId = guildId,
                    enforce = enforcing,
                    pattern = pattern ?: "",
                    hintMessage = hint ?: ""
            )
        }

        return dtos.toFlux()
                .flatMap { mongo.addGuild(it) }
                .map { it.wasAcknowledged() }
                .onErrorContinue { t, u ->
                    logger.warn("Could not import guild username rule: $u", t)
                }
                .filter { it }
                .count()
                .toFlux()
    }

    fun getAllKeys(): Set<String> {
        return jedis.keys("*:*username*") ?: emptySet()
    }

    fun getAllGids(): Set<String> {
        return getAllKeys().map { it.substringBefore(":") }.toSet()
    }
}