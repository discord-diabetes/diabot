package com.dongtronic.diabot.data

import com.dongtronic.diabot.util.Logger
import com.dongtronic.diabot.util.RedisKeyFormats
import redis.clients.jedis.Jedis

class RulesDAO private constructor() {
    private var jedis: Jedis? = null
    private val logger by Logger()


    init {
        jedis = Jedis(System.getenv("REDIS_URL"))
    }

    fun setRulesChannel(guildId: String, channelId: String) {
        val redisKey = RedisKeyFormats.rulesChannel.replace("{{guildid}}", guildId)

        jedis!!.set(redisKey, channelId)
    }

    fun getRulesChannel(guildId: String): String? {
        val redisKey = RedisKeyFormats.rulesChannel.replace("{{guildid}}", guildId)

        return jedis!!.get(redisKey)
    }

    fun setRuleText(guildId: String, ruleId: String, text: String) {
        val redisKey = RedisKeyFormats.ruleText
                .replace("{{guildid}}", guildId)
                .replace("{{ruleid}}", ruleId)

        jedis!!.set(redisKey, text)
    }

    fun setRuleTitle(guildId: String, ruleId: String, title: String) {
        val redisKey = RedisKeyFormats.ruleTitle
                .replace("{{guildid}}", guildId)
                .replace("{{ruleid}}", ruleId)

        jedis!!.set(redisKey, title)
    }

    fun setRuleMessage(guildId: String, ruleId: String, messageId: String) {
        val redisKey = RedisKeyFormats.ruleMessage
                .replace("{{guildid}}", guildId)
                .replace("{{ruleid}}", ruleId)

        jedis!!.set(redisKey, messageId)
    }

    fun getRuleText(guildId: String, ruleId: String): String? {
        val redisKey = RedisKeyFormats.ruleText
                .replace("{{guildid}}", guildId)
                .replace("{{ruleid}}", ruleId)

        return jedis!!.get(redisKey)
    }

    fun getRuleTitle(guildId: String, ruleId: String): String? {
        val redisKey = RedisKeyFormats.ruleTitle
                .replace("{{guildid}}", guildId)
                .replace("{{ruleid}}", ruleId)

        return jedis!!.get(redisKey)
    }

    fun getRuleMessage(guildId: String, ruleId: String): String? {
        val redisKey = RedisKeyFormats.ruleMessage
                .replace("{{guildid}}", guildId)
                .replace("{{ruleid}}", ruleId)

        return jedis!!.get(redisKey)
    }

    fun listRuleIds(guildId: String): MutableCollection<String>? {
        val redisKey = RedisKeyFormats.ruleIds.replace("{{guildid}}", guildId)

        val rulesListLength = jedis!!.llen(redisKey)

        return jedis!!.lrange(redisKey, 0, rulesListLength - 1)
    }


    fun addRule(guildId: String, rule: String): Int {
        return 69
    }

    companion object {
        private var instance: RulesDAO? = null

        fun getInstance(): RulesDAO {
            if (instance == null) {
                instance = RulesDAO()
            }
            return instance as RulesDAO
        }
    }
}
