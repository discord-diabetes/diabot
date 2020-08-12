package com.dongtronic.diabot.data.redis

import com.dongtronic.diabot.util.RedisKeyFormats
import com.dongtronic.diabot.util.logger
import net.dv8tion.jda.api.entities.Guild
import redis.clients.jedis.Jedis
import java.util.*

class NightscoutDAO private constructor() {
    private var jedis: Jedis? = null
    private val logger = logger()


    init {
        jedis = Jedis(System.getenv("REDIS_URL"))
    }

    fun getNightscoutUrl(userId: String): String? {
        val redisKey = RedisKeyFormats.nightscoutUrlFormat.replace("{{userid}}", userId)

        return jedis!!.get(redisKey)
    }

    fun setNightscoutUrl(userId: String, url: String) {
        val redisKey = RedisKeyFormats.nightscoutUrlFormat.replace("{{userid}}", userId)

        jedis!!.set(redisKey, url)
    }

    fun removeNIghtscoutUrl(userId: String) {
        val redisKey = RedisKeyFormats.nightscoutUrlFormat.replace("{{userid}}", userId)

        jedis!!.del(redisKey)
    }

    fun isNightscoutPublic(userId: String, guildId: String): Boolean {
        val redisKey = RedisKeyFormats.nightscoutPublicFormat.replace("{{userid}}", userId).replace("{{guildid}}", guildId)

        return !jedis!!.get(redisKey).isNullOrEmpty()
    }

    fun setNightscoutPublic(userId: String, guild: Guild, public: Boolean) {
        val redisKey = RedisKeyFormats.nightscoutPublicFormat.replace("{{userid}}", userId).replace("{{guildid}}", guild.id)

        if (public) {
            jedis!!.set(redisKey, guild.name)
        } else {
            jedis!!.del(redisKey)
        }
    }

    fun isNightscoutDisplay(userId: String): Boolean {
        val redisKey = RedisKeyFormats.nightscoutDisplayFormat.replace("{{userid}}", userId)

        return !jedis!!.get(redisKey).isNullOrEmpty()
    }

    fun getNightscoutDisplay(userId: String): String {
        val redisKey = RedisKeyFormats.nightscoutDisplayFormat.replace("{{userid}}", userId)

        return jedis!!.get(redisKey).toString()
    }

    fun setNightscoutDisplay(userId: String, display: String) {
        val redisKey = RedisKeyFormats.nightscoutDisplayFormat.replace("{{userid}}", userId)

        jedis!!.set(redisKey, display)
    }

    fun removeNightscoutDisplay(userId: String) {
        val redisKey = RedisKeyFormats.nightscoutDisplayFormat.replace("{{userid}}", userId)

        jedis!!.del(redisKey)
    }

    fun getNightscoutToken(userId: String): String? {
        val redisKey = RedisKeyFormats.nightscoutTokenFormat.replace("{{userid}}", userId)

        return jedis!!.get(redisKey)?.toString()
    }

    fun setNightscoutToken(userId: String, token: String) {
        val redisKey = RedisKeyFormats.nightscoutTokenFormat.replace("{{userid}}", userId)

        if (token.isNotEmpty()) {
            jedis!!.set(redisKey, token)
        } else {
            jedis!!.del(redisKey)
        }
    }

    fun removeNightscoutToken(userId: String) {
        val redisKey = RedisKeyFormats.nightscoutTokenFormat.replace("{{userid}}", userId)

        jedis!!.del(redisKey)
    }

    fun listUsers(): TreeMap<String, String> {
        val keys = jedis!!.keys(RedisKeyFormats.allNightscoutUrlsFormat)
        val result = TreeMap<String, String>()

        for (key in keys) {
            val value = jedis!!.get(key)
            result[key] = value
        }

        return result
    }

    fun listShortChannels(guildId: String): MutableCollection<String> {
        val redisKey = RedisKeyFormats.nightscoutShortChannelsFormat.replace("{{guildid}}", guildId)
        val channelListLength = jedis!!.llen(redisKey)

        return jedis!!.lrange(redisKey, 0, channelListLength - 1)
    }

    fun addShortChannel(guildId: String, channelId: String) {
        val redisKey = RedisKeyFormats.nightscoutShortChannelsFormat.replace("{{guildid}}", guildId)
        val shortChannels = listShortChannels(guildId)

        if (!shortChannels.contains(channelId)) {
            jedis!!.lpush(redisKey, channelId)
        }
    }

    fun removeShortChannel(guildId: String, channelId: String) {
        val redisKey = RedisKeyFormats.nightscoutShortChannelsFormat.replace("{{guildid}}", guildId)
        jedis!!.lrem(redisKey, 0, channelId)
    }

    companion object {
        private var instance: NightscoutDAO? = null

        fun getInstance(): NightscoutDAO {
            if (instance == null) {
                instance = NightscoutDAO()
            }
            return instance as NightscoutDAO
        }
    }
}
