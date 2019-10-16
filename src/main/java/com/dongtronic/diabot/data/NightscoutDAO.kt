package com.dongtronic.diabot.data

import com.dongtronic.diabot.util.RedisKeyFormats
import net.dv8tion.jda.api.entities.User
import org.slf4j.LoggerFactory
import redis.clients.jedis.Jedis
import java.util.*

class NightscoutDAO private constructor() {
    private var jedis: Jedis? = null
    private val logger = LoggerFactory.getLogger(NightscoutDAO::class.java)


    init {
        if (System.getenv("REDIS_URL") != null) {
            jedis = Jedis(System.getenv("REDIS_URL"))
        } else if (System.getenv("DIABOT_REDIS_URL") != null) {
            jedis = Jedis(System.getenv("DIABOT_REDIS_URL"))
        }
    }

    fun getNightscoutUrl(user: User): String? {
        val redisKey = RedisKeyFormats.nightscoutUrlFormat.replace("{{userid}}", user.id)

        return jedis!!.get(redisKey)
    }

    fun setNightscoutUrl(user: User, url: String) {
        val redisKey = RedisKeyFormats.nightscoutUrlFormat.replace("{{userid}}", user.id)

        jedis!!.set(redisKey, url)
    }

    fun removeNIghtscoutUrl(user: User) {
        val redisKey = RedisKeyFormats.nightscoutUrlFormat.replace("{{userid}}", user.id)

        jedis!!.del(redisKey)
    }

    fun isNightscoutPublic(user: User): Boolean {
        val redisKey = RedisKeyFormats.nightscoutPublicFormat.replace("{{userid}}", user.id)

        return !jedis!!.get(redisKey).isNullOrEmpty()
    }

    fun setNightscoutPublic(user: User, public: Boolean) {
        val redisKey = RedisKeyFormats.nightscoutPublicFormat.replace("{{userid}}", user.id)

        if (public) {
            jedis!!.set(redisKey, "true")
        } else {
            jedis!!.del(redisKey)
        }
    }

    fun isNightscoutDisplay(user: User): Boolean {
        val redisKey = RedisKeyFormats.nightscoutDisplayFormat.replace("{{userid}}", user.id)

        return !jedis!!.get(redisKey).isNullOrEmpty()
    }

    fun getNightscoutDisplay(user: User): String {
        val redisKey = RedisKeyFormats.nightscoutDisplayFormat.replace("{{userid}}", user.id)

        return jedis!!.get(redisKey).toString()
    }

    fun setNightscoutDisplay(user: User, display: String) {
        val redisKey = RedisKeyFormats.nightscoutDisplayFormat.replace("{{userid}}", user.id)

        jedis!!.set(redisKey, display)
    }

    fun removeNightscoutDisplay(user: User) {
        val redisKey = RedisKeyFormats.nightscoutDisplayFormat.replace("{{userid}}", user.id)

        jedis!!.del(redisKey)
    }

    fun isNightscoutToken(user: User): Boolean {
        val redisKey = RedisKeyFormats.nightscoutTokenFormat.replace("{{userid}}", user.id)

        return !jedis!!.get(redisKey).isNullOrEmpty()
    }

    fun getNightscoutToken(user: User): String {
        val redisKey = RedisKeyFormats.nightscoutTokenFormat.replace("{{userid}}", user.id)

        return jedis!!.get(redisKey).toString()
    }

    fun setNightscoutToken(user: User, token: String) {
        val redisKey = RedisKeyFormats.nightscoutTokenFormat.replace("{{userid}}", user.id)

        if (token.isNotEmpty()) {
            jedis!!.set(redisKey, token)
        } else {
            jedis!!.del(redisKey)
        }
    }

    fun removeNightscoutToken(user: User) {
        val redisKey = RedisKeyFormats.nightscoutTokenFormat.replace("{{userid}}", user.id)

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
