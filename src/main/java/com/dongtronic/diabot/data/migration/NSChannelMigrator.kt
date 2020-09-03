package com.dongtronic.diabot.data.migration

import com.dongtronic.diabot.data.mongodb.ChannelDAO
import com.dongtronic.diabot.data.mongodb.ChannelDTO
import com.dongtronic.diabot.data.redis.NightscoutDAO
import com.dongtronic.diabot.util.logger
import org.litote.kmongo.contains
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import redis.clients.jedis.Jedis

class NSChannelMigrator : Migrator {
    private val channelDAO = ChannelDAO.instance
    private val nightscoutRedis = NightscoutDAO.getInstance()
    private val jedis: Jedis = Jedis(System.getenv("REDIS_URL"))
    private val logger = logger()

    override fun needsMigration(): Mono<Boolean> {
        val keys = jedis.keys("*:nightscoutshortchannels")

        return channelDAO.collection
                .countDocuments(ChannelDTO::attributes contains ChannelDTO.ChannelAttribute.NIGHTSCOUT_SHORT)
                .toMono()
                .map {
                    return@map it == 0L || keys.size.toLong() > it
                }
    }

    override fun migrate(): Flux<Long> {
        val keys = jedis.keys("*:nightscoutshortchannels")

        logger.info("Got keys $keys")
        return Flux.fromIterable(keys)
                .map {
                    val guildId = it.substringBefore(":")
                    val channels = nightscoutRedis.listShortChannels(guildId)
                    // convert to a pair: guildId<=>listOfShortChannels
                    it to channels
                }.flatMap { pair ->
                    val guildId = pair.first.substringBefore(":")
                    return@flatMap pair.second.toFlux().flatMap {
                        // add the NIGHTSCOUT_SHORT attribute to each channel
                        channelDAO.changeAttribute(guildId, it, ChannelDTO.ChannelAttribute.NIGHTSCOUT_SHORT, true)
                    }.count()
                }
    }
}