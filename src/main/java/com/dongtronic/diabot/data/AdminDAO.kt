package com.dongtronic.diabot.data

import com.dongtronic.diabot.util.RedisKeyFormats
import net.dv8tion.jda.core.entities.User
import org.slf4j.LoggerFactory
import redis.clients.jedis.Jedis

class AdminDAO private constructor() {
    private var jedis: Jedis? = null
    private val logger = LoggerFactory.getLogger(AdminDAO::class.java)


    init {
        if (System.getenv("REDIS_URL") != null) {
            jedis = Jedis(System.getenv("REDIS_URL"))
        } else if (System.getenv("DIABOT_REDIS_URL") != null) {
            jedis = Jedis(System.getenv("DIABOT_REDIS_URL"))
        }
    }

    fun addAdminChannel(guildId: String, channelId: String) {
        val key = RedisKeyFormats.adminChannelIds.replace("{{guildid}}", guildId)

        jedis!!.lpush(key, channelId)
    }

    fun removeAdminChannel(guildId: String, channelId: String) {
        val key = RedisKeyFormats.adminChannelIds.replace("{{guildid}}", guildId)

        jedis!!.lrem(key, 0, channelId)
    }

    fun listAdminChannels(guildId: String): MutableList<String>? {

        val key = RedisKeyFormats.adminChannelIds.replace("{{guildid}}", guildId)

        val channelListLength = jedis!!.llen(key)

        return jedis!!.lrange(key, 0, channelListLength - 1)
    }

    companion object {
        private var instance: AdminDAO? = null

        fun getInstance(): AdminDAO {
            if (instance == null) {
                instance = AdminDAO()
            }
            return instance as AdminDAO
        }
    }
}
