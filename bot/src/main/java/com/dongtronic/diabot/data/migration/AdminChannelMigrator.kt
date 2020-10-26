package com.dongtronic.diabot.data.migration

import com.dongtronic.diabot.data.mongodb.ChannelDAO
import com.dongtronic.diabot.data.mongodb.ChannelDTO
import com.dongtronic.diabot.data.redis.AdminDAO
import com.dongtronic.diabot.util.logger
import org.litote.kmongo.contains
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import redis.clients.jedis.Jedis

class AdminChannelMigrator : Migrator {
    private val channelDAO = ChannelDAO.instance
    private val adminRedis = AdminDAO.getInstance()
    private val jedis: Jedis = Jedis(System.getenv("REDIS_URL"))
    private val logger = logger()

    override fun needsMigration(): Mono<Boolean> {
        val keys = jedis.keys("*:adminchannels")

        return channelDAO.collection
                .countDocuments(ChannelDTO::attributes contains ChannelDTO.ChannelAttribute.ADMIN)
                .toMono()
                .map {
                    return@map it == 0L || keys.size.toLong() > it
                }
    }

    override fun migrate(): Flux<Long> {
        val keys = jedis.keys("*:adminchannels")

        logger.info("Got keys $keys")
        return Flux.fromIterable(keys)
                .map {
                    val guildId = it.substringBefore(":")
                    val channels = adminRedis.listAdminChannels(guildId) ?: mutableListOf()
                    // convert to a pair: guildId<=>listOfAdminChannels
                    it to channels
                }.flatMap { pair ->
                    val guildId = pair.first.substringBefore(":")
                    return@flatMap pair.second.toFlux().flatMap {
                        // add the ADMIN attribute to each channel
                        channelDAO.changeAttribute(guildId, it, ChannelDTO.ChannelAttribute.ADMIN)
                    }.count()
                }
    }
}