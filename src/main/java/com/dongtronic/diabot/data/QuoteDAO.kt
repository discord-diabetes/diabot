package com.dongtronic.diabot.data

import com.dongtronic.diabot.util.MongoDB
import com.dongtronic.diabot.util.findMany
import com.dongtronic.diabot.util.findOne
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Updates
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import com.mongodb.reactivestreams.client.MongoCollection
import net.dv8tion.jda.api.entities.TextChannel
import org.bson.conversions.Bson
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.updateOne
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

class QuoteDAO private constructor() {
    private var collection: MongoCollection<QuoteDTO>? = null
    private var quoteIndexes: MongoCollection<QuoteIndexDTO>? = null
    private val logger = LoggerFactory.getLogger(QuoteDAO::class.java)
    val enabledGuilds = System.getenv().getOrDefault("QUOTE_ENABLE_GUILDS", "").split(",")
    val maxQuotes = System.getenv().getOrDefault("QUOTE_MAX", "5000").toIntOrNull() ?: 5000

    init {
        collection = MongoDB.getInstance().database.getCollection("quotes", QuoteDTO::class.java)
        quoteIndexes = MongoDB.getInstance().database.getCollection("quote-index", QuoteIndexDTO::class.java)
    }

    /**
     * Gets a [QuoteDTO] from a quote ID
     *
     * @param guildId guild ID
     * @param quoteId quote ID
     * @return [QuoteDTO] instance matching the quote ID
     */
    fun getQuote(guildId: Long, quoteId: Long): Mono<QuoteDTO> {
        return collection!!.findOne(filter(guildId, quoteId))
    }

    /**
     * Creates a copy of the provided [QuoteDTO] instance with a valid quote ID and inserts it into the database.
     *
     * @param quote the quote to insert
     * @return the created [QuoteDTO] instance if successful
     */
    fun addQuote(quote: QuoteDTO): Mono<QuoteDTO> {
        return incrementId(quote.guildId)
                .onErrorMap { IllegalStateException("Could not find guild's quote index") }
                .flatMap { id ->
            val quoteDTO = quote.copy(quoteId = id)

            collection!!.insertOne(quoteDTO).toMono().map {
                if (!it.wasAcknowledged())
                    throw IllegalStateException("Could not insert quote")

                quoteDTO
            }
        }
    }

    /**
     * Updates the data of a [QuoteDTO] instance in the database
     *
     * @param quoteDTO quote DTO
     * @return the result of the update command
     */
    fun updateQuote(quoteDTO: QuoteDTO): Mono<UpdateResult> {
        return collection!!.updateOne(filter(quoteDTO.guildId, quoteDTO.quoteId), quoteDTO).toMono()
    }

    /**
     * Deletes a quote from the database
     *
     * @param guildId guild ID
     * @param quoteId quote ID
     * @return the result of the delete command
     */
    fun deleteQuote(guildId: Long, quoteId: Long): Mono<DeleteResult> {
        return collection!!.deleteOne(filter(guildId, quoteId)).toMono()
    }

    /**
     * Returns all quotes defined under a guild matching the given filter, if any
     *
     * @param guildId guild ID
     * @param filter the mongo filter to match against, if any
     * @return all quotes matching the filter for the specified guild
     */
    fun getQuotes(guildId: Long, filter: Bson? = null): Flux<QuoteDTO> {
        val joinedFilter = if (filter != null)
            and(filter(guildId), filter)
        else
            filter(guildId)

        return collection!!.findMany(joinedFilter)
    }

    /**
     * Gets the amount of quotes in this guild
     *
     * @param guildId guild ID
     * @return number of quotes for the specified guild
     */
    fun quoteAmount(guildId: Long): Mono<Long> {
        return collection!!.countDocuments(filter(guildId)).toMono()
    }

    /**
     * Increments the guild-wide quote ID index by one and returns the index after incrementing
     *
     * @param guildId guild ID
     * @return the ID index of the specified guild after incrementing
     */
    fun incrementId(guildId: Long): Mono<Long> {
        // insert if not existing and return the document after incrementing
        val options = FindOneAndUpdateOptions()
                .upsert(true)
                .returnDocument(ReturnDocument.AFTER)

        return quoteIndexes!!.findOneAndUpdate(QuoteIndexDTO::guildId eq guildId,
                Updates.inc("quoteIndex", 1), options)
                .toMono()
                .map { it.quoteIndex }
    }

    companion object {
        private var instance: QuoteDAO? = null

        fun getInstance(): QuoteDAO {
            if (instance == null) {
                instance = QuoteDAO()
            }
            return instance as QuoteDAO
        }

        /**
         * Checks the restrictions for the guild which the channel belongs to.
         * This initiates a blocking call to quoteAmount().
         *
         * @param channel the channel to send messages to
         * @param warnDisabledGuild whether to send a warning message when the guild is not enabled for quotes
         * @param checkQuoteLimit whether to check if the guild has reached the max quote limit
         * @return true if the guild passed restrictions, false if not
         */
        fun checkRestrictions(channel: TextChannel,
                              warnDisabledGuild: Boolean = false,
                              checkQuoteLimit: Boolean = true): Boolean {
            if (!getInstance().enabledGuilds.contains(channel.guild.id)) {
                if (warnDisabledGuild) {
                    channel.sendMessage("This guild is not permitted to use the quoting system").queue()
                }
                return false
            }

            val numOfQuotes = getInstance().quoteAmount(channel.guild.idLong).block() ?: -1
            if (checkQuoteLimit && numOfQuotes >= getInstance().maxQuotes) {
                channel.sendMessage("Could not create quote as your guild has reached " +
                        "the max of ${getInstance().maxQuotes} quotes").queue()
                return false
            }
            return true
        }

        /**
         * Creates a Mongo query filter for the specified guild ID and (optionally) quote ID.
         * If quote ID is null then this query will only search for quotes under a guild.
         *
         * @param guildId guild ID
         * @param quoteId quote ID, optional
         * @return MongoDB query filter for the provided guild ID and quote ID, if any
         */
        fun filter(guildId: Long, quoteId: Long? = null): Bson {
            val guildFilter = QuoteDTO::guildId.eq(guildId)

            if (quoteId != null) {
                return and(guildFilter, QuoteDTO::quoteId.eq(quoteId))
            }

            return guildFilter
        }
    }
}