package com.dongtronic.diabot.data

import com.dongtronic.diabot.util.RedisKeyFormats
import org.slf4j.LoggerFactory
import redis.clients.jedis.Jedis

class QuoteDAO private constructor() {
    private var jedis: Jedis? = null
    private val logger = LoggerFactory.getLogger(QuoteDAO::class.java)

    init {
        jedis = Jedis(System.getenv("REDIS_URL"))
    }

    /**
     * Gets a [QuoteDTO] from a quote ID
     *
     * @param guildId guild ID
     * @param quoteId quote ID
     * @return [QuoteDTO] instance, null if quote not found
     */
    fun getQuote(guildId: String, quoteId: String): QuoteDTO? {
        val author = getQuoteAuthor(guildId, quoteId) ?: return null
        val message = getQuoteMessage(guildId, quoteId) ?: return null
        val timestamp = getQuoteTime(guildId, quoteId) ?: return null

        return QuoteDTO(id = quoteId, author = author, message = message, time = timestamp)
    }

    /**
     * Creates a [QuoteDTO] instance from the included parameters and inserts it into the database.
     *
     * @param guildId guild ID
     * @param author the quote's author
     * @param message the quote message
     * @param timestamp quote creation in unix time
     * @return the created [QuoteDTO] instance if successful, null otherwise
     */
    fun addQuote(guildId: String, author: String, message: String, timestamp: Long): QuoteDTO? {
        // cancel if the new quote id cannot be found
        val id = incrementId(guildId) ?: return null
        val quoteDTO = QuoteDTO(id = id.toString(), author = author, message = message, time = timestamp)

        setQuote(guildId, quoteDTO)
        return getQuote(guildId, id.toString())
    }

    /**
     * Places the data of a [QuoteDTO] instance into redis
     *
     * @param guildId guild ID
     * @param quoteDTO quote DTO
     * @param store whether to store this quote's ID in the quote list under a new entry
     */
    fun setQuote(guildId: String, quoteDTO: QuoteDTO, store: Boolean = true) {
        val id = quoteDTO.id

        setQuoteAuthor(guildId, id, quoteDTO.author)
        setQuoteMessage(guildId, id, quoteDTO.message)
        setQuoteTime(guildId, id, quoteDTO.time)
        if (store) {
            storeQuote(guildId, id, delete = false)
        }
    }

    /**
     * Deletes a quote from redis
     *
     * @param guildId guild ID
     * @param quoteId quote ID
     */
    fun deleteQuote(guildId: String, quoteId: String) {
        setQuoteAuthor(guildId, quoteId, "")
        setQuoteMessage(guildId, quoteId, "")
        setQuoteTime(guildId, quoteId, -1)
        storeQuote(guildId, quoteId, delete = true)
    }

    /**
     * Gets the quote's author
     *
     * @param guildId guild ID
     * @param quoteId quote ID
     * @return the author, null if not found
     */
    fun getQuoteAuthor(guildId: String, quoteId: String): String? {
        val redisKey = RedisKeyFormats.quoteAuthor
                .replace("{{guildid}}", guildId)
                .replace("{{quoteid}}", quoteId)

        return jedis!!.get(redisKey)
    }

    /**
     * Sets the author for a quote
     *
     * @param guildId guild ID
     * @param quoteId quote ID
     * @param author the quote's author. set blank to delete
     */
    private fun setQuoteAuthor(guildId: String, quoteId: String, author: String) {
        val redisKey = RedisKeyFormats.quoteAuthor
                .replace("{{guildid}}", guildId)
                .replace("{{quoteid}}", quoteId)

        if (author.isBlank()) {
            jedis!!.del(redisKey)
        } else {
            jedis!!.set(redisKey, author)
        }
    }

    /**
     * Gets a quote's message text
     *
     * @param guildId guild ID
     * @param quoteId quote ID
     * @return message text, null if not found
     */
    fun getQuoteMessage(guildId: String, quoteId: String): String? {
        val redisKey = RedisKeyFormats.quoteMessage
                .replace("{{guildid}}", guildId)
                .replace("{{quoteid}}", quoteId)

        return jedis!!.get(redisKey)
    }

    /**
     * Sets the message text for a quote
     *
     * @param guildId guild ID
     * @param quoteId quote ID
     * @param message the quote's message content. set blank to delete
     */
    private fun setQuoteMessage(guildId: String, quoteId: String, message: String) {
        val redisKey = RedisKeyFormats.quoteMessage
                .replace("{{guildid}}", guildId)
                .replace("{{quoteid}}", quoteId)

        if (message.isBlank()) {
            jedis!!.del(redisKey)
        } else {
            jedis!!.set(redisKey, message)
        }
    }

    /**
     * Gets the time when the quote was created
     *
     * @param guildId guild ID
     * @param quoteId quote ID
     * @return creation time in unix time, null if not found
     */
    fun getQuoteTime(guildId: String, quoteId: String): Long? {
        val redisKey = RedisKeyFormats.quoteTime
                .replace("{{guildid}}", guildId)
                .replace("{{quoteid}}", quoteId)

        return jedis!!.get(redisKey)?.toLongOrNull()
    }

    /**
     * Sets the time when the quote was created.
     *
     * @param guildId guild ID
     * @param quoteId quote ID
     * @param time creation time in unix time. set to -1L to delete
     */
    private fun setQuoteTime(guildId: String, quoteId: String, time: Long) {
        val redisKey = RedisKeyFormats.quoteTime
                .replace("{{guildid}}", guildId)
                .replace("{{quoteid}}", quoteId)

        if (time == -1L) {
            jedis!!.del(redisKey)
        } else {
            jedis!!.set(redisKey, time.toString())
        }
    }

    /**
     * Creates a list of all the quote IDs defined under a guild
     *
     * @param guildId guild ID
     * @return all quote IDs for the specified guild, null if not found
     */
    fun listQuoteIds(guildId: String): Collection<String>? {
        val redisKey = RedisKeyFormats.quoteIds.replace("{{guildid}}", guildId)
        val length = quoteAmount(guildId)?.minus(1) ?: 0

        return jedis!!.lrange(redisKey, 0, length)
    }

    /**
     * Gets the amount of quotes in this guild
     *
     * @param guildId guild ID
     * @return number of quotes for the specified guild, null if not found
     */
    fun quoteAmount(guildId: String): Long? {
        val redisKey = RedisKeyFormats.quoteIds.replace("{{guildid}}", guildId)
        return jedis!!.llen(redisKey)
    }

    /**
     * Increments the guild-wide quote ID index by one and returns the index after incrementing
     *
     * @param guildId guild ID
     * @return the ID index of the specified guild after incrementing
     */
    fun incrementId(guildId: String): Long? {
        val redisKey = RedisKeyFormats.quoteIndex.replace("{{guildid}}", guildId)

        return jedis!!.incr(redisKey)
    }

    /**
     * Stores (or deletes) the quote ID inside the quote list key
     *
     * @param guildId guild ID
     * @param quoteId quote ID
     * @param delete whether to delete the quote ID from the list
     */
    fun storeQuote(guildId: String, quoteId: String, delete: Boolean = false) {
        val redisKey = RedisKeyFormats.quoteIds.replace("{{guildid}}", guildId)

        if (delete) {
            jedis!!.lrem(redisKey, 0, quoteId)
        } else {
            jedis!!.lpush(redisKey, quoteId)
        }
    }

    companion object {
        private var instance: QuoteDAO? = null

        fun getInstance(): QuoteDAO {
            if (instance == null) {
                instance = QuoteDAO()
            }
            return instance as QuoteDAO
        }
    }
}
