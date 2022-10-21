package com.dongtronic.diabot.data.migration.redis

import com.dongtronic.diabot.data.migration.MigrationManager
import com.dongtronic.diabot.data.mongodb.NightscoutDAO
import com.dongtronic.diabot.data.mongodb.NightscoutUserDTO
import com.dongtronic.diabot.util.logger
import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import redis.clients.jedis.Jedis

@ChangeLog(order = "002")
class NightscoutMigrator {
    private val mongo = NightscoutDAO.instance
    private val redis by lazy { com.dongtronic.diabot.data.redis.NightscoutDAO.getInstance() }
    private val jedis by lazy { Jedis(System.getenv("REDIS_URL")) }
    private val logger = logger()

    fun needsMigration(): Boolean {
        if (!MigrationManager.canRedisMigrate()) return false

        return mongo.collection.countDocuments().toMono().map {
            return@map it == 0L || getAllUids().size.toLong() > it
        }.block()!!
    }

    @ChangeSet(order = "001", id = "redisNightscout", author = "Garlic")
    fun migrate() {
        if (!needsMigration()) return

        val dtos = getAllUids().mapNotNull { userId ->
            val url = redis.getNightscoutUrl(userId)
            val token = redis.getNightscoutToken(userId)
            val display = redis.getNightscoutDisplay(userId).split(" ")
            val public = getPublicGuilds(userId).toList()

            // if all of their data is empty then skip them
            if (url == null &&
                    token == null &&
                    display.isEmpty() &&
                    public.isEmpty()
            ) {
                return@mapNotNull null
            }

            NightscoutUserDTO(
                    userId = userId,
                    url = url,
                    token = token,
                    displayOptions = display,
                    publicGuilds = public
            )
        }

        dtos.toFlux()
                .flatMap { mongo.addUser(it) }
                .map { it.wasAcknowledged() }
                .onErrorContinue { t, u ->
                    logger.warn("Could not import nightscout: $u", t)
                }
                .filter { it }
                .blockLast()!!
    }

    /**
     * Gets a list of the guilds which a user has set their NS to public in, from redis
     */
    private fun getPublicGuilds(userId: String): Set<String> {
        return jedis.keys("$userId:*:nightscoutpublic")
                .map { it.split(":")[1] }
                .toSet()
    }

    /**
     * Gets a list of all the user IDs which have any form of Nightscout data saved in redis
     */
    private fun getAllUids(): Set<String> {
        return jedis.keys("*:nightscout*")
                .map { it.substringBefore(":") }
                .toSet()
    }
}
