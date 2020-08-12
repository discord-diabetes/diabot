package com.dongtronic.diabot.data.migration

import com.dongtronic.diabot.data.mongodb.ChannelDAO
import com.dongtronic.diabot.data.mongodb.ChannelDTO
import com.dongtronic.diabot.data.redis.AdminDAO
import com.dongtronic.diabot.util.RedisKeyFormats
import com.dongtronic.diabot.util.logger
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
        val adminChannels = allKeys(RedisKeyFormats.adminChannelIds)
        val keys = jedis.keys(adminChannels)

        return channelDAO.collection.countDocuments().toMono().map {
            return@map it == 0L || keys.size.toLong() > it
        }
    }

    override fun migrate(): Flux<Long> {
        val adminChannels = allKeys(RedisKeyFormats.adminChannelIds)
        val keys = jedis.keys(adminChannels)

        logger.info("Got keys $keys")
        return Flux.fromIterable(keys)
                .map {
                    val guildId = it.substringBefore(":")
                    val channels = adminRedis.listAdminChannels(guildId) ?: mutableListOf()

                    it to channels
                }.flatMap { pair ->
                    val guildId = pair.first.substringBefore(":")
                    return@flatMap pair.second.toFlux().flatMap {
                        channelDAO.changeAttribute(guildId, it, ChannelDTO.ChannelAttribute.ADMIN)
                    }.count()
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