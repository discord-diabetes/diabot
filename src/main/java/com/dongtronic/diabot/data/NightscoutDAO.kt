package com.dongtronic.diabot.data

import com.dongtronic.diabot.util.RedisKeyFormats
import net.dv8tion.jda.core.entities.User
import redis.clients.jedis.Jedis

class NightscoutDAO private constructor() {
    private var jedis: Jedis? = null


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
