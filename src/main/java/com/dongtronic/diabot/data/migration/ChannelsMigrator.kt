package com.dongtronic.diabot.data.migration

import com.dongtronic.diabot.data.mongodb.ChannelDAO
import com.dongtronic.diabot.data.mongodb.ChannelDTO
import com.dongtronic.diabot.data.redis.AdminDAO
import com.dongtronic.diabot.data.redis.NightscoutDAO
import com.dongtronic.diabot.flatMapNotNull
import com.dongtronic.diabot.util.RedisKeyFormats
import com.dongtronic.diabot.util.logger
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import redis.clients.jedis.Jedis

class ChannelsMigrator : Migrator {
    private val channelDAO = ChannelDAO.instance
    private val nightscoutRedis = NightscoutDAO.getInstance()
    private val adminRedis = AdminDAO.getInstance()
    private val jedis: Jedis = Jedis(System.getenv("REDIS_URL"))
    private val logger = logger()

    override fun checkAndMigrate(): Flux<Long> {
        TODO("Not yet implemented")
    }

    override fun needsMigration(): Mono<Boolean> {
        val adminChannels = allKeys(RedisKeyFormats.adminChannelIds)
        val simpleNsChannels = allKeys(RedisKeyFormats.nightscoutShortChannelsFormat)
        val keys = jedis.keys(adminChannels)
                .plus(jedis.keys(simpleNsChannels))

        logger.info("Got keys $keys")
        return Mono.just(keys.isNotEmpty())
    }

    override fun migrate(): Flux<Long> {
        val adminChannels = allKeys(RedisKeyFormats.adminChannelIds)
        val simpleNsChannels = allKeys(RedisKeyFormats.nightscoutShortChannelsFormat)
        val keys = jedis.keys(adminChannels)
                .plus(jedis.keys(simpleNsChannels))

        logger.info("Got keys $keys")
        return Flux.fromIterable(keys)
                .map {
                    val parts = it.split(":")
                    val guildId = parts[0]
                    val channels = when (parts[1]) {
                        "adminchannels" -> adminRedis.listAdminChannels(guildId)
                        "nightscoutshortchannels" -> nightscoutRedis.listShortChannels(guildId)
                        else -> emptyList<String>()
                    }

                    it to channels
                }/*.doOnNext {
                    logger.info("Got pair $it")
                }*/.flatMapNotNull { pair ->
                    if (pair.second == null) return@flatMapNotNull null
                    val parts = pair.first.split(":")
                    logger.info("parts: $parts")
                    val guildId = parts[0]
                    return@flatMapNotNull when {
                        pair.first.endsWith("adminchannels") -> {
                            pair.second!!.toFlux().flatMap {
                                channelDAO.changeAttribute(guildId, it, ChannelDTO.ChannelAttribute.ADMIN)
                            }.count()
                        }
                        pair.first.endsWith("nightscoutshortchannels") -> {
                            pair.second!!.toFlux().flatMap {
                                channelDAO.changeAttribute(guildId, it, ChannelDTO.ChannelAttribute.NIGHTSCOUT_SHORT)
                            }.count()
                        }
                        else -> null
                    }
                }
    }

    private fun allKeys(format: String, userIds: Boolean = false): String {
        var all = format.replace("{{guildid}}", "*")
        if (userIds) {
            all = all.replace("{{userid}}", "*")
        }
        return all
    }
}