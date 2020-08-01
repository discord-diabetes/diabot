package com.dongtronic.diabot.data.redis

import com.dongtronic.diabot.util.Logger
import com.dongtronic.diabot.util.RedisKeyFormats
import redis.clients.jedis.Jedis

class AdminDAO private constructor() {
    private var jedis: Jedis? = null
    private val logger by Logger()


    init {
        jedis = Jedis(System.getenv("REDIS_URL"))
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

    fun setUsernamePattern(guildId: String, pattern: String) {
        val redisKey = RedisKeyFormats.usernamePattern.replace("{{guildid}}", guildId)

        val compiled = pattern.toRegex()

        jedis!!.set(redisKey, compiled.pattern)
    }

    fun getUsernamePattern(guildId: String): String? {
        val redisKey = RedisKeyFormats.usernamePattern.replace("{{guildid}}", guildId)

        return jedis!!.get(redisKey)
    }

    fun setUsernameHint(guildId: String, hint: String) {
        val redisKey = RedisKeyFormats.usernameHint.replace("{{guildid}}", guildId)

        jedis!!.set(redisKey, hint)
    }

    fun getUsernameHint(guildId: String): String? {
        val redisKey = RedisKeyFormats.usernameHint.replace("{{guildid}}", guildId)

        return jedis!!.get(redisKey)
    }

    fun setUsernameEnforcementEnabled(guildId: String, enabled: Boolean) {
        val redisKey = RedisKeyFormats.enforceUsernames.replace("{{guildid}}", guildId)

        val value = if (enabled) "true" else "false"

        jedis!!.set(redisKey, value)
    }

    fun getUsernameEnforcementEnabled(guildId: String): Boolean {
        val redisKey = RedisKeyFormats.enforceUsernames.replace("{{guildid}}", guildId)

        val value = jedis!!.get(redisKey)

        return value == "true"
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
