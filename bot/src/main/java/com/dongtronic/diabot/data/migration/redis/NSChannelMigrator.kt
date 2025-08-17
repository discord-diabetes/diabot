// Mongock v5 deprecated ChangeLog and ChangeSet, but recommends keeping existing uses the same.
@file:Suppress("DEPRECATION")

package com.dongtronic.diabot.data.migration.redis

import com.dongtronic.diabot.data.migration.MigrationManager
import com.dongtronic.diabot.data.mongodb.ChannelDAO
import com.dongtronic.diabot.data.mongodb.ChannelDTO
import com.dongtronic.diabot.data.redis.NightscoutDAO
import com.dongtronic.diabot.util.logger
import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import org.litote.kmongo.contains
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import redis.clients.jedis.Jedis

@ChangeLog(order = "003")
class NSChannelMigrator {
    private val mongo = ChannelDAO.instance
    private val redis by lazy { NightscoutDAO.getInstance() }
    private val jedis by lazy { Jedis(System.getenv("REDIS_URL")) }
    private val logger = logger()

    fun needsMigration(): Boolean {
        if (!MigrationManager.canRedisMigrate()) return false

        val keys = jedis.keys("*:nightscoutshortchannels")

        return mongo.collection
            .countDocuments(ChannelDTO::attributes contains ChannelDTO.ChannelAttribute.NIGHTSCOUT_SHORT)
            .toMono()
            .map {
                return@map it == 0L || keys.size.toLong() > it
            }
            .block()!!
    }

    @ChangeSet(order = "001", id = "redisNightscoutShortChannels", author = "Garlic")
    fun migrate() {
        if (!needsMigration()) return

        val keys = jedis.keys("*:nightscoutshortchannels")

        logger.info("Got keys $keys")
        Flux.fromIterable(keys)
            .map {
                val guildId = it.substringBefore(":")
                val channels = redis.listShortChannels(guildId)
                // convert to a pair: guildId<=>listOfShortChannels
                it to channels
            }.flatMap { pair ->
                val guildId = pair.first.substringBefore(":")
                return@flatMap pair.second.toFlux().flatMap {
                    // add the NIGHTSCOUT_SHORT attribute to each channel
                    mongo.changeAttribute(guildId, it, ChannelDTO.ChannelAttribute.NIGHTSCOUT_SHORT, true)
                }.count()
            }.blockLast()!!
    }
}
