package com.dongtronic.diabot.data

import com.dongtronic.diabot.util.RedisKeyFormats
import org.slf4j.LoggerFactory
import redis.clients.jedis.Jedis

class RewardDAO private constructor() {
    private var jedis: Jedis? = null
    private val logger = LoggerFactory.getLogger(RewardDAO::class.java)


    init {
        if (System.getenv("REDIS_URL") != null) {
            jedis = Jedis(System.getenv("REDIS_URL"))
        } else if (System.getenv("DIABOT_REDIS_URL") != null) {
            jedis = Jedis(System.getenv("DIABOT_REDIS_URL"))
        }
    }

    fun getSimpleRewards(guildId: String): MutableList<String>? {
        val key = RedisKeyFormats.simpleRewards.replace("{{guildid}}", guildId)

        val roleListLength = jedis!!.llen(key)

        return jedis!!.lrange(key, 0, roleListLength - 1)
    }

    fun addSimpleReward(guildId: String, requiredRoleId: String, rewardRoleId: String) {
        val key = RedisKeyFormats.simpleRewards.replace("{{guildid}}", guildId)
        val rewardString = "$requiredRoleId:$rewardRoleId"

        val rewards = getSimpleRewards(guildId)

        if (rewards == null || !rewards.contains(rewardString)) {
            jedis!!.lpush(key, rewardString)
        }
    }

    fun removeSimpleReward(guildId: String, requiredRoleId: String, rewardRoleId: String) {
        val key = RedisKeyFormats.simpleRewards.replace("{{guildid}}", guildId)

        val rewardString = "$requiredRoleId:$rewardRoleId"

        jedis!!.lrem(key, 0, rewardString)
    }

    fun optOut(guildId: String, userId: String) {
        val key = RedisKeyFormats.rewardOptout.replace("{{guildid}}", guildId)

        val rewards = getSimpleRewards(guildId)

        if (rewards == null || !getOptOut(guildId, userId)) {
            jedis!!.lpush(key, userId)
        }
    }

    fun optIn(guildId: String, userId: String) {
        val key = RedisKeyFormats.rewardOptout.replace("{{guildid}}", guildId)

        jedis!!.lrem(key, 0, userId)
    }

    fun getOptOut(guildId: String, userId: String): Boolean {
        val key = RedisKeyFormats.rewardOptout.replace("{{guildid}}", guildId)

        val optOutListLength = jedis!!.llen(key)

        val optOutList = jedis!!.lrange(key, 0, optOutListLength - 1)

        return optOutList.contains(userId)
    }

    fun getOptOuts(guildId: String): MutableList<String>? {
        val key = RedisKeyFormats.rewardOptout.replace("{{guildid}}", guildId)

        val optOutListLength = jedis!!.llen(key)

        return jedis!!.lrange(key, 0, optOutListLength - 1)
    }

    companion object {
        private var instance: RewardDAO? = null

        fun getInstance(): RewardDAO {
            if (instance == null) {
                instance = RewardDAO()
            }
            return instance as RewardDAO
        }
    }
}
