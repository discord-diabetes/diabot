// Mongock v5 deprecated ChangeLog and ChangeSet, but recommends keeping existing uses the same.
@file:Suppress("DEPRECATION")

package com.dongtronic.diabot.data.migration.redis

import com.dongtronic.diabot.data.migration.MigrationManager
import com.dongtronic.diabot.data.mongodb.NameRuleDAO
import com.dongtronic.diabot.data.mongodb.NameRuleDTO
import com.dongtronic.diabot.util.logger
import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import redis.clients.jedis.Jedis

@ChangeLog(order = "007")
class UsernamesMigrator {
    private val mongo = NameRuleDAO.instance
    private val redis by lazy { com.dongtronic.diabot.data.redis.AdminDAO.getInstance() }
    private val jedis by lazy { Jedis(System.getenv("REDIS_URL")) }
    private val logger = logger()

    fun needsMigration(): Boolean {
        if (!MigrationManager.canRedisMigrate()) return false

        return mongo.collection.countDocuments().toMono().map {
            return@map it == 0L || getAllGids().size.toLong() > it
        }.block()!!
    }

    @ChangeSet(order = "001", id = "redisUsernameRules", author = "Garlic")
    fun migrate() {
        if (!needsMigration()) return

        val dtos = getAllGids().mapNotNull { guildId ->
            val enforcing = redis.getUsernameEnforcementEnabled(guildId)
            val hint = redis.getUsernameHint(guildId)
            val pattern = redis.getUsernamePattern(guildId)

            // if all the data is useless then skip this guild
            if (!enforcing
                && hint.isNullOrEmpty()
                && pattern.isNullOrEmpty()
            ) {
                return@mapNotNull null
            }

            NameRuleDTO(
                guildId = guildId,
                enforce = enforcing,
                pattern = pattern ?: "",
                hintMessage = hint ?: ""
            )
        }

        dtos.toFlux()
            .flatMap { mongo.addGuild(it) }
            .map { it.wasAcknowledged() }
            .onErrorContinue { t, u ->
                logger.warn("Could not import guild username rule: $u", t)
            }
            .filter { it }
            .blockLast()!!
    }

    /**
     * Gets all the guild IDs with username rules
     */
    private fun getAllGids(): Set<String> {
        return jedis.keys("*:*username*")
            .map { it.substringBefore(":") }
            .toSet()
    }
}
